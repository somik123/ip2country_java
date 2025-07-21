package org.somik.ip2country.entity;

import java.math.BigInteger;

import jakarta.persistence.Entity;

@Entity
public class Ip2cV6 extends Ip2c {

    public Ip2cV6() {
        super();
    }

    public Ip2cV6(BigInteger ipFrom, BigInteger ipTo, String countryCode, String dbId) {
        super(ipFrom, ipTo, countryCode, dbId);
    }

    @Override
    public String toString() {
        return "Ip2cV6{" +
                "ipFrom='" + getIpFrom() + '\'' +
                ", ipTo='" + getIpTo() + '\'' +
                ", countryCode='" + getCountryCode() + '\'' +
                ", dbId='" + getDbId() + '\'' +
                '}';
    }
}