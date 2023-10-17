package com.andrew.ens;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class UserCurrentStatus {
    private Status status;
    private int templateCreationId;
    private int contactCreationId;
    private int messageIdToDelete;
}
