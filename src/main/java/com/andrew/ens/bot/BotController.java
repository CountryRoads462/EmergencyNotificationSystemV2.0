package com.andrew.ens.bot;

import com.andrew.ens.user.application.port.in.CreateUserUseCase;
import com.andrew.ens.user_states.application.port.in.GetUserStatesUseCase;
import com.andrew.ens.user_states.application.port.in.SaveUserStatusUseCase;
import com.andrew.ens.user_states.domain.Status;
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

import static com.andrew.ens.bot.BotMessages.ENTER_TEMPLATE_NAME;
import static com.andrew.ens.user_states.domain.Status.TEMPLATE_CREATE_NAME;
import static com.andrew.ens.user_states.domain.Status.TEMPLATE_CREATE_NAME_WAITING;

@Configuration
@RequiredArgsConstructor
public class BotController {

    private final CreateUserUseCase createUserUseCase;

    private final SaveUserStatusUseCase saveUserStatusUseCase;
    private final GetUserStatesUseCase getUserStatesUseCase;

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

                if (!userStates.containsKey(userId)) {
                    createUserUseCase.createUser(update);
                    saveUserStatus(userId, TEMPLATE_CREATE_NAME);
                }

                action(update, userId);
            }

            private synchronized void action(Update update, Long userId) throws TelegramApiException {
                switch (userStates.get(userId)) {
                    case TEMPLATE_CREATE_NAME -> {
                        sendText(ENTER_TEMPLATE_NAME, userId);
                        saveUserStatus(userId, TEMPLATE_CREATE_NAME_WAITING);
                    }
                    case TEMPLATE_CREATE_NAME_WAITING -> {
                        Message message = update.getMessage();
                        String text = message.getText();

                        if (text.matches("\\W{1,16}") &&
                            message.isUserMessage()) {


                        } else {
                            saveUserStatus(userId, TEMPLATE_CREATE_NAME);
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

            private synchronized void saveUserStatus(long userId, Status status) {
                userStates.put(userId, status);
                saveUserStatusUseCase.saveUserStatus(userId, status);
            }
        };

        telegramBotsApi.registerBot(bot);
        return bot;
    }
}
