package com.andrew.ens.bot.adapter.in;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@FunctionalInterface
public interface HandlerAction {
    void execAction(BotController bot, Long userId, String text) throws TelegramApiException;
}
