package com.eduadmin.student.store;

import java.util.*;

/**
 * 简单的内存试听记录存储，供统计使用。
 */
public class AuditionStore {
    public static class Record {
        public Long id;
        public Long studentId;
        public Long courseId;
        public Long teacherId;
        public Date time;
        public String status; // booked/finished
        public String feedback;
        public String result; // 适合报班/需再评估
    }

    private static final Map<Long, Record> records = new LinkedHashMap<>();
    private static long idSeq = 1L;

    public static synchronized Record save(Record r) {
        if (r.id == null) r.id = idSeq++;
        records.put(r.id, r);
        return r;
    }

    public static synchronized Optional<Record> get(Long id) {
        return Optional.ofNullable(records.get(id));
    }

    public static synchronized List<Record> list() {
        return new ArrayList<>(records.values());
    }
}