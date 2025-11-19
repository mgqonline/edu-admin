package com.eduadmin.baseplatform.classroom;

import com.eduadmin.baseplatform.classroom.entity.Classroom;
import com.eduadmin.baseplatform.classroom.repo.ClassroomRepository;
import com.eduadmin.baseplatform.classroom.ClassroomSeat;
import com.eduadmin.baseplatform.classroom.ClassroomSeatRepository;
import com.eduadmin.common.api.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/base/classroom")
public class ClassroomController {

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private ClassroomSeatRepository seatRepository;

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(@RequestParam(value = "campusId", required = false) Long campusId,
                                                 @RequestParam(value = "q", required = false) String keyword,
                                                 @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                                 @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        List<Classroom> found;
        if (campusId != null) {
            found = classroomRepository.findByCampusId(campusId);
        } else {
            found = classroomRepository.findAll();
        }
        String k = keyword == null ? null : keyword.trim();
        List<Classroom> filtered = new ArrayList<>();
        for (Classroom it : found) {
            boolean ok = true;
            if (k != null && !k.isEmpty()) {
                String name = it.getName() == null ? "" : it.getName();
                String roomCode = it.getRoomCode() == null ? "" : it.getRoomCode();
                ok = name.contains(k) || roomCode.contains(k);
            }
            if (ok) filtered.add(it);
        }
        filtered.sort((a, b) -> Long.compare((b.getId() == null ? 0L : b.getId()), (a.getId() == null ? 0L : a.getId())));
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        int total = filtered.size();
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);
        List<Classroom> pageList = filtered.subList(from, to);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Classroom it : pageList) items.add(toMap(it));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("total", total);
        out.put("page", page);
        out.put("size", size);
        out.put("pages", (int) Math.ceil((double) total / size));
        return ApiResponse.success(out);
    }

    @PostMapping("/save")
    public ApiResponse<Map<String, Object>> save(@RequestBody Map<String, Object> payload) {
        ValidationResult vr = validate(payload);
        if (!vr.ok) return ApiResponse.error(vr.code, vr.msg);
        Classroom c = new Classroom();
        c.setCampusId(toLong(payload.get("campusId")));
        c.setName(String.valueOf(payload.getOrDefault("name", "")));
        c.setRoomCode(String.valueOf(payload.getOrDefault("roomCode", "")));
        c.setCapacity(toInt(payload.get("capacity")));
        c.setUsableSeats(toInt(payload.get("usableSeats")));
        if (payload.containsKey("seatRows")) c.setSeatRows(toInt(payload.get("seatRows")));
        if (payload.containsKey("seatCols")) c.setSeatCols(toInt(payload.get("seatCols")));
        if (payload.containsKey("seatMap")) c.setSeatMap(String.valueOf(payload.get("seatMap")));
        c.setStatus(String.valueOf(payload.getOrDefault("status", "enabled")));
        c.setNote(String.valueOf(payload.getOrDefault("note", "")));
        Classroom saved = classroomRepository.save(c);
        syncSeats(saved.getId(), c.getSeatRows(), c.getSeatCols(), c.getSeatMap());
        return ApiResponse.success(toMap(saved));
    }

    @PutMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Optional<Classroom> opt = classroomRepository.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "教室不存在");
        Classroom c = opt.get();
        if (payload.containsKey("campusId")) c.setCampusId(toLong(payload.get("campusId")));
        if (payload.containsKey("name")) c.setName(String.valueOf(payload.get("name")));
        if (payload.containsKey("roomCode")) c.setRoomCode(String.valueOf(payload.get("roomCode")));
        if (payload.containsKey("capacity")) c.setCapacity(toInt(payload.get("capacity")));
        if (payload.containsKey("usableSeats")) c.setUsableSeats(toInt(payload.get("usableSeats")));
        if (payload.containsKey("seatRows")) c.setSeatRows(toInt(payload.get("seatRows")));
        if (payload.containsKey("seatCols")) c.setSeatCols(toInt(payload.get("seatCols")));
        if (payload.containsKey("seatMap")) c.setSeatMap(String.valueOf(payload.get("seatMap")));
        if (payload.containsKey("status")) c.setStatus(String.valueOf(payload.get("status")));
        if (payload.containsKey("note")) c.setNote(String.valueOf(payload.get("note")));
        // 校验：座位不能超过容量
        Integer cap = c.getCapacity() == null ? 0 : c.getCapacity();
        Integer seats = c.getUsableSeats() == null ? 0 : c.getUsableSeats();
        if (seats > cap) return ApiResponse.error(400, "可用座位数不能超过容纳人数");
        Classroom saved = classroomRepository.save(c);
        syncSeats(saved.getId(), c.getSeatRows(), c.getSeatCols(), c.getSeatMap());
        return ApiResponse.success(toMap(saved));
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable Long id) {
        Optional<Classroom> opt = classroomRepository.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "教室不存在");
        classroomRepository.deleteById(id);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", id);
        return ApiResponse.success(out);
    }

    private Map<String, Object> toMap(Classroom c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("campusId", c.getCampusId());
        m.put("name", c.getName());
        m.put("roomCode", c.getRoomCode());
        m.put("capacity", c.getCapacity());
        m.put("usableSeats", c.getUsableSeats());
        m.put("seatRows", c.getSeatRows());
        m.put("seatCols", c.getSeatCols());
        m.put("seatMap", c.getSeatMap());
        m.put("status", c.getStatus());
        m.put("note", c.getNote());
        return m;
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        try { return Long.valueOf(String.valueOf(v)); } catch (Exception e) { return null; }
    }
    private static Integer toInt(Object v) {
        if (v == null) return null;
        try { return Integer.valueOf(String.valueOf(v)); } catch (Exception e) { return null; }
    }

    private static class ValidationResult { boolean ok; int code; String msg; ValidationResult(boolean ok){ this.ok=ok; } ValidationResult(int code, String msg){ this.ok=false; this.code=code; this.msg=msg; } }
    private ValidationResult validate(Map<String, Object> payload){
        Long campusId = toLong(payload.get("campusId"));
        String name = String.valueOf(payload.getOrDefault("name", "")).trim();
        Integer cap = toInt(payload.get("capacity"));
        Integer seats = toInt(payload.get("usableSeats"));
        Integer seatRows = toInt(payload.get("seatRows"));
        Integer seatCols = toInt(payload.get("seatCols"));
        if (campusId == null) return new ValidationResult(400, "请选择校区");
        if (name.isEmpty()) return new ValidationResult(400, "请输入教室名称");
        int capV = cap == null ? 0 : cap;
        int seatV = seats == null ? 0 : seats;
        if (capV < 0 || seatV < 0) return new ValidationResult(400, "容量与座位不能为负数");
        if (seatV > capV) return new ValidationResult(400, "可用座位数不能超过容纳人数");
        if (seatRows != null && seatCols != null) {
            int total = seatRows * seatCols;
            if (capV != 0 && capV != total) {
                // 若提供行列则容量应与行列乘积一致（或容量未设置）
                return new ValidationResult(400, "容量应等于座位行列乘积");
            }
        }
        return new ValidationResult(true);
    }

    @GetMapping("/seats/{classroomId}")
    public ApiResponse<List<Map<String, Object>>> seats(@PathVariable("classroomId") Long classroomId) {
        List<ClassroomSeat> list = seatRepository.findByClassroomIdOrderByRowIndexAscColIndexAsc(classroomId);
        List<Map<String, Object>> data = new ArrayList<>();
        int seatNo = 0;
        for (ClassroomSeat s : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("classroomId", s.getClassroomId());
            m.put("rowIndex", s.getRowIndex());
            m.put("colIndex", s.getColIndex());
            m.put("label", s.getLabel());
            m.put("usable", s.getUsable());
            if (Boolean.TRUE.equals(s.getUsable())) {
                seatNo += 1;
                m.put("seatNo", seatNo);
            } else {
                m.put("seatNo", null);
            }
            data.add(m);
        }
        return ApiResponse.success(data);
    }

    private void syncSeats(Long classroomId, Integer seatRows, Integer seatCols, String seatMapJson) {
        if (classroomId == null) return;
        int rows = seatRows == null ? 0 : seatRows;
        int cols = seatCols == null ? 0 : seatCols;
        if (rows <= 0 || cols <= 0) {
            // 没有座位维度时不维护座位表
            return;
        }
        // 解析 seatMap
        int[][] grid = null;
        if (seatMapJson != null && !seatMapJson.isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                grid = mapper.readValue(seatMapJson, int[][].class);
            } catch (Exception ignored) {}
        }
        seatRepository.deleteByClassroomId(classroomId);
        List<ClassroomSeat> batch = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                ClassroomSeat s = new ClassroomSeat();
                s.setClassroomId(classroomId);
                s.setRowIndex(r);
                s.setColIndex(c);
                s.setLabel(rowLabel(r) + (c + 1));
                boolean usable = true;
                if (grid != null && r < grid.length && grid[r] != null && c < grid[r].length) {
                    usable = grid[r][c] != 0;
                }
                s.setUsable(usable);
                batch.add(s);
            }
        }
        if (!batch.isEmpty()) seatRepository.saveAll(batch);
    }

    private String rowLabel(int r) {
        if (r < 26) return String.valueOf((char)('A' + r));
        return String.valueOf(r + 1);
    }
}
