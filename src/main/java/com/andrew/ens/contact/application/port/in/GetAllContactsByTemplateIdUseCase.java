package com.andrew.ens.contact.application.port.in;

import com.andrew.ens.contact.adapter.out.persistence.Contact;

import java.util.List;

public interface GetAllContactsByTemplateIdUseCase {
    List<Contact> getAllContactsByTemplateId(int templateId);
}
