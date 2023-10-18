package com.andrew.ens.template.application.port.in;

public interface GetInfoTemplateExistsByNameAndOwnerIdUseCase {
    boolean getInfoTemplateExistsByNameAndOwnerId(long ownerId, String name);
}
