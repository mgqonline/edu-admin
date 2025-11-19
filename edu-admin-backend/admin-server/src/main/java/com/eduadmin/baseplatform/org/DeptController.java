package com.eduadmin.baseplatform.org;

import com.eduadmin.baseplatform.org.entity.Dept;
import com.eduadmin.baseplatform.org.repo.DeptRepository;
import com.eduadmin.common.api.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/base/dept")
public class DeptController {

    @Autowired
    private DeptRepository deptRepository;

    @GetMapping("/tree")
    public ApiResponse<List<Map<String, Object>>> tree(@RequestParam(value = "campusId", required = false) Long campusId) {
        List<Dept> all = campusId == null ? deptRepository.findAll() : deptRepository.findByCampusIdOrderBySortOrderAsc(campusId);
        Map<Long, List<Map<String, Object>>> children = new HashMap<>();
        Map<Long, Map<String, Object>> nodeMap = new HashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Dept d : all) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", d.getId());
            node.put("title", d.getTitle());
            node.put("status", d.getStatus());
            node.put("campusId", d.getCampusId());
            node.put("sortOrder", d.getSortOrder());
            nodeMap.put(d.getId(), node);
        }
        for (Dept d : all) {
            Long pid = d.getParentId();
            if (pid == null || pid == 0) {
                roots.add(nodeMap.get(d.getId()));
            } else {
                children.computeIfAbsent(pid, k -> new ArrayList<>()).add(nodeMap.get(d.getId()));
            }
        }
        for (Map.Entry<Long, List<Map<String, Object>>> e : children.entrySet()) {
            Map<String, Object> parent = nodeMap.get(e.getKey());
            if (parent != null) parent.put("children", e.getValue());
        }
        return ApiResponse.success(roots);
    }

    @PostMapping("/save")
    public ApiResponse<Map<String, Object>> save(@RequestBody Map<String, Object> payload) {
        Dept d = new Dept();
        d.setTitle(String.valueOf(payload.getOrDefault("title", "")));
        Object pidObj = payload.get("parentId");
        d.setParentId(pidObj == null ? null : Long.valueOf(String.valueOf(pidObj)));
        Object campusIdObj = payload.get("campusId");
        if (campusIdObj == null) return ApiResponse.error(400, "缺少校区ID");
        d.setCampusId(Long.valueOf(String.valueOf(campusIdObj)));
        d.setStatus(String.valueOf(payload.getOrDefault("status", "enabled")));
        Object sortObj = payload.get("sortOrder");
        d.setSortOrder(sortObj == null ? 0 : Integer.valueOf(String.valueOf(sortObj)));
        Dept saved = deptRepository.save(d);
        Map<String, Object> out = new HashMap<>();
        out.put("id", saved.getId());
        out.put("title", saved.getTitle());
        out.put("parentId", saved.getParentId());
        out.put("campusId", saved.getCampusId());
        out.put("status", saved.getStatus());
        out.put("sortOrder", saved.getSortOrder());
        return ApiResponse.success(out);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Optional<Dept> opt = deptRepository.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "部门不存在");
        Dept d = opt.get();
        if (payload.containsKey("title")) d.setTitle(String.valueOf(payload.get("title")));
        if (payload.containsKey("parentId")) {
            Object pidObj = payload.get("parentId");
            d.setParentId(pidObj == null ? null : Long.valueOf(String.valueOf(pidObj)));
        }
        if (payload.containsKey("campusId")) {
            Object campusIdObj = payload.get("campusId");
            d.setCampusId(campusIdObj == null ? d.getCampusId() : Long.valueOf(String.valueOf(campusIdObj)));
        }
        if (payload.containsKey("status")) d.setStatus(String.valueOf(payload.get("status")));
        if (payload.containsKey("sortOrder")) {
            Object sortObj = payload.get("sortOrder");
            d.setSortOrder(sortObj == null ? d.getSortOrder() : Integer.valueOf(String.valueOf(sortObj)));
        }
        Dept saved = deptRepository.save(d);
        Map<String, Object> out = new HashMap<>();
        out.put("id", saved.getId());
        out.put("title", saved.getTitle());
        out.put("parentId", saved.getParentId());
        out.put("campusId", saved.getCampusId());
        out.put("status", saved.getStatus());
        out.put("sortOrder", saved.getSortOrder());
        return ApiResponse.success(out);
    }

    @PostMapping("/disable/{id}")
    public ApiResponse<Map<String, Object>> disable(@PathVariable Long id) {
        Optional<Dept> opt = deptRepository.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "部门不存在");
        Dept d = opt.get();
        d.setStatus("disabled");
        Dept saved = deptRepository.save(d);
        Map<String, Object> out = new HashMap<>();
        out.put("id", saved.getId());
        out.put("title", saved.getTitle());
        out.put("status", saved.getStatus());
        return ApiResponse.success(out);
    }
}