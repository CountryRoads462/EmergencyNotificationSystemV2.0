package com.andrew.ens.current_creating.application.port.in;

public interface SetContactIdByUserIdUseCase {
    void setContactIdByUserId(long userId, int contactId);
}
