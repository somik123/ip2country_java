package org.somik.ip2country.dto;

import java.math.BigInteger;

import lombok.Data;

@Data
public class ResultDto {
    private String ip;
    private BigInteger dec;
    private String country;
    private String confidence;
    private String[] raw_country;
    private String time_taken;

    public ResultDto() {
        // Default constructor for serialization/deserialization
    }

    public ResultDto(String ip, BigInteger dec, String country, String confidence, String[] raw_country, String time_taken) {
        this.ip = ip;
        this.dec = dec;
        this.country = country;
        this.confidence = confidence;
        this.raw_country = raw_country;
        this.time_taken = time_taken;
    }
}
