package com.eduadmin.finance.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.finance.entity.FinanceSettlement;
import com.eduadmin.finance.repo.FinanceSettlementRepository;
import com.eduadmin.student.entity.Student;
import com.eduadmin.student.repo.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.eduadmin.dashboard.store.DashboardStore;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/finance")
public class FinanceSettlementController {

    @Autowired
    private FinanceSettlementRepository repository;
    @Autowired(required = false)
    private StudentRepository studentRepository;

    // 费用计算：单价 * 课时 + 教材费 - 优惠
    @PostMapping("/settlement/calculate")
    public ApiResponse<Map<String, Object>> calculate(@RequestBody Map<String, Object> body) {
      double unitPrice = toDouble(body.get("unitPrice"));
      double hours = toDouble(body.get("hours"));
      double textbookFee = toDouble(body.get("textbookFee"));
      double discount = toDouble(body.get("discount"));
      String opType = String.valueOf(body.getOrDefault("opType", "")).trim();
      // 续费默认优惠：学费部分打8折（减免20%）+ 现金券100
      // 学费部分 = 单价 * 课时；教材费不参与折扣
      if ("renew".equalsIgnoreCase(opType)) {
        double tuition = unitPrice * hours;
        double defaultDiscount = round2(tuition * 0.20 + 100.0);
        if (discount <= 0) {
          discount = defaultDiscount;
        }
      }
      double total = Math.max(0, unitPrice * hours + textbookFee - discount);
      Map<String, Object> out = new LinkedHashMap<>();
      out.put("totalFee", round2(total));
      return ApiResponse.success(out);
    }

    @PostMapping("/settlement/save")
    public ApiResponse<FinanceSettlement> save(@RequestBody Map<String, Object> body) {
      FinanceSettlement rec = new FinanceSettlement();
      rec.setStudentId(toLong(body.get("studentId")));
      rec.setClassId(toLong(body.get("classId")));
      rec.setClassroomId(toLong(body.get("classroomId")));
      try { rec.setSeatNo(Integer.valueOf(String.valueOf(body.get("seatNo")))); } catch (Exception ignore) { rec.setSeatNo(null); }
      rec.setUnitPrice(toBD(body.get("unitPrice")));
      rec.setHours(toBD(body.get("hours")));
      rec.setTextbookFee(toBD(body.get("textbookFee")));
      rec.setDiscount(toBD(body.get("discount")));
      rec.setTotalFee(toBD(body.get("totalFee")));
      rec.setOpType(String.valueOf(body.getOrDefault("opType", null)));
      rec.setMethod(String.valueOf(body.getOrDefault("method", "")));
      String rcNo = String.valueOf(body.getOrDefault("receiptNo", genReceiptNo()));
      rec.setReceiptNo(rcNo);
      rec.setReceiver(String.valueOf(body.getOrDefault("receiver", "")));
      rec.setVoucherUrl(String.valueOf(body.getOrDefault("voucherUrl", "")));
      rec.setStatus(String.valueOf(body.getOrDefault("status", "paid")));
      rec.setArrears(false);
      rec.setCreatedAt(new Date());
      // 座位占用校验：同一班级+教室+座位不可重复
      if (rec.getClassroomId() != null && rec.getClassId() != null && rec.getSeatNo() != null) {
        boolean occupied = repository.existsByClassroomIdAndClassIdAndSeatNo(rec.getClassroomId(), rec.getClassId(), rec.getSeatNo());
        if (occupied) {
          return ApiResponse.error(409, "该座位已被占用，请选择其他座位");
        }
      }
      FinanceSettlement saved = repository.save(rec);
      // 记录到首页看板：根据操作类型分类
      try {
        double amount = rec.getTotalFee() != null ? rec.getTotalFee().doubleValue() : 0D;
        String type = rec.getOpType() == null ? "" : rec.getOpType().toLowerCase();
        if ("renew".equals(type)) {
          DashboardStore.addOperation("renew", amount, rec.getClassId(), rec.getCreatedAt());
        } else if ("enroll".equals(type)) {
          DashboardStore.addOperation("enroll", amount, rec.getClassId(), rec.getCreatedAt());
        }
      } catch (Exception ignore) { /* no-op */ }
      return ApiResponse.success(saved);
    }

    @GetMapping("/settlement/list")
    public ApiResponse<List<FinanceSettlement>> list() {
      List<FinanceSettlement> arr = repository.findAllByOrderByIdDesc();
      return ApiResponse.success(arr);
    }

    // 释放座位占用：将记录的 seatNo 清空（可选清空 classroomId）
    @PutMapping("/settlement/release-seat/{id}")
    public ApiResponse<FinanceSettlement> releaseSeat(@PathVariable("id") Long id) {
      Optional<FinanceSettlement> optional = repository.findById(id);
      if (!optional.isPresent()) return ApiResponse.error(404, "记录不存在");
      FinanceSettlement rec = optional.get();
      rec.setSeatNo(null);
      // 如需同时解除教室绑定，可取消下一行注释
      // rec.setClassroomId(null);
      repository.save(rec);
      return ApiResponse.success(rec);
    }

    @PutMapping("/arrears/mark/{id}")
    public ApiResponse<FinanceSettlement> markArrears(@PathVariable("id") Long id) {
      Optional<FinanceSettlement> optional = repository.findById(id);
      if (!optional.isPresent()) return ApiResponse.error(404, "记录不存在");
      FinanceSettlement rec = optional.get();
      rec.setStatus("unpaid");
      rec.setArrears(true);
      repository.save(rec);
      return ApiResponse.success(rec);
    }

    // 查询占用座位：返回 { seatNo, studentId, studentName }
    @GetMapping("/settlement/occupied-seats")
    public ApiResponse<List<Map<String, Object>>> occupiedSeats(@RequestParam Long classroomId,
                                                                @RequestParam Long classId) {
      List<FinanceSettlement> list = repository.findByClassroomIdAndClassId(classroomId, classId);
      Map<Long, String> nameCache = new HashMap<>();
      List<Map<String, Object>> out = new ArrayList<>();
      for (FinanceSettlement fs : list) {
        if (fs.getSeatNo() == null) continue;
        Long sid = fs.getStudentId();
        String name = null;
        if (sid != null) {
          if (nameCache.containsKey(sid)) { name = nameCache.get(sid); }
          else if (studentRepository != null) {
            Optional<Student> opt = studentRepository.findById(sid);
            name = opt.map(Student::getName).orElse(null);
            if (name != null) nameCache.put(sid, name);
          }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("seatNo", fs.getSeatNo());
        m.put("studentId", sid);
        m.put("studentName", name);
        out.add(m);
      }
      return ApiResponse.success(out);
    }

    @PostMapping("/reminder/send")
    public ApiResponse<Map<String, Object>> sendReminder(@RequestBody Map<String, Object> payload) {
      Map<String, Object> out = new HashMap<>();
      out.put("sent", true);
      out.put("channels", payload.getOrDefault("channels", Arrays.asList("sms","wechat")));
      out.put("studentId", payload.get("studentId"));
      out.put("amountDue", payload.get("amountDue"));
      return ApiResponse.success(out);
    }

    private static double toDouble(Object v) { try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return 0D; } }
    private static long toLong(Object v) { try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return 0L; } }
    private static double round2(double v) { return new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(); }
    private static BigDecimal toBD(Object v) { try { return new BigDecimal(String.valueOf(v)).setScale(2, BigDecimal.ROUND_HALF_UP); } catch (Exception e) { return BigDecimal.ZERO; } }
    private static String genReceiptNo() {
      Calendar c = Calendar.getInstance();
      return String.format("RC-%04d%02d%02d%02d%02d%02d-%04d",
              c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH),
              c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), new Random().nextInt(10000));
    }
}