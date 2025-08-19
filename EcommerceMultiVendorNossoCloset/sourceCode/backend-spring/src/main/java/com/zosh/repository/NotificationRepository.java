package com.zosh.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zosh.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
