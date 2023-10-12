package com.andrew.ens.user.application.port.in;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CreateUserUseCase {
    void createUser(Update update);
}
