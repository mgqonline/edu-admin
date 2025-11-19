package com.eduadmin.student.service;

import com.eduadmin.student.entity.GuardianPerm;
import com.eduadmin.student.repo.GuardianPermRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GuardianService {

    @Autowired
    private GuardianPermRepository guardianRepo;

    private final ObjectMapper mapper = new ObjectMapper();

    public List<GuardianPerm> listByStudent(Long studentId) {
        return guardianRepo.findByStudentIdOrderByIdDesc(studentId);
    }

    public GuardianPerm setPerms(Map<String, Object> payload) {
        Long studentId = toLong(payload.get("studentId"));
        String guardianPhone = String.valueOf(payload.getOrDefault("guardianPhone", ""));
        Optional<GuardianPerm> opt = guardianRepo.findByStudentIdAndGuardianPhone(studentId, guardianPhone);
        GuardianPerm target = opt.orElseGet(GuardianPerm::new);
        if (target.getId() == null) {
            target.setStudentId(studentId);
            target.setGuardianPhone(guardianPhone);
        }
        target.setGuardianName(String.valueOf(payload.getOrDefault("guardianName", "")));
        target.setRelation(String.valueOf(payload.getOrDefault("relation", "")));
        Map<String, Object> vpIn = (Map<String, Object>) payload.getOrDefault("viewPerms", Collections.emptyMap());
        Map<String, Boolean> vp = new HashMap<>();
        vp.put("schedule", Boolean.TRUE.equals(vpIn.get("schedule")));
        vp.put("grades", Boolean.TRUE.equals(vpIn.get("grades")));
        vp.put("payments", Boolean.TRUE.equals(vpIn.get("payments")));
        try { target.setViewPermsJson(mapper.writeValueAsString(vp)); } catch (Exception ignore) {}
        Map<String, Object> apIn = (Map<String, Object>) payload.getOrDefault("actionPerms", Collections.emptyMap());
        Map<String, Boolean> ap = new HashMap<>();
        ap.put("leave", Boolean.TRUE.equals(apIn.get("leave")));
        ap.put("renew", Boolean.TRUE.equals(apIn.get("renew")));
        try { target.setActionPermsJson(mapper.writeValueAsString(ap)); } catch (Exception ignore) {}
        return guardianRepo.save(target);
    }

    public List<GuardianPerm> generate(Long studentId, int count) {
        if (count < 1) count = 10;
        if (count > 100) count = 100;
        String[] names = {"王", "李", "张", "刘", "陈", "杨", "黄", "赵", "周", "吴", "郑", "孙"};
        String[] rels = {"父亲", "母亲", "亲属"};
        java.util.Random r = new java.util.Random();
        List<GuardianPerm> created = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            GuardianPerm g = new GuardianPerm();
            g.setStudentId(studentId);
            String name = names[r.nextInt(names.length)] + new String(new char[]{(char)('一' + r.nextInt(8))});
            g.setGuardianName(name);
            g.setGuardianPhone("13" + (r.nextInt(9) + 1) + String.format("%09d", Math.abs(r.nextInt())));
            g.setRelation(rels[r.nextInt(rels.length)]);
            Map<String, Boolean> vp = new HashMap<>();
            vp.put("schedule", r.nextBoolean());
            vp.put("grades", r.nextBoolean());
            vp.put("payments", r.nextBoolean());
            Map<String, Boolean> ap = new HashMap<>();
            ap.put("leave", r.nextBoolean());
            ap.put("renew", r.nextBoolean());
            try {
                g.setViewPermsJson(mapper.writeValueAsString(vp));
                g.setActionPermsJson(mapper.writeValueAsString(ap));
            } catch (Exception ignore) {}
            created.add(guardianRepo.save(g));
        }
        return created;
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.valueOf(String.valueOf(o)); } catch (Exception e) { return null; }
    }
}
