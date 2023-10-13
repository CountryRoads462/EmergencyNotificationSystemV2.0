package com.andrew.ens.template.application;

import com.andrew.ens.template.adapter.out.persistence.TemplatesRepository;
import com.andrew.ens.template.application.port.in.CreateIncompleteTemplateUseCase;
import com.andrew.ens.template.application.port.in.SetTemplateTextUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemplatesService implements
        CreateIncompleteTemplateUseCase,
        SetTemplateTextUseCase {

    private final TemplatesRepository templatesRepo;

    @Override
    public int createIncompleteTemplate(String templateName, long userId) {
        return templatesRepo.createIncompleteTemplate(
                templateName,
                userId
        );
    }

    @Override
    public void setTemplateText(int templateId, String text) {
        templatesRepo.setText(text, templateId);
    }
}
