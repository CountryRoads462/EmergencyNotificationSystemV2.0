package com.andrew.ens.bot.adapter.out;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(value = "bot")
public class BotCreds {
    String token;
    String username;
}
