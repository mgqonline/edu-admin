package com.eduadmin.finance.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.finance.entity.FinanceSettlement;
import com.eduadmin.finance.entity.InvoiceApplication;
import com.eduadmin.finance.entity.InvoiceRecord;
import com.eduadmin.finance.repo.FinanceSettlementRepository;
import com.eduadmin.finance.repo.InvoiceApplicationRepository;
import com.eduadmin.finance.repo.InvoiceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/finance/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceApplicationRepository appRepo;
    @Autowired
    private InvoiceRecordRepository recordRepo;
    @Autowired
    private FinanceSettlementRepository settlementRepo;

    @PostMapping("/apply")
    public ApiResponse<InvoiceApplication> apply(@RequestBody Map<String, Object> body) {
        Long studentId = toLong(body.get("studentId"));
        Long settlementId = toLong(body.get("settlementId"));
        String title = String.valueOf(body.getOrDefault("title", ""));
        String taxNo = String.valueOf(body.getOrDefault("taxNo", ""));
        BigDecimal amount = toBD(body.get("amount"));

        // 简单校验：结算记录存在
        if (settlementId != null && settlementId > 0) {
            Optional<FinanceSettlement> opt = settlementRepo.findById(settlementId);
            if (!opt.isPresent()) return ApiResponse.error(404, "缴费记录不存在");
        }

        InvoiceApplication app = new InvoiceApplication();
        app.setStudentId(studentId);
        app.setSettlementId(settlementId);
        app.setTitle(title);
        app.setTaxNo(taxNo);
        app.setAmount(amount);
        app.setStatus("APPLIED");
        app.setCreatedAt(new Date());
        return ApiResponse.success(appRepo.save(app));
    }

    @GetMapping("/applications")
    public ApiResponse<List<InvoiceApplication>> applications(@RequestParam(value = "status", required = false) String status) {
        List<InvoiceApplication> list = status == null || status.isEmpty() ? appRepo.findAllByOrderByIdDesc() : appRepo.findByStatusOrderByIdDesc(status);
        return ApiResponse.success(list);
    }

    @PostMapping("/issue")
    public ApiResponse<InvoiceRecord> issue(@RequestBody Map<String, Object> body) {
        Long applicationId = toLong(body.get("applicationId"));
        Long settlementId = toLong(body.get("settlementId"));
        Long studentId = toLong(body.get("studentId"));
        Long campusId = toLong(body.get("campusId"));
        String invoiceNo = String.valueOf(body.getOrDefault("invoiceNo", genInvoiceNo()));
        String deliveryStatus = String.valueOf(body.getOrDefault("deliveryStatus", "pending"));
        BigDecimal amount = toBD(body.get("amount"));
        Date invoiceDate = parseDate(String.valueOf(body.getOrDefault("invoiceDate", today())));

        // 若传了申请ID，读取申请数据
        if (applicationId != null && applicationId > 0) {
            Optional<InvoiceApplication> opt = appRepo.findById(applicationId);
            if (!opt.isPresent()) return ApiResponse.error(404, "发票申请不存在");
            InvoiceApplication app = opt.get();
            if (studentId == null || studentId == 0) studentId = app.getStudentId();
            if (settlementId == null || settlementId == 0) settlementId = app.getSettlementId();
            if (amount == null || amount.doubleValue() == 0D) amount = app.getAmount();
            app.setStatus("INVOICED");
            appRepo.save(app);
        }

        InvoiceRecord rec = new InvoiceRecord();
        rec.setApplicationId(applicationId);
        rec.setSettlementId(settlementId);
        rec.setStudentId(studentId);
        rec.setCampusId(campusId);
        rec.setInvoiceNo(invoiceNo);
        rec.setInvoiceDate(invoiceDate);
        rec.setDeliveryStatus(deliveryStatus);
        rec.setAmount(amount);
        rec.setCreatedAt(new Date());
        return ApiResponse.success(recordRepo.save(rec));
    }

    @GetMapping("/records")
    public ApiResponse<List<InvoiceRecord>> records(@RequestParam(value = "month", required = false) String month,
                                                    @RequestParam(value = "campusId", required = false) Long campusId) {
        if (month == null || month.isEmpty()) {
            List<InvoiceRecord> all = campusId == null ? recordRepo.findAllByOrderByIdDesc() : recordRepo.findByCampusIdAndInvoiceDateBetween(campusId, startOfMonth(new Date()), endOfMonth(new Date()));
            return ApiResponse.success(all);
        }
        Date[] rng = monthRange(month);
        List<InvoiceRecord> list = campusId == null ? recordRepo.findByInvoiceDateBetween(rng[0], rng[1]) : recordRepo.findByCampusIdAndInvoiceDateBetween(campusId, rng[0], rng[1]);
        return ApiResponse.success(list);
    }

    @GetMapping("/stat/summary")
    public ApiResponse<Map<String, Object>> summary(@RequestParam("month") String month,
                                                    @RequestParam(value = "campusId", required = false) Long campusId) {
        Date[] rng = monthRange(month);
        List<InvoiceRecord> list = campusId == null ? recordRepo.findByInvoiceDateBetween(rng[0], rng[1]) : recordRepo.findByCampusIdAndInvoiceDateBetween(campusId, rng[0], rng[1]);
        Map<String, Object> out = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        Map<Long, BigDecimal> byCampus = new LinkedHashMap<>();
        for (InvoiceRecord r : list) {
            BigDecimal amt = nvl(r.getAmount());
            total = total.add(amt);
            Long c = r.getCampusId();
            if (c != null) byCampus.put(c, byCampus.getOrDefault(c, BigDecimal.ZERO).add(amt));
        }
        out.put("month", month);
        out.put("totalAmount", total);
        out.put("byCampus", byCampus);
        return ApiResponse.success(out);
    }

    @GetMapping("/export/details")
    public ResponseEntity<byte[]> exportDetails(@RequestParam("month") String month,
                                                @RequestParam(value = "campusId", required = false) Long campusId) {
        Date[] rng = monthRange(month);
        List<InvoiceRecord> list = campusId == null ? recordRepo.findByInvoiceDateBetween(rng[0], rng[1]) : recordRepo.findByCampusIdAndInvoiceDateBetween(campusId, rng[0], rng[1]);
        StringBuilder sb = new StringBuilder();
        sb.append("发票号,开票日期,校区ID,学员ID,金额,寄送状态,申请ID,缴费记录ID\n");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        for (InvoiceRecord r : list) {
            sb.append(s(r.getInvoiceNo())).append(',')
              .append(r.getInvoiceDate() == null ? "" : df.format(r.getInvoiceDate())).append(',')
              .append(n(r.getCampusId())).append(',')
              .append(n(r.getStudentId())).append(',')
              .append(n(r.getAmount())).append(',')
              .append(s(r.getDeliveryStatus())).append(',')
              .append(n(r.getApplicationId())).append(',')
              .append(n(r.getSettlementId()))
              .append('\n');
        }
        byte[] bytes = sb.toString().getBytes();
        String filename = "invoice-details-" + month + (campusId != null ? ("-" + campusId) : "") + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }

    private static BigDecimal toBD(Object v) { try { return new BigDecimal(String.valueOf(v)); } catch (Exception e) { return BigDecimal.ZERO; } }
    private static Long toLong(Object v) { try { return Long.valueOf(String.valueOf(v)); } catch (Exception e) { return null; } }
    private static BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private static String s(String v) { return v == null ? "" : v; }
    private static String n(Object v) { return v == null ? "" : String.valueOf(v); }
    private static String today() { return new SimpleDateFormat("yyyy-MM-dd").format(new Date()); }
    private static Date parseDate(String s) { try { return new SimpleDateFormat("yyyy-MM-dd").parse(s); } catch (Exception e) { return new Date(); } }
    private static Date startOfMonth(Date d) { Calendar c = Calendar.getInstance(); c.setTime(d); c.set(Calendar.DAY_OF_MONTH,1); zeroTime(c); return c.getTime(); }
    private static Date endOfMonth(Date d) { Calendar c = Calendar.getInstance(); c.setTime(d); c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH)); endTime(c); return c.getTime(); }
    private static void zeroTime(Calendar c) { c.set(Calendar.HOUR_OF_DAY,0); c.set(Calendar.MINUTE,0); c.set(Calendar.SECOND,0); c.set(Calendar.MILLISECOND,0); }
    private static void endTime(Calendar c) { c.set(Calendar.HOUR_OF_DAY,23); c.set(Calendar.MINUTE,59); c.set(Calendar.SECOND,59); c.set(Calendar.MILLISECOND,999); }
    private static Date[] monthRange(String ym) {
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM");
            f.setLenient(false);
            Date m = f.parse(ym);
            Calendar c = Calendar.getInstance();
            c.setTime(m);
            c.set(Calendar.DAY_OF_MONTH,1);
            zeroTime(c);
            Date start = c.getTime();
            c.add(Calendar.MONTH,1);
            c.add(Calendar.DAY_OF_MONTH,-1);
            endTime(c);
            Date end = c.getTime();
            return new Date[]{start, end};
        } catch (Exception e) {
            return new Date[]{startOfMonth(new Date()), endOfMonth(new Date())};
        }
    }
    private static String genInvoiceNo() { return "INV-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()); }
}