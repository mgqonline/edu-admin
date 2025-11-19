package com.eduadmin.notify.repo;

import com.eduadmin.notify.entity.UserMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMessageRepository extends JpaRepository<UserMessage, Long> {
    List<UserMessage> findByUsernameOrderByCreatedAtDesc(String username);
}