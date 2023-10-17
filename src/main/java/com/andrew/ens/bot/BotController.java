package com.andrew.ens.bot;

import com.andrew.ens.contact.adapter.out.persistence.Contact;
import com.andrew.ens.contact.application.port.in.*;
import com.andrew.ens.template.adapter.out.persistence.Template;
import com.andrew.ens.template.application.port.in.CreateIncompleteTemplateUseCase;
import com.andrew.ens.template.application.port.in.DeleteTemplateByIdUseCase;
import com.andrew.ens.template.application.port.in.GetTemplateTextUseCase;
import com.andrew.ens.template.application.port.in.GetTemplatesByOwnerIdUseCase;
import com.andrew.ens.template.application.port.in.SetTemplateTextUseCase;
import com.andrew.ens.user.application.port.in.CreateUserUseCase;
import com.andrew.ens.user.application.port.in.GetInfoUserHasAnyTemplatesUseCase;
import com.andrew.ens.user.application.port.in.GetInfoUserHasChosenTemplateUseCase;
import com.andrew.ens.user.application.port.in.SetChosenTemplateUseCase;
import com.andrew.ens.Status;
import com.andrew.ens.UserCurrentStatus;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.andrew.ens.Status.*;
import static com.andrew.ens.bot.BotKeyboards.EDIT_TEMPLATES_KEYBOARD;
import static com.andrew.ens.bot.BotKeyboards.MENU_KEYBOARD;
import static com.andrew.ens.bot.BotKeyboards.SETTINGS_KEYBOARD;
import static com.andrew.ens.bot.BotMessages.CREATE_TEMPLATE_OR_ADD_CONTACT_MESSAGE;
import static com.andrew.ens.bot.BotMessages.SELECT_ONE_OR_MORE_OPTIONS_MESSAGE;
import static com.andrew.ens.bot.BotMessages.CREATE_CONTACT_EMAIL_MESSAGE;
import static com.andrew.ens.bot.BotMessages.CREATE_CONTACT_NAME_MESSAGE;
import static com.andrew.ens.bot.BotMessages.CREATE_CONTACT_PHONE_NUMBER_MESSAGE;
import static com.andrew.ens.bot.BotMessages.TEMPLATE_CREATE_NAME_MESSAGE;
import static com.andrew.ens.bot.BotMessages.TEMPLATE_CREATE_TEXT_MESSAGE;
import static com.andrew.ens.bot.buttons.CallBackData.ADD_CONTACT_CALL_BACK;
import static com.andrew.ens.bot.buttons.CallBackData.BACK_TO_MENU_CALL_BACK;
import static com.andrew.ens.bot.buttons.CallBackData.CHANGE_THE_TEMPLATE_NAME_CALL_BACK;
import static com.andrew.ens.bot.buttons.CallBackData.CHOOSE_TEMPLATE_CALL_BACK;
import static com.andrew.ens.bot.buttons.CallBackData.CREATE_TEMPLATE_CALL_BACK;
import static com.andrew.ens.bot.buttons.CallBackData.DELETE_ALL_CONTACTS_CALL_BACK;
import static com.andrew.ens.bot.buttons.CallBackData.DELETE_CONTACT_CALL_BACK;
import static com.andrew.ens.bot.buttons.CallBackData.DELETE_TEMPLATE_CALL_BACK;
import static com.andrew.ens.bot.buttons.CallBackData.EDIT_TEMPLATE_CALL_BACK;
import static com.andrew.ens.bot.buttons.CallBackData.SEND_EMERGENCY_MESSAGE_CALL_BACK;
import static com.andrew.ens.bot.buttons.CallBackData.SETTINGS_CALL_BACK;

@Configuration
@RequiredArgsConstructor
public class BotController {

    private final CreateUserUseCase createUserUseCase;
    private final GetInfoUserHasChosenTemplateUseCase getInfoUserHasChosenTemplateUseCase;
    private final SetChosenTemplateUseCase setChosenTemplateUseCase;

    private final CreateIncompleteTemplateUseCase createIncompleteTemplateUseCase;
    private final SetTemplateTextUseCase setTemplateTextUseCase;
    private final GetTemplateTextUseCase getTemplateTextUseCase;
    private final GetTemplatesByOwnerIdUseCase getTemplatesByOwnerIdUseCase;
    private final DeleteTemplateByIdUseCase deleteTemplateByIdUseCase;

    private final CreateIncompleteContactUseCase createIncompleteContactUseCase;
    private final SetContactEmailUseCase setContactEmailUseCase;
    private final SetContactPhoneNumberUseCase setContactPhoneNumberUseCase;
    private final DeleteAllContactsUseCase deleteAllContactsUseCase;
    private final GetAllContactsByTemplateIdUseCase getAllContactsByTemplateIdUseCase;
    private final DeleteContactByIdUseCase deleteContactByIdUseCase;

    private ConcurrentHashMap<Long, UserCurrentStatus> userStates;

    @Bean
    TelegramLongPollingBot bot(
            @Value("${app.telegram.token}") String botToken,
            @Value("${app.telegram.username}") String botUsername
    ) throws TelegramApiException {
        userStates = new ConcurrentHashMap<>();

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        TelegramLongPollingBot bot = new TelegramLongPollingBot(botToken) {

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
                    sendKeyboard(userId, "Ef", MENU_KEYBOARD);
                    setUserStatus(userId, MAIN_MENU_WAITING);
                }

                System.out.println(userStates.get(userId));

                DeleteMessage deleteMessage = DeleteMessage.builder()
                        .messageId(userStates.get(userId).getMessageIdToDelete())
                        .chatId(userId)
                        .build();

                execute(deleteMessage);

                action(update, userId);
            }

            private synchronized void action(Update update, Long userId) throws TelegramApiException {
                String text;
                Message message;
                if (update.hasMessage()) {
                    message = update.getMessage();
                    text = message.getText();
                } else {
                    CallbackQuery callbackQuery = update.getCallbackQuery();
                    message = callbackQuery.getMessage();
                    text = callbackQuery.getData();
                }

                switch (userStates.get(userId).getStatus()) {
                    case MAIN_MENU_WAITING -> {
                        switch (text) {
                            case SEND_EMERGENCY_MESSAGE_CALL_BACK -> {
                                sendText(userId, "Send emergency message");
                            }
                            case SETTINGS_CALL_BACK -> {
                                setUserStatus(userId, SETTINGS_WAITING);
                                sendKeyboard(userId, "Ed", SETTINGS_KEYBOARD);
                            }
                            default -> {
                                sendText(userId, "Chose, send or settings");
                            }
                        }
                    }
                    case TEMPLATE_CREATE_NAME_WAITING -> {
                        if (text.matches("\\w{1,16}")) {
                            int templateId = createIncompleteTemplateUseCase
                                    .createIncompleteTemplate(text, userId);

                            setUserTemplateCreationId(userId, templateId);

                            setUserStatus(userId, TEMPLATE_CREATE_TEXT_WAITING);
                            sendText(userId, TEMPLATE_CREATE_TEXT_MESSAGE);

                        } else {
                            sendText(userId, TEMPLATE_CREATE_NAME_MESSAGE);
                        }
                    }
                    case TEMPLATE_CREATE_TEXT_WAITING -> {
                        if (text.matches("\\W{1,128}") &&
                                message.isUserMessage()) {
                            int templateId = userStates.get(userId).getTemplateCreationId();

                            setTemplateTextUseCase.setTemplateText(templateId, text);

                            setUserStatus(userId, TEMPLATE_CREATE_CONTACT_NAME_WAITING);
                            sendText(userId, CREATE_CONTACT_NAME_MESSAGE);

                        } else {
                            sendText(userId, TEMPLATE_CREATE_TEXT_MESSAGE);
                        }
                    }
                    case TEMPLATE_CREATE_CONTACT_NAME_WAITING -> {
                        if (text.matches("\\W{1,16}") &&
                                message.isUserMessage()) {
                            int contactId = createIncompleteContactUseCase
                                    .createIncompleteContact(text);

                            setUserContactCreationId(userId, contactId);

                            setUserStatus(userId, TEMPLATE_CREATE_CONTACT_EMAIL_WAITING);
                            sendText(userId, CREATE_CONTACT_EMAIL_MESSAGE);

                        } else {
                            sendText(userId, CREATE_CONTACT_NAME_MESSAGE);
                        }
                    }
                    case TEMPLATE_CREATE_CONTACT_EMAIL_WAITING -> {
                        if (text.matches("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\\\.[A-Z]{2,6}$") &&
                                message.isUserMessage()) {
                            int contactId = userStates.get(userId).getContactCreationId();

                            setContactEmailUseCase
                                    .setContactEmail(contactId, text);

                            setUserStatus(userId, TEMPLATE_CREATE_CONTACT_PHONE_NUMBER_WAITING);
                            sendText(userId, CREATE_CONTACT_PHONE_NUMBER_MESSAGE);

                        } else {
                            sendText(userId, CREATE_CONTACT_EMAIL_MESSAGE);
                        }
                    }
                    case TEMPLATE_CREATE_CONTACT_PHONE_NUMBER_WAITING -> {
                        if (text.matches("[0-9-+ ]+") &&
                                message.isUserMessage()) {
                            int contactId = userStates.get(userId).getContactCreationId();

                            int templateId = userStates.get(userId).getTemplateCreationId();

                            setContactPhoneNumberUseCase
                                    .setContactPhoneNumber(contactId, text);

                            if (!getInfoUserHasChosenTemplateUseCase
                                    .userHasChosenTemplate(userId)) {
                                setChosenTemplateUseCase
                                        .setChosenTemplate(userId, templateId);
                            }

                            setUserStatus(userId, TEMPLATE_CREATE_CONTACT_WAITING);

                        } else {
                            sendText(userId, CREATE_CONTACT_PHONE_NUMBER_MESSAGE);
                        }
                    }
                    case TEMPLATE_CREATE_CONTACT_WAITING -> {
                        switch (text) {
                            case ADD_CONTACT_CALL_BACK -> {
                                setUserStatus(userId, TEMPLATE_CREATE_CONTACT_NAME_WAITING);
                                sendText(userId, CREATE_CONTACT_NAME_MESSAGE);
                            }
                            case CREATE_TEMPLATE_CALL_BACK -> {
                                int templateId = userStates.get(userId).getTemplateCreationId();

                                String templateText = getTemplateTextUseCase
                                        .getTemplateText(templateId);

                                sendText(userId, templateText);
                                sendKeyboard(userId, "Ef", MENU_KEYBOARD);
                                setUserStatus(userId, MAIN_MENU_WAITING);
                            }
                            default -> {
                                sendText(userId, CREATE_TEMPLATE_OR_ADD_CONTACT_MESSAGE);
                            }
                        }
                    }
                    case SETTINGS_WAITING -> {
                        InlineKeyboardMarkup allTemplatesKeyboard
                                = getAllTemplatesKeyboard(userId);

                        switch (text) {
                            case CREATE_TEMPLATE_CALL_BACK -> {
                                sendText(userId, TEMPLATE_CREATE_NAME_MESSAGE);
                                setUserStatus(userId, TEMPLATE_CREATE_NAME_WAITING);
                            }
                            case CHOOSE_TEMPLATE_CALL_BACK -> {
                                sendText(userId, SELECT_ONE_OR_MORE_OPTIONS_MESSAGE);
                                sendKeyboard(userId, "e", allTemplatesKeyboard);
                                setUserStatus(userId, SETTINGS_CHOOSE_TEMPLATE_WAITING);
                            }
                            case DELETE_TEMPLATE_CALL_BACK -> {
                                sendText(userId, SELECT_ONE_OR_MORE_OPTIONS_MESSAGE);
                                sendKeyboard(userId, "e", allTemplatesKeyboard);
                                setUserStatus(userId, SETTINGS_DELETE_TEMPLATE_WAITING);
                            }
                            case EDIT_TEMPLATE_CALL_BACK -> {
                                sendText(userId, SELECT_ONE_OR_MORE_OPTIONS_MESSAGE);
                                sendKeyboard(userId, "e", allTemplatesKeyboard);
                                setUserStatus(userId, SETTINGS_EDIT_TEMPLATE_WAITING);
                            }
                            case BACK_TO_MENU_CALL_BACK -> {
                                setUserStatus(userId, MAIN_MENU_WAITING);

                                int templateId = userStates.get(userId).getTemplateCreationId();

                                String templateText = getTemplateTextUseCase
                                        .getTemplateText(templateId);

                                sendText(userId, templateText);
                                sendKeyboard(userId, "Ef", MENU_KEYBOARD);
                            }
                            default -> {
                                sendText(userId, "Press one of the button");
                            }
                        }
                    }
                    case SETTINGS_CHOOSE_TEMPLATE_WAITING -> {
                        setChosenTemplateUseCase.setChosenTemplate(userId, Integer.parseInt(text));

                        int templateId = userStates.get(userId).getTemplateCreationId();

                        String templateText = getTemplateTextUseCase
                                .getTemplateText(templateId);

                        sendText(userId, templateText);
                        sendKeyboard(userId, "Ef", MENU_KEYBOARD);
                        setUserStatus(userId, MAIN_MENU_WAITING);
                    }
                    case SETTINGS_DELETE_TEMPLATE_WAITING -> {
                        deleteTemplateByIdUseCase
                                .deleteTemplateById(Integer.parseInt(text));

                        int templateId = userStates.get(userId).getTemplateCreationId();

                        String templateText = getTemplateTextUseCase
                                .getTemplateText(templateId);

                        sendText(userId, templateText);
                        sendKeyboard(userId, "Ef", MENU_KEYBOARD);
                        setUserStatus(userId, MAIN_MENU_WAITING);
                    }
                    case SETTINGS_EDIT_TEMPLATE_WAITING -> {
                        sendKeyboard(userId, "ef", EDIT_TEMPLATES_KEYBOARD);
                        setUserStatus(userId, EDIT_TEMPLATES_WAITING);
                    }
                    case SETTINGS_BACK_TO_MENU_WAITING -> {
                        int templateId = userStates.get(userId).getTemplateCreationId();

                        String templateText = getTemplateTextUseCase
                                .getTemplateText(templateId);

                        sendText(userId, templateText);
                        sendKeyboard(userId, "Ef", MENU_KEYBOARD);
                        setUserStatus(userId, MAIN_MENU_WAITING);
                    }
                    case EDIT_TEMPLATES_WAITING -> {
                        switch (text) {
                            case ADD_CONTACT_CALL_BACK -> {
                                sendText(userId, "Add contact");
                                setUserStatus(userId, EDIT_TEMPLATES_ADD_CONTACT_CHOSE_TEMPLATE_WAITING);
                            }
                            case DELETE_CONTACT_CALL_BACK -> {
                                sendText(userId, "Delete contact");
                                setUserStatus(userId, EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_TEMPLATE_WAITING);
                            }
                            case DELETE_ALL_CONTACTS_CALL_BACK -> {
                                deleteAllContactsUseCase.deleteAllContacts();

                                sendKeyboard(userId, "Ef", SETTINGS_KEYBOARD);
                                setUserStatus(userId, SETTINGS_WAITING);
                            }
                            case CHANGE_THE_TEMPLATE_NAME_CALL_BACK -> {
                                sendText(userId, "Choose template");
                                setUserStatus(userId, EDIT_TEMPLATES_CHANGE_THE_TEMPLATE_NAME_WAITING);
                            }
                            case BACK_TO_MENU_CALL_BACK -> {
                                sendKeyboard(userId, "Ef", SETTINGS_KEYBOARD);
                                setUserStatus(userId, SETTINGS_WAITING);
                            }
                            default -> {
                                sendText(userId, "Press one of the buttons");
                            }
                        }
                    }
                    case EDIT_TEMPLATES_ADD_CONTACT_CHOSE_TEMPLATE_WAITING -> {
                        sendText(userId, "Choose template");
                        sendKeyboard(userId, "ef", getAllTemplatesKeyboard(userId));
                        setUserStatus(userId, EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT);
                    }
                    case EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT -> {
                        sendText(userId, CREATE_CONTACT_NAME_MESSAGE);
                        setUserStatus(userId, EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_NAME_WAITING);
                    }
                    case EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_NAME_WAITING -> {
                        if (text.matches("\\W{1,16}") &&
                                message.isUserMessage()) {
                            int contactId = createIncompleteContactUseCase
                                    .createIncompleteContact(text);

                            setUserContactCreationId(userId, contactId);

                            sendText(userId, CREATE_CONTACT_EMAIL_MESSAGE);
                            setUserStatus(userId, EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_EMAIL_WAITING);

                        } else {
                            sendText(userId, CREATE_CONTACT_NAME_MESSAGE);
                        }
                    }
                    case EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_EMAIL_WAITING -> {
                        if (text.matches("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\\\.[A-Z]{2,6}$") &&
                                message.isUserMessage()) {
                            int contactId = userStates.get(userId).getContactCreationId();

                            setContactEmailUseCase
                                    .setContactEmail(contactId, text);

                            sendText(userId, CREATE_CONTACT_PHONE_NUMBER_MESSAGE);
                            setUserStatus(userId, EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_PHONE_NUMBER_WAITING);

                        } else {
                            sendText(userId, CREATE_CONTACT_EMAIL_MESSAGE);
                        }
                    }
                    case EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_PHONE_NUMBER_WAITING -> {
                        if (text.matches("[0-9-+ ]+") &&
                                message.isUserMessage()) {
                            int contactId = userStates.get(userId).getContactCreationId();

                            int templateId = userStates.get(userId).getTemplateCreationId();

                            setContactPhoneNumberUseCase
                                    .setContactPhoneNumber(contactId, text);

                            if (!getInfoUserHasChosenTemplateUseCase
                                    .userHasChosenTemplate(userId)) {
                                setChosenTemplateUseCase
                                        .setChosenTemplate(userId, templateId);
                            }

                            setUserStatus(userId, EDIT_TEMPLATES_WAITING);

                        } else {
                            sendText(userId, CREATE_CONTACT_PHONE_NUMBER_MESSAGE);
                        }
                    }
                    case EDIT_TEMPLATES_DELETE_CONTACT_WAITING -> {
                        sendText(userId, "Choose template to delete");
                        sendKeyboard(userId, "ef", getAllTemplatesKeyboard(userId));
                        setUserStatus(userId, EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_TEMPLATE_WAITING);
                    }
                    case EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_TEMPLATE_WAITING -> {
                        sendText(userId, "Choose contact to delete");
                        sendKeyboard(userId, "ef", getAllContactsKeyboard(
                                userStates.get(userId).getTemplateCreationId())
                        );
                        setUserStatus(userId, EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_CONTACT_WAITING);
                    }
                    case EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_CONTACT_WAITING -> {
                        deleteContactByIdUseCase.deleteContactById(Integer.parseInt(text));

                        setUserStatus(userId, EDIT_TEMPLATES_WAITING);
                    }
                    case EDIT_TEMPLATES_CHANGE_THE_TEMPLATE_NAME_WAITING -> {
                        sendText(userId, "Choose template");
                        sendKeyboard(userId, "ef", getAllTemplatesKeyboard(userId));
                        setUserStatus(userId, EDIT_TEMPLATES_CHANGE_THE_TEMPLATE_NAME_CHOSE_TEMPLATE_WAITING);
                    }
                    case EDIT_TEMPLATES_CHANGE_THE_TEMPLATE_NAME_CHOSE_TEMPLATE_WAITING -> {
                        int templateId = Integer.parseInt(text);
                        setUserTemplateCreationId(userId, templateId);

                        sendText(userId, TEMPLATE_CREATE_NAME_MESSAGE);
                    }
                    default -> {
                    }
                }
            }

            private synchronized void sendText(long chatId, String text) throws TelegramApiException {
                SendMessage sendMessage = SendMessage.builder()
                        .chatId(chatId)
                        .text(text)
                        .build();

                setUserMessageIdToDelete(chatId, sendMessage.getReplyToMessageId());

                execute(sendMessage);
            }

            private synchronized void setUserStatus(long userId, Status status) {
                if (userStates.containsKey(userId)) {
                    userStates.get(userId).setStatus(status);
                } else {
                    userStates.put(userId, UserCurrentStatus.builder()
                            .status(status)
                            .build());
                }
            }

            private synchronized void setUserTemplateCreationId(long userId, int templateId) {
                if (userStates.containsKey(userId)) {
                    userStates.get(userId).setTemplateCreationId(templateId);
                } else {
                    userStates.put(userId, UserCurrentStatus.builder()
                            .templateCreationId(templateId)
                            .build());
                }
            }

            private synchronized void setUserContactCreationId(long userId, int contactId) {
                if (userStates.containsKey(userId)) {
                    userStates.get(userId).setContactCreationId(contactId);
                } else {
                    userStates.put(userId, UserCurrentStatus.builder()
                            .contactCreationId(contactId)
                            .build());
                }
            }

            private synchronized void setUserMessageIdToDelete(long userId, int messageId) {
                if (userStates.containsKey(userId)) {
                    userStates.get(userId).setMessageIdToDelete(messageId);
                } else {
                    userStates.put(userId, UserCurrentStatus.builder()
                            .messageIdToDelete(messageId)
                            .build());
                }
            }

            private synchronized void sendKeyboard(
                    Long userId,
                    String text,
                    InlineKeyboardMarkup keyboard) throws TelegramApiException {
                SendMessage sm = SendMessage.builder()
                        .chatId(userId)
                        .parseMode("HTML").text(text)
                        .replyMarkup(keyboard)
                        .build();

                execute(sm);
            }

            private synchronized InlineKeyboardMarkup getAllTemplatesKeyboard(long userId) {
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
                        .build();
            }

            private synchronized InlineKeyboardMarkup getAllContactsKeyboard(int templateId) {
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
                        .build();
            }
        };

        telegramBotsApi.registerBot(bot);
        return bot;
    }
}
