package org.somik.ip2country.dto;

import lombok.Data;

@Data
public class ResponseDto {
    private String status;
    private String message;
    private ResultDto result;

    public ResponseDto() {
        // Default constructor for serialization/deserialization
    }
    
    public ResponseDto(String status, String message, ResultDto result) {
        this.status = status;
        this.message = message;
        this.result = result;
    }
}
