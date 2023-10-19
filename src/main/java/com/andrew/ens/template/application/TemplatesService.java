package com.andrew.ens.template.application;

import com.andrew.ens.template.adapter.out.persistence.Template;
import com.andrew.ens.template.adapter.out.persistence.TemplatesRepository;
import com.andrew.ens.template.application.port.in.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TemplatesService implements
        CreateIncompleteTemplateUseCase,
        SetTemplateTextUseCase,
        GetTemplatesByOwnerIdUseCase,
        DeleteTemplateByIdUseCase,
        GetTemplateByIdUseCase,
        GetInfoTemplateExistsByNameAndOwnerIdUseCase,
        SetTemplateNameByTemplateIdUseCase {

    @Autowired
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

    @Override
    public List<Template> getTemplatesByOwnerId(long ownerId) {
        return templatesRepo.getTemplatesByOwnerId(ownerId);
    }

    @Override
    public void deleteTemplateById(int templateId) {
        templatesRepo.deleteTemplateById(templateId);
    }

    @Override
    public Optional<Template> getTemplateById(int templateId) {
        return templatesRepo.getTemplateById(templateId);
    }

    @Override
    public boolean getInfoTemplateExistsByNameAndOwnerId(long ownerId, String name) {
        return templatesRepo.getCountOfTemplatesWithNameAndOwnerId(ownerId, name) != 0;
    }

    @Override
    public void setTemplateNameByTemplateId(int templateId, String newName) {
        templatesRepo.setTemplateNameByTemplateId(templateId, newName);
    }
}
