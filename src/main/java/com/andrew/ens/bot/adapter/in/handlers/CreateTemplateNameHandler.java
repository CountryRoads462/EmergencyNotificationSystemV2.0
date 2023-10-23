package com.andrew.ens.bot.adapter.in.handlers;

import com.andrew.ens.bot.adapter.in.BotController;
import com.andrew.ens.bot.adapter.in.BotHandler;
import com.andrew.ens.status.domain.Status;
import com.andrew.ens.template.application.port.in.CreateIncompleteTemplateUseCase;
import com.andrew.ens.template.application.port.in.GetInfoTemplateExistsByNameAndOwnerIdUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.andrew.ens.bot.adapter.in.BotKeyboards.CANCEL_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotKeyboards.SETTINGS_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotMessages.SETTINGS_KEYBOARD_TEXT;
import static com.andrew.ens.bot.adapter.in.BotMessages.TEMPLATE_CREATE_NAME_MESSAGE;
import static com.andrew.ens.bot.adapter.in.BotMessages.TEMPLATE_CREATE_TEXT_MESSAGE;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.CANCEL_CALL_BACK;
import static com.andrew.ens.status.domain.Status.CREATE_TEMPLATE_TEXT_WAITING;
import static com.andrew.ens.status.domain.Status.SETTINGS_WAITING;

@Component
@RequiredArgsConstructor
public class CreateTemplateNameHandler implements BotHandler {

    private final GetInfoTemplateExistsByNameAndOwnerIdUseCase
            getInfoTemplateExistsByNameAndOwnerIdUseCase;
    private final CreateIncompleteTemplateUseCase createIncompleteTemplateUseCase;

    @Override
    public Status getStatus() {
        return Status.CREATE_TEMPLATE_NAME_WAITING;
    }

    @Override
    public void execAction(
            Update update,
            BotController bot,
            Long userId,
            String text
    ) throws TelegramApiException {
        if (text.equals(CANCEL_CALL_BACK)) {
            bot.sendKeyboard(userId, SETTINGS_KEYBOARD_TEXT, SETTINGS_KEYBOARD);
            bot.setUserStatus(userId, SETTINGS_WAITING);
            return;
        }

        if (getInfoTemplateExistsByNameAndOwnerIdUseCase
                .getInfoTemplateExistsByNameAndOwnerId(userId, text)) {
            bot.sendText(userId, "You already have a template with this name");
            return;
        }

        if (text.matches(".{1,16}")) {
            int templateId = createIncompleteTemplateUseCase
                    .createIncompleteTemplate(text, userId);
            bot.setUserTemplateCreationId(userId, templateId);


            bot.sendKeyboard(userId, TEMPLATE_CREATE_TEXT_MESSAGE, CANCEL_KEYBOARD);
            bot.setUserStatus(userId, CREATE_TEMPLATE_TEXT_WAITING);

        } else {
            bot.sendKeyboard(userId, TEMPLATE_CREATE_NAME_MESSAGE, CANCEL_KEYBOARD);
        }
    }
}
