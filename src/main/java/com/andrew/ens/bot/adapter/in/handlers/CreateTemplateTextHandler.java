package com.andrew.ens.bot.adapter.in.handlers;

import com.andrew.ens.bot.adapter.in.BotController;
import com.andrew.ens.bot.adapter.in.BotHandler;
import com.andrew.ens.status.domain.Status;
import com.andrew.ens.template.application.port.in.DeleteTemplateByIdUseCase;
import com.andrew.ens.template.application.port.in.SetTemplateTextUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.andrew.ens.bot.adapter.in.BotKeyboards.SETTINGS_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotMessages.CREATE_TEMPLATE_CREATED_MESSAGE;
import static com.andrew.ens.bot.adapter.in.BotMessages.SETTINGS_KEYBOARD_TEXT;
import static com.andrew.ens.bot.adapter.in.BotMessages.TEMPLATE_CREATE_TEXT_MESSAGE;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.CANCEL_CALL_BACK;
import static com.andrew.ens.status.domain.Status.SETTINGS_WAITING;

@Component
@RequiredArgsConstructor
public class CreateTemplateTextHandler implements BotHandler {

    private final DeleteTemplateByIdUseCase deleteTemplateByIdUseCase;
    private final SetTemplateTextUseCase setTemplateTextUseCase;

    @Override
    public Status getStatus() {
        return Status.CREATE_TEMPLATE_TEXT_WAITING;
    }

    @Override
    public void execAction(
            Update update,
            BotController bot,
            Long userId,
            String text
    ) throws TelegramApiException {
        if (text.equals(CANCEL_CALL_BACK)) {
            deleteTemplateByIdUseCase.deleteTemplateById(
                    bot.getUserStates().get(userId).getTemplateCreationId()
            );

            bot.sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
            bot.setUserStatus(userId, SETTINGS_WAITING);
            return;
        }

        if (text.matches(".{1,128}")) {
            setTemplateTextUseCase.setTemplateText(
                    bot.getUserStates().get(userId).getTemplateCreationId(),
                    text
            );

            bot.sendText(userId, CREATE_TEMPLATE_CREATED_MESSAGE);
            bot.sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
            bot.setUserStatus(userId, SETTINGS_WAITING);

        } else {
            bot.sendText(userId, TEMPLATE_CREATE_TEXT_MESSAGE);
        }
    }
}
