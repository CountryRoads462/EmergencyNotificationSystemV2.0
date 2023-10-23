package com.andrew.ens.bot.adapter.in;

import com.andrew.ens.contact.adapter.out.persistence.Contact;
import com.andrew.ens.contact.application.port.in.GetAllContactsByTemplateIdUseCase;
import com.andrew.ens.status.domain.Status;
import com.andrew.ens.status.domain.UserCurrentStatus;
import com.andrew.ens.template.adapter.out.persistence.Template;
import com.andrew.ens.template.application.port.in.GetTemplateByIdUseCase;
import com.andrew.ens.template.application.port.in.GetTemplatesByOwnerIdUseCase;
import com.andrew.ens.user.application.port.in.CreateUserUseCase;
import com.andrew.ens.user.application.port.in.GetChosenTemplateIdUseCase;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.andrew.ens.bot.adapter.in.BotKeyboards.MAIN_MENU_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.buttons.BotButtons.BACK_BUTTON;
import static com.andrew.ens.status.domain.Status.MAIN_MENU_WAITING;

@Service
public class BotController extends TelegramLongPollingBot {

    public BotController(
            BotCreds creds,
            CreateUserUseCase createUserUseCase,
            GetChosenTemplateIdUseCase getChosenTemplateIdUseCase,
            GetTemplatesByOwnerIdUseCase getTemplatesByOwnerIdUseCase,
            GetTemplateByIdUseCase getTemplateByIdUseCase,
            GetAllContactsByTemplateIdUseCase getAllContactsByTemplateIdUseCase,
            Set<BotHandler> handlers
    ) {
        super(creds.token);
        this.botUsername = creds.username;
        this.createUserUseCase = createUserUseCase;
        this.getChosenTemplateIdUseCase = getChosenTemplateIdUseCase;
        this.getTemplatesByOwnerIdUseCase = getTemplatesByOwnerIdUseCase;
        this.getTemplateByIdUseCase = getTemplateByIdUseCase;
        this.getAllContactsByTemplateIdUseCase = getAllContactsByTemplateIdUseCase;

        for (BotHandler handler : handlers) {
            this.handlers.put(handler.getStatus(), handler);
        }
    }

    private final CreateUserUseCase createUserUseCase;
    private final GetChosenTemplateIdUseCase getChosenTemplateIdUseCase;

    private final GetTemplatesByOwnerIdUseCase getTemplatesByOwnerIdUseCase;
    private final GetTemplateByIdUseCase getTemplateByIdUseCase;

    private final GetAllContactsByTemplateIdUseCase getAllContactsByTemplateIdUseCase;

    @Getter
    private final ConcurrentHashMap<Long, UserCurrentStatus> userStates = new ConcurrentHashMap<>();

    private final String botUsername;

    private final Map<Status, BotHandler> handlers = new HashMap<>();

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    @SneakyThrows
    public synchronized void onUpdateReceived(Update update) {
        long userId;
        if (update.hasMessage()) {
            userId = update.getMessage().getFrom().getId();
        } else {
            userId = update.getCallbackQuery().getFrom().getId();
        }

        if (!userStates.containsKey(userId)) {
            createUserUseCase.createUser(update);

            setUserStatus(userId, MAIN_MENU_WAITING);
        }

        action(update, userId);
    }

    private synchronized void action(
            Update update,
            Long userId
    ) {
        String text;
        Message message;
        if (update.hasMessage()) {
            message = update.getMessage();
            text = message.getText();
        } else {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            text = callbackQuery.getData();
        }

        Status status = userStates.get(userId).getStatus();

        Optional.of(handlers.get(status)).ifPresent(it -> {
            try {
                it.execAction(update, this, userId, text);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public synchronized void sendText(
            long chatId,
            String text
    ) throws TelegramApiException {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();

        execute(sendMessage);
    }

    public synchronized void sendMainMenuKeyboard(
            long userId
    ) throws TelegramApiException {
        String text = "Selected template: none";

        Optional<Integer> templateIdOptional = getChosenTemplateIdUseCase
                .getChosenTemplateId(userId);

        userStates.get(userId).setReadyToSend(false);

        if (templateIdOptional.isPresent()) {
            int templateId = templateIdOptional.get();

            Optional<Template> templateOptional = getTemplateByIdUseCase
                    .getTemplateById(templateId);

            if (templateOptional.isPresent()) {
                Template template = templateOptional.get();

                int numberOfContacts = getAllContactsByTemplateIdUseCase
                        .getAllContactsByTemplateId(templateId).size();

                text = String.format("Selected template: %s (%d contacts)\n"
                                + "\"%s\"",
                        template.getName(),
                        numberOfContacts,
                        template.getText()
                );

                userStates.get(userId).setReadyToSend(true);
            }
        }

        sendKeyboard(userId, text, MAIN_MENU_KEYBOARD);
    }

    public synchronized void sendKeyboard(
            Long userId,
            String text,
            InlineKeyboardMarkup keyboard
    ) throws TelegramApiException {
        SendMessage sm = SendMessage.builder()
                .chatId(userId)
                .parseMode("HTML").text(text)
                .replyMarkup(keyboard)
                .build();

        execute(sm);
    }

    public synchronized void setUserStatus(long userId, Status status) {
        if (userStates.containsKey(userId)) {
            userStates.get(userId).setStatus(status);
        } else {
            userStates.put(userId, UserCurrentStatus.builder()
                    .status(status)
                    .build());
        }
    }

    public synchronized void setUserTemplateCreationId(long userId, int templateId) {
        if (userStates.containsKey(userId)) {
            userStates.get(userId).setTemplateCreationId(templateId);
        } else {
            userStates.put(userId, UserCurrentStatus.builder()
                    .templateCreationId(templateId)
                    .build());
        }
    }

    public synchronized void setUserContactCreationId(long userId, int contactId) {
        if (userStates.containsKey(userId)) {
            userStates.get(userId).setContactCreationId(contactId);
        } else {
            userStates.put(userId, UserCurrentStatus.builder()
                    .contactCreationId(contactId)
                    .build());
        }
    }

    public synchronized InlineKeyboardMarkup getAllTemplatesKeyboard(long userId) {
        List<Template> userTemplates = getTemplatesByOwnerIdUseCase
                .getTemplatesByOwnerId(userId);


        List<InlineKeyboardButton> buttonList = new ArrayList<>();

        for (Template template :
                userTemplates) {
            buttonList.add(InlineKeyboardButton.builder()
                    .text(template.getName())
                    .callbackData(String.valueOf(template.getId()))
                    .build());
        }

        return InlineKeyboardMarkup.builder()
                .keyboardRow(buttonList)
                .keyboardRow(List.of(BACK_BUTTON))
                .build();
    }

    public synchronized InlineKeyboardMarkup getAllContactsKeyboard(int templateId) {
        List<Contact> templateContacts = getAllContactsByTemplateIdUseCase
                .getAllContactsByTemplateId(templateId);

        List<InlineKeyboardButton> buttonList = new ArrayList<>();

        for (Contact contact :
                templateContacts) {
            buttonList.add(InlineKeyboardButton.builder()
                    .text(contact.getName())
                    .callbackData(String.valueOf(contact.getId()))
                    .build());
        }

        return InlineKeyboardMarkup.builder()
                .keyboardRow(buttonList)
                .keyboardRow(List.of(BACK_BUTTON))
                .build();
    }

}
