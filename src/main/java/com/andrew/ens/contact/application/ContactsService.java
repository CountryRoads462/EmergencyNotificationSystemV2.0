package com.andrew.ens.contact.application;

import com.andrew.ens.contact.adapter.out.persistence.ContactsRepository;
import com.andrew.ens.contact.application.port.in.CreateIncompleteContactUseCase;
import com.andrew.ens.contact.application.port.in.SetContactEmailUseCase;
import com.andrew.ens.contact.application.port.in.SetContactPhoneNumberUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactsService implements
        CreateIncompleteContactUseCase,
        SetContactEmailUseCase,
        SetContactPhoneNumberUseCase {

    private final ContactsRepository contactsRepository;

    @Override
    public int createIncompleteContact(String name) {
        return contactsRepository.createIncompleteContact(name);
    }

    @Override
    public void setContactEmail(int contactId, String email) {
        contactsRepository.setContactEmail(contactId, email);
    }

    @Override
    public void setContactPhoneNumber(int contactId, String phoneNumber) {
        contactsRepository.setContactPhoneNumber(contactId, phoneNumber);
    }
}
