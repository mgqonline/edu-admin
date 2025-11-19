package com.eduadmin.dashboard.store;

import java.util.*;

/**
 * 简易看板统计内存存储：记录报名与续费操作，用于聚合首页数据。
 * 仅用于联调与演示，重启后数据将重置。
 */
public class DashboardStore {
    // 操作记录：{type: enroll/renew, amount: Double, classId: Long?, time: Date}
    public static final List<Map<String, Object>> operations = new ArrayList<>();

    public static synchronized void addOperation(String type, Double amount, Long classId, Date time) {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("type", type);
        op.put("amount", amount == null ? 0.0 : amount);
        op.put("classId", classId);
        op.put("time", time == null ? new Date() : time);
        operations.add(op);
    }

    public static List<Map<String, Object>> findBetween(Date start, Date end) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> op : operations) {
            Date t = (Date) op.get("time");
            boolean ok = (start == null || !t.before(start)) && (end == null || !t.after(end));
            if (ok) out.add(op);
        }
        return out;
    }

    public static Map<String, Object> aggregateToday() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Date start = c.getTime();
        Calendar c2 = (Calendar) c.clone();
        c2.add(Calendar.DAY_OF_MONTH, 1);
        Date end = c2.getTime();
        List<Map<String, Object>> list = findBetween(start, end);
        int enrollCount = 0;
        int renewCount = 0;
        double renewAmount = 0.0;
        for (Map<String, Object> op : list) {
            String type = String.valueOf(op.get("type"));
            if ("enroll".equals(type)) {
                enrollCount++;
            } else if ("renew".equals(type)) {
                renewCount++;
                Object am = op.get("amount");
                if (am instanceof Number) renewAmount += ((Number) am).doubleValue();
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("todayEnrollCount", enrollCount);
        out.put("todayRenewCount", renewCount);
        out.put("todayRenewAmount", renewAmount);
        return out;
    }

    public static Map<String, Object> trend7Days() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        List<String> dates = new ArrayList<>();
        List<Integer> enrollCounts = new ArrayList<>();
        List<Integer> renewCounts = new ArrayList<>();
        List<Double> renewAmounts = new ArrayList<>();
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("MM-dd");
        for (int i = 6; i >= 0; i--) {
            Calendar s = (Calendar) c.clone();
            s.add(Calendar.DAY_OF_MONTH, -i);
            Date start = s.getTime();
            Calendar e = (Calendar) s.clone();
            e.add(Calendar.DAY_OF_MONTH, 1);
            Date end = e.getTime();
            List<Map<String, Object>> list = findBetween(start, end);
            int ecount = 0;
            int rcount = 0;
            double rsum = 0.0;
            for (Map<String, Object> op : list) {
                String type = String.valueOf(op.get("type"));
                if ("enroll".equals(type)) ecount++;
                else if ("renew".equals(type)) {
                    rcount++;
                    Object am = op.get("amount");
                    if (am instanceof Number) rsum += ((Number) am).doubleValue();
                }
            }
            dates.add(fmt.format(start));
            enrollCounts.add(ecount);
            renewCounts.add(rcount);
            renewAmounts.add(rsum);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("dates", dates);
        out.put("enrollCounts", enrollCounts);
        out.put("renewCounts", renewCounts);
        out.put("renewAmounts", renewAmounts);
        return out;
    }

    public static Map<String, Object> enrollPieByCourseTypeLast30Days() {
        Calendar c = Calendar.getInstance();
        Date end = c.getTime();
        c.add(Calendar.DAY_OF_MONTH, -30);
        Date start = c.getTime();
        List<Map<String, Object>> list = findBetween(start, end);
        Map<String, Integer> counter = new LinkedHashMap<>();
        // 仅统计两类：一对一 / 班课
        counter.put("一对一", 0);
        counter.put("班课", 0);
        for (Map<String, Object> op : list) {
            if (!"enroll".equals(String.valueOf(op.get("type")))) continue;
            Object cidObj = op.get("classId");
            if (!(cidObj instanceof Number)) continue;
            Long classId = ((Number) cidObj).longValue();
            Map<String, Object> cls = com.eduadmin.course.store.InMemoryCourseStore.classes.get(classId);
            if (cls == null) continue;
            String courseId = String.valueOf(cls.get("courseId"));
            Map<String, Object> course = com.eduadmin.course.store.InMemoryCourseStore.courses.get(courseId);
            if (course == null) continue;
            String typeRaw = String.valueOf(course.getOrDefault("type", "班课"));
            String type = canonicalCourseType(typeRaw);
            counter.put(type, counter.get(type) + 1);
        }
        List<String> labels = new ArrayList<>(counter.keySet());
        List<Integer> values = new ArrayList<>();
        for (String k : labels) values.add(counter.get(k));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("labels", labels);
        out.put("values", values);
        out.put("metric", "enroll_by_course_type");
        return out;
    }

    private static String canonicalCourseType(String t) {
        if (t == null) return "班课";
        String v = String.valueOf(t);
        // 数字枚举兼容
        if ("2".equals(v)) return "一对一";
        if ("1".equals(v)) return "班课";
        // 文本枚举规范化
        if ("一对一".equals(v) || "一对一辅导".equals(v)) return "一对一";
        // 将“大班课/小班课”等归并为“班课”
        if ("班课".equals(v) || "大班课".equals(v) || "小班课".equals(v)) return "班课";
        // 其他未知类型默认归并为“班课”
        return "班课";
    }
}