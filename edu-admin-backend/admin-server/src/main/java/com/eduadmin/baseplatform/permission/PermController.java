package com.eduadmin.baseplatform.permission;

import com.eduadmin.baseplatform.permission.entity.PermCatalog;
import com.eduadmin.baseplatform.permission.repo.PermCatalogRepository;
import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.baseplatform.role.entity.Role;
import com.eduadmin.baseplatform.role.repo.RoleRepository;
import com.eduadmin.baseplatform.role.repo.RolePermRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/base/perm")
public class PermController {

    @Autowired
    private PermCatalogRepository permRepo;

    @Autowired
    private RolePermRepository rolePermRepository;

    @Autowired
    private RoleRepository roleRepository;

    // 前端使用的权限目录聚合：按类型分组，返回 label/value 列表
    @GetMapping("/catalog")
    public ApiResponse<Map<String, List<Map<String, String>>>> catalog() {
        List<PermCatalog> all = permRepo.findAll();
        Map<String, List<PermCatalog>> grouped = all.stream()
                .collect(Collectors.groupingBy(pc -> pc.getType() == null ? "menu" : pc.getType()));

        Map<String, List<Map<String, String>>> data = new HashMap<>();
        data.put("menus", toOptions(grouped.getOrDefault("menu", Collections.emptyList())));
        data.put("buttons", toOptions(grouped.getOrDefault("button", Collections.emptyList())));
        data.put("dataScopes", toOptions(grouped.getOrDefault("data", Collections.emptyList())));
        return ApiResponse.success(data);
    }

    private List<Map<String, String>> toOptions(List<PermCatalog> list) {
        return list.stream()
                .sorted(Comparator.comparing(PermCatalog::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(pc -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("label", pc.getLabel());
                    m.put("value", pc.getValue());
                    return m;
                })
                .collect(Collectors.toList());
    }

    // CRUD：列表查询（可按type过滤）
    @GetMapping("/catalog/items")
    public ApiResponse<List<PermCatalog>> list(@RequestParam(value = "type", required = false) String type) {
        List<PermCatalog> items = (type == null || type.isEmpty())
                ? permRepo.findAll()
                : permRepo.findByTypeOrderBySortOrderAsc(type);
        return ApiResponse.success(items);
    }

    @PostMapping("/catalog/save")
    public ApiResponse<PermCatalog> save(@RequestBody PermCatalog payload) {
        // 简单唯一性检查：value不可重复
        Optional<PermCatalog> existing = permRepo.findByValue(payload.getValue());
        if (existing.isPresent()) {
            return ApiResponse.error(400, "权限值已存在");
        }
        if (payload.getSortOrder() == null) payload.setSortOrder(0);
        PermCatalog saved = permRepo.save(payload);
        return ApiResponse.success(saved);
    }

    @PutMapping("/catalog/update/{id}")
    public ApiResponse<PermCatalog> update(@PathVariable Long id, @RequestBody PermCatalog payload) {
        Optional<PermCatalog> opt = permRepo.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "权限项不存在");
        PermCatalog pc = opt.get();
        pc.setType(payload.getType());
        pc.setLabel(payload.getLabel());
        pc.setValue(payload.getValue());
        pc.setSortOrder(payload.getSortOrder() == null ? 0 : payload.getSortOrder());
        PermCatalog saved = permRepo.save(pc);
        return ApiResponse.success(saved);
    }

    @DeleteMapping("/catalog/delete/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        Optional<PermCatalog> opt = permRepo.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "权限项不存在");
        PermCatalog pc = opt.get();
        List<com.eduadmin.baseplatform.role.entity.RolePerm> refs = rolePermRepository.findByValue(pc.getValue());
        if (refs != null && !refs.isEmpty()) {
            return ApiResponse.error(409, "该权限已关联角色，无法删除");
        }
        permRepo.deleteById(id);
        return ApiResponse.success(true);
    }

    /**
     * 查看某权限值关联的角色列表
     */
    @GetMapping("/catalog/roles")
    public ApiResponse<List<Map<String, Object>>> rolesByPerm(@RequestParam("value") String value) {
        List<com.eduadmin.baseplatform.role.entity.RolePerm> refs = rolePermRepository.findByValue(value);
        List<Map<String, Object>> out = new ArrayList<>();
        for (com.eduadmin.baseplatform.role.entity.RolePerm rp : refs) {
            Long rid = rp.getRoleId();
            if (rid == null) continue;
            Optional<Role> rOpt = roleRepository.findById(rid);
            if (rOpt.isPresent()) {
                Role r = rOpt.get();
                Map<String, Object> m = new HashMap<>();
                m.put("id", r.getId());
                m.put("code", r.getCode());
                m.put("name", r.getName());
                m.put("status", r.getStatus());
                out.add(m);
            }
        }
        return ApiResponse.success(out);
    }
}