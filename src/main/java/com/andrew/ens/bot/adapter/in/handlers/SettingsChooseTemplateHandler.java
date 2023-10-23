package com.andrew.ens.bot.adapter.in.handlers;

import com.andrew.ens.bot.adapter.in.BotController;
import com.andrew.ens.bot.adapter.in.BotHandler;
import com.andrew.ens.status.domain.Status;
import com.andrew.ens.user.application.port.in.SetChosenTemplateUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.andrew.ens.bot.adapter.in.BotKeyboards.SETTINGS_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotMessages.SETTINGS_KEYBOARD_TEXT;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.BACK_CALL_BACK;
import static com.andrew.ens.status.domain.Status.MAIN_MENU_WAITING;
import static com.andrew.ens.status.domain.Status.SETTINGS_WAITING;

@Component
@RequiredArgsConstructor
public class SettingsChooseTemplateHandler implements BotHandler {

    private final SetChosenTemplateUseCase setChosenTemplateUseCase;

    @Override
    public Status getStatus() {
        return Status.SETTINGS_CHOOSE_TEMPLATE_WAITING;
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

        setChosenTemplateUseCase.setChosenTemplate(userId, Integer.parseInt(text));

        bot.sendMainMenuKeyboard(userId);
        bot.setUserStatus(userId, MAIN_MENU_WAITING);
    }
}
