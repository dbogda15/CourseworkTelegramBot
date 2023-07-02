package me.dbogda.telegrambot.service;

import me.dbogda.telegrambot.entity.NotificationTask;
import me.dbogda.telegrambot.repository.NotificationTaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationTaskService {
    private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    public void save(NotificationTask notificationTask) {
        notificationTaskRepository.save(notificationTask);
    }

    public List<NotificationTask> getDailyNotificationTasks (LocalDateTime localDateTime) {
        LocalDateTime time = LocalDateTime.now();
        return notificationTaskRepository.findAll()
                .stream()
                .filter(n -> n.getNotificationDateTime().getDayOfMonth() == localDateTime.getDayOfMonth())
                .collect(Collectors.toList());
    }
    public List<NotificationTask> getNotificationTasksByDateTime (LocalDateTime localDateTime) {
        return notificationTaskRepository.findAllByNotificationDateTime(localDateTime);
    }

    public void delete(NotificationTask notificationTask){
        notificationTaskRepository.delete(notificationTask);
    }
}
