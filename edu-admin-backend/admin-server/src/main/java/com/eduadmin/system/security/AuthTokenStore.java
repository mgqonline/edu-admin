package com.eduadmin.system.security;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthTokenStore {
    private final Map<String, Set<String>> tokenPerms = new ConcurrentHashMap<>();
    private final Map<String, Long> tokenUserIds = new ConcurrentHashMap<>();

    public void save(String token, Collection<String> perms, Long userId) {
        tokenPerms.put(token, new HashSet<>(perms == null ? Collections.emptyList() : perms));
        if (userId != null) tokenUserIds.put(token, userId);
    }

    public Set<String> permsOf(String token) {
        return token == null ? Collections.emptySet() : tokenPerms.getOrDefault(token, Collections.emptySet());
    }

    public Long userIdOf(String token) { return tokenUserIds.get(token); }
}