package com.andrew.ens.user_states.application;

import com.andrew.ens.user_states.adapter.out.persistence.UserStatesRepository;
import com.andrew.ens.user_states.adapter.out.persistence.UserStatus;
import com.andrew.ens.user_states.application.port.in.GetUserStatesUseCase;
import com.andrew.ens.user_states.application.port.in.SaveUserStatusUseCase;
import com.andrew.ens.user_states.domain.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserStatesService implements
        SaveUserStatusUseCase,
        GetUserStatesUseCase {

    private final UserStatesRepository userStatesRepo;

    @Override
    public void saveUserStatus(long userId, Status status) {
        userStatesRepo.save(new UserStatus(userId, status));
    }

    @Override
    public ConcurrentHashMap<Long, Status> getUserStates() {
        ConcurrentHashMap<Long, Status> userStates = new ConcurrentHashMap<>();
        for (UserStatus userStatus :
                userStatesRepo.findAll()) {
            userStates.put(
                    userStatus.getUserId(),
                    userStatus.getStatus()
            );
        }
        return userStates;
    }
}
