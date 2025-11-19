package com.eduadmin.student.grade;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.student.grade.entity.StudentGrade;
import com.eduadmin.student.grade.repo.StudentGradeRepository;
import com.eduadmin.student.entity.Student;
import com.eduadmin.student.repo.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student/grade")
public class StudentGradeController {

    @Autowired
    private StudentGradeRepository repo;

    @Autowired
    private StudentRepository studentRepo;

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(value = "studentId", required = false) Long studentId,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "term", required = false) String term,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ) {
        if (page == null || page < 1) page = 1;
        if (size == null || size <= 0) size = 10;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page - 1, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "examDate", "id"));
        String sub = subject == null ? "" : subject;
        String tm = term == null ? "" : term;
        org.springframework.data.domain.Page<StudentGrade> pg;
        if (studentId != null) {
            pg = repo.findByStudentIdAndSubjectContainingIgnoreCaseAndTermContainingIgnoreCase(studentId, sub, tm, pageable);
        } else {
            pg = repo.findBySubjectContainingIgnoreCaseAndTermContainingIgnoreCase(sub, tm, pageable);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", pg.getContent());
        out.put("total", pg.getTotalElements());
        out.put("page", page);
        out.put("size", size);
        out.put("pages", pg.getTotalPages());
        return ApiResponse.success(out);
    }

    @PostMapping("/save")
    public ApiResponse<StudentGrade> save(@RequestBody Map<String, Object> payload) {
        try {
            StudentGrade g = new StudentGrade();
            if (payload.get("id") != null) {
                Optional<StudentGrade> opt = repo.findById(Long.valueOf(String.valueOf(payload.get("id"))));
                if (opt.isPresent()) g = opt.get();
            }
            g.setStudentId(Long.valueOf(String.valueOf(payload.get("studentId"))));
            g.setSubject(String.valueOf(payload.getOrDefault("subject", "")));
            g.setTerm(String.valueOf(payload.getOrDefault("term", "")));
            g.setScore(payload.get("score") == null ? null : Double.valueOf(String.valueOf(payload.get("score"))));
            if (payload.get("examDate") != null) {
                try {
                    g.setExamDate(new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(payload.get("examDate"))));
                } catch (Exception ignore) {}
            }
            g.setStatus(String.valueOf(payload.getOrDefault("status", "")));
            g.setRemark(String.valueOf(payload.getOrDefault("remark", "")));
            StudentGrade saved = repo.save(g);
            return ApiResponse.success(saved);
        } catch (Exception e) {
            return ApiResponse.error(400, "保存失败: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ApiResponse<StudentGrade> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Optional<StudentGrade> opt = repo.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "成绩记录不存在");
        StudentGrade g = opt.get();
        if (payload.containsKey("studentId")) g.setStudentId(Long.valueOf(String.valueOf(payload.get("studentId"))));
        if (payload.containsKey("subject")) g.setSubject(String.valueOf(payload.get("subject")));
        if (payload.containsKey("term")) g.setTerm(String.valueOf(payload.get("term")));
        if (payload.containsKey("score")) g.setScore(Double.valueOf(String.valueOf(payload.get("score"))));
        if (payload.containsKey("examDate")) {
            try { g.setExamDate(new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(payload.get("examDate")))); } catch (Exception ignore) {}
        }
        if (payload.containsKey("status")) g.setStatus(String.valueOf(payload.get("status")));
        if (payload.containsKey("remark")) g.setRemark(String.valueOf(payload.get("remark")));
        return ApiResponse.success(repo.save(g));
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ApiResponse.error(404, "成绩记录不存在");
        repo.deleteById(id);
        return ApiResponse.success(true);
    }

    @PostMapping("/import")
    public ApiResponse<Map<String, Object>> importGrades(@RequestBody List<Map<String, Object>> rows) {
        int saved = 0;
        for (Map<String, Object> r : rows) {
            try {
                StudentGrade g = new StudentGrade();
                g.setStudentId(Long.valueOf(String.valueOf(r.get("studentId"))));
                g.setSubject(String.valueOf(r.getOrDefault("subject", "")));
                g.setTerm(String.valueOf(r.getOrDefault("term", "")));
                g.setScore(r.get("score") == null ? null : Double.valueOf(String.valueOf(r.get("score"))));
                if (r.get("examDate") != null) {
                    try { g.setExamDate(new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(r.get("examDate")))); } catch (Exception ignore) {}
                }
                g.setStatus(String.valueOf(r.getOrDefault("status", "")));
                g.setRemark(String.valueOf(r.getOrDefault("remark", "")));
                repo.save(g);
                saved++;
            } catch (Exception ignore) {}
        }
        Map<String, Object> out = new HashMap<>();
        out.put("imported", saved);
        return ApiResponse.success(out);
    }

    @GetMapping(value = "/export", produces = "text/csv; charset=UTF-8")
    public @ResponseBody byte[] export(
            @RequestParam(value = "studentId", required = false) Long studentId,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "term", required = false) String term
    ) {
        String sub = subject == null ? "" : subject;
        String tm = term == null ? "" : term;
        List<StudentGrade> list = (studentId != null)
                ? repo.findByStudentIdAndSubjectContainingIgnoreCaseAndTermContainingIgnoreCase(studentId, sub, tm)
                : repo.findBySubjectContainingIgnoreCaseAndTermContainingIgnoreCase(sub, tm);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder sb = new StringBuilder();
        sb.append("id,studentId,studentName,subject,term,score,examDate,status,remark\n");
        for (StudentGrade g : list) {
            String name = null;
            try {
                Optional<Student> sOpt = studentRepo.findById(g.getStudentId());
                name = sOpt.map(Student::getName).orElse("");
            } catch (Exception ignore) {}
            sb.append(String.valueOf(g.getId()==null?"":g.getId())).append(',')
              .append(String.valueOf(g.getStudentId())).append(',')
              .append(name==null?"":name).append(',')
              .append(g.getSubject()==null?"":g.getSubject()).append(',')
              .append(g.getTerm()==null?"":g.getTerm()).append(',')
              .append(g.getScore()==null?"":String.valueOf(g.getScore())).append(',')
              .append(g.getExamDate()==null?"":fmt.format(g.getExamDate())).append(',')
              .append(g.getStatus()==null?"":g.getStatus()).append(',')
              .append(g.getRemark()==null?"":g.getRemark()).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}