package com.andrew.ens.bot;

import com.andrew.ens.Status;
import com.andrew.ens.UserCurrentStatus;
import com.andrew.ens.contact.adapter.out.persistence.Contact;
import com.andrew.ens.contact.application.port.in.*;
import com.andrew.ens.google_smtp.application.port.in.SendEmailUseCase;
import com.andrew.ens.template.adapter.out.persistence.Template;
import com.andrew.ens.template.application.port.in.*;
import com.andrew.ens.user.application.port.in.CreateUserUseCase;
import com.andrew.ens.user.application.port.in.GetChosenTemplateIdUseCase;
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
    private final SetChosenTemplateUseCase setChosenTemplateUseCase;

    private final CreateIncompleteTemplateUseCase createIncompleteTemplateUseCase;
    private final SetTemplateTextUseCase setTemplateTextUseCase;
    private final GetTemplatesByOwnerIdUseCase getTemplatesByOwnerIdUseCase;
    private final DeleteTemplateByIdUseCase deleteTemplateByIdUseCase;
    private final GetTemplateByIdUseCase getTemplateByIdUseCase;
    private final GetInfoTemplateExistsByNameAndOwnerIdUseCase getInfoTemplateExistsByNameAndOwnerIdUseCase;
    private final SetTemplateNameByTemplateIdUseCase setTemplateNameByTemplateIdUseCase;

    private final CreateIncompleteContactUseCase createIncompleteContactUseCase;
    private final SetContactEmailUseCase setContactEmailUseCase;
    private final SetContactPhoneNumberUseCase setContactPhoneNumberUseCase;
    private final DeleteAllContactsUseCase deleteAllContactsUseCase;
    private final GetAllContactsByTemplateIdUseCase getAllContactsByTemplateIdUseCase;
    private final DeleteContactByIdUseCase deleteContactByIdUseCase;
    private final GetInfoContactExistsByPhoneNumberAndTemplateIdUseCase getInfoContactExistsByPhoneNumberAndTemplateIdUseCase;
    private final GetInfoContactExistsByEmailAndTemplateIdUseCase getInfoContactExistsByEmailAndTemplateIdUseCase;
    private final GetInfoContactExistsByNameAndTemplateIdUseCase getInfoContactExistsByNameAndTemplateIdUseCase;
    private final SetContactTemplateIdUseCase setContactTemplateIdUseCase;

    private final SendEmailUseCase sendEmailUseCase;

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
                    text = callbackQuery.getData();
                }

                System.out.println(text);

                switch (userStates.get(userId).getStatus()) {
                    case MAIN_MENU_WAITING -> {
                        switch (text) {
                            case SEND_EMERGENCY_MESSAGE_CALL_BACK -> {
                                if (userStates.get(userId).isReadyToSend()) {
                                    sendKeyboard(userId, "Are you sure?", CONFIRM_KEYBOARD);
                                    setUserStatus(userId, MAIN_MENU_SEND_EMERGENCY_MESSAGE_WAITING);

                                } else {
                                    sendText(userId, "You have to choose a template");
                                    sendMainMenuKeyboard(userId);
                                }
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
                                sendKeyboard(userId, "Select the one you want to edit", allTemplatesKeyboard);
                                setUserStatus(userId, SETTINGS_EDIT_TEMPLATE_CHOSE_TEMPLATE_WAITING);
                            }
                            case BACK_CALL_BACK -> {
                                sendMainMenuKeyboard(userId);
                                setUserStatus(userId, MAIN_MENU_WAITING);
                            }
                            default -> sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
                        }
                    }
                    case MAIN_MENU_SEND_EMERGENCY_MESSAGE_WAITING -> {
                        switch (text) {
                            case CONFIRM_CALL_BACK -> {
                                Optional<Integer> templateIdOptional = getChosenTemplateIdUseCase
                                        .getChosenTemplateId(userId);

                                if (templateIdOptional.isPresent()) {
                                    Optional<Template> templateOptional = getTemplateByIdUseCase
                                            .getTemplateById(templateIdOptional.get());

                                    if (templateOptional.isPresent()) {
                                        Template template = templateOptional.get();

                                        String templateName = template.getName();
                                        String templateText = template.getText();

                                        getAllContactsByTemplateIdUseCase
                                                .getAllContactsByTemplateId(template.getId())
                                                .stream()
                                                .map(Contact::getEmail)
                                                        .forEach(email -> {
                                                            sendEmailUseCase.sendEmail(
                                                                    templateName,
                                                                    templateText,
                                                                    email
                                                            );
                                                        });

                                        sendText(userId, "Emails have been sent successfully");
                                    }
                                }

                                sendMainMenuKeyboard(userId);
                                setUserStatus(userId, MAIN_MENU_WAITING);
                            }
                            case CANCEL_CALL_BACK -> {
                                sendMainMenuKeyboard(userId);
                                setUserStatus(userId, MAIN_MENU_WAITING);
                            }
                            default -> {
                                sendKeyboard(userId, "Are you sure?", CONFIRM_KEYBOARD);
                                setUserStatus(userId, MAIN_MENU_SEND_EMERGENCY_MESSAGE_WAITING);
                            }
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
                            sendKeyboard(userId, "Select one from the list", getAllTemplatesKeyboard(userId));
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
                            sendKeyboard(userId, "Select the one you want to delete", getAllTemplatesKeyboard(userId));
                            setUserStatus(userId, SETTINGS_DELETE_TEMPLATE_WAITING);
                        }
                    }
                    case SETTINGS_DELETE_TEMPLATE_CONFIRM_WAITING -> {
                        switch (text) {
                            case CONFIRM_CALL_BACK -> {
                                deleteTemplateByIdUseCase.deleteTemplateById(
                                        userStates.get(userId).getTemplateCreationId()
                                );

                                sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
                                setUserStatus(userId, SETTINGS_WAITING);
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
                    case EDIT_TEMPLATES_WAITING -> {
                        switch (text) {
                            case ADD_CONTACT_CALL_BACK -> {
                                sendKeyboard(userId, CREATE_CONTACT_NAME_MESSAGE, CANCEL_KEYBOARD);
                                setUserStatus(userId, EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_NAME_WAITING);
                            }
                            case DELETE_CONTACT_CALL_BACK -> {
                                sendKeyboard(
                                        userId,
                                        "Select the one you want to delete",
                                        getAllContactsKeyboard(
                                                userStates.get(userId).getTemplateCreationId()
                                        )
                                );
                                setUserStatus(userId, EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_TEMPLATE_WAITING);
                            }
                            case DELETE_ALL_CONTACTS_CALL_BACK -> {
                                sendKeyboard(userId, "Are you sure?", CONFIRM_KEYBOARD);
                                setUserStatus(userId, EDIT_TEMPLATES_DELETE_ALL_CONTACTS_CONFIRM);
                            }
                            case CHANGE_THE_TEMPLATE_NAME_CALL_BACK -> {
                                sendKeyboard(userId, "Enter new name", CANCEL_KEYBOARD);
                                setUserStatus(userId, EDIT_TEMPLATES_CHANGE_THE_TEMPLATE_NAME_WAITING);
                            }
                            case BACK_CALL_BACK -> {
                                sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
                                setUserStatus(userId, SETTINGS_WAITING);
                            }
                            default -> {
                                sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
                                setUserStatus(userId, EDIT_TEMPLATES_WAITING);
                            }
                        }
                    }
                    case EDIT_TEMPLATES_DELETE_ALL_CONTACTS_CONFIRM -> {
                        switch (text) {
                            case CONFIRM_CALL_BACK -> {
                                deleteAllContactsUseCase.deleteAllContacts();

                                sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
                                setUserStatus(userId, EDIT_TEMPLATES_WAITING);
                            }
                            case CANCEL_CALL_BACK -> {
                                sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
                                setUserStatus(userId, EDIT_TEMPLATES_WAITING);
                            }
                            default -> {
                                sendKeyboard(userId, "Are you sure?", CONFIRM_KEYBOARD);
                                setUserStatus(userId, EDIT_TEMPLATES_DELETE_ALL_CONTACTS_CONFIRM);
                            }
                        }
                    }
                    case EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_NAME_WAITING -> {
                        if (text.equals(CANCEL_CALL_BACK)) {
                            sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
                            setUserStatus(userId, EDIT_TEMPLATES_WAITING);
                            break;
                        }

                        if (getInfoContactExistsByNameAndTemplateIdUseCase
                                .getInfoContactExistsByNameAndTemplateId(
                                        text,
                                        userStates.get(userId).getTemplateCreationId()
                                )) {
                            sendText(userId, "You already have a contact with this name");
                            break;
                        }

                        if (text.matches(".{1,16}")) {
                            int contactId = createIncompleteContactUseCase
                                    .createIncompleteContact(text);
                            setUserContactCreationId(userId, contactId);

                            sendKeyboard(userId, CREATE_CONTACT_EMAIL_MESSAGE, CANCEL_KEYBOARD);
                            setUserStatus(userId, EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_EMAIL_WAITING);

                        } else {
                            sendKeyboard(userId, CREATE_CONTACT_NAME_MESSAGE, CANCEL_KEYBOARD);
                        }
                    }
                    case EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_EMAIL_WAITING -> {
                        int contactId = userStates.get(userId).getContactCreationId();

                        if (text.equals(CANCEL_CALL_BACK)) {
                            deleteContactByIdUseCase.deleteContactById(contactId);

                            sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
                            setUserStatus(userId, EDIT_TEMPLATES_WAITING);
                            break;
                        }

                        if (getInfoContactExistsByEmailAndTemplateIdUseCase
                                .getInfoContactExistsByEmailAndTemplateId(
                                        text,
                                        userStates.get(userId).getTemplateCreationId()
                                )) {
                            sendText(userId, "You already have a contact with this email");
                            break;
                        }

                        if (text.matches(".+@.+")) {
                            setContactEmailUseCase
                                    .setContactEmail(contactId, text);

                            sendKeyboard(userId, CREATE_CONTACT_PHONE_NUMBER_MESSAGE, CANCEL_KEYBOARD);
                            setUserStatus(userId, EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_PHONE_NUMBER_WAITING);

                        } else {
                            sendKeyboard(userId, CREATE_CONTACT_EMAIL_MESSAGE, CANCEL_KEYBOARD);
                        }
                    }
                    case EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_PHONE_NUMBER_WAITING -> {
                        int contactId = userStates.get(userId).getContactCreationId();
                        int templateId = userStates.get(userId).getTemplateCreationId();

                        if (text.equals(CANCEL_CALL_BACK)) {
                            deleteContactByIdUseCase.deleteContactById(contactId);

                            sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
                            setUserStatus(userId, EDIT_TEMPLATES_WAITING);
                            break;
                        }

                        if (getInfoContactExistsByPhoneNumberAndTemplateIdUseCase
                                .getInfoContactExistsByPhoneNumberAndTemplateId(
                                        text,
                                        templateId
                                )) {
                            sendText(userId, "You already have a contact with this phone number");
                            break;
                        }

                        if (text.matches("[0-9-+ ]+")) {
                            setContactPhoneNumberUseCase
                                    .setContactPhoneNumber(contactId, text);

                            setContactTemplateIdUseCase
                                    .setContactTemplateId(contactId, templateId);

                            sendText(userId, "The contact was successfully created");
                            sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
                            setUserStatus(userId, EDIT_TEMPLATES_WAITING);

                        } else {
                            sendKeyboard(userId, CREATE_CONTACT_PHONE_NUMBER_MESSAGE, CANCEL_KEYBOARD);
                        }
                    }
                    case EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_TEMPLATE_WAITING -> {
                        if (text.equals(BACK_CALL_BACK)) {
                            sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
                            setUserStatus(userId, EDIT_TEMPLATES_WAITING);
                            break;
                        }

                        if (update.hasCallbackQuery()) {
                            userStates.get(userId).setContactCreationId(Integer.parseInt(text));

                            sendKeyboard(userId, "Are you sure?", CONFIRM_KEYBOARD);
                            setUserStatus(userId, EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_TEMPLATE_CONFIRM_WAITING);

                        } else {
                            sendKeyboard(
                                    userId,
                                    "Select the one you want to delete",
                                    getAllContactsKeyboard(
                                            userStates.get(userId).getTemplateCreationId()
                                    )
                            );
                            setUserStatus(userId, EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_TEMPLATE_WAITING);
                        }
                    }
                    case EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_TEMPLATE_CONFIRM_WAITING -> {
                        switch (text) {
                            case CONFIRM_CALL_BACK -> {
                                deleteContactByIdUseCase.deleteContactById(
                                        userStates.get(userId).getContactCreationId()
                                );

                                sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
                                setUserStatus(userId, EDIT_TEMPLATES_WAITING);
                            }
                            case CANCEL_CALL_BACK -> {
                                sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
                                setUserStatus(userId, EDIT_TEMPLATES_WAITING);
                            }
                            default -> {
                                sendKeyboard(userId, "Are you sure?", CONFIRM_KEYBOARD);
                                setUserStatus(userId, EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_TEMPLATE_CONFIRM_WAITING);
                            }
                        }
                    }
                    case EDIT_TEMPLATES_CHANGE_THE_TEMPLATE_NAME_WAITING -> {
                        if (text.equals(CANCEL_CALL_BACK)) {
                            sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
                            setUserStatus(userId, EDIT_TEMPLATES_WAITING);
                            break;
                        }

                        if (getInfoTemplateExistsByNameAndOwnerIdUseCase
                                .getInfoTemplateExistsByNameAndOwnerId(userId, text)) {
                            sendText(userId, "You already have a template with this name");
                            break;
                        }

                        if (text.matches(".{1,16}")) {
                            int templateId = userStates.get(userId).getTemplateCreationId();

                            setTemplateNameByTemplateIdUseCase
                                    .setTemplateNameByTemplateId(templateId, text);

                            sendText(userId, "Name changed successfully");
                            sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
                            setUserStatus(userId, EDIT_TEMPLATES_WAITING);

                        } else {
                            sendKeyboard(userId, TEMPLATE_CREATE_NAME_MESSAGE, CANCEL_KEYBOARD);
                        }
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

                userStates.get(userId).setReadyToSend(false);

                if (templateIdOptional.isPresent()) {
                    int templateId = templateIdOptional.get();

                    Optional<Template> templateOptional = getTemplateByIdUseCase
                            .getTemplateById(templateId);

                    if (templateOptional.isPresent()) {
                        Template template = templateOptional.get();

                        int numberOfContacts = getAllContactsByTemplateIdUseCase
                                .getAllContactsByTemplateId(templateId).size();

                        text = String.format("Selected template: %s (%d contacts)\n" +
                                        "\"%s\"",
                                template.getName(),
                                numberOfContacts,
                                template.getText()
                        );

                        userStates.get(userId).setReadyToSend(true);
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
                        .keyboardRow(List.of(BACK_BUTTON))
                        .build();
            }
        };

        telegramBotsApi.registerBot(bot);
        return bot;
    }
}
