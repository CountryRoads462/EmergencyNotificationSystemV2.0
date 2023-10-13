package com.andrew.ens.template.application.port.in;

public interface CreateIncompleteTemplateUseCase {
    int createIncompleteTemplate(String templateName, long userId);
}
