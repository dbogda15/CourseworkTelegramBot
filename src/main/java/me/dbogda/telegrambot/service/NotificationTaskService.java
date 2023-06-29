package me.dbogda.telegrambot.service;

import me.dbogda.telegrambot.entity.NotificationTask;
import me.dbogda.telegrambot.repository.NotificationTaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationTaskService {
    private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    public void save(NotificationTask notificationTask) {
        notificationTaskRepository.save(notificationTask);
    }

    public List<NotificationTask> getNotificationTasksByDateTime (LocalDateTime localDateTime) {
        return notificationTaskRepository.findAllByNotificationDateTime(localDateTime);
    }
}
