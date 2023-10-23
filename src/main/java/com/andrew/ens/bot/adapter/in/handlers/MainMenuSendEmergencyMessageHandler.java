package com.andrew.ens.bot.adapter.in.handlers;

import com.andrew.ens.bot.adapter.in.BotController;
import com.andrew.ens.bot.adapter.in.BotHandler;
import com.andrew.ens.contact.adapter.out.persistence.Contact;
import com.andrew.ens.contact.application.port.in.GetAllContactsByTemplateIdUseCase;
import com.andrew.ens.google_smtp.application.port.in.SendEmailUseCase;
import com.andrew.ens.status.domain.Status;
import com.andrew.ens.template.adapter.out.persistence.Template;
import com.andrew.ens.template.application.port.in.GetTemplateByIdUseCase;
import com.andrew.ens.user.application.port.in.GetChosenTemplateIdUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

import static com.andrew.ens.bot.adapter.in.BotKeyboards.CONFIRM_KEYBOARD;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.CANCEL_CALL_BACK;
import static com.andrew.ens.bot.adapter.in.buttons.CallBackData.CONFIRM_CALL_BACK;
import static com.andrew.ens.status.domain.Status.MAIN_MENU_SEND_EMERGENCY_MESSAGE_WAITING;
import static com.andrew.ens.status.domain.Status.MAIN_MENU_WAITING;

@Component
@RequiredArgsConstructor
public class MainMenuSendEmergencyMessageHandler implements BotHandler {

    private final GetChosenTemplateIdUseCase getChosenTemplateIdUseCase;
    private final GetTemplateByIdUseCase getTemplateByIdUseCase;
    private final GetAllContactsByTemplateIdUseCase getAllContactsByTemplateIdUseCase;
    private final SendEmailUseCase sendEmailUseCase;

    @Override
    public Status getStatus() {
        return Status.MAIN_MENU_SEND_EMERGENCY_MESSAGE_WAITING;
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
                Optional<Integer> templateIdOptional = getChosenTemplateIdUseCase
                        .getChosenTemplateId(userId);

                if (templateIdOptional.isPresent()) {
                    Optional<Template> templateOptional = getTemplateByIdUseCase
                            .getTemplateById(templateIdOptional.get());

                    if (templateOptional.isPresent()) {
                        Template template = templateOptional.get();

                        String templateName = template.getName();
                        String templateText = template.getText();

                        getAllContactsByTemplateIdUseCase
                                .getAllContactsByTemplateId(template.getId())
                                .stream()
                                .map(Contact::getEmail)
                                .forEach(email -> {
                                    sendEmailUseCase.sendEmail(
                                            templateName,
                                            templateText,
                                            email
                                    );
                                });

                        bot.sendText(userId, "Emails have been sent successfully");
                    }
                }

                bot.sendMainMenuKeyboard(userId);
                bot.setUserStatus(userId, MAIN_MENU_WAITING);
            }
            case CANCEL_CALL_BACK -> {
                bot.sendMainMenuKeyboard(userId);
                bot.setUserStatus(userId, MAIN_MENU_WAITING);
            }
            default -> {
                bot.sendKeyboard(userId, "Are you sure?", CONFIRM_KEYBOARD);
                bot.setUserStatus(userId, MAIN_MENU_SEND_EMERGENCY_MESSAGE_WAITING);
            }
        }
    }
}
