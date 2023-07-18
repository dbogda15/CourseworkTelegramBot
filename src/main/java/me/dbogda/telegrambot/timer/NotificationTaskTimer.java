package me.dbogda.telegrambot.timer;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import me.dbogda.telegrambot.service.NotificationTaskService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Component
public class NotificationTaskTimer {
    private final NotificationTaskService notificationTaskService;
    private final TelegramBot telegramBot;
    public NotificationTaskTimer(NotificationTaskService notificationTaskService, TelegramBot telegramBot) {
        this.notificationTaskService = notificationTaskService;
        this.telegramBot = telegramBot;
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void task(){
        notificationTaskService.getNotificationTasksByDateTime(
                LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))
                .forEach(
                notificationTask -> {
                   telegramBot.execute(new SendMessage(notificationTask.getChatId(),"Hey! Your task at the moment: " + notificationTask.getMessage()));
                   notificationTaskService.delete(notificationTask);
                });

    }
}
