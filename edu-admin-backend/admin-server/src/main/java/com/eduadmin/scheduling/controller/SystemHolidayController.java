package com.eduadmin.scheduling.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.scheduling.entity.SystemHoliday;
import com.eduadmin.scheduling.repo.SystemHolidayRepository;
import com.eduadmin.system.security.RequiresPerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/base/holiday")
public class SystemHolidayController {

    @Autowired
    private SystemHolidayRepository holidayRepository;

    @RequiresPerm("base:holiday:view")
    @GetMapping("/list")
    public ApiResponse<List<SystemHoliday>> list(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "start", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
            @RequestParam(value = "end", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date end
    ) {
        List<SystemHoliday> items;
        if (year != null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, Calendar.JANUARY);
            c.set(Calendar.DAY_OF_MONTH, 1);
            Date s = c.getTime();
            c.set(Calendar.MONTH, Calendar.DECEMBER);
            c.set(Calendar.DAY_OF_MONTH, 31);
            Date e = c.getTime();
            items = holidayRepository.findByDateOnlyBetween(s, e);
        } else if (start != null && end != null) {
            items = holidayRepository.findByDateOnlyBetween(start, end);
        } else {
            items = holidayRepository.findAll();
        }
        // 按日期升序
        items.sort(Comparator.comparing(SystemHoliday::getDateOnly));
        return ApiResponse.success(items);
    }

    @RequiresPerm("base:holiday:add")
    @PostMapping("/create")
    public ApiResponse<SystemHoliday> create(@RequestBody Map<String, Object> payload) {
        Date date = toDate(payload.get("date"));
        String name = String.valueOf(payload.getOrDefault("name", "节假日"));
        if (date == null) return ApiResponse.error(400, "date 不能为空，格式 yyyy-MM-dd");
        if (holidayRepository.existsByDateOnly(date)) return ApiResponse.error(409, "该日期已存在节假日");
        SystemHoliday sh = new SystemHoliday();
        sh.setDateOnly(date);
        sh.setName(name);
        SystemHoliday saved = holidayRepository.save(sh);
        return ApiResponse.success(saved);
    }

    @RequiresPerm("base:holiday:edit")
    @PutMapping("/update/{id}")
    public ApiResponse<SystemHoliday> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Optional<SystemHoliday> opt = holidayRepository.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "节假日不存在");
        SystemHoliday sh = opt.get();
        String name = String.valueOf(payload.getOrDefault("name", sh.getName()));
        Date date = toDate(payload.getOrDefault("date", sh.getDateOnly()));
        if (date == null) return ApiResponse.error(400, "date 格式必须为 yyyy-MM-dd");
        // 如果更换日期且新日期已存在，提示冲突
        if (!sameDay(date, sh.getDateOnly()) && holidayRepository.existsByDateOnly(date)) {
            return ApiResponse.error(409, "新的日期已存在节假日");
        }
        sh.setName(name);
        sh.setDateOnly(date);
        SystemHoliday saved = holidayRepository.save(sh);
        return ApiResponse.success(saved);
    }

    @RequiresPerm("base:holiday:delete")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        if (!holidayRepository.existsById(id)) return ApiResponse.error(404, "节假日不存在");
        holidayRepository.deleteById(id);
        return ApiResponse.success(true);
    }

    private Date toDate(Object obj) {
        try {
            if (obj == null) return null;
            if (obj instanceof Date) return (Date) obj;
            String s = String.valueOf(obj);
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
            return df.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean sameDay(Date a, Date b) {
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return df.format(a).equals(df.format(b));
    }
}