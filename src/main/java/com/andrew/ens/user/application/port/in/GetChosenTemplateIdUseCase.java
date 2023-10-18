package com.andrew.ens.user.application.port.in;

import java.util.Optional;

public interface GetChosenTemplateIdUseCase {
    Optional<Integer> getChosenTemplateId(long userId);
}
