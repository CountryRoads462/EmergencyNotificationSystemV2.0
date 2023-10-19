package com.andrew.ens.bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static com.andrew.ens.bot.BotButtons.*;

public class BotKeyboards {

    public final static InlineKeyboardMarkup CANCEL_KEYBOARD
            = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(CANCEL_BUTTON))
            .build();

    public final static InlineKeyboardMarkup CONFIRM_KEYBOARD
            = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(CONFIRM_BUTTON))
            .keyboardRow(List.of(CANCEL_BUTTON))
            .build();

    public final static InlineKeyboardMarkup MAIN_MENU_KEYBOARD
            = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(BotButtons.SEND_EMERGENCY_MESSAGE))
            .keyboardRow(List.of(BotButtons.SETTING_BUTTON))
            .build();

    public final static InlineKeyboardMarkup SETTINGS_KEYBOARD
            = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(CREATE_TEMPLATE_BUTTON))
            .keyboardRow(List.of(CHOOSE_TEMPLATE_BUTTON))
            .keyboardRow(List.of(DELETE_TEMPLATE_BUTTON))
            .keyboardRow(List.of(EDIT_TEMPLATE_BUTTON))
            .keyboardRow(List.of(BACK_BUTTON))
            .build();

    public final static InlineKeyboardMarkup EDIT_TEMPLATES_KEYBOARD
            = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(ADD_CONTACT_BUTTON))
            .keyboardRow(List.of(DELETE_CONTACT_BUTTON))
            .keyboardRow(List.of(DELETE_ALL_CONTACTS_BUTTON))
            .keyboardRow(List.of(CHANGE_THE_TEMPLATE_NAME_BUTTON))
            .keyboardRow(List.of(BACK_BUTTON))
            .build();
}
