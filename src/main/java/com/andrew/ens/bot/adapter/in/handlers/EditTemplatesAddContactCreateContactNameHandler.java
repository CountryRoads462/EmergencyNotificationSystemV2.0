package com.andrew.ens.bot.adapter.in.handlers;

import com.andrew.ens.bot.adapter.in.BotController;
import com.andrew.ens.bot.adapter.in.BotHandler;
import com.andrew.ens.contact.application.port.in.CreateIncompleteContactUseCase;
import com.andrew.ens.contact.application.port.in.GetInfoContactExistsByNameAndTemplateIdUseCase;
import com.andrew.ens.status.domain.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.andrew.ens.bot.adapter.in.BotKeyboards.CANCEL_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotKeyboards.EDIT_TEMPLATES_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotMessages.CREATE_CONTACT_EMAIL_MESSAGE;
import static com.andrew.ens.bot.adapter.in.BotMessages.CREATE_CONTACT_NAME_MESSAGE;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.CANCEL_CALL_BACK;
import static com.andrew.ens.status.domain.Status.EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_EMAIL_WAITING;
import static com.andrew.ens.status.domain.Status.EDIT_TEMPLATES_WAITING;

@Component
@RequiredArgsConstructor
public class EditTemplatesAddContactCreateContactNameHandler implements BotHandler {

    private final GetInfoContactExistsByNameAndTemplateIdUseCase
            getInfoContactExistsByNameAndTemplateIdUseCase;
    private final CreateIncompleteContactUseCase createIncompleteContactUseCase;

    @Override
    public Status getStatus() {
        return Status.EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_NAME_WAITING;
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

        if (getInfoContactExistsByNameAndTemplateIdUseCase
                .getInfoContactExistsByNameAndTemplateId(
                        text,
                        bot.getUserStates().get(userId).getTemplateCreationId()
                )) {
            bot.sendText(userId, "You already have a contact with this name");
            return;
        }

        if (text.matches(".{1,16}")) {
            int contactId = createIncompleteContactUseCase
                    .createIncompleteContact(text);
            bot.setUserContactCreationId(userId, contactId);

            bot.sendKeyboard(userId, CREATE_CONTACT_EMAIL_MESSAGE, CANCEL_KEYBOARD);
            bot.setUserStatus(
                    userId,
                    EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_EMAIL_WAITING
            );

        } else {
            bot.sendKeyboard(userId, CREATE_CONTACT_NAME_MESSAGE, CANCEL_KEYBOARD);
        }
    }
}
