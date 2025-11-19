package com.eduadmin.finance.controller;

import com.eduadmin.finance.entity.RefundRequest;
import com.eduadmin.finance.entity.RefundApproverConfig;
import com.eduadmin.finance.repo.RefundRequestRepository;
import com.eduadmin.finance.repo.RefundApproverConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/finance/refund")
public class RefundController {
    @Autowired
    private RefundRequestRepository repository;
    @Autowired
    private RefundApproverConfigRepository approverConfigRepo;

    private BigDecimal calcAutoAmount(BigDecimal remainingHours, BigDecimal unitPrice, BigDecimal serviceFee) {
        if (remainingHours == null) remainingHours = BigDecimal.ZERO;
        if (unitPrice == null) unitPrice = BigDecimal.ZERO;
        if (serviceFee == null) serviceFee = BigDecimal.ZERO;
        return remainingHours.multiply(unitPrice).subtract(serviceFee);
    }

    @PostMapping("/apply")
    public Map<String, Object> apply(@RequestBody Map<String, Object> body) {
        RefundRequest r = new RefundRequest();
        r.setStudentId(Long.valueOf(String.valueOf(body.get("studentId"))));
        r.setClassId(Long.valueOf(String.valueOf(body.get("classId"))));
        r.setReason(String.valueOf(body.getOrDefault("reason", "")));
        BigDecimal remainingHours = new BigDecimal(String.valueOf(body.getOrDefault("remainingHours", "0")));
        BigDecimal unitPrice = new BigDecimal(String.valueOf(body.getOrDefault("unitPrice", "0")));
        BigDecimal serviceFee = new BigDecimal(String.valueOf(body.getOrDefault("serviceFee", "0")));
        r.setRemainingHours(remainingHours);
        r.setUnitPrice(unitPrice);
        r.setServiceFee(serviceFee);
        BigDecimal autoAmount = calcAutoAmount(remainingHours, unitPrice, serviceFee);
        r.setAutoAmount(autoAmount);
        r.setFinalAmount(autoAmount);
        r.setStatus("PENDING_L1");
        repository.save(r);
        return resp("ok", r);
    }

    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(value = "status", required = false) String status) {
        List<RefundRequest> all = repository.findAllByOrderByIdDesc();
        if (status != null && !status.isEmpty()) {
            all.removeIf(r -> !status.equals(r.getStatus()));
        }
        List<Map<String, Object>> withLabel = new ArrayList<>();
        for (RefundRequest r : all) {
            withLabel.add(withLabels(r));
        }
        return resp("ok", withLabel);
    }

    @GetMapping("/detail/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        Optional<RefundRequest> opt = repository.findById(id);
        if (!opt.isPresent()) return resp("error", "not_found");
        return resp("ok", withLabels(opt.get()));
    }

    // ====== 审批人配置 ======
    @GetMapping("/approver/config")
    public Map<String, Object> getApproverConfig() {
        RefundApproverConfig cfg = ensureApproverCfg();
        Map<String, Object> m = new HashMap<>();
        m.put("level1ApproverIds", toIdList(cfg.getLevel1ApproverIds()));
        m.put("level2ApproverIds", toIdList(cfg.getLevel2ApproverIds()));
        m.put("financeApproverIds", toIdList(cfg.getFinanceApproverIds()));
        return resp("ok", m);
    }

    @PutMapping("/approver/config")
    public Map<String, Object> saveApproverConfig(@RequestBody Map<String, Object> body) {
        RefundApproverConfig cfg = ensureApproverCfg();
        cfg.setLevel1ApproverIds(fromIdList((List<?>) body.getOrDefault("level1ApproverIds", Collections.emptyList())));
        cfg.setLevel2ApproverIds(fromIdList((List<?>) body.getOrDefault("level2ApproverIds", Collections.emptyList())));
        cfg.setFinanceApproverIds(fromIdList((List<?>) body.getOrDefault("financeApproverIds", Collections.emptyList())));
        approverConfigRepo.save(cfg);
        Map<String, Object> m = new HashMap<>();
        m.put("level1ApproverIds", toIdList(cfg.getLevel1ApproverIds()));
        m.put("level2ApproverIds", toIdList(cfg.getLevel2ApproverIds()));
        m.put("financeApproverIds", toIdList(cfg.getFinanceApproverIds()));
        return resp("ok", m);
    }

    @PutMapping("/adjust/{id}")
    public Map<String, Object> adjust(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<RefundRequest> opt = repository.findById(id);
        if (!opt.isPresent()) return resp("error", "not_found");
        RefundRequest r = opt.get();
        BigDecimal finalAmount = new BigDecimal(String.valueOf(body.getOrDefault("finalAmount", r.getFinalAmount())));
        r.setFinalAmount(finalAmount);
        r.setReason(r.getReason() + "\n[调整] " + String.valueOf(body.getOrDefault("note", "")));
        // 调整后必须重新走审批，从第一层开始
        r.setStatus("PENDING_L1");
        repository.save(r);
        return resp("ok", r);
    }

    @PostMapping("/approve/{id}")
    public Map<String, Object> approve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<RefundRequest> opt = repository.findById(id);
        if (!opt.isPresent()) return resp("error", "not_found");
        RefundRequest r = opt.get();
        String level = String.valueOf(body.getOrDefault("level", "1"));
        boolean pass = Boolean.parseBoolean(String.valueOf(body.getOrDefault("pass", true)));
        Long approverId = body.get("approverId") == null ? null : Long.valueOf(String.valueOf(body.get("approverId")));
        Date now = new Date();
        if (!pass) {
            r.setStatus("REJECTED");
        } else {
            // 校验审批级别与当前状态匹配
            switch (level) {
                case "1":
                    if (!"PENDING_L1".equals(r.getStatus())) return resp("error", "invalid_status_for_level");
                    break;
                case "2":
                    if (!"PENDING_L2".equals(r.getStatus())) return resp("error", "invalid_status_for_level");
                    break;
                case "finance":
                    if (!"PENDING_FINANCE".equals(r.getStatus())) return resp("error", "invalid_status_for_level");
                    break;
                default:
                    return resp("error", "invalid_level");
            }
            // 校验审批人是否在允许名单内
            if (approverId == null) return resp("error", "approver_required");
            RefundApproverConfig cfg = ensureApproverCfg();
            List<Long> allowed;
            if ("1".equals(level)) allowed = toIdList(cfg.getLevel1ApproverIds());
            else if ("2".equals(level)) allowed = toIdList(cfg.getLevel2ApproverIds());
            else allowed = toIdList(cfg.getFinanceApproverIds());
            if (allowed.isEmpty() || !allowed.contains(approverId)) return resp("error", "approver_not_allowed");
            switch (level) {
                case "1":
                    r.setLevel1ApproverId(approverId);
                    r.setLevel1At(now);
                    r.setStatus("PENDING_L2");
                    break;
                case "2":
                    r.setLevel2ApproverId(approverId);
                    r.setLevel2At(now);
                    r.setStatus("PENDING_FINANCE");
                    break;
                case "finance":
                    r.setFinanceApproverId(approverId);
                    r.setFinanceAt(now);
                    r.setStatus("APPROVED");
                    break;
                default:
                    return resp("error", "invalid_level");
            }
        }
        repository.save(r);
        return resp("ok", r);
    }

    @PostMapping("/execute/{id}")
    public Map<String, Object> execute(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<RefundRequest> opt = repository.findById(id);
        if (!opt.isPresent()) return resp("error", "not_found");
        RefundRequest r = opt.get();
        if (!"APPROVED".equals(r.getStatus())) {
            return resp("error", "not_approved");
        }
        if ("REFUNDED".equals(r.getStatus()) && r.getRefundAt() != null) {
            return resp("error", "already_refunded");
        }
        String inputMethod = String.valueOf(body.getOrDefault("refundMethod", ""));
        String normalized = normalizeMethod(inputMethod);
        if (normalized == null) {
            return resp("error", "invalid_refund_method");
        }
        r.setRefundMethod(normalized);
        r.setRefundTxnId(String.valueOf(body.getOrDefault("refundTxnId", "")));
        r.setRefundAt(new Date());
        r.setStatus("REFUNDED");
        repository.save(r);
        return resp("ok", withLabels(r));
    }

    private Map<String, Object> resp(String code, Object data) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", code);
        m.put("data", data);
        return m;
    }

    private RefundApproverConfig ensureApproverCfg() {
        Optional<RefundApproverConfig> opt = approverConfigRepo.findTopByOrderByIdAsc();
        if (opt.isPresent()) return opt.get();
        RefundApproverConfig cfg = new RefundApproverConfig();
        cfg.setLevel1ApproverIds("");
        cfg.setLevel2ApproverIds("");
        cfg.setFinanceApproverIds("");
        return approverConfigRepo.save(cfg);
    }

    private List<Long> toIdList(String csv) {
        List<Long> list = new ArrayList<>();
        if (csv == null || csv.trim().isEmpty()) return list;
        for (String s : csv.split(",")) {
            try { list.add(Long.valueOf(s.trim())); } catch (Exception ignore) {}
        }
        return list;
    }

    private String fromIdList(List<?> arr) {
        if (arr == null) return "";
        List<String> out = new ArrayList<>();
        for (Object o : arr) {
            if (o == null) continue;
            String s = String.valueOf(o).trim();
            if (s.isEmpty()) continue;
            try {
                Long v = Long.valueOf(s);
                out.add(String.valueOf(v));
            } catch (Exception ignore) {}
        }
        return String.join(",", out);
    }

    // ====== 退款方式规范化与标签 ======
    private static final Set<String> ALLOWED_METHODS = new HashSet<>(Arrays.asList(
            "cash", "wechat", "alipay", "bankcard", "banktransfer"
    ));

    private String normalizeMethod(String raw) {
        if (raw == null) return null;
        String v = raw.trim().toLowerCase(Locale.ROOT);
        // 中文别名映射到标准代码值
        switch (v) {
            case "现金": return "cash";
            case "微信": return "wechat";
            case "支付宝": return "alipay";
            case "银行卡": return "bankcard";
            case "银行转账": return "banktransfer";
        }
        // 英文代码值直接使用
        if (ALLOWED_METHODS.contains(v)) return v;
        return null;
    }

    private String methodLabel(String code) {
        if (code == null) return null;
        switch (code) {
            case "cash": return "现金";
            case "wechat": return "微信";
            case "alipay": return "支付宝";
            case "bankcard": return "银行卡";
            case "banktransfer": return "银行转账";
            default: return code;
        }
    }

    private Map<String, Object> withLabels(RefundRequest r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("studentId", r.getStudentId());
        m.put("classId", r.getClassId());
        m.put("remainingHours", r.getRemainingHours());
        m.put("unitPrice", r.getUnitPrice());
        m.put("serviceFee", r.getServiceFee());
        m.put("autoAmount", r.getAutoAmount());
        m.put("finalAmount", r.getFinalAmount());
        m.put("reason", r.getReason());
        m.put("status", r.getStatus());
        m.put("level1ApproverId", r.getLevel1ApproverId());
        m.put("level1At", r.getLevel1At());
        m.put("level2ApproverId", r.getLevel2ApproverId());
        m.put("level2At", r.getLevel2At());
        m.put("financeApproverId", r.getFinanceApproverId());
        m.put("financeAt", r.getFinanceAt());
        m.put("refundMethod", r.getRefundMethod());
        m.put("refundTxnId", r.getRefundTxnId());
        m.put("refundAt", r.getRefundAt());
        m.put("createdAt", r.getCreatedAt());
        m.put("updatedAt", r.getUpdatedAt());
        // 附加中文标签以便前端直接展示
        m.put("refundMethodLabel", methodLabel(r.getRefundMethod()));
        return m;
    }
}