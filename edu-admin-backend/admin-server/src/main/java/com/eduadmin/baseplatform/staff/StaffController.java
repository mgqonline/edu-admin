package com.eduadmin.baseplatform.staff;

import com.eduadmin.baseplatform.staff.entity.Staff;
import com.eduadmin.baseplatform.staff.repo.StaffRepository;
import com.eduadmin.common.api.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/base/staff")
public class StaffController {

    @Autowired
    private StaffRepository staffRepo;

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(@RequestParam(value = "campusId", required = false) Long campusId,
                                                 @RequestParam(value = "deptId", required = false) Long deptId,
                                                 @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                                 @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        List<Staff> found;
        if (campusId != null && deptId != null) found = staffRepo.findByCampusIdAndDeptId(campusId, deptId);
        else if (campusId != null) found = staffRepo.findByCampusId(campusId);
        else if (deptId != null) found = staffRepo.findByDeptId(deptId);
        else found = staffRepo.findAll();

        found.sort((a, b) -> Long.compare(b.getId() == null ? 0L : b.getId(), a.getId() == null ? 0L : a.getId()));
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        int total = found.size();
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);
        List<Staff> pageList = found.subList(from, to);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Staff s : pageList) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("name", s.getName());
            m.put("deptId", s.getDeptId());
            m.put("campusId", s.getCampusId());
            m.put("status", s.getStatus());
            m.put("roleId", s.getRoleId());
            m.put("roleIds", s.getRoleIds());
            m.put("username", s.getUsername());
            m.put("mobile", s.getMobile());
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

    @PostMapping("/save")
    public ApiResponse<Map<String, Object>> save(@RequestBody Map<String, Object> payload) {
        Staff s = new Staff();
        s.setName(String.valueOf(payload.getOrDefault("name", "")));
        Object deptObj = payload.get("deptId");
        s.setDeptId(deptObj == null ? null : Long.valueOf(String.valueOf(deptObj)));
        Object campusObj = payload.get("campusId");
        s.setCampusId(campusObj == null ? null : Long.valueOf(String.valueOf(campusObj)));
        s.setStatus(String.valueOf(payload.getOrDefault("status", "enabled")));
        s.setRoleId(payload.get("roleId") == null ? null : String.valueOf(payload.get("roleId")));
        Object roleIdsObj = payload.get("roleIds");
        if (roleIdsObj instanceof java.util.Collection) {
            java.util.Collection<?> coll = (java.util.Collection<?>) roleIdsObj;
            String joined = String.join(",", coll.stream().map(x -> String.valueOf(x)).toArray(String[]::new));
            s.setRoleIds(joined);
        } else if (roleIdsObj != null) {
            s.setRoleIds(String.valueOf(roleIdsObj));
        } else if (s.getRoleId() != null) {
            s.setRoleIds(s.getRoleId());
        }
        String username = String.valueOf(payload.getOrDefault("username", "")).trim();
        if (username.isEmpty()) username = "staff" + System.currentTimeMillis();
        s.setUsername(username);
        String mobile = String.valueOf(payload.getOrDefault("mobile", "")).trim();
        if (!mobile.isEmpty()) s.setMobile(mobile);
        String password = String.valueOf(payload.getOrDefault("password", "")).trim();
        if (password.isEmpty()) password = "123456";
        s.setPassword(password);
        Staff saved = staffRepo.save(s);
        Map<String, Object> out = new HashMap<>();
        out.put("id", saved.getId());
        out.put("name", saved.getName());
        out.put("deptId", saved.getDeptId());
        out.put("campusId", saved.getCampusId());
        out.put("status", saved.getStatus());
        out.put("roleId", saved.getRoleId());
        out.put("roleIds", saved.getRoleIds());
        out.put("username", saved.getUsername());
        out.put("mobile", saved.getMobile());
        return ApiResponse.success(out);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Optional<Staff> opt = staffRepo.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "教职工不存在");
        Staff s = opt.get();
        if (payload.containsKey("name")) s.setName(String.valueOf(payload.get("name")));
        if (payload.containsKey("deptId")) {
            Object deptObj = payload.get("deptId");
            s.setDeptId(deptObj == null ? null : Long.valueOf(String.valueOf(deptObj)));
        }
        if (payload.containsKey("campusId")) {
            Object campusObj = payload.get("campusId");
            s.setCampusId(campusObj == null ? null : Long.valueOf(String.valueOf(campusObj)));
        }
        if (payload.containsKey("status")) s.setStatus(String.valueOf(payload.get("status")));
        if (payload.containsKey("roleId")) s.setRoleId(String.valueOf(payload.get("roleId")));
        if (payload.containsKey("roleIds")) {
            Object roleIdsObj = payload.get("roleIds");
            if (roleIdsObj instanceof java.util.Collection) {
                java.util.Collection<?> coll = (java.util.Collection<?>) roleIdsObj;
                String joined = String.join(",", coll.stream().map(x -> String.valueOf(x)).toArray(String[]::new));
                s.setRoleIds(joined);
            } else if (roleIdsObj != null) {
                s.setRoleIds(String.valueOf(roleIdsObj));
            }
        }
        if (payload.containsKey("username")) {
            String uname = String.valueOf(payload.get("username")).trim();
            if (!uname.isEmpty()) s.setUsername(uname);
        }
        if (payload.containsKey("mobile")) {
            String mob = String.valueOf(payload.get("mobile")).trim();
            s.setMobile(mob);
        }
        if (payload.containsKey("password")) {
            String pwd = String.valueOf(payload.get("password")).trim();
            if (!pwd.isEmpty()) s.setPassword(pwd);
        }
        Staff saved = staffRepo.save(s);
        Map<String, Object> out = new HashMap<>();
        out.put("id", saved.getId());
        out.put("name", saved.getName());
        out.put("deptId", saved.getDeptId());
        out.put("campusId", saved.getCampusId());
        out.put("status", saved.getStatus());
        out.put("roleId", saved.getRoleId());
        out.put("roleIds", saved.getRoleIds());
        out.put("username", saved.getUsername());
        out.put("mobile", saved.getMobile());
        return ApiResponse.success(out);
    }

    @PostMapping("/status/{id}")
    public ApiResponse<Map<String, Object>> changeStatus(@PathVariable Long id, @RequestParam("status") String status) {
        Optional<Staff> opt = staffRepo.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "教职工不存在");
        Staff s = opt.get();
        s.setStatus(status);
        Staff saved = staffRepo.save(s);
        Map<String, Object> out = new HashMap<>();
        out.put("id", saved.getId());
        out.put("status", saved.getStatus());
        return ApiResponse.success(out);
    }

    @PostMapping("/reset-password/{id}")
    public ApiResponse<Map<String, Object>> resetPassword(@PathVariable Long id) {
        Optional<Staff> opt = staffRepo.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "教职工不存在");
        Staff s = opt.get();
        s.setPassword("123456");
        Staff saved = staffRepo.save(s);
        Map<String, Object> out = new HashMap<>();
        out.put("id", saved.getId());
        out.put("password", "123456");
        return ApiResponse.success(out);
    }
}