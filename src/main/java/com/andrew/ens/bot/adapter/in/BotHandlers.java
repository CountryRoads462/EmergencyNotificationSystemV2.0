package com.andrew.ens.bot.adapter.in;

import com.andrew.ens.status.domain.Status;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class BotHandlers {
    public static BotHandler create(Status status, HandlerAction handlerAction) {
        return new BotHandler() {
            @Override
            public Status getStatus() {
                return status;
            }

            @Override
            public void execAction(
                    Update update,
                    BotController bot,
                    Long userId,
                    String text
            ) throws TelegramApiException {
                handlerAction.execAction(update, bot, userId, text);
            }
        };
    }
}
