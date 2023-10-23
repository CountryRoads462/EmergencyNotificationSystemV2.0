package com.andrew.ens.bot.adapter.in.handlers;

import com.andrew.ens.bot.adapter.in.BotController;
import com.andrew.ens.bot.adapter.in.BotHandler;
import com.andrew.ens.status.domain.Status;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.andrew.ens.bot.adapter.in.BotKeyboards.CONFIRM_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotKeyboards.EDIT_TEMPLATES_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.BACK_CALL_BACK;
import static com.andrew.ens.status.domain.Status.EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_TEMPLATE_CONFIRM_WAITING;
import static com.andrew.ens.status.domain.Status.EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_TEMPLATE_WAITING;
import static com.andrew.ens.status.domain.Status.EDIT_TEMPLATES_WAITING;

@Component
public class EditTemplatesDeleteContactChoseTemplateHandler implements BotHandler {

    @Override
    public Status getStatus() {
        return Status.EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_TEMPLATE_WAITING;
    }

    @Override
    public void execAction(
            Update update,
            BotController bot,
            Long userId,
            String text
    ) throws TelegramApiException {
        if (text.equals(BACK_CALL_BACK)) {
            bot.sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
            bot.setUserStatus(userId, EDIT_TEMPLATES_WAITING);
            return;
        }

        if (update.hasCallbackQuery()) {
            bot.getUserStates().get(userId).setContactCreationId(Integer.parseInt(text));

            bot.sendKeyboard(userId, "Are you sure?", CONFIRM_KEYBOARD);
            bot.setUserStatus(
                    userId,
                    EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_TEMPLATE_CONFIRM_WAITING
            );

        } else {
            bot.sendKeyboard(
                    userId,
                    "Select the one you want to delete",
                    bot.getAllContactsKeyboard(
                            bot.getUserStates().get(userId).getTemplateCreationId()
                    )
            );
            bot.setUserStatus(
                    userId,
                    EDIT_TEMPLATES_DELETE_CONTACT_CHOSE_TEMPLATE_WAITING
            );
        }
    }
}
