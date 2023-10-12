package com.andrew.ens.template.application;

import com.andrew.ens.template.adapter.out.persistence.Template;
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
    public void createIncompleteTemplate(String templateName, long userId) {
        templatesRepo.save(Template.builder()
                .name(templateName)
                .ownerId(userId)
                .build());
    }

    @Override
    public void setTemplateText(String text, long userId) {
        templatesRepo.setText(text, userId);
    }
}
