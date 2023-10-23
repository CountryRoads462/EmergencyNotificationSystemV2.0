package com.andrew.ens.bot.adapter.in.handlers;

import com.andrew.ens.bot.adapter.in.BotController;
import com.andrew.ens.bot.adapter.in.BotHandler;
import com.andrew.ens.contact.application.port.in.DeleteAllContactsUseCase;
import com.andrew.ens.status.domain.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.andrew.ens.bot.adapter.in.BotKeyboards.CONFIRM_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.BotKeyboards.EDIT_TEMPLATES_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.CANCEL_CALL_BACK;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.CONFIRM_CALL_BACK;
import static com.andrew.ens.status.domain.Status.EDIT_TEMPLATES_DELETE_ALL_CONTACTS_CONFIRM_WAITING;
import static com.andrew.ens.status.domain.Status.EDIT_TEMPLATES_WAITING;

@Component
@RequiredArgsConstructor
public class EditTemplatesDeleteAllContactsConfirmHandler implements BotHandler {

    private final DeleteAllContactsUseCase deleteAllContactsUseCase;

    @Override
    public Status getStatus() {
        return Status.EDIT_TEMPLATES_DELETE_ALL_CONTACTS_CONFIRM_WAITING;
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
                deleteAllContactsUseCase.deleteAllContacts();

                bot.sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
                bot.setUserStatus(userId, EDIT_TEMPLATES_WAITING);
            }
            case CANCEL_CALL_BACK -> {
                bot.sendKeyboard(userId, "Edit template", EDIT_TEMPLATES_KEYBOARD);
                bot.setUserStatus(userId, EDIT_TEMPLATES_WAITING);
            }
            default -> {
                bot.sendKeyboard(userId, "Are you sure?", CONFIRM_KEYBOARD);
                bot.setUserStatus(userId, EDIT_TEMPLATES_DELETE_ALL_CONTACTS_CONFIRM_WAITING);
            }
        }
    }
}
