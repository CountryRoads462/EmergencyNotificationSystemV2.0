package com.andrew.ens.bot.buttons;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class CreateTemplateButton extends InlineKeyboardButton {
    public static final CreateTemplateButton
            CREATE_TEMPLATE_BUTTON = (CreateTemplateButton) InlineKeyboardButton.builder()
            .text("Create")
            .callbackData("create.template")
            .build();
}
