package com.andrew.ens.bot.adapter.in.handlers;

import com.andrew.ens.bot.adapter.in.BotController;
import com.andrew.ens.bot.adapter.in.BotHandler;
import com.andrew.ens.status.domain.Status;
import com.andrew.ens.template.application.port.in.GetInfoTemplateExistsByNameAndOwnerIdUseCase;
import com.andrew.ens.template.application.port.in.SetTemplateNameByTemplateIdUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.andrew.ens.bot.adapter.in.BotKeyboards.CANCEL_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotKeyboards.EDIT_TEMPLATES_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotMessages.TEMPLATE_CREATE_NAME_MESSAGE;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.CANCEL_CALL_BACK;
import static com.andrew.ens.status.domain.Status.EDIT_TEMPLATES_WAITING;

@Component
@RequiredArgsConstructor
public class EditTemplatesChangeTheTemplateNameHandler implements BotHandler {

    private final GetInfoTemplateExistsByNameAndOwnerIdUseCase
            getInfoTemplateExistsByNameAndOwnerIdUseCase;
    private final SetTemplateNameByTemplateIdUseCase setTemplateNameByTemplateIdUseCase;

    @Override
    public Status getStatus() {
        return Status.EDIT_TEMPLATES_CHANGE_THE_TEMPLATE_NAME_WAITING;
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

        if (getInfoTemplateExistsByNameAndOwnerIdUseCase
                .getInfoTemplateExistsByNameAndOwnerId(userId, text)) {
            bot.sendText(userId, "You already have a template with this name");
            return;
        }

        if (text.matches(".{1,16}")) {
            int templateId = bot.getUserStates().get(userId).getTemplateCreationId();

            setTemplateNameByTemplateIdUseCase
                    .setTemplateNameByTemplateId(templateId, text);

            bot.sendText(userId, "Name changed successfully");
            bot.sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
            bot.setUserStatus(userId, EDIT_TEMPLATES_WAITING);

        } else {
            bot.sendKeyboard(userId, TEMPLATE_CREATE_NAME_MESSAGE, CANCEL_KEYBOARD);
        }
    }
}
