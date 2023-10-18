package com.andrew.ens.bot;

import com.andrew.ens.Status;
import com.andrew.ens.UserCurrentStatus;
import com.andrew.ens.contact.adapter.out.persistence.Contact;
import com.andrew.ens.contact.application.port.in.*;
import com.andrew.ens.template.adapter.out.persistence.Template;
import com.andrew.ens.template.application.port.in.*;
import com.andrew.ens.user.application.port.in.CreateUserUseCase;
import com.andrew.ens.user.application.port.in.GetChosenTemplateIdUseCase;
import com.andrew.ens.user.application.port.in.GetInfoUserHasChosenTemplateUseCase;
import com.andrew.ens.user.application.port.in.SetChosenTemplateUseCase;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.andrew.ens.Status.*;
import static com.andrew.ens.bot.BotButtons.BACK_BUTTON;
import static com.andrew.ens.bot.BotKeyboards.*;
import static com.andrew.ens.bot.BotMessages.*;
import static com.andrew.ens.bot.buttons.CallBackData.*;

@Configuration
@RequiredArgsConstructor
public class BotController {

    private final CreateUserUseCase createUserUseCase;
    private final GetChosenTemplateIdUseCase getChosenTemplateIdUseCase;
    private final GetInfoUserHasChosenTemplateUseCase getInfoUserHasChosenTemplateUseCase;
    private final SetChosenTemplateUseCase setChosenTemplateUseCase;

    private final CreateIncompleteTemplateUseCase createIncompleteTemplateUseCase;
    private final SetTemplateTextUseCase setTemplateTextUseCase;
    private final GetTemplateTextUseCase getTemplateTextUseCase;
    private final GetTemplatesByOwnerIdUseCase getTemplatesByOwnerIdUseCase;
    private final DeleteTemplateByIdUseCase deleteTemplateByIdUseCase;
    private final GetTemplateByIdUseCase getTemplateByIdUseCase;
    private final GetInfoTemplateExistsByNameAndOwnerIdUseCase getInfoTemplateExistsByNameAndOwnerIdUseCase;

    private final CreateIncompleteContactUseCase createIncompleteContactUseCase;
    private final SetContactEmailUseCase setContactEmailUseCase;
    private final SetContactPhoneNumberUseCase setContactPhoneNumberUseCase;
    private final DeleteAllContactsUseCase deleteAllContactsUseCase;
    private final GetAllContactsByTemplateIdUseCase getAllContactsByTemplateIdUseCase;
    private final DeleteContactByIdUseCase deleteContactByIdUseCase;

    private final ConcurrentHashMap<Long, UserCurrentStatus> userStates = new ConcurrentHashMap<>();

    @Bean
    TelegramLongPollingBot bot(
            @Value("${app.telegram.token}") String botToken,
            @Value("${app.telegram.username}") String botUsername
    ) throws TelegramApiException {
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

                    setUserStatus(userId, MAIN_MENU_WAITING);
                }

                System.out.println(userStates.get(userId));

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

                System.out.println(text);

                switch (userStates.get(userId).getStatus()) {
                    case MAIN_MENU_WAITING -> {
                        switch (text) {
                            case SEND_EMERGENCY_MESSAGE_CALL_BACK -> {
                                sendText(userId, "Send emergency message");
                                sendKeyboard(userId, MAIN_MENU_KEYBOARD_TEXT, MAIN_MENU_KEYBOARD);
                            }
                            case SETTINGS_CALL_BACK -> {
                                sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
                                setUserStatus(userId, SETTINGS_WAITING);
                            }
                            default -> sendMainMenuKeyboard(userId);
                        }
                    }
                    case SETTINGS_WAITING -> {
                        InlineKeyboardMarkup allTemplatesKeyboard
                                = getAllTemplatesKeyboard(userId);

                        switch (text) {
                            case CREATE_TEMPLATE_CALL_BACK -> {
                                sendKeyboard(userId, TEMPLATE_CREATE_NAME_MESSAGE, CANCEL_KEYBOARD);
                                setUserStatus(userId, CREATE_TEMPLATE_NAME_WAITING);
                            }
                            case CHOOSE_TEMPLATE_CALL_BACK -> {
                                sendKeyboard(userId, "Select one from the list", allTemplatesKeyboard);
                                setUserStatus(userId, SETTINGS_CHOOSE_TEMPLATE_WAITING);
                            }
                            case DELETE_TEMPLATE_CALL_BACK -> {
                                sendKeyboard(userId, "Select the one you want to delete", allTemplatesKeyboard);
                                setUserStatus(userId, SETTINGS_DELETE_TEMPLATE_WAITING);
                            }
                            case EDIT_TEMPLATE_CALL_BACK -> {
                                sendKeyboard(userId, "Select one from the list", allTemplatesKeyboard);
                                setUserStatus(userId, SETTINGS_EDIT_TEMPLATE_CHOSE_TEMPLATE_WAITING);
                            }
                            case BACK_CALL_BACK -> {
                                sendMainMenuKeyboard(userId);
                                setUserStatus(userId, MAIN_MENU_WAITING);
                            }
                            default -> sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
                        }
                    }
                    case SETTINGS_EDIT_TEMPLATE_CHOSE_TEMPLATE_WAITING -> {
                        if (text.equals(BACK_CALL_BACK)) {
                            sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
                            setUserStatus(userId, SETTINGS_WAITING);
                            break;
                        }

                        if (update.hasCallbackQuery()) {
                            userStates.get(userId).setTemplateCreationId(Integer.parseInt(text));

                            sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
                            setUserStatus(userId, EDIT_TEMPLATES_WAITING);

                        } else {
                            InlineKeyboardMarkup allTemplatesKeyboard
                                    = getAllTemplatesKeyboard(userId);

                            sendKeyboard(userId, "Select one from the list", allTemplatesKeyboard);
                            setUserStatus(userId, SETTINGS_EDIT_TEMPLATE_CHOSE_TEMPLATE_WAITING);
                        }
                    }
                    case CREATE_TEMPLATE_NAME_WAITING -> {
                        if (text.equals(CANCEL_CALL_BACK)) {
                            sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
                            setUserStatus(userId, SETTINGS_WAITING);
                            break;
                        }

                        if (getInfoTemplateExistsByNameAndOwnerIdUseCase
                                .getInfoTemplateExistsByNameAndOwnerId(userId, text)) {
                            sendText(userId, "You already have a template with this name");
                            break;
                        }

                        if (text.matches(".{1,16}")) {
                            int templateId = createIncompleteTemplateUseCase
                                    .createIncompleteTemplate(text, userId);
                            setUserTemplateCreationId(userId, templateId);


                            sendKeyboard(userId, TEMPLATE_CREATE_TEXT_MESSAGE, CANCEL_KEYBOARD);
                            setUserStatus(userId, CREATE_TEMPLATE_TEXT_WAITING);

                        } else {
                            sendKeyboard(userId, TEMPLATE_CREATE_NAME_MESSAGE, CANCEL_KEYBOARD);
                        }
                    }
                    case CREATE_TEMPLATE_TEXT_WAITING -> {
                        if (text.equals(CANCEL_CALL_BACK)) {
                            deleteTemplateByIdUseCase.deleteTemplateById(
                                    userStates.get(userId).getTemplateCreationId()
                            );

                            sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
                            setUserStatus(userId, SETTINGS_WAITING);
                            break;
                        }

                        if (text.matches(".{1,128}")) {
                            setTemplateTextUseCase.setTemplateText(
                                    userStates.get(userId).getTemplateCreationId(),
                                    text
                            );

                            sendText(userId, CREATE_TEMPLATE_CREATED_MESSAGE);
                            sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
                            setUserStatus(userId, SETTINGS_WAITING);

                        } else {
                            sendText(userId, TEMPLATE_CREATE_TEXT_MESSAGE);
                        }
                    }
                    case TEMPLATE_CREATE_CONTACT_NAME_WAITING -> {
                        if (text.matches("\\w{1,16}")) {
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
                        if (text.matches("\\w+@\\w+")) {
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
                        if (text.matches("[0-9-+ ]+")) {
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
                            sendKeyboard(userId, "EF", CREATE_TEMPLATE_KEYBOARD);

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
                                sendKeyboard(userId, "Ef", MAIN_MENU_KEYBOARD);
                                setUserStatus(userId, MAIN_MENU_WAITING);
                            }
                            default -> {
                                sendText(userId, CREATE_TEMPLATE_OR_ADD_CONTACT_MESSAGE);
                            }
                        }
                    }
                    case SETTINGS_CHOOSE_TEMPLATE_WAITING -> {
                        if (text.equals(BACK_CALL_BACK)) {
                            sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
                            setUserStatus(userId, SETTINGS_WAITING);
                            break;
                        }

                        setChosenTemplateUseCase.setChosenTemplate(userId, Integer.parseInt(text));

                        sendMainMenuKeyboard(userId);
                        setUserStatus(userId, MAIN_MENU_WAITING);
                    }
                    case SETTINGS_DELETE_TEMPLATE_WAITING -> {
                        if (text.equals(BACK_CALL_BACK)) {
                            sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
                            setUserStatus(userId, SETTINGS_WAITING);
                            break;
                        }

                        if (update.hasCallbackQuery()) {
                            userStates.get(userId).setTemplateCreationId(Integer.parseInt(text));

                            sendKeyboard(userId, "Are you sure?", CONFIRM_KEYBOARD);
                            setUserStatus(userId, SETTINGS_DELETE_TEMPLATE_CONFIRM_WAITING);

                        } else {
                            InlineKeyboardMarkup allTemplatesKeyboard
                                    = getAllTemplatesKeyboard(userId);

                            sendKeyboard(userId, "Select the one you want to delete", allTemplatesKeyboard);
                            setUserStatus(userId, SETTINGS_DELETE_TEMPLATE_WAITING);
                        }
                    }
                    case SETTINGS_DELETE_TEMPLATE_CONFIRM_WAITING -> {
                        switch (text) {
                            case CONFIRM_CALL_BACK -> {
                                deleteTemplateByIdUseCase.deleteTemplateById(
                                        userStates.get(userId).getTemplateCreationId()
                                );

                                sendMainMenuKeyboard(userId);
                                setUserStatus(userId, MAIN_MENU_WAITING);
                            }
                            case CANCEL_CALL_BACK -> {
                                sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
                                setUserStatus(userId, SETTINGS_WAITING);
                            }
                            default -> {
                                sendKeyboard(userId, "Are you sure?", CONFIRM_KEYBOARD);
                                setUserStatus(userId, SETTINGS_DELETE_TEMPLATE_CONFIRM_WAITING);
                            }
                        }
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
                        sendKeyboard(userId, "Ef", MAIN_MENU_KEYBOARD);
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
                            case BACK_CALL_BACK -> {
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
                        if (text.matches("\\w{1,16}") &&
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
                        if (text.matches("\\w+@\\w+")) {
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
                        if (text.matches("[0-9-+ ]+")) {
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

                execute(sendMessage);
            }

            private synchronized void sendMainMenuKeyboard(long userId) throws TelegramApiException {
                String text = "Selected template: none";

                Optional<Integer> templateIdOptional = getChosenTemplateIdUseCase
                        .getChosenTemplateId(userId);

                if (templateIdOptional.isPresent()) {
                    int templateId = templateIdOptional.get();

                    Optional<Template> templateOptional = getTemplateByIdUseCase
                            .getTemplateById(templateId);

                    if (templateOptional.isPresent()) {
                        Template template = templateOptional.get();

                        text =  String.format("Selected template: %s\n" +
                                        "\"%s\"",
                                template.getName(),
                                template.getText()
                        );
                    }
                }

                sendKeyboard(userId, text, MAIN_MENU_KEYBOARD);
            }

            private synchronized void sendKeyboard(
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
                        .keyboardRow(List.of(BACK_BUTTON))
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
