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
    private Integer templateCreationId;
    private int contactCreationId;
}
