package com.eduadmin.notify.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationSender {
    private static final Logger log = LoggerFactory.getLogger(NotificationSender.class);

    public void sendToTeacher(Long teacherId, String title, String content, Map<String, Object> meta) {
        log.info("[Notify][Teacher] teacherId={}, title={}, content={}, meta={}", teacherId, title, content, meta);
    }

    public void sendToStudent(Long studentId, String title, String content, Map<String, Object> meta) {
        log.info("[Notify][Student] studentId={}, title={}, content={}, meta={}", studentId, title, content, meta);
    }

    public void sendToGuardian(String guardianPhone, String title, String content, Map<String, Object> meta) {
        log.info("[Notify][Guardian] phone={}, title={}, content={}, meta={}", guardianPhone, title, content, meta);
    }
}