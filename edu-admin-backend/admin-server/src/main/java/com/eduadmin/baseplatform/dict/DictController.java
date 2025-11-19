package com.eduadmin.baseplatform.dict;

import com.eduadmin.baseplatform.dict.entity.DictItem;
import com.eduadmin.baseplatform.dict.repo.DictItemRepository;
import com.eduadmin.common.api.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/base/dict")
public class DictController {

    @Autowired
    private DictItemRepository dictRepo;

    @GetMapping("/all")
    public ApiResponse<Map<String, List<Map<String, Object>>>> all() {
        List<DictItem> all = dictRepo.findAll();
        Map<String, List<DictItem>> grouped = all.stream().collect(Collectors.groupingBy(DictItem::getType));
        Map<String, List<Map<String, Object>>> dicts = new HashMap<>();
        dicts.put("courseCategory", toList(grouped.getOrDefault("courseCategory", Collections.emptyList())));
        dicts.put("classType", toList(grouped.getOrDefault("classType", Collections.emptyList())));
        dicts.put("studentStatus", toList(grouped.getOrDefault("studentStatus", Collections.emptyList())));
        dicts.put("commonStatus", toList(grouped.getOrDefault("commonStatus", Collections.emptyList())));

        // 如果库为空，返回默认数据以便前端可用
        if (all.isEmpty()) {
            dicts = new HashMap<>();
            dicts.put("courseCategory", Arrays.asList(entry("K12", "K12"), entry("skill", "职业技能"), entry("language", "语言培训"), entry("interest", "兴趣爱好")));
            dicts.put("classType", Arrays.asList(entry("small", "小班(≤15)"), entry("medium", "中班(16-30)"), entry("large", "大班(≥31)"), entry("1v1", "一对一")));
            dicts.put("studentStatus", Arrays.asList(entry("potential", "潜在"), entry("trial", "试听"), entry("studying", "在读"), entry("suspend", "休学"), entry("quit", "退学"), entry("graduate", "毕业")));
            dicts.put("commonStatus", Arrays.asList(entry("enabled", "启用"), entry("disabled", "禁用"), entry("done", "已完成"), entry("undone", "未完成"), entry("normal", "正常"), entry("abnormal", "异常")));
        }
        return ApiResponse.success(dicts);
    }

    @GetMapping("/items")
    public ApiResponse<List<DictItem>> items(@RequestParam("type") String type) {
        return ApiResponse.success(dictRepo.findByTypeOrderBySortOrderAsc(type));
    }

    @PostMapping("/save")
    public ApiResponse<DictItem> save(@RequestBody DictItem item) {
        if (item.getType() == null || item.getCode() == null || item.getName() == null) {
            return ApiResponse.error(400, "type/code/name 不能为空");
        }
        DictItem saved = dictRepo.save(item);
        return ApiResponse.success(saved);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<DictItem> update(@PathVariable Long id, @RequestBody DictItem payload) {
        Optional<DictItem> opt = dictRepo.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "字典项不存在");
        DictItem d = opt.get();
        if (payload.getType() != null) d.setType(payload.getType());
        if (payload.getCode() != null) d.setCode(payload.getCode());
        if (payload.getName() != null) d.setName(payload.getName());
        if (payload.getSortOrder() != null) d.setSortOrder(payload.getSortOrder());
        if (payload.getStatus() != null) d.setStatus(payload.getStatus());
        DictItem saved = dictRepo.save(d);
        return ApiResponse.success(saved);
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        if (!dictRepo.existsById(id)) return ApiResponse.error(404, "字典项不存在");
        dictRepo.deleteById(id);
        return ApiResponse.success(true);
    }

    private Map<String, Object> entry(String code, String name) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", code);
        m.put("name", name);
        return m;
    }

    private List<Map<String, Object>> toList(List<DictItem> items) {
        return items.stream().map(i -> {
            Map<String, Object> m = new HashMap<>();
            m.put("code", i.getCode());
            m.put("name", i.getName());
            m.put("status", i.getStatus());
            m.put("id", i.getId());
            m.put("sortOrder", i.getSortOrder());
            return m;
        }).collect(Collectors.toList());
    }
}