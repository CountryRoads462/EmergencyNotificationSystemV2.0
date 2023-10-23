package com.andrew.ens.bot.adapter.in.handlers;

import com.andrew.ens.bot.adapter.in.BotController;
import com.andrew.ens.bot.adapter.in.BotHandler;
import com.andrew.ens.status.domain.Status;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.andrew.ens.bot.adapter.in.BotKeyboards.CANCEL_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotKeyboards.CONFIRM_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotKeyboards.EDIT_TEMPLATES_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotKeyboards.SETTINGS_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotMessages.CREATE_CONTACT_NAME_MESSAGE;
import static com.andrew.ens.bot.adapter.in.BotMessages.SETTINGS_KEYBOARD_TEXT;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.ADD_CONTACT_CALL_BACK;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.BACK_CALL_BACK;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.CHANGE_THE_TEMPLATE_NAME_CALL_BACK;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.CHANGE_THE_TEMPLATE_TEXT_CALL_BACK;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.DELETE_ALL_CONTACTS_CALL_BACK;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.DELETE_CONTACT_CALL_BACK;
import static com.andrew.ens.status.domain.Status.EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_NAME_WAITING;
import static com.andrew.ens.status.domain.Status.EDIT_TEMPLATES_CHANGE_THE_TEMPLATE_NAME_WAITING;
import static com.andrew.ens.status.domain.Status.EDIT_TEMPLATES_CHANGE_THE_TEMPLATE_TEXT_WAITING;
import static com.andrew.ens.status.domain.Status.EDIT_TEMPLATES_DELETE_ALL_CONTACTS_CONFIRM_WAITING;
import static com.andrew.ens.status.domain.Status.EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_TEMPLATE_WAITING;
import static com.andrew.ens.status.domain.Status.EDIT_TEMPLATES_WAITING;
import static com.andrew.ens.status.domain.Status.SETTINGS_WAITING;

@Component
public class EditTemplatesHandler implements BotHandler {

    @Override
    public Status getStatus() {
        return Status.EDIT_TEMPLATES_WAITING;
    }

    @Override
    public void execAction(
            Update update,
            BotController bot,
            Long userId,
            String text
    ) throws TelegramApiException {
        switch (text) {
            case ADD_CONTACT_CALL_BACK -> {
                bot.sendKeyboard(userId, CREATE_CONTACT_NAME_MESSAGE, CANCEL_KEYBOARD);
                bot.setUserStatus(
                        userId,
                        EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_NAME_WAITING
                );
            }
            case DELETE_CONTACT_CALL_BACK -> {
                bot.sendKeyboard(
                        userId,
                        "Select the one you want to delete",
                        bot.getAllContactsKeyboard(
                                bot.getUserStates().get(userId).getTemplateCreationId()
                        )
                );
                bot.setUserStatus(
                        userId,
                        EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_TEMPLATE_WAITING
                );
            }
            case DELETE_ALL_CONTACTS_CALL_BACK -> {
                bot.sendKeyboard(userId, "Are you sure?", CONFIRM_KEYBOARD);
                bot.setUserStatus(userId, EDIT_TEMPLATES_DELETE_ALL_CONTACTS_CONFIRM_WAITING);
            }
            case CHANGE_THE_TEMPLATE_NAME_CALL_BACK -> {
                bot.sendKeyboard(userId, "Enter new name", CANCEL_KEYBOARD);
                bot.setUserStatus(
                        userId,
                        EDIT_TEMPLATES_CHANGE_THE_TEMPLATE_NAME_WAITING
                );
            }
            case CHANGE_THE_TEMPLATE_TEXT_CALL_BACK -> {
                bot.sendKeyboard(userId, "Enter new text", CANCEL_KEYBOARD);
                bot.setUserStatus(
                        userId,
                        EDIT_TEMPLATES_CHANGE_THE_TEMPLATE_TEXT_WAITING
                );
            }
            case BACK_CALL_BACK -> {
                bot.sendKeyboard(
                        userId,
                        SETTINGS_KEYBOARD_TEXT,
                        SETTINGS_KEYBOARD
                );
                bot.setUserStatus(userId, SETTINGS_WAITING);
            }
            default -> {
                bot.sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
                bot.setUserStatus(userId, EDIT_TEMPLATES_WAITING);
            }
        }
    }
}
