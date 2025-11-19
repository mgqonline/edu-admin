package com.eduadmin.student.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.student.entity.Audition;
import com.eduadmin.student.repo.AuditionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/audition")
public class AuditionController {

    @Autowired
    private AuditionRepository auditionRepository;

    static class AuditionDTO {
        public Long id;
        public Long studentId;
        public Long courseId;
        public Long teacherId;
        public String time; // yyyy-MM-dd HH:mm
        public String status; // booked/finished
        public String feedback; // 教师评价
        public String result; // 适合报班/需再评估
    }

    private final SimpleDateFormat dtFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private AuditionDTO fromEntity(Audition e) {
        AuditionDTO dto = new AuditionDTO();
        dto.id = e.getId();
        dto.studentId = e.getStudentId();
        dto.courseId = e.getCourseId();
        dto.teacherId = e.getTeacherId();
        dto.time = e.getTime() == null ? null : dtFmt.format(e.getTime());
        dto.status = e.getStatus();
        dto.feedback = e.getFeedback();
        dto.result = e.getResult();
        return dto;
    }

    private Map<String, Object> toMap(AuditionDTO a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.id);
        m.put("studentId", a.studentId);
        m.put("courseId", a.courseId);
        m.put("teacherId", a.teacherId);
        m.put("time", a.time);
        m.put("status", a.status);
        m.put("feedback", a.feedback);
        m.put("result", a.result);
        return m;
    }

    @PostMapping("/book")
    public ApiResponse<Map<String, Object>> book(@RequestBody AuditionDTO dto) {
        try {
            Audition e = new Audition();
            e.setStudentId(dto.studentId);
            e.setCourseId(dto.courseId);
            e.setTeacherId(dto.teacherId);
            try { e.setTime(dto.time == null ? null : dtFmt.parse(dto.time)); } catch (Exception ignore) {}
            e.setStatus("booked");
            e.setFeedback(dto.feedback);
            e.setResult(dto.result);
            e = auditionRepository.save(e);
            return ApiResponse.success(toMap(fromEntity(e)));
        } catch (Exception ex) {
            return ApiResponse.error(500, "预约试听失败: " + ex.getMessage());
        }
    }

    @PostMapping("/feedback")
    public ApiResponse<Map<String, Object>> feedback(@RequestBody AuditionDTO dto) {
        try {
            Optional<Audition> opt = auditionRepository.findById(dto.id);
            if (!opt.isPresent()) return ApiResponse.error(404, "未找到试听记录");
            Audition e = opt.get();
            e.setFeedback(dto.feedback);
            e.setResult(dto.result);
            e.setStatus("finished");
            e = auditionRepository.save(e);
            return ApiResponse.success(toMap(fromEntity(e)));
        } catch (Exception ex) {
            return ApiResponse.error(500, "提交反馈失败: " + ex.getMessage());
        }
    }

    @PostMapping("/convert")
    public ApiResponse<Map<String, Object>> convert(@RequestBody Map<String, Object> payload) {
        try {
            Long auditionId = ((Number) payload.get("auditionId")).longValue();
            Optional<Audition> opt = auditionRepository.findById(auditionId);
            if (!opt.isPresent()) return ApiResponse.error(404, "未找到试听记录");
            Map<String, Object> out = toMap(fromEntity(opt.get()));
            out.put("convertReady", true);
            return ApiResponse.success(out);
        } catch (Exception ex) {
            return ApiResponse.error(500, "转换提示失败: " + ex.getMessage());
        }
    }

    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam(required = false) Long studentId) {
        try {
            List<Audition> found = studentId == null ? auditionRepository.findAll() : auditionRepository.findByStudentId(studentId);
            List<Map<String, Object>> out = new ArrayList<>();
            for (Audition e : found) {
                out.add(toMap(fromEntity(e)));
            }
            return ApiResponse.success(out);
        } catch (Exception ex) {
            return ApiResponse.error(500, "查询失败: " + ex.getMessage());
        }
    }

    @PostMapping("/delete")
    public ApiResponse<Boolean> delete(@RequestBody Map<String, Object> payload) {
        try {
            Long id = ((Number) payload.get("id")).longValue();
            if (!auditionRepository.existsById(id)) {
                return ApiResponse.error(404, "未找到试听记录");
            }
            auditionRepository.deleteById(id);
            return ApiResponse.success(true);
        } catch (Exception ex) {
            return ApiResponse.error(500, "删除失败: " + ex.getMessage());
        }
    }
}