package org.example.client.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Request {
    private float temperature;

    @JsonCreator
    public Request(@JsonProperty("temperature") float temperature) {
        this.temperature = temperature;
    }

}
