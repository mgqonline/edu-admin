package com.eduadmin.student.service;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.student.entity.StudentReferral;
import com.eduadmin.student.repo.StudentReferralRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class StudentReferralService {

    @Autowired
    private StudentReferralRepository referralRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long asLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    @Transactional
    public ApiResponse<Map<String, Object>> relate(Map<String, Object> payload) {
        Long referrerId = asLong(payload.get("referrerId"));
        Long newStudentId = asLong(payload.get("newStudentId"));
        if (referrerId == null || newStudentId == null) {
            return ApiResponse.error(400, "referrerId与newStudentId不能为空或格式错误");
        }
        if (referralRepository.existsByReferrerIdAndNewStudentId(referrerId, newStudentId)) {
            return ApiResponse.error(409, "该转介绍关系已存在");
        }
        Object ruleObj = payload.getOrDefault("rewardRule", new HashMap<>());
        String ruleJson;
        try { ruleJson = objectMapper.writeValueAsString(ruleObj); }
        catch (Exception e) { ruleJson = "{}"; }

        StudentReferral r = new StudentReferral();
        r.setReferrerId(referrerId);
        r.setNewStudentId(newStudentId);
        r.setRewardRuleJson(ruleJson);
        r.setTime(new Date());
        r = referralRepository.save(r);

        return ApiResponse.success(toMap(r));
    }

    public ApiResponse<List<Map<String, Object>>> list(Long referrerId, Long newStudentId) {
        List<StudentReferral> list;
        if (referrerId != null && newStudentId != null) {
            list = referralRepository.findByReferrerIdAndNewStudentIdOrderByIdAsc(referrerId, newStudentId);
        } else if (referrerId != null) {
            list = referralRepository.findByReferrerIdOrderByIdAsc(referrerId);
        } else if (newStudentId != null) {
            list = referralRepository.findByNewStudentIdOrderByIdAsc(newStudentId);
        } else {
            list = referralRepository.findAll();
            list.sort(Comparator.comparing(StudentReferral::getId));
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (StudentReferral r : list) out.add(toMap(r));
        return ApiResponse.success(out);
    }

    private Map<String, Object> toMap(StudentReferral r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("referrerId", r.getReferrerId());
        m.put("newStudentId", r.getNewStudentId());
        try {
            Map<String, Object> rule = objectMapper.readValue(
                    Optional.ofNullable(r.getRewardRuleJson()).orElse("{}"),
                    new TypeReference<Map<String, Object>>(){}
            );
            m.put("rewardRule", rule);
        } catch (Exception e) {
            m.put("rewardRule", new HashMap<String, Object>());
        }
        m.put("time", r.getTime() != null ? r.getTime().toInstant().toString() : null);
        return m;
    }
}