package com.andrew.ens.current_creating.application;

import com.andrew.ens.current_creating.adapter.out.persistence.CurrentCreating;
import com.andrew.ens.current_creating.adapter.out.persistence.CurrentCreatingRepository;
import com.andrew.ens.current_creating.application.port.in.CreateCurrentCreatingUseCase;
import com.andrew.ens.current_creating.application.port.in.GetContactIdByUserIdUseCase;
import com.andrew.ens.current_creating.application.port.in.GetTemplateIdByUserIdUseCase;
import com.andrew.ens.current_creating.application.port.in.SetContactIdByUserIdUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentCreatingService implements
        CreateCurrentCreatingUseCase,
        GetTemplateIdByUserIdUseCase,
        SetContactIdByUserIdUseCase,
        GetContactIdByUserIdUseCase {

    private final CurrentCreatingRepository currentCreatingRepo;

    @Override
    public void createCurrentCreating(CurrentCreating currentCreating) {
        currentCreatingRepo.save(currentCreating);
    }

    @Override
    public int getTemplateIdByUserId(long userId) {
        return currentCreatingRepo.getTemplateIdByUserId(userId);
    }

    @Override
    public void setContactIdByUserId(long userId, int contactId) {
        currentCreatingRepo.setContactIdByUserId(userId, contactId);
    }

    @Override
    public int getContactIdByUserId(long userId) {
        return currentCreatingRepo.getContactIdByUserId(userId);
    }
}
