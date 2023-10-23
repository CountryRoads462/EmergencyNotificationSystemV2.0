package com.andrew.ens.bot.adapter.in.handlers;

import com.andrew.ens.bot.adapter.in.BotController;
import com.andrew.ens.bot.adapter.in.BotHandler;
import com.andrew.ens.status.domain.Status;
import com.andrew.ens.template.application.port.in.DeleteTemplateByIdUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.andrew.ens.bot.adapter.in.BotKeyboards.CONFIRM_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotKeyboards.SETTINGS_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotMessages.SETTINGS_KEYBOARD_TEXT;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.CANCEL_CALL_BACK;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.CONFIRM_CALL_BACK;
import static com.andrew.ens.status.domain.Status.SETTINGS_DELETE_TEMPLATE_CONFIRM_WAITING;
import static com.andrew.ens.status.domain.Status.SETTINGS_WAITING;

@Component
@RequiredArgsConstructor
public class SettingsDeleteTemplateConfirmHandler implements BotHandler {

    private final DeleteTemplateByIdUseCase deleteTemplateByIdUseCase;

    @Override
    public Status getStatus() {
        return Status.SETTINGS_DELETE_TEMPLATE_CONFIRM_WAITING;
    }

    @Override
    public void execAction(
            Update update,
            BotController bot,
            Long userId,
            String text
    ) throws TelegramApiException {
        switch (text) {
            case CONFIRM_CALL_BACK -> {
                deleteTemplateByIdUseCase.deleteTemplateById(
                        bot.getUserStates().get(userId).getTemplateCreationId()
                );

                bot.sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
                bot.setUserStatus(userId, SETTINGS_WAITING);
            }
            case CANCEL_CALL_BACK -> {
                bot.sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
                bot.setUserStatus(userId, SETTINGS_WAITING);
            }
            default -> {
                bot.sendKeyboard(userId, "Are you sure?", CONFIRM_KEYBOARD);
                bot.setUserStatus(userId, SETTINGS_DELETE_TEMPLATE_CONFIRM_WAITING);
            }
        }
    }
}
