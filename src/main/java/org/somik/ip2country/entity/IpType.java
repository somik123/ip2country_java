package org.somik.ip2country.entity;

public enum IpType {
    IPv4("IPv4"),
    IPv6("IPv6");

    private final String type;

    IpType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }
}
