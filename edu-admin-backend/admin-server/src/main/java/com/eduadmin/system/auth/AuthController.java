package com.eduadmin.system.auth;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.baseplatform.role.entity.Role;
import com.eduadmin.baseplatform.role.entity.RolePerm;
import com.eduadmin.baseplatform.role.repo.RolePermRepository;
import com.eduadmin.baseplatform.role.repo.RoleRepository;
import com.eduadmin.baseplatform.staff.entity.Staff;
import com.eduadmin.baseplatform.staff.repo.StaffRepository;
import com.eduadmin.system.auth.entity.AdminUser;
import com.eduadmin.system.auth.entity.PasswordResetToken;
import com.eduadmin.system.auth.repo.AdminUserRepository;
import com.eduadmin.system.auth.repo.PasswordResetTokenRepository;
import com.eduadmin.system.security.AuthTokenStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AdminUserRepository userRepo;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RolePermRepository rolePermRepository;

    @Autowired
    private AuthTokenStore tokenStore;

    @Autowired
    private PasswordResetTokenRepository resetRepo;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> payload) {
        String username = payload.getOrDefault("username", "");
        String password = payload.getOrDefault("password", "");

        // 若不存在 admin 账号则创建（避免已有其他用户但缺少管理员的情况）
        if (!userRepo.findByUsername("admin").isPresent()) {
            AdminUser admin = new AdminUser();
            admin.setUsername("admin");
            admin.setPassword("admin123"); // 演示环境默认密码
            admin.setStatus("enabled");
            userRepo.save(admin);
        }

        Optional<AdminUser> opt = userRepo.findByUsername(username);
        if (!opt.isPresent()) return ApiResponse.error(401, "用户不存在或密码错误");
        AdminUser user = opt.get();
        if (!Objects.equals(user.getPassword(), password)) return ApiResponse.error(401, "用户不存在或密码错误");
        if (!"enabled".equalsIgnoreCase(user.getStatus())) return ApiResponse.error(403, "用户已禁用");

        String token = UUID.randomUUID().toString();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> userView = new HashMap<>();
        userView.put("id", user.getId());
        userView.put("username", user.getUsername());
        userView.put("status", user.getStatus());
        data.put("token", token);
        data.put("user", userView);
        data.put("perms", Collections.singletonList("*")); // 管理员拥有全部权限
        // 将令牌与权限集保存到后端，便于拦截器校验
        tokenStore.save(token, Collections.singletonList("*"), user.getId());
        return ApiResponse.success(data);
    }

    @PostMapping("/logout")
    public ApiResponse<Boolean> logout() {
        // 简化：前端清理令牌即可，服务端不维护会话状态
        return ApiResponse.success(true);
    }

    /**
     * 修改密码：支持管理员与教职工账号。
     * 请求体：{ username, oldPassword, newPassword }
     */
    @PostMapping("/password/change")
    public ApiResponse<Map<String, Object>> changePassword(@RequestBody Map<String, String> payload) {
        String username = String.valueOf(payload.getOrDefault("username", "")).trim();
        String oldPassword = String.valueOf(payload.getOrDefault("oldPassword", "")).trim();
        String newPassword = String.valueOf(payload.getOrDefault("newPassword", "")).trim();
        if (username.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) {
            return ApiResponse.error(400, "参数不完整");
        }

        // 先尝试管理员账号
        Optional<AdminUser> adminOpt = userRepo.findByUsername(username);
        if (adminOpt.isPresent()) {
            AdminUser user = adminOpt.get();
            if (!Objects.equals(user.getPassword(), oldPassword)) {
                return ApiResponse.error(401, "旧密码不正确");
            }
            if (!"enabled".equalsIgnoreCase(user.getStatus())) {
                return ApiResponse.error(403, "账号已禁用");
            }
            user.setPassword(newPassword);
            userRepo.save(user);
            Map<String, Object> out = new HashMap<>();
            out.put("type", "admin");
            out.put("username", user.getUsername());
            return ApiResponse.success(out);
        }

        // 再尝试教职工账号
        Optional<Staff> staffOpt = staffRepository.findByUsername(username);
        if (!staffOpt.isPresent()) {
            return ApiResponse.error(404, "账号不存在");
        }
        Staff staff = staffOpt.get();
        if (!Objects.equals(staff.getPassword(), oldPassword)) {
            return ApiResponse.error(401, "旧密码不正确");
        }
        if (!"enabled".equalsIgnoreCase(staff.getStatus())) {
            return ApiResponse.error(403, "账号已禁用");
        }
        staff.setPassword(newPassword);
        staffRepository.save(staff);
        Map<String, Object> out = new HashMap<>();
        out.put("type", "staff");
        out.put("username", staff.getUsername());
        return ApiResponse.success(out);
    }

    /**
     * 忘记密码：请求验证码
     * 请求体：{ username }
     * 返回：{ code }（演示环境直接返回验证码；生产应通过短信/邮件发送）
     */
    @PostMapping("/password/reset/request")
    public ApiResponse<Map<String, Object>> requestReset(@RequestBody Map<String, String> payload) {
        String username = String.valueOf(payload.getOrDefault("username", "")).trim();
        if (username.isEmpty()) return ApiResponse.error(400, "用户名不能为空");

        boolean exists = userRepo.findByUsername(username).isPresent() || staffRepository.findByUsername(username).isPresent();
        if (!exists) return ApiResponse.error(404, "账号不存在");

        String code = String.valueOf((int)(Math.random()*900000 + 100000));
        PasswordResetToken token = new PasswordResetToken();
        token.setUsername(username);
        token.setCode(code);
        token.setExpireAt(java.time.Instant.now().plus(java.time.Duration.ofMinutes(10)));
        token.setUsed(false);
        resetRepo.save(token);

        Map<String, Object> data = new HashMap<>();
        data.put("code", code); // 演示直接返回验证码
        data.put("expireMinutes", 10);
        return ApiResponse.success(data);
    }

    /**
     * 忘记密码：确认重置
     * 请求体：{ username, code, newPassword }
     */
    @PostMapping("/password/reset/confirm")
    public ApiResponse<Map<String, Object>> confirmReset(@RequestBody Map<String, String> payload) {
        String username = String.valueOf(payload.getOrDefault("username", "")).trim();
        String code = String.valueOf(payload.getOrDefault("code", "")).trim();
        String newPassword = String.valueOf(payload.getOrDefault("newPassword", "")).trim();
        if (username.isEmpty() || code.isEmpty() || newPassword.isEmpty()) {
            return ApiResponse.error(400, "参数不完整");
        }
        Optional<PasswordResetToken> tokenOpt = resetRepo.findTopByUsernameAndCodeAndUsedFalseAndExpireAtAfter(username, code, java.time.Instant.now());
        if (!tokenOpt.isPresent()) return ApiResponse.error(401, "验证码无效或已过期");
        PasswordResetToken token = tokenOpt.get();

        Optional<AdminUser> adminOpt = userRepo.findByUsername(username);
        if (adminOpt.isPresent()) {
            AdminUser user = adminOpt.get();
            if (!"enabled".equalsIgnoreCase(user.getStatus())) return ApiResponse.error(403, "账号已禁用");
            user.setPassword(newPassword);
            userRepo.save(user);
        } else {
            Optional<com.eduadmin.baseplatform.staff.entity.Staff> staffOpt = staffRepository.findByUsername(username);
            if (!staffOpt.isPresent()) return ApiResponse.error(404, "账号不存在");
            com.eduadmin.baseplatform.staff.entity.Staff staff = staffOpt.get();
            if (!"enabled".equalsIgnoreCase(staff.getStatus())) return ApiResponse.error(403, "账号已禁用");
            staff.setPassword(newPassword);
            staffRepository.save(staff);
        }

        token.setUsed(true);
        resetRepo.save(token);
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        return ApiResponse.success(data);
    }

    // 员工登录：根据员工绑定的角色返回权限集
    @PostMapping("/staff/login")
    public ApiResponse<Map<String, Object>> staffLogin(@RequestBody Map<String, String> payload) {
        String username = payload.getOrDefault("username", "");
        String password = payload.getOrDefault("password", "");

        Optional<Staff> opt = staffRepository.findByUsername(username);
        if (!opt.isPresent()) return ApiResponse.error(401, "员工不存在或密码错误");
        Staff staff = opt.get();
        if (!Objects.equals(staff.getPassword(), password)) return ApiResponse.error(401, "员工不存在或密码错误");
        if (!"enabled".equalsIgnoreCase(staff.getStatus())) return ApiResponse.error(403, "员工已禁用");

        String token = UUID.randomUUID().toString();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> userView = new HashMap<>();
        userView.put("id", staff.getId());
        userView.put("username", staff.getUsername());
        userView.put("name", staff.getName());
        userView.put("status", staff.getStatus());
        userView.put("roleId", staff.getRoleId());
        userView.put("roleIds", staff.getRoleIds());

        Set<String> perms = new HashSet<>();
        Map<String, Object> roleView = new HashMap<>();
        java.util.List<Long> roleIds = new java.util.ArrayList<>();
        if (staff.getRoleIds() != null && !staff.getRoleIds().trim().isEmpty()) {
            for (String part : staff.getRoleIds().split(",")) {
                try { roleIds.add(Long.valueOf(part.trim())); } catch (Exception ignore) {}
            }
        } else if (staff.getRoleId() != null && !staff.getRoleId().trim().isEmpty()) {
            try { roleIds.add(Long.valueOf(staff.getRoleId())); } catch (Exception ignore) {}
        }
        java.util.List<Map<String,Object>> rolesView = new java.util.ArrayList<>();
        for (Long rid : roleIds) {
            Optional<Role> roleOpt = roleRepository.findById(rid);
            if (roleOpt.isPresent()) {
                Role role = roleOpt.get();
                Map<String,Object> rv = new HashMap<>();
                rv.put("id", role.getId());
                rv.put("name", role.getName());
                rv.put("code", role.getCode());
                rv.put("status", role.getStatus());
                rolesView.add(rv);
                for (RolePerm rp : rolePermRepository.findByRoleId(rid)) { perms.add(rp.getValue()); }
            }
        }

        data.put("token", token);
        data.put("user", userView);
        data.put("role", roleView);
        data.put("roles", rolesView);
        data.put("perms", perms);
        // 保存令牌-权限映射供后端校验
        tokenStore.save(token, perms, staff.getId());
        return ApiResponse.success(data);
    }

    @PostMapping("/user/import/excel")
    public ApiResponse<Map<String, Object>> importUsersExcel(@RequestParam("file") MultipartFile file) {
        int ok = 0; int fail = 0;
        List<Map<String, Object>> errors = new ArrayList<>();
        try (org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(file.getInputStream())) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
            int rows = sheet.getPhysicalNumberOfRows();
            for (int i = 1; i < rows; i++) {
                org.apache.poi.ss.usermodel.Row r = sheet.getRow(i);
                if (r == null) continue;
                try {
                    String roleType = cellStr(r, 0); // admin/staff
                    String username = cellStr(r, 1);
                    String name = cellStr(r, 2);
                    String mobile = cellStr(r, 3);
                    String deptIdStr = cellStr(r, 4);
                    String campusIdStr = cellStr(r, 5);
                    String roleIdStr = cellStr(r, 6);
                    String status = cellStr(r, 7);

                    if (roleType == null || roleType.trim().isEmpty()) throw new IllegalArgumentException("缺少角色类型");
                    if ("admin".equalsIgnoreCase(roleType)) {
                        if (username == null || username.trim().isEmpty()) throw new IllegalArgumentException("管理员用户名不能为空");
                        if (userRepo.findByUsername(username).isPresent()) throw new IllegalArgumentException("管理员用户名已存在");
                        AdminUser u = new AdminUser();
                        u.setUsername(username.trim());
                        u.setPassword("123456");
                        u.setStatus((status == null || status.trim().isEmpty()) ? "enabled" : status.trim());
                        userRepo.save(u);
                        ok++;
                    } else if ("staff".equalsIgnoreCase(roleType)) {
                        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("员工姓名不能为空");
                        Staff s = new Staff();
                        s.setName(name.trim());
                        if (deptIdStr != null && !deptIdStr.trim().isEmpty()) try { s.setDeptId(Long.valueOf(deptIdStr.trim())); } catch (Exception ignore) {}
                        if (campusIdStr != null && !campusIdStr.trim().isEmpty()) try { s.setCampusId(Long.valueOf(campusIdStr.trim())); } catch (Exception ignore) {}
                        if (roleIdStr != null && !roleIdStr.trim().isEmpty()) s.setRoleId(roleIdStr.trim());
                        s.setStatus((status == null || status.trim().isEmpty()) ? "enabled" : status.trim());
                        if (username != null && !username.trim().isEmpty()) s.setUsername(username.trim());
                        if (mobile != null && !mobile.trim().isEmpty()) {
                            if (!mobile.matches("^1[3-9]\\d{9}$")) throw new IllegalArgumentException("手机号格式不合法");
                            s.setMobile(mobile.trim());
                            if (s.getUsername() == null || s.getUsername().trim().isEmpty()) s.setUsername(mobile.trim());
                        }
                        if (s.getPassword() == null || s.getPassword().trim().isEmpty()) s.setPassword("123456");
                        staffRepository.save(s);
                        ok++;
                    } else {
                        throw new IllegalArgumentException("不支持的角色类型: " + roleType);
                    }
                } catch (Exception e) {
                    fail++;
                    Map<String, Object> err = new HashMap<>();
                    err.put("row", i + 1);
                    err.put("error", e.getMessage());
                    errors.add(err);
                }
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "导入失败: " + e.getMessage());
        }
        Map<String, Object> out = new HashMap<>();
        out.put("success", ok);
        out.put("failed", fail);
        out.put("errors", errors);
        return ApiResponse.success(out);
    }

    @PostMapping("/user/init-accounts")
    public ApiResponse<Map<String, Object>> initAccounts(@RequestBody Map<String, Object> payload) {
        String target = String.valueOf(payload.getOrDefault("target", "staff")); // staff/admin
        int updated = 0;
        if ("admin".equalsIgnoreCase(target)) {
            for (AdminUser u : userRepo.findAll()) {
                if (u.getPassword() == null || u.getPassword().trim().isEmpty()) { u.setPassword("123456"); updated++; }
            }
            userRepo.flush();
        } else {
            for (Staff s : staffRepository.findAll()) {
                if (s.getUsername() == null || s.getUsername().trim().isEmpty()) {
                    String uname = (s.getMobile() != null && s.getMobile().matches("^1[3-9]\\d{9}$")) ? s.getMobile() : ("staff" + s.getId());
                    s.setUsername(uname);
                }
                if (s.getPassword() == null || s.getPassword().trim().isEmpty()) { s.setPassword("123456"); updated++; }
                staffRepository.save(s);
            }
        }
        Map<String, Object> out = new HashMap<>();
        out.put("updated", updated);
        return ApiResponse.success(out);
    }

    private String cellStr(org.apache.poi.ss.usermodel.Row r, int idx) {
        org.apache.poi.ss.usermodel.Cell c = r.getCell(idx);
        if (c == null) return null;
        switch (c.getCellType()) {
            case STRING: return c.getStringCellValue();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(c)) {
                    java.util.Date d = c.getDateCellValue();
                    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    return df.format(d);
                }
                return new java.math.BigDecimal(c.getNumericCellValue()).stripTrailingZeros().toPlainString();
            case BOOLEAN: return String.valueOf(c.getBooleanCellValue());
            case FORMULA:
                try { return c.getStringCellValue(); } catch (Exception ignore) { return String.valueOf(c.getNumericCellValue()); }
            default: return null;
        }
    }
}