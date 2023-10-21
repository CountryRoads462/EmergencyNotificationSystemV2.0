package com.andrew.ens.bot.adapter.out;

import com.andrew.ens.bot.adapter.out.buttons.BotButtons;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static com.andrew.ens.bot.adapter.out.buttons.BotButtons.ADD_CONTACT_BUTTON;
import static com.andrew.ens.bot.adapter.out.buttons.BotButtons.BACK_BUTTON;
import static com.andrew.ens.bot.adapter.out.buttons.BotButtons.CANCEL_BUTTON;
import static com.andrew.ens.bot.adapter.out.buttons.BotButtons.CHANGE_THE_TEMPLATE_NAME_BUTTON;
import static com.andrew.ens.bot.adapter.out.buttons.BotButtons.CHANGE_THE_TEMPLATE_TEXT_BUTTON;
import static com.andrew.ens.bot.adapter.out.buttons.BotButtons.CHOOSE_TEMPLATE_BUTTON;
import static com.andrew.ens.bot.adapter.out.buttons.BotButtons.CONFIRM_BUTTON;
import static com.andrew.ens.bot.adapter.out.buttons.BotButtons.CREATE_TEMPLATE_BUTTON;
import static com.andrew.ens.bot.adapter.out.buttons.BotButtons.DELETE_ALL_CONTACTS_BUTTON;
import static com.andrew.ens.bot.adapter.out.buttons.BotButtons.DELETE_CONTACT_BUTTON;
import static com.andrew.ens.bot.adapter.out.buttons.BotButtons.DELETE_TEMPLATE_BUTTON;
import static com.andrew.ens.bot.adapter.out.buttons.BotButtons.EDIT_TEMPLATE_BUTTON;

public class BotKeyboards {

    public static final InlineKeyboardMarkup CANCEL_KEYBOARD
            = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(CANCEL_BUTTON))
            .build();

    public static final InlineKeyboardMarkup CONFIRM_KEYBOARD
            = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(CONFIRM_BUTTON))
            .keyboardRow(List.of(CANCEL_BUTTON))
            .build();

    public static final InlineKeyboardMarkup MAIN_MENU_KEYBOARD
            = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(BotButtons.SEND_EMERGENCY_MESSAGE))
            .keyboardRow(List.of(BotButtons.SETTING_BUTTON))
            .build();

    public static final InlineKeyboardMarkup SETTINGS_KEYBOARD
            = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(CREATE_TEMPLATE_BUTTON))
            .keyboardRow(List.of(CHOOSE_TEMPLATE_BUTTON))
            .keyboardRow(List.of(DELETE_TEMPLATE_BUTTON))
            .keyboardRow(List.of(EDIT_TEMPLATE_BUTTON))
            .keyboardRow(List.of(BACK_BUTTON))
            .build();

    public static final InlineKeyboardMarkup EDIT_TEMPLATES_KEYBOARD
            = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(ADD_CONTACT_BUTTON))
            .keyboardRow(List.of(DELETE_CONTACT_BUTTON))
            .keyboardRow(List.of(DELETE_ALL_CONTACTS_BUTTON))
            .keyboardRow(List.of(CHANGE_THE_TEMPLATE_NAME_BUTTON))
            .keyboardRow(List.of(CHANGE_THE_TEMPLATE_TEXT_BUTTON))
            .keyboardRow(List.of(BACK_BUTTON))
            .build();
}
