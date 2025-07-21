package org.somik.ip2country.service;

import org.somik.ip2country.dto.ResponseDto;

public interface Ip2cService {
    ResponseDto getCountryByIp(String ip);

    ResponseDto getCountryByIpFromCsv(String ip);

    ResponseDto updateDatabase();
}
