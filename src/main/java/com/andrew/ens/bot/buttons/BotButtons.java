package com.andrew.ens.bot.buttons;

import com.andrew.ens.bot.buttons.CallBackData;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static com.andrew.ens.bot.buttons.CallBackData.CANCEL_CALL_BACK;
import static com.andrew.ens.bot.buttons.CallBackData.CONFIRM_CALL_BACK;
import static com.andrew.ens.bot.buttons.CallBackData.CREATE_TEMPLATE_CALL_BACK;

@Component
public class BotButtons {

    public static final InlineKeyboardButton SEND_EMERGENCY_MESSAGE
            = InlineKeyboardButton.builder()
            .text("Tap to send emergency message")
            .callbackData(CallBackData.SEND_EMERGENCY_MESSAGE_CALL_BACK)
            .build();

    public static final InlineKeyboardButton SETTING_BUTTON
            = InlineKeyboardButton.builder()
            .text("Settings")
            .callbackData(CallBackData.SETTINGS_CALL_BACK)
            .build();

    public static final InlineKeyboardButton CREATE_TEMPLATE_BUTTON
            = InlineKeyboardButton.builder()
            .text("Create template")
            .callbackData(CREATE_TEMPLATE_CALL_BACK)
            .build();

    public static final InlineKeyboardButton CANCEL_BUTTON
            = InlineKeyboardButton.builder()
            .text("Cancel")
            .callbackData(CANCEL_CALL_BACK)
            .build();

    public static final InlineKeyboardButton CONFIRM_BUTTON
            = InlineKeyboardButton.builder()
            .text("Confirm")
            .callbackData(CONFIRM_CALL_BACK)
            .build();

    public static final InlineKeyboardButton CHOOSE_TEMPLATE_BUTTON
            = InlineKeyboardButton.builder()
            .text("Choose template")
            .callbackData(CallBackData.CHOOSE_TEMPLATE_CALL_BACK)
            .build();

    public static final InlineKeyboardButton DELETE_TEMPLATE_BUTTON
            = InlineKeyboardButton.builder()
            .text("Delete template")
            .callbackData(CallBackData.DELETE_TEMPLATE_CALL_BACK)
            .build();

    public static final InlineKeyboardButton EDIT_TEMPLATE_BUTTON
            = InlineKeyboardButton.builder()
            .text("Edit template")
            .callbackData(CallBackData.EDIT_TEMPLATE_CALL_BACK)
            .build();

    public static final InlineKeyboardButton BACK_BUTTON
            = InlineKeyboardButton.builder()
            .text("Back")
            .callbackData(CallBackData.BACK_CALL_BACK)
            .build();

    public static final InlineKeyboardButton ADD_CONTACT_BUTTON
            = InlineKeyboardButton.builder()
            .text("Add contact")
            .callbackData(CallBackData.ADD_CONTACT_CALL_BACK)
            .build();

    public static final InlineKeyboardButton DELETE_CONTACT_BUTTON
            = InlineKeyboardButton.builder()
            .text("Delete contact")
            .callbackData(CallBackData.DELETE_CONTACT_CALL_BACK)
            .build();

    public static final InlineKeyboardButton DELETE_ALL_CONTACTS_BUTTON
            = InlineKeyboardButton.builder()
            .text("Delete all contacts")
            .callbackData(CallBackData.DELETE_ALL_CONTACTS_CALL_BACK)
            .build();

    public static final InlineKeyboardButton CHANGE_THE_TEMPLATE_NAME_BUTTON
            = InlineKeyboardButton.builder()
            .text("Change the template name")
            .callbackData(CallBackData.CHANGE_THE_TEMPLATE_NAME_CALL_BACK)
            .build();

    public static final InlineKeyboardButton CHANGE_THE_TEMPLATE_TEXT_BUTTON
            = InlineKeyboardButton.builder()
            .text("Change the template text")
            .callbackData(CallBackData.CHANGE_THE_TEMPLATE_TEXT_CALL_BACK)
            .build();
}
