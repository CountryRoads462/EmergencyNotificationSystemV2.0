package com.andrew.ens.bot;

import com.andrew.ens.contact.application.port.in.CreateIncompleteContactUseCase;
import com.andrew.ens.template.application.port.in.CreateIncompleteTemplateUseCase;
import com.andrew.ens.template.application.port.in.SetTemplateTextUseCase;
import com.andrew.ens.user.application.port.in.CreateUserUseCase;
import com.andrew.ens.user.application.port.in.GetInfoUserHasAnyTemplatesUseCase;
import com.andrew.ens.user_status.application.port.in.GetUserStatesUseCase;
import com.andrew.ens.user_status.application.port.in.SaveUserStatusUseCase;
import com.andrew.ens.user_status.domain.Status;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.ConcurrentHashMap;

import static com.andrew.ens.bot.BotMessages.*;
import static com.andrew.ens.user_status.domain.Status.*;

@Configuration
@RequiredArgsConstructor
public class BotController {

    private final CreateUserUseCase createUserUseCase;
    private final GetInfoUserHasAnyTemplatesUseCase userHasAnyTemplatesUseCase;

    private final SaveUserStatusUseCase saveUserStatusUseCase;
    private final GetUserStatesUseCase getUserStatesUseCase;

    private final CreateIncompleteTemplateUseCase createIncompleteTemplateUseCase;
    private final SetTemplateTextUseCase setTemplateTextUseCase;

    private final CreateIncompleteContactUseCase createIncompleteContactUseCase;

    private ConcurrentHashMap<Long, Status> userStates;

    @Bean
    TelegramLongPollingBot bot(
            @Value("${app.telegram.token}") String botToken,
            @Value("${app.telegram.username}") String botUsername
    ) throws TelegramApiException {
        userStates = getUserStatesUseCase.getUserStates();

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        TelegramLongPollingBot bot = new TelegramLongPollingBot(botToken) {

            @Override
            public String getBotUsername() {
                return botUsername;
            }

            @Override
            @SneakyThrows
            public synchronized void onUpdateReceived(Update update) {
                Long userId = update.getMessage().getFrom().getId();

                if (!userStates.containsKey(userId) &&
                        !userHasAnyTemplatesUseCase.userHasAnyTemplates(userId)) {
                    createUserUseCase.createUser(update);
                    sendText(ENTER_TEMPLATE_NAME, userId);
                    setUserStatus(userId, TEMPLATE_CREATE_NAME_WAITING);
                }

                action(update, userId);
            }

            private synchronized void action(Update update, Long userId) throws TelegramApiException {
                Message message = update.getMessage();
                String text = message.getText();

                switch (userStates.get(userId)) {
                    case TEMPLATE_CREATE_NAME_WAITING -> {
                        if (text.matches("\\W{1,16}") &&
                            message.isUserMessage()) {
                            createIncompleteTemplateUseCase
                                    .createIncompleteTemplate(text, userId);
                            setUserStatus(userId, TEMPLATE_CREATE_TEXT_WAITING);
                            sendText(ENTER_TEMPLATE_TEXT, userId);

                        } else {
                            sendText(ENTER_TEMPLATE_NAME, userId);
                        }
                    }
                    case TEMPLATE_CREATE_TEXT_WAITING -> {
                        if (text.matches("\\W{1,128}") &&
                                message.isUserMessage()) {
                            setTemplateTextUseCase
                                    .setTemplateText(text, userId);
                            setUserStatus(userId, TEMPLATE_CREATE_CONTACT_NAME_WAITING);
                            sendText(ENTER_TEMPLATE_CONTACT_NAME, userId);

                        } else {
                            sendText(ENTER_TEMPLATE_TEXT, userId);
                        }
                    }
                    case TEMPLATE_CREATE_CONTACT_NAME_WAITING -> {
                        if (text.matches("\\W{1,16}") &&
                                message.isUserMessage()) {
                            createIncompleteContactUseCase
                                    .createIncompleteContact(text);
                            setUserStatus(userId, TEMPLATE_CREATE_CONTACT_NAME_WAITING);
                            sendText(ENTER_TEMPLATE_CONTACT_NAME, userId);

                        } else {
                            sendText(ENTER_TEMPLATE_CONTACT_NAME, userId);
                        }
                    }
                    default -> {
                    }
                }
            }

            private synchronized void sendText(String text, Long chatId) throws TelegramApiException {
                SendMessage sendMessage = SendMessage.builder()
                        .chatId(chatId)
                        .text(text)
                        .build();

                execute(sendMessage);
            }

            private synchronized void setUserStatus(long userId, Status status) {
                userStates.put(userId, status);
                saveUserStatusUseCase.saveUserStatus(userId, status);
            }
        };

        telegramBotsApi.registerBot(bot);
        return bot;
    }
}
