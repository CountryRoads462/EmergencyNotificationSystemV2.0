package com.andrew.ens.bot.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static com.andrew.ens.bot.BotButtons.CREATE_TEMPLATE_BUTTON;

public class AddMoreContactKeyboard extends InlineKeyboardMarkup {

    public static final AddMoreContactKeyboard
            ADD_MORE_CONTACT_KEYBOARD = (AddMoreContactKeyboard) InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(CREATE_TEMPLATE_BUTTON))
            .build();
}
