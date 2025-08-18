package com.nossocloset.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nossocloset.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {



}
