package com.andrew.ens.user.application.port.in;

public interface SetChosenTemplateUseCase {
    void setChosenTemplate(long userId, int templateId);
}
