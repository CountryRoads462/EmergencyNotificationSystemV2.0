package com.andrew.ens.user.application;

import com.andrew.ens.user.adapter.out.persistence.UsersRepository;
import com.andrew.ens.user.application.port.in.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsersService implements
        CreateUserUseCase,
        SetChosenTemplateUseCase,
        GetChosenTemplateIdUseCase {

    @Autowired
    private final UsersRepository usersRepo;

    @Override
    public void createUser(Update update) {
        User user;
        if (update.hasMessage()) {
            user = update.getMessage().getFrom();
        } else {
            user = update.getCallbackQuery().getFrom();
        }

        usersRepo.createUser(
                user.getFirstName(),
                user.getLastName(),
                user.getId(),
                user.getUserName()
        );
    }

    @Override
    public void setChosenTemplate(long userId, int templateId) {
        usersRepo.setChosenTemplate(userId, templateId);
    }

    @Override
    public Optional<Integer> getChosenTemplateId(long userId) {
        return usersRepo.getChosenTemplateId(userId);
    }
}
