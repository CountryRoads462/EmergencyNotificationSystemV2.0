package com.andrew.ens.user_status.application.port.in;

import com.andrew.ens.user_status.domain.Status;

import java.util.concurrent.ConcurrentHashMap;

public interface GetUserStatesUseCase {
    ConcurrentHashMap<Long, Status> getUserStates();
}
