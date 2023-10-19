package com.andrew.ens.contact.application.port.in;

public interface GetInfoContactExistsByEmailAndTemplateIdUseCase {
    boolean getInfoContactExistsByEmailAndTemplateId(String email, int templateId);
}
