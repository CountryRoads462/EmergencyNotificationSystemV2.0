package com.andrew.ens.bot.adapter.in;

import com.andrew.ens.status.domain.Status;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface BotHandler {
    Status getStatus();

    void execAction(
            Update update,
            BotController bot,
            Long userId,
            String text
    ) throws TelegramApiException;

}
