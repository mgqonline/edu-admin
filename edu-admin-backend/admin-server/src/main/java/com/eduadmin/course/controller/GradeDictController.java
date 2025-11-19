package com.eduadmin.course.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.baseplatform.campus.entity.Campus;
import com.eduadmin.baseplatform.campus.repo.CampusRepository;
import com.eduadmin.course.entity.GradeDict;
import com.eduadmin.course.repo.GradeDictRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dict/grade")
public class GradeDictController {

    @Autowired
    private GradeDictRepository gradeRepo;
    @Autowired
    private CampusRepository campusRepo;

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(@RequestParam(value = "status", required = false) String status,
                                                 @RequestParam(value = "campusId", required = false) Long campusId,
                                                 @RequestParam(value = "q", required = false) String keyword,
                                                 @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                                 @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        List<GradeDict> list;
        if (campusId != null) {
            if (status == null || status.isEmpty()) list = gradeRepo.findByCampusIdOrderBySortOrderAsc(campusId);
            else list = gradeRepo.findByCampusIdAndStatusOrderBySortOrderAsc(campusId, status);
            if (keyword != null && !keyword.trim().isEmpty()) {
                list = gradeRepo.findByCampusIdAndNameContainingOrderBySortOrderAsc(campusId, keyword.trim());
            }
        } else {
            if (status == null || status.isEmpty()) list = gradeRepo.findAll();
            else list = gradeRepo.findByStatusOrderBySortOrderAsc(status);
            if (keyword != null && !keyword.trim().isEmpty()) {
                list = gradeRepo.findByNameContainingOrderBySortOrderAsc(keyword.trim());
            }
        }
        // 排序：入库时间降序（用自增ID近似）
        list.sort((a, b) -> Long.compare(b.getId() == null ? 0L : b.getId(), a.getId() == null ? 0L : a.getId()));
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        int total = list.size();
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);
        List<GradeDict> pageList = list.subList(from, to);
        // 构建校区名称映射，提升前端显示友好度
        Set<Long> campusIds = new HashSet<>();
        for (GradeDict g : pageList) { if (g.getCampusId() != null) campusIds.add(g.getCampusId()); }
        Map<Long, String> campusNameMap = new HashMap<>();
        if (!campusIds.isEmpty()) {
            List<Campus> campuses = campusRepo.findAllById(campusIds);
            for (Campus c : campuses) campusNameMap.put(c.getId(), c.getName());
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (GradeDict g : pageList) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", g.getId());
            m.put("campusId", g.getCampusId());
            m.put("campusName", campusNameMap.get(g.getCampusId()));
            m.put("name", g.getName());
            m.put("status", g.getStatus());
            m.put("sortOrder", g.getSortOrder());
            items.add(m);
        }
        Map<String, Object> out = new HashMap<>();
        out.put("items", items);
        out.put("total", total);
        out.put("page", page);
        out.put("size", size);
        out.put("pages", (int) Math.ceil((double) total / size));
        return ApiResponse.success(out);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Optional<GradeDict> opt = gradeRepo.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "年级不存在");
        GradeDict entity = opt.get();
        if (payload.containsKey("campusId")) entity.setCampusId(Long.valueOf(String.valueOf(payload.get("campusId"))));
        if (payload.containsKey("name")) {
            String name = String.valueOf(payload.get("name")).trim();
            if (name.isEmpty()) return ApiResponse.error(400, "年级名称不能为空");
            // 重名校验：同校相同名称且不是当前ID
            Long campusId = entity.getCampusId();
            if (payload.containsKey("campusId")) campusId = Long.valueOf(String.valueOf(payload.get("campusId")));
            Optional<GradeDict> dup = gradeRepo.findByCampusIdAndName(campusId, name);
            if (dup.isPresent() && !dup.get().getId().equals(id)) {
                return ApiResponse.error(409, "同一学校已存在该年级名称");
            }
            entity.setName(name);
        }
        if (payload.containsKey("status")) entity.setStatus(String.valueOf(payload.get("status")));
        if (payload.containsKey("sortOrder")) entity.setSortOrder(Integer.valueOf(String.valueOf(payload.get("sortOrder"))));
        GradeDict saved = gradeRepo.save(entity);
        Map<String, Object> out = new HashMap<>();
        out.put("id", saved.getId());
        out.put("campusId", saved.getCampusId());
        out.put("campusName", saved.getCampusId() == null ? null : campusRepo.findById(saved.getCampusId()).map(Campus::getName).orElse(null));
        out.put("name", saved.getName());
        out.put("status", saved.getStatus());
        out.put("sortOrder", saved.getSortOrder());
        return ApiResponse.success(out);
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable Long id) {
        if (!gradeRepo.findById(id).isPresent()) return ApiResponse.error(404, "年级不存在");
        gradeRepo.deleteById(id);
        Map<String, Object> out = new HashMap<>();
        out.put("id", id);
        return ApiResponse.success(out);
    }

    @PostMapping("/save")
    public ApiResponse<Map<String, Object>> save(@RequestBody Map<String, Object> payload) {
        String name = String.valueOf(payload.getOrDefault("name", "")).trim();
        if (name.isEmpty()) return ApiResponse.error(400, "年级名称不能为空");
        Object campusIdObj = payload.get("campusId");
        if (campusIdObj == null) return ApiResponse.error(400, "请先选择学校（校区）");
        Long campusId = Long.valueOf(String.valueOf(campusIdObj));
        Optional<GradeDict> dup = gradeRepo.findByCampusIdAndName(campusId, name);
        if (dup.isPresent()) {
            return ApiResponse.error(409, "同一学校已存在该年级名称");
        }
        GradeDict entity = new GradeDict();
        entity.setCampusId(campusId);
        entity.setName(name);
        entity.setStatus(String.valueOf(payload.getOrDefault("status", "enabled")));
        Object sortOrder = payload.get("sortOrder");
        entity.setSortOrder(sortOrder == null ? 0 : Integer.parseInt(String.valueOf(sortOrder)));
        GradeDict saved = gradeRepo.save(entity);
        Map<String, Object> out = new HashMap<>();
        out.put("id", saved.getId());
        out.put("campusId", saved.getCampusId());
        out.put("campusName", saved.getCampusId() == null ? null : campusRepo.findById(saved.getCampusId()).map(Campus::getName).orElse(null));
        out.put("name", saved.getName());
        out.put("status", saved.getStatus());
        out.put("sortOrder", saved.getSortOrder());
        return ApiResponse.success(out);
    }
}