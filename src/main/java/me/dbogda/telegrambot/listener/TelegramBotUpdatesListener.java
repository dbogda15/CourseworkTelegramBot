package me.dbogda.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import me.dbogda.telegrambot.entity.NotificationTask;
import me.dbogda.telegrambot.service.NotificationTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class TelegramBotUpdatesListener implements UpdatesListener {
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final Pattern pattern = Pattern.compile(
            "(\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{2})\\s+([A-zА-я\\d\\s.,;:-?!]+)"
    );
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final NotificationTaskService notificationTaskService;

    public TelegramBotUpdatesListener(NotificationTaskService notificationTaskService, TelegramBot telegramBot) {
        this.notificationTaskService = notificationTaskService;
        this.telegramBot = telegramBot;
    }
    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.stream()
                    .filter(update -> update.message() != null)
                    .forEach(update -> {
                        logger.info("Processing update: {}", update);
                        Message message = update.message();
                        Long chatId = message.chat().id();
                        String text = message.text();

                        if ("/start".equals(text)) {
                            sendMessage(chatId, """
                                    Hello!
                                    You can ask me to remind you of the task!
                                    For example: "01.02.2023 10:00 Send a message to my cousin"
                                    You can also get the list of daily tasks by command /daily
                                    """);
                        } else if ("/daily".equals(text)) {
                           sendNotificationsList(chatId);
                        } else if (text != null) {
                            createNotification(chatId, text);
                        }
                    });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        if (!sendResponse.isOk()) {
            logger.error("Error: {}", sendResponse.description());
        }
    }

    private LocalDateTime parse(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, dateTimeFormatter);
        } catch (DateTimeException e) {
            return null;
        }
    }

    public Map<Long, List<NotificationTask>> dailyTasksMap(LocalDateTime localDateTime) {
        return notificationTaskService.getDailyNotificationTasks(localDateTime)
                .stream()
                .collect(Collectors.groupingBy(NotificationTask::getChatId));
    }
    public List<String> getNotificationsTextList(List<NotificationTask> notificationTasks){
        List<String> text = new ArrayList<>();
        for (NotificationTask task : notificationTasks) {
            text.add(task.getNotificationDateTime().getHour() + ":"
                    + task.getNotificationDateTime().getMinute()
                    + " " + task.getMessage());
        }
        return text;
    }

    public void createNotification(Long chatId, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            LocalDateTime dateTime = parse(matcher.group(1));
            if (Objects.isNull(dateTime)) {
                sendMessage(chatId, "Incorrect date/time format");
            } else {
                String taskText = matcher.group(2);
                NotificationTask notificationTask = new NotificationTask();
                notificationTask.setChatId(chatId);
                notificationTask.setMessage(taskText);
                notificationTask.setNotificationDateTime(dateTime);
                notificationTaskService.save(notificationTask);
                sendMessage(chatId, "Good! I will remind you of this task!");
            }
        } else {
            sendMessage(chatId, "Please, try to check your message and send it again");
        }
    }

    public void sendNotificationsList(Long chatId) {
        Map<Long, List<NotificationTask>> dailyTasks = dailyTasksMap(LocalDateTime.now());
        if (dailyTasks.containsKey(chatId)) {
            sendMessage(chatId, "Your daily tasks: " + getNotificationsTextList(dailyTasks.get(chatId)).toString());
        } else {
            sendMessage(chatId, "You don't have any tasks for today");
        }
    }
}

