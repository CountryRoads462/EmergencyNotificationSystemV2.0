package com.andrew.ens.user.application;

import com.andrew.ens.user.adapter.out.persistence.UsersRepository;
import com.andrew.ens.user.application.port.in.CreateUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Service
@RequiredArgsConstructor
public class UsersService implements
        CreateUserUseCase {

    @Autowired
    private final UsersRepository usersRepo;

    @Override
    public void createUser(Update update) {
        User user = update.getMessage().getFrom();

        usersRepo.createUser(
                user.getFirstName(),
                user.getLastName(),
                user.getId(),
                user.getUserName()
        );
    }

    public boolean userExistsById(Update update) {
        return usersRepo.existsById(update.getMessage().getFrom().getId());
    }
}
