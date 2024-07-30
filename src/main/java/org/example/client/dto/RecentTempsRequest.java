package org.example.client.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RecentTempsRequest {
    private int count;
    private int offset;

}
