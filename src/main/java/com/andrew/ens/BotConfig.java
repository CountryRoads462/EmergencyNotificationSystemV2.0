package com.andrew.ens;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Configuration
@RequiredArgsConstructor
public class BotConfig {

    private final Environment env;

    @Bean
    BotRegistration botRegistration() throws TelegramApiException {
        return new BotRegistration(
                env.getProperty("app.telegram.token"),
                env.getProperty("app.telegram.username")
        );
    }
}
