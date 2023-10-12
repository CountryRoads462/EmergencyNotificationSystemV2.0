package com.andrew.ens.user_status.application.port.in;

import com.andrew.ens.user_status.domain.Status;

public interface SaveUserStatusUseCase {
    void saveUserStatus(long userId, Status status);
}
