package com.andrew.ens.bot.adapter.in.handlers;

import com.andrew.ens.bot.adapter.in.BotController;
import com.andrew.ens.bot.adapter.in.BotHandler;
import com.andrew.ens.status.domain.Status;
import com.andrew.ens.template.application.port.in.SetTemplateTextUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.andrew.ens.bot.adapter.in.BotKeyboards.CANCEL_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotKeyboards.EDIT_TEMPLATES_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotMessages.TEMPLATE_CREATE_TEXT_MESSAGE;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.CANCEL_CALL_BACK;
import static com.andrew.ens.status.domain.Status.EDIT_TEMPLATES_WAITING;

@Component
@RequiredArgsConstructor
public class EditTemplatesChangeTheTemplateTextHandler implements BotHandler {

    private final SetTemplateTextUseCase setTemplateTextUseCase;

    @Override
    public Status getStatus() {
        return Status.EDIT_TEMPLATES_CHANGE_THE_TEMPLATE_TEXT_WAITING;
    }

    @Override
    public void execAction(
            Update update,
            BotController bot,
            Long userId,
            String text
    ) throws TelegramApiException {
        if (text.equals(CANCEL_CALL_BACK)) {
            bot.sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
            bot.setUserStatus(userId, EDIT_TEMPLATES_WAITING);
            return;
        }

        if (text.matches(".{1,128}")) {
            int templateId = bot.getUserStates().get(userId).getTemplateCreationId();

            setTemplateTextUseCase
                    .setTemplateText(templateId, text);

            bot.sendText(userId, "Text changed successfully");
            bot.sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
            bot.setUserStatus(userId, EDIT_TEMPLATES_WAITING);

        } else {
            bot.sendKeyboard(userId, TEMPLATE_CREATE_TEXT_MESSAGE, CANCEL_KEYBOARD);
        }
    }
}
