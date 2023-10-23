package com.andrew.ens.bot.adapter.in.handlers;

import com.andrew.ens.bot.adapter.in.BotController;
import com.andrew.ens.bot.adapter.in.BotHandler;
import com.andrew.ens.contact.application.port.in.DeleteContactByIdUseCase;
import com.andrew.ens.contact.application.port.in.GetInfoContactExistsByPhoneNumberAndTemplateIdUseCase;
import com.andrew.ens.contact.application.port.in.SetContactPhoneNumberUseCase;
import com.andrew.ens.contact.application.port.in.SetContactTemplateIdUseCase;
import com.andrew.ens.status.domain.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.andrew.ens.bot.adapter.in.BotKeyboards.CANCEL_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotKeyboards.EDIT_TEMPLATES_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotMessages.CREATE_CONTACT_PHONE_NUMBER_MESSAGE;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.CANCEL_CALL_BACK;
import static com.andrew.ens.status.domain.Status.EDIT_TEMPLATES_WAITING;

@Component
@RequiredArgsConstructor
public class EditTemplatesAddContactCreateContactPhoneNumberHandler implements BotHandler {

    private final DeleteContactByIdUseCase deleteContactByIdUseCase;
    private final GetInfoContactExistsByPhoneNumberAndTemplateIdUseCase
            getInfoContactExistsByPhoneNumberAndTemplateIdUseCase;
    private final SetContactPhoneNumberUseCase setContactPhoneNumberUseCase;
    private final SetContactTemplateIdUseCase setContactTemplateIdUseCase;

    @Override
    public Status getStatus() {
        return Status.EDIT_TEMPLATES_ADD_CONTACT_CREATE_CONTACT_PHONE_NUMBER_WAITING;
    }

    @Override
    public void execAction(
            Update update,
            BotController bot,
            Long userId,
            String text
    ) throws TelegramApiException {
        int contactId = bot.getUserStates().get(userId).getContactCreationId();
        int templateId = bot.getUserStates().get(userId).getTemplateCreationId();

        if (text.equals(CANCEL_CALL_BACK)) {
            deleteContactByIdUseCase.deleteContactById(contactId);

            bot.sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
            bot.setUserStatus(userId, EDIT_TEMPLATES_WAITING);
            return;
        }

        if (getInfoContactExistsByPhoneNumberAndTemplateIdUseCase
                .getInfoContactExistsByPhoneNumberAndTemplateId(
                        text,
                        templateId
                )) {
            bot.sendText(userId, "You already have a contact with this phone number");
            return;
        }

        if (text.matches("[0-9-+ ]+")) {
            setContactPhoneNumberUseCase
                    .setContactPhoneNumber(contactId, text);

            setContactTemplateIdUseCase
                    .setContactTemplateId(contactId, templateId);

            bot.sendText(userId, "The contact was successfully created");
            bot.sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
            bot.setUserStatus(userId, EDIT_TEMPLATES_WAITING);

        } else {
            bot.sendKeyboard(
                    userId,
                    CREATE_CONTACT_PHONE_NUMBER_MESSAGE,
                    CANCEL_KEYBOARD
            );
        }
    }
}
