package com.eduadmin.course.service;

import com.eduadmin.course.entity.Textbook;
import com.eduadmin.course.entity.TextbookInventoryRecord;
import com.eduadmin.course.repo.TextbookInventoryRecordRepository;
import com.eduadmin.course.repo.TextbookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TextbookService {

    @Autowired
    private TextbookRepository textbookRepository;

    @Autowired
    private TextbookInventoryRecordRepository recordRepository;

    public Map<String, Object> save(Map<String, Object> tb) {
        String id = tb.get("textbookId") != null ? String.valueOf(tb.get("textbookId")) : null;
        if (id == null || id.equals("null") || id.trim().isEmpty()) {
            id = UUID.randomUUID().toString();
        }
        Textbook entity = textbookRepository.findByTextbookId(id).orElse(null);
        Date now = new Date();
        if (entity == null) {
            entity = new Textbook();
            entity.setTextbookId(id);
            entity.setCreatedAt(now);
            // 默认库存 0
            entity.setStock(0);
        }
        // 更新字段，仅当存在传入值时
        if (tb.get("name") != null) entity.setName(String.valueOf(tb.get("name")));
        if (tb.get("publisher") != null) entity.setPublisher(String.valueOf(tb.get("publisher")));
        Object priceObj = tb.get("unitPrice");
        if (priceObj != null) {
            if (priceObj instanceof Number) entity.setUnitPrice(((Number) priceObj).doubleValue());
            else {
                try { entity.setUnitPrice(Double.valueOf(String.valueOf(priceObj))); } catch (Exception ignore) {}
            }
        }
        Object stockObj = tb.get("stock");
        if (stockObj != null) {
            if (stockObj instanceof Number) entity.setStock(((Number) stockObj).intValue());
            else {
                try { entity.setStock(Integer.valueOf(String.valueOf(stockObj))); } catch (Exception ignore) {}
            }
        }
        // 课程ID列表
        List<String> courseIds = new ArrayList<>();
        Object cidsObj = tb.get("courseIds");
        if (cidsObj instanceof Collection) {
            for (Object o : ((Collection<?>) cidsObj)) courseIds.add(String.valueOf(o));
        }
        entity.setCourseIdsPlain(courseIds.isEmpty() ? null : String.join(",", courseIds));

        entity.setUpdatedAt(now);
        Textbook saved = textbookRepository.save(entity);
        return toMap(saved);
    }

    public List<Map<String, Object>> list(String keyword) {
        List<Textbook> found;
        if (keyword != null && !keyword.isEmpty()) {
            found = textbookRepository.findByNameContainingOrPublisherContaining(keyword, keyword);
        } else {
            found = textbookRepository.findAll();
        }
        return found.stream().map(this::toMap).collect(Collectors.toList());
    }

    public Map<String, Object> inventoryIn(String textbookId, int qty) {
        Textbook tb = textbookRepository.findByTextbookId(textbookId).orElse(null);
        if (tb == null) return new HashMap<>();
        int stock = tb.getStock() == null ? 0 : tb.getStock();
        stock += qty;
        tb.setStock(stock);
        tb.setUpdatedAt(new Date());
        textbookRepository.save(tb);

        TextbookInventoryRecord r = new TextbookInventoryRecord();
        r.setTextbookId(textbookId);
        r.setType("in");
        r.setQty(qty);
        r.setTime(new Date());
        recordRepository.save(r);
        return toMap(tb);
    }

    public Map<String, Object> inventoryOut(String textbookId, int qty) {
        Textbook tb = textbookRepository.findByTextbookId(textbookId).orElse(null);
        if (tb == null) return new HashMap<>();
        int stock = tb.getStock() == null ? 0 : tb.getStock();
        stock = Math.max(0, stock - qty);
        tb.setStock(stock);
        tb.setUpdatedAt(new Date());
        textbookRepository.save(tb);

        TextbookInventoryRecord r = new TextbookInventoryRecord();
        r.setTextbookId(textbookId);
        r.setType("out");
        r.setQty(qty);
        r.setTime(new Date());
        recordRepository.save(r);
        return toMap(tb);
    }

    public List<Map<String, Object>> inventoryRecords(String textbookId) {
        List<TextbookInventoryRecord> recs = recordRepository.findByTextbookIdOrderByTimeAsc(textbookId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (TextbookInventoryRecord r : recs) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", r.getType());
            m.put("qty", r.getQty());
            m.put("time", r.getTime());
            out.add(m);
        }
        return out;
    }

    public List<Map<String, Object>> lowStock(int threshold) {
        List<Textbook> found = textbookRepository.findByStockLessThanEqual(threshold);
        return found.stream().map(this::toMap).collect(Collectors.toList());
    }

    private Map<String, Object> toMap(Textbook t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("textbookId", t.getTextbookId());
        m.put("name", t.getName());
        m.put("publisher", t.getPublisher());
        m.put("unitPrice", t.getUnitPrice());
        m.put("stock", t.getStock() == null ? 0 : t.getStock());
        List<String> cids = new ArrayList<>();
        if (t.getCourseIdsPlain() != null && !t.getCourseIdsPlain().trim().isEmpty()) {
            cids = Arrays.stream(t.getCourseIdsPlain().split(","))
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.toList());
        }
        m.put("courseIds", cids);
        return m;
    }
}