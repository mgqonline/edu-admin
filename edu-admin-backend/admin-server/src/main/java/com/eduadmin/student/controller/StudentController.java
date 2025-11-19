package com.eduadmin.student.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.baseplatform.campus.entity.Campus;
import com.eduadmin.baseplatform.campus.repo.CampusRepository;
import com.eduadmin.student.entity.Student;
import com.eduadmin.student.entity.StudentHistory;
import com.eduadmin.student.repo.StudentHistoryRepository;
import com.eduadmin.student.repo.StudentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    static class StudentDTO {
        public Long id;
        public String name;
        public String gender; // male/female
        public String birthDate; // yyyy-MM-dd
        public String idType; // 身份证/护照 等
        public String idNumber;
        public String photoUrl;
        public Long campusId; // 关联校区ID
        public List<Map<String, Object>> guardians; // [{name, relation, phone, emergency}]
        public List<String> originChannels; // 地推/转介绍/线上
        public List<String> interests;
        public List<String> studyTags; // 学情标签
    }

    static class HistoryRecord {
        public Long id;
        public Long studentId;
        public String type; // 报班/缴费/请假/调班
        public String detail;
        public String time; // ISO string
        public Double feeAmount; // 费用（用于缴费/续费统计，可选）
    }

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private StudentHistoryRepository studentHistoryRepository;
    @Autowired(required = false)
    private CampusRepository campusRepository;

    private final ObjectMapper mapper = new ObjectMapper();
    private final SimpleDateFormat dtFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @PostMapping("/save")
    public ApiResponse<Map<String, Object>> save(@RequestBody StudentDTO dto) {
        try {
            Student s = new Student();
            s.setName(dto.name);
            s.setGender(dto.gender);
            s.setBirthDate(dto.birthDate);
            s.setIdType(dto.idType);
            s.setIdNumber(dto.idNumber);
            s.setPhotoUrl(dto.photoUrl);
            s.setCampusId(dto.campusId);
            s.setGuardiansJson(dto.guardians == null ? "[]" : mapper.writeValueAsString(dto.guardians));
            s.setOriginChannelsJson(dto.originChannels == null ? "[]" : mapper.writeValueAsString(dto.originChannels));
            s.setInterestsJson(dto.interests == null ? "[]" : mapper.writeValueAsString(dto.interests));
            s.setStudyTagsJson(dto.studyTags == null ? "[]" : mapper.writeValueAsString(dto.studyTags));
            s.setCreatedAt(new Date());
            s = studentRepository.save(s);
            return ApiResponse.success(toMap(fromEntity(s)));
        } catch (Exception ex) {
            return ApiResponse.error(500, "保存学生失败: " + ex.getMessage());
        }
    }

    @PostMapping("/update")
    public ApiResponse<Map<String, Object>> update(@RequestBody StudentDTO dto) {
        try {
            Optional<Student> opt = studentRepository.findById(dto.id);
            if (!opt.isPresent()) return ApiResponse.error(404, "学生不存在");
            Student s = opt.get();
            s.setName(dto.name);
            s.setGender(dto.gender);
            s.setBirthDate(dto.birthDate);
            s.setIdType(dto.idType);
            s.setIdNumber(dto.idNumber);
            s.setPhotoUrl(dto.photoUrl);
            s.setCampusId(dto.campusId);
            s.setGuardiansJson(dto.guardians == null ? "[]" : mapper.writeValueAsString(dto.guardians));
            s.setOriginChannelsJson(dto.originChannels == null ? "[]" : mapper.writeValueAsString(dto.originChannels));
            s.setInterestsJson(dto.interests == null ? "[]" : mapper.writeValueAsString(dto.interests));
            s.setStudyTagsJson(dto.studyTags == null ? "[]" : mapper.writeValueAsString(dto.studyTags));
            s = studentRepository.save(s);
            return ApiResponse.success(toMap(fromEntity(s)));
        } catch (Exception ex) {
            return ApiResponse.error(500, "更新学生失败: " + ex.getMessage());
        }
    }

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page - 1,
                size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt", "id")
        );
        org.springframework.data.domain.Page<Student> pg = studentRepository.findAll(pageable);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Student s : pg.getContent()) items.add(toMap(fromEntity(s)));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("total", pg.getTotalElements());
        out.put("page", page);
        out.put("size", size);
        out.put("pages", pg.getTotalPages());
        return ApiResponse.success(out);
    }

    @GetMapping("/search")
    public ApiResponse<List<Map<String, Object>>> search(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit
    ) {
        String keyword = name == null ? "" : name.trim();
        List<Student> found = keyword.isEmpty() ? studentRepository.findAll() : studentRepository.findByNameContaining(keyword);
        found.sort((a, b) -> {
            Long ba = b.getId() == null ? 0L : b.getId();
            Long aa = a.getId() == null ? 0L : a.getId();
            return Long.compare(ba, aa);
        });
        if (limit == null || limit < 1) limit = 20;
        int c = Math.min(found.size(), limit);
        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = 0; i < c; i++) {
            Student s = found.get(i);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("name", s.getName());
            out.add(m);
        }
        return ApiResponse.success(out);
    }

    @GetMapping("/detail")
    public ApiResponse<Map<String, Object>> detail(@RequestParam Long id) {
        Optional<Student> opt = studentRepository.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "未找到学生");
        return ApiResponse.success(toMap(fromEntity(opt.get())));
    }

    @PostMapping("/history/add")
    public ApiResponse<Map<String, Object>> addHistory(@RequestBody HistoryRecord record) {
        try {
            StudentHistory h = new StudentHistory();
            h.setStudentId(record.studentId);
            h.setType(record.type);
            h.setDetail(record.detail);
            try {
                // 兼容 yyyy-MM-dd HH:mm 或 ISO 字符串
                Date t = null;
                try { t = record.time == null ? null : dtFmt.parse(record.time); } catch (Exception ignore) {}
                if (t == null && record.time != null) {
                    t = javax.xml.bind.DatatypeConverter.parseDateTime(record.time).getTime();
                }
                h.setTime(t == null ? new Date() : t);
            } catch (Exception e) {
                h.setTime(new Date());
            }
            h.setFeeAmount(record.feeAmount);
            h = studentHistoryRepository.save(h);

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("id", h.getId());
            out.put("studentId", h.getStudentId());
            out.put("type", h.getType());
            out.put("detail", h.getDetail());
            out.put("time", dtFmt.format(h.getTime()));
            if (h.getFeeAmount() != null) out.put("feeAmount", h.getFeeAmount());
            // 缴费/续费写入看板
            try {
                String t = String.valueOf(h.getType());
                if ("缴费".equals(t) || "续费".equals(t)) {
                    com.eduadmin.dashboard.store.DashboardStore.addOperation("renew", h.getFeeAmount() == null ? 0.0 : h.getFeeAmount(), null, new Date());
                }
            } catch (Exception ignore) {}
            return ApiResponse.success(out);
        } catch (Exception ex) {
            return ApiResponse.error(500, "添加历史失败: " + ex.getMessage());
        }
    }

    @GetMapping("/history/list")
    public ApiResponse<List<Map<String, Object>>> history(@RequestParam Long studentId) {
        List<StudentHistory> rs = studentHistoryRepository.findByStudentIdOrderByTimeDesc(studentId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (StudentHistory r : rs) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("studentId", r.getStudentId());
            m.put("type", r.getType());
            m.put("detail", r.getDetail());
            m.put("time", dtFmt.format(r.getTime()));
            if (r.getFeeAmount() != null) m.put("feeAmount", r.getFeeAmount());
            out.add(m);
        }
        return ApiResponse.success(out);
    }

    @PostMapping("/import")
    public ApiResponse<Map<String, Object>> importStudents(@RequestBody List<StudentDTO> batch) {
        try {
            for (StudentDTO dto : batch) {
                Student s = dto.id == null ? new Student() : studentRepository.findById(dto.id).orElse(new Student());
                s.setName(dto.name);
                s.setGender(dto.gender);
                s.setBirthDate(dto.birthDate);
                s.setIdType(dto.idType);
                s.setIdNumber(dto.idNumber);
                s.setPhotoUrl(dto.photoUrl);
                s.setCampusId(dto.campusId);
                s.setGuardiansJson(dto.guardians == null ? "[]" : mapper.writeValueAsString(dto.guardians));
                s.setOriginChannelsJson(dto.originChannels == null ? "[]" : mapper.writeValueAsString(dto.originChannels));
                s.setInterestsJson(dto.interests == null ? "[]" : mapper.writeValueAsString(dto.interests));
                s.setStudyTagsJson(dto.studyTags == null ? "[]" : mapper.writeValueAsString(dto.studyTags));
                if (s.getCreatedAt() == null) s.setCreatedAt(new Date());
                studentRepository.save(s);
            }
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("count", batch.size());
            return ApiResponse.success(out);
        } catch (Exception ex) {
            return ApiResponse.error(500, "导入失败: " + ex.getMessage());
        }
    }

    @PostMapping("/import/demo")
    public ApiResponse<Map<String, Object>> importDemoStudents() {
        try {
            List<Long> campusIds = new ArrayList<>();
            if (campusRepository != null) {
                try {
                    for (Campus c : campusRepository.findAll()) {
                        if (c != null && c.getId() != null) campusIds.add(c.getId());
                    }
                } catch (Exception ignore) {}
            }
            Long defaultCampusId = campusIds.isEmpty() ? null : campusIds.get(0);

            String[] surnames = new String[]{"张","李","王","刘","陈","杨","黄","赵","周","吴"};
            String[] givenA = new String[]{"一","二","三","四","五"};
            String[] givenB = new String[]{"明","芳","杰","娜","婷"};
            String[] channels = new String[]{"地推","转介绍","线上"};
            String[] interestsDict = new String[]{"美术","钢琴","篮球","羽毛球","围棋","编程"};
            String[] tagsDict = new String[]{"基础一般","注意力需提升","积极主动","作业完成度高","需家校沟通"};

            int count = 50;
            for (int i = 0; i < count; i++) {
                String name = surnames[i % surnames.length] + givenA[(i / surnames.length) % givenA.length] + givenB[i % givenB.length];
                Student s = new Student();
                s.setName(name);
                s.setGender((i % 2 == 0) ? "male" : "female");
                int year = 2011 + (i % 5);
                int month = 1 + (i % 12);
                int day = 1 + (i % 28);
                s.setBirthDate(String.format("%04d-%02d-%02d", year, month, day));
                s.setIdType("身份证");
                s.setIdNumber("ID" + String.format("%06d", i + 1));
                s.setCampusId(defaultCampusId);
                s.setPhotoUrl("");

                List<Map<String, Object>> guardians = new ArrayList<>();
                Map<String, Object> g1 = new LinkedHashMap<>();
                g1.put("name", surnames[i % surnames.length] + "先生");
                g1.put("relation", "父亲");
                g1.put("phone", "138" + String.format("%08d", i + 10000000));
                g1.put("emergency", Boolean.TRUE);
                guardians.add(g1);
                Map<String, Object> g2 = new LinkedHashMap<>();
                g2.put("name", surnames[(i + 1) % surnames.length] + "女士");
                g2.put("relation", "母亲");
                g2.put("phone", "139" + String.format("%08d", i + 10000000));
                g2.put("emergency", Boolean.FALSE);
                guardians.add(g2);

                List<String> originChannels = Arrays.asList(channels[i % channels.length]);
                List<String> interests = Arrays.asList(interestsDict[i % interestsDict.length]);
                List<String> studyTags = Arrays.asList(tagsDict[i % tagsDict.length]);

                s.setGuardiansJson(mapper.writeValueAsString(guardians));
                s.setOriginChannelsJson(mapper.writeValueAsString(originChannels));
                s.setInterestsJson(mapper.writeValueAsString(interests));
                s.setStudyTagsJson(mapper.writeValueAsString(studyTags));

                s.setCreatedAt(new Date());
                studentRepository.save(s);
            }
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("count", count);
            return ApiResponse.success(out);
        } catch (Exception ex) {
            return ApiResponse.error(500, "生成数据失败: " + ex.getMessage());
        }
    }

    @GetMapping("/export")
    public ApiResponse<List<Map<String, Object>>> exportStudents() {
        List<Student> all = studentRepository.findAll();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Student s : all) list.add(toMap(fromEntity(s)));
        return ApiResponse.success(list);
    }

    private StudentDTO fromEntity(Student s) {
        StudentDTO dto = new StudentDTO();
        dto.id = s.getId();
        dto.name = s.getName();
        dto.gender = s.getGender();
        dto.birthDate = s.getBirthDate();
        dto.idType = s.getIdType();
        dto.idNumber = s.getIdNumber();
        dto.photoUrl = s.getPhotoUrl();
        dto.campusId = s.getCampusId();
        try {
            dto.guardians = s.getGuardiansJson() == null ? Collections.emptyList() : mapper.readValue(s.getGuardiansJson(), new TypeReference<List<Map<String, Object>>>(){});
        } catch (Exception ignore) { dto.guardians = Collections.emptyList(); }
        try {
            dto.originChannels = s.getOriginChannelsJson() == null ? Collections.emptyList() : mapper.readValue(s.getOriginChannelsJson(), new TypeReference<List<String>>(){});
        } catch (Exception ignore) { dto.originChannels = Collections.emptyList(); }
        try {
            dto.interests = s.getInterestsJson() == null ? Collections.emptyList() : mapper.readValue(s.getInterestsJson(), new TypeReference<List<String>>(){});
        } catch (Exception ignore) { dto.interests = Collections.emptyList(); }
        try {
            dto.studyTags = s.getStudyTagsJson() == null ? Collections.emptyList() : mapper.readValue(s.getStudyTagsJson(), new TypeReference<List<String>>(){});
        } catch (Exception ignore) { dto.studyTags = Collections.emptyList(); }
        return dto;
    }

    private Map<String, Object> toMap(StudentDTO s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.id);
        m.put("name", s.name);
        m.put("gender", s.gender);
        m.put("birthDate", s.birthDate);
        m.put("idType", s.idType);
        m.put("idNumber", s.idNumber);
        m.put("photoUrl", s.photoUrl);
        m.put("campusId", s.campusId);
        if (s.campusId != null && campusRepository != null) {
            Optional<Campus> opt = campusRepository.findById(s.campusId);
            opt.ifPresent(c -> {
                m.put("campusName", c.getName());
                m.put("campusTitle", c.getName());
            });
        }
        m.put("guardians", s.guardians == null ? Collections.emptyList() : s.guardians);
        m.put("originChannels", s.originChannels == null ? Collections.emptyList() : s.originChannels);
        m.put("interests", s.interests == null ? Collections.emptyList() : s.interests);
        m.put("studyTags", s.studyTags == null ? Collections.emptyList() : s.studyTags);
        return m;
    }
}