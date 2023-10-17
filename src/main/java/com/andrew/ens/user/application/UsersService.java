package com.andrew.ens.user.application;

import com.andrew.ens.user.adapter.out.persistence.UsersRepository;
import com.andrew.ens.user.application.port.in.CreateUserUseCase;
import com.andrew.ens.user.application.port.in.GetInfoUserHasAnyTemplatesUseCase;
import com.andrew.ens.user.application.port.in.GetInfoUserHasChosenTemplateUseCase;
import com.andrew.ens.user.application.port.in.SetChosenTemplateUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Service
@RequiredArgsConstructor
public class UsersService implements
        CreateUserUseCase,
        GetInfoUserHasAnyTemplatesUseCase,
        GetInfoUserHasChosenTemplateUseCase,
        SetChosenTemplateUseCase {

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
    public boolean userHasAnyTemplates(long userId) {
        return usersRepo.getNumberOfTemplatesById(userId) != 0;
    }

    @Override
    public boolean userHasChosenTemplate(long userId) {
        return usersRepo.userHasChosenTemplate(userId) != null;
    }

    @Override
    public void setChosenTemplate(long userId, int templateId) {
        usersRepo.setChosenTemplate(userId, templateId);
    }
}
