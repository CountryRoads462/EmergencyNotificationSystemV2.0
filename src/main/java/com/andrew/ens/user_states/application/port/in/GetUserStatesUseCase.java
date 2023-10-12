package com.andrew.ens.user_states.application.port.in;

import com.andrew.ens.user_states.domain.Status;

import java.util.concurrent.ConcurrentHashMap;

public interface GetUserStatesUseCase {
    ConcurrentHashMap<Long, Status> getUserStates();
}
