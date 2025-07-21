package org.somik.ip2country.entity;

import java.math.BigInteger;

import jakarta.persistence.Entity;

@Entity
public class Ip2cV4 extends Ip2c {

    public Ip2cV4() {
        super();
    }

    public Ip2cV4(BigInteger ipFrom, BigInteger ipTo, String countryCode, String dbId) {
        super(ipFrom, ipTo, countryCode, dbId);
    }

    @Override
    public String toString() {
        return "Ip2cV4{" +
                "ipFrom='" + getIpFrom() + '\'' +
                ", ipTo='" + getIpTo() + '\'' +
                ", countryCode='" + getCountryCode() + '\'' +
                ", dbId='" + getDbId() + '\'' +
                '}';
    }

}
