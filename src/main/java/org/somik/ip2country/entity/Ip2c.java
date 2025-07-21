package org.somik.ip2country.entity;

import java.math.BigInteger;

import io.micrometer.common.lang.NonNull;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@MappedSuperclass
@Data
public abstract class Ip2c {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private BigInteger ipFrom;
    @NonNull
    private BigInteger ipTo;
    @NonNull
    private String countryCode;
    @NonNull
    private String dbId;

    public Ip2c() {
        // Default constructor for JPA
    }

    public Ip2c(@NonNull BigInteger ipFrom, @NonNull BigInteger ipTo, @NonNull String countryCode, @NonNull String dbId) {
        this.ipFrom = ipFrom;
        this.ipTo = ipTo;
        this.countryCode = countryCode;
        this.dbId = dbId;
    }
}
