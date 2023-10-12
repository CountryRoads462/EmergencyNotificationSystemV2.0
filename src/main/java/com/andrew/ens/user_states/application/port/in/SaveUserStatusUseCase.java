package com.andrew.ens.user_states.application.port.in;

import com.andrew.ens.user_states.domain.Status;

public interface SaveUserStatusUseCase {
    void saveUserStatus(long userId, Status status);
}
