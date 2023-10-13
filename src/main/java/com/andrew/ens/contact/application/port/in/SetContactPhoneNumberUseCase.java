package com.andrew.ens.contact.application.port.in;

public interface SetContactPhoneNumberUseCase {
    void setContactPhoneNumber(int contactId, String phoneNumber);
}
