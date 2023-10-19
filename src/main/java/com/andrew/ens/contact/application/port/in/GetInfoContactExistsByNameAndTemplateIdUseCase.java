package com.andrew.ens.contact.application.port.in;

public interface GetInfoContactExistsByNameAndTemplateIdUseCase {
    boolean getInfoContactExistsByNameAndTemplateId(String name, int templateId);
}
