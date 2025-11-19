package com.eduadmin.baseplatform.campus;

import com.eduadmin.baseplatform.campus.entity.Campus;
import com.eduadmin.baseplatform.campus.repo.CampusRepository;
import com.eduadmin.common.api.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/base/campus")
public class CampusController {

    @Autowired
    private CampusRepository campusRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list() {
        List<Campus> all = campusRepository.findAll();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Campus c : all) {
            out.add(toMap(c));
        }
        return ApiResponse.success(out);
    }

    @PostMapping("/save")
    public ApiResponse<Map<String, Object>> save(@RequestBody Map<String, Object> payload) {
        Campus c = new Campus();
        c.setName(String.valueOf(payload.getOrDefault("name", "")));
        c.setAddress(String.valueOf(payload.getOrDefault("address", "")));
        c.setPhone(String.valueOf(payload.getOrDefault("phone", "")));
        c.setManager(String.valueOf(payload.getOrDefault("manager", "")));
        c.setRegion(String.valueOf(payload.getOrDefault("region", "")));
        Map<String, Object> config = (Map<String, Object>) payload.getOrDefault("config", new HashMap<>());
        config.putIfAbsent("signRule", "qrcode");
        config.putIfAbsent("conflictCheckLevel", "strict");
        try {
            c.setConfigJson(mapper.writeValueAsString(config));
        } catch (Exception e) {
            c.setConfigJson("{}");
        }
        c.setStatus(String.valueOf(payload.getOrDefault("status", "enabled")));
        Campus saved = campusRepository.save(c);
        return ApiResponse.success(toMap(saved));
    }

    @PutMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Optional<Campus> opt = campusRepository.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "校区不存在");
        Campus c = opt.get();
        if (payload.containsKey("name")) c.setName(String.valueOf(payload.get("name")));
        if (payload.containsKey("address")) c.setAddress(String.valueOf(payload.get("address")));
        if (payload.containsKey("phone")) c.setPhone(String.valueOf(payload.get("phone")));
        if (payload.containsKey("manager")) c.setManager(String.valueOf(payload.get("manager")));
        if (payload.containsKey("region")) c.setRegion(String.valueOf(payload.get("region")));
        if (payload.containsKey("config")) {
            try {
                c.setConfigJson(mapper.writeValueAsString((Map<String, Object>) payload.get("config")));
            } catch (Exception e) {
                // ignore
            }
        }
        if (payload.containsKey("status")) c.setStatus(String.valueOf(payload.get("status")));
        Campus saved = campusRepository.save(c);
        return ApiResponse.success(toMap(saved));
    }

    @PostMapping("/disable/{id}")
    public ApiResponse<Map<String, Object>> disable(@PathVariable Long id) {
        Optional<Campus> opt = campusRepository.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "校区不存在");
        Campus c = opt.get();
        c.setStatus("disabled");
        Campus saved = campusRepository.save(c);
        return ApiResponse.success(toMap(saved));
    }

    private Map<String, Object> toMap(Campus c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("name", c.getName());
        m.put("address", c.getAddress());
        m.put("phone", c.getPhone());
        m.put("manager", c.getManager());
        m.put("region", c.getRegion());
        m.put("status", c.getStatus());
        try {
            Map<String, Object> cfg = c.getConfigJson() == null ? new HashMap<>() : mapper.readValue(c.getConfigJson(), new TypeReference<Map<String, Object>>(){});
            m.put("config", cfg);
        } catch (Exception e) {
            m.put("config", new HashMap<>());
        }
        return m;
    }
}