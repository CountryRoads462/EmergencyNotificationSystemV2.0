package com.andrew.ens.contact.application;

import com.andrew.ens.contact.adapter.out.persistence.Contact;
import com.andrew.ens.contact.adapter.out.persistence.ContactsRepository;
import com.andrew.ens.contact.application.port.in.CreateIncompleteContactUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactsService implements
        CreateIncompleteContactUseCase {

    private final ContactsRepository contactsRepository;

    @Override
    public void createIncompleteContact(String name) {
        contactsRepository.save(Contact.builder()
                .name(name)
                .build());
    }
}
