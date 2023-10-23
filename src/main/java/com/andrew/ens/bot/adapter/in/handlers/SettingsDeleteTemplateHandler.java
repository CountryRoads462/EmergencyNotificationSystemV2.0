package com.andrew.ens.bot.adapter.in.handlers;

import com.andrew.ens.bot.adapter.in.BotController;
import com.andrew.ens.bot.adapter.in.BotHandler;
import com.andrew.ens.status.domain.Status;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.andrew.ens.bot.adapter.in.BotKeyboards.CONFIRM_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotKeyboards.SETTINGS_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotMessages.SETTINGS_KEYBOARD_TEXT;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.BACK_CALL_BACK;
import static com.andrew.ens.status.domain.Status.SETTINGS_DELETE_TEMPLATE_CONFIRM_WAITING;
import static com.andrew.ens.status.domain.Status.SETTINGS_DELETE_TEMPLATE_WAITING;
import static com.andrew.ens.status.domain.Status.SETTINGS_WAITING;

@Component
public class SettingsDeleteTemplateHandler implements BotHandler {

    @Override
    public Status getStatus() {
        return Status.SETTINGS_DELETE_TEMPLATE_WAITING;
    }

    @Override
    public void execAction(
            Update update,
            BotController bot,
            Long userId,
            String text
    ) throws TelegramApiException {
        if (text.equals(BACK_CALL_BACK)) {
            bot.sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
            bot.setUserStatus(userId, SETTINGS_WAITING);
            return;
        }

        if (update.hasCallbackQuery()) {
            bot.getUserStates().get(userId).setTemplateCreationId(Integer.parseInt(text));

            bot.sendKeyboard(userId, "Are you sure?", CONFIRM_KEYBOARD);
            bot.setUserStatus(userId, SETTINGS_DELETE_TEMPLATE_CONFIRM_WAITING);

        } else {
            bot.sendKeyboard(
                    userId,
                    "Select the one you want to delete",
                    bot.getAllTemplatesKeyboard(userId)
            );
            bot.setUserStatus(userId, SETTINGS_DELETE_TEMPLATE_WAITING);
        }
    }
}
