package com.andrew.ens.current_creating.application.port.in;

import com.andrew.ens.current_creating.adapter.out.persistence.CurrentCreating;

public interface CreateCurrentCreatingUseCase {
    void createCurrentCreating(CurrentCreating  currentCreating);
}
