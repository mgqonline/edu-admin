package com.eduadmin.baseplatform.role;

import com.eduadmin.baseplatform.role.entity.Role;
import com.eduadmin.baseplatform.role.entity.RolePerm;
import com.eduadmin.baseplatform.role.repo.RolePermRepository;
import com.eduadmin.baseplatform.role.repo.RoleRepository;
import com.eduadmin.common.api.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/base/role")
public class RoleController {
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RolePermRepository rolePermRepository;

    @GetMapping("/list")
    public ApiResponse<List<Role>> list() {
        return ApiResponse.success(roleRepository.findAll());
    }

    @PostMapping("/save")
    public ApiResponse<Role> save(@RequestBody Map<String, Object> payload) {
        Role role = new Role();
        role.setName(String.valueOf(payload.getOrDefault("name", "")));
        role.setCode(String.valueOf(payload.getOrDefault("code", "")));
        role.setStatus(String.valueOf(payload.getOrDefault("status", "enabled")));
        if (role.getCode() == null || role.getCode().trim().isEmpty()) {
            return ApiResponse.error(400, "角色编码不能为空");
        }
        if (roleRepository.findByCode(role.getCode()).isPresent()) {
            return ApiResponse.error(400, "角色编码已存在");
        }
        Role saved = roleRepository.save(role);
        return ApiResponse.success(saved);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<Role> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Optional<Role> opt = roleRepository.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "角色不存在");
        Role r = opt.get();
        if (payload.containsKey("name")) r.setName(String.valueOf(payload.get("name")));
        if (payload.containsKey("code")) r.setCode(String.valueOf(payload.get("code")));
        if (payload.containsKey("status")) r.setStatus(String.valueOf(payload.get("status")));
        Role saved = roleRepository.save(r);
        return ApiResponse.success(saved);
    }

    @PostMapping("/disable/{id}")
    public ApiResponse<Role> disable(@PathVariable Long id) {
        Optional<Role> opt = roleRepository.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "角色不存在");
        Role r = opt.get();
        r.setStatus("disabled");
        Role saved = roleRepository.save(r);
        return ApiResponse.success(saved);
    }

    @GetMapping("/perms/{id}")
    public ApiResponse<Set<String>> getPerms(@PathVariable Long id) {
        if (!roleRepository.existsById(id)) return ApiResponse.error(404, "角色不存在");
        List<RolePerm> list = rolePermRepository.findByRoleId(id);
        Set<String> perms = new HashSet<>();
        for (RolePerm rp : list) perms.add(rp.getValue());
        return ApiResponse.success(perms);
    }

    @PostMapping("/perms/{id}")
    public ApiResponse<Set<String>> setPerms(@PathVariable Long id, @RequestBody Set<String> perms) {
        if (!roleRepository.existsById(id)) return ApiResponse.error(404, "角色不存在");
        rolePermRepository.deleteByRoleId(id);
        for (String v : perms) {
            RolePerm rp = new RolePerm();
            rp.setRoleId(id);
            rp.setValue(v);
            rolePermRepository.save(rp);
        }
        return getPerms(id);
    }
}