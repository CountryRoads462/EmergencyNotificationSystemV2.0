package com.andrew.ens.template.application.port.in;

public interface CreateIncompleteTemplateUseCase {
    void createIncompleteTemplate(String templateName, long userId);
}
