package com.andrew.ens.contact.application.port.in;

public interface GetInfoContactExistsByPhoneNumberAndTemplateIdUseCase {
    boolean getInfoContactExistsByPhoneNumberAndTemplateId(String phoneNumber, int templateId);
}
