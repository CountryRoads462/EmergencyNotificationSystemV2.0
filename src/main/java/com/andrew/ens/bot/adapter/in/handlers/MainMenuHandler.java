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
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.SEND_EMERGENCY_MESSAGE_CALL_BACK;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.SETTINGS_CALL_BACK;
import static com.andrew.ens.status.domain.Status.MAIN_MENU_SEND_EMERGENCY_MESSAGE_WAITING;
import static com.andrew.ens.status.domain.Status.SETTINGS_WAITING;

@Component
public class MainMenuHandler implements BotHandler {

    @Override
    public Status getStatus() {
        return Status.MAIN_MENU_WAITING;
    }

    @Override
    public void execAction(
            Update update,
            BotController bot,
            Long userId,
            String text
    ) throws TelegramApiException {
        switch (text) {
            case SEND_EMERGENCY_MESSAGE_CALL_BACK -> {
                if (bot.getUserStates().get(userId).isReadyToSend()) {
                    bot.sendKeyboard(userId, "Are you sure?", CONFIRM_KEYBOARD);
                    bot.setUserStatus(userId, MAIN_MENU_SEND_EMERGENCY_MESSAGE_WAITING);

                } else {
                    bot.sendText(userId, "You have to choose a template");
                    bot.sendMainMenuKeyboard(userId);
                }
            }
            case SETTINGS_CALL_BACK -> {
                bot.sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
                bot.setUserStatus(userId, SETTINGS_WAITING);
            }
            default -> bot.sendMainMenuKeyboard(userId);
        }
    }
}
