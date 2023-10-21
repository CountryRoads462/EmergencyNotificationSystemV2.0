package com.andrew.ens.bot.adapter.out;

import com.andrew.ens.status.domain.Status;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.andrew.ens.bot.adapter.out.BotKeyboards.CANCEL_KEYBOARD;
import static com.andrew.ens.bot.adapter.out.BotKeyboards.SETTINGS_KEYBOARD;
import static com.andrew.ens.bot.adapter.out.BotMessages.SETTINGS_KEYBOARD_TEXT;
import static com.andrew.ens.bot.adapter.out.BotMessages.TEMPLATE_CREATE_NAME_MESSAGE;
import static com.andrew.ens.bot.adapter.out.buttons.CallBackData.BACK_CALL_BACK;
import static com.andrew.ens.bot.adapter.out.buttons.CallBackData.CHOOSE_TEMPLATE_CALL_BACK;
import static com.andrew.ens.bot.adapter.out.buttons.CallBackData.CREATE_TEMPLATE_CALL_BACK;
import static com.andrew.ens.bot.adapter.out.buttons.CallBackData.DELETE_TEMPLATE_CALL_BACK;
import static com.andrew.ens.bot.adapter.out.buttons.CallBackData.EDIT_TEMPLATE_CALL_BACK;
import static com.andrew.ens.status.domain.Status.CREATE_TEMPLATE_NAME_WAITING;
import static com.andrew.ens.status.domain.Status.MAIN_MENU_WAITING;
import static com.andrew.ens.status.domain.Status.SETTINGS_CHOOSE_TEMPLATE_WAITING;
import static com.andrew.ens.status.domain.Status.SETTINGS_DELETE_TEMPLATE_WAITING;
import static com.andrew.ens.status.domain.Status.SETTINGS_EDIT_TEMPLATE_CHOSE_TEMPLATE_WAITING;

@Component
public class SettingsHandler implements BotHandler {
    @Override
    public Status getStatus() {
        return Status.SETTINGS_WAITING;
    }

    @Override
    public void execAction(BotController bot, Long userId, String text) throws TelegramApiException {
        InlineKeyboardMarkup allTemplatesKeyboard
                = bot.getAllTemplatesKeyboard(userId);

        switch (text) {
            case CREATE_TEMPLATE_CALL_BACK -> {
                bot.sendKeyboard(
                        userId,
                        TEMPLATE_CREATE_NAME_MESSAGE,
                        CANCEL_KEYBOARD
                );
                bot.setUserStatus(userId, CREATE_TEMPLATE_NAME_WAITING);
            }
            case CHOOSE_TEMPLATE_CALL_BACK -> {
                bot.sendKeyboard(
                        userId,
                        "Select one from the list",
                        allTemplatesKeyboard
                );
                bot.setUserStatus(userId, SETTINGS_CHOOSE_TEMPLATE_WAITING);
            }
            case DELETE_TEMPLATE_CALL_BACK -> {
                bot.sendKeyboard(
                        userId,
                        "Select the one you want to delete",
                        allTemplatesKeyboard
                );
                bot.setUserStatus(userId, SETTINGS_DELETE_TEMPLATE_WAITING);
            }
            case EDIT_TEMPLATE_CALL_BACK -> {
                bot.sendKeyboard(
                        userId,
                        "Select the one you want to edit",
                        allTemplatesKeyboard
                );
                bot.setUserStatus(
                        userId,
                        SETTINGS_EDIT_TEMPLATE_CHOSE_TEMPLATE_WAITING
                );
            }
            case BACK_CALL_BACK -> {
                bot.sendMainMenuKeyboard(userId);
                bot.setUserStatus(userId, MAIN_MENU_WAITING);
            }
            default -> bot.sendKeyboard(
                    userId,
                    SETTINGS_KEYBOARD_TEXT,
                    SETTINGS_KEYBOARD
            );
        }
    }
}
