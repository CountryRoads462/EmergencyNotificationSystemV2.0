package com.andrew.ens.contact.application;

import com.andrew.ens.contact.adapter.out.persistence.Contact;
import com.andrew.ens.contact.adapter.out.persistence.ContactsRepository;
import com.andrew.ens.contact.application.port.in.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactsService implements
        CreateIncompleteContactUseCase,
        SetContactEmailUseCase,
        SetContactPhoneNumberUseCase,
        DeleteAllContactsUseCase,
        GetAllContactsByTemplateIdUseCase,
        DeleteContactByIdUseCase {

    @Autowired
    private final ContactsRepository contactsRepo;

    @Override
    public int createIncompleteContact(String name) {
        return contactsRepo.createIncompleteContact(name);
    }

    @Override
    public void setContactEmail(int contactId, String email) {
        contactsRepo.setContactEmail(contactId, email);
    }

    @Override
    public void setContactPhoneNumber(int contactId, String phoneNumber) {
        contactsRepo.setContactPhoneNumber(contactId, phoneNumber);
    }

    @Override
    public void deleteAllContacts() {
        contactsRepo.deleteAllContacts();
    }

    @Override
    public List<Contact> getAllContactsByTemplateId(int templateId) {
        return contactsRepo.getAllContactsByTemplateId(templateId);
    }

    @Override
    public void deleteContactById(int contactId) {
        contactsRepo.deleteContactById(contactId);
    }
}
