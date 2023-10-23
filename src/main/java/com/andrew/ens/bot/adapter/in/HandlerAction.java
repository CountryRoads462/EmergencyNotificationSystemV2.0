package com.andrew.ens.bot.adapter.in;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@FunctionalInterface
public interface HandlerAction {
    void execAction(
            Update update,
            BotController bot,
            Long userId,
            String text
    ) throws TelegramApiException;
}
