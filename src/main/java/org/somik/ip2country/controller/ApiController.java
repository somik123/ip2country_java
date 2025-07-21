package org.somik.ip2country.controller;

import java.util.logging.Logger;

import org.somik.ip2country.dto.ResponseDto;
import org.somik.ip2country.service.Ip2cService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private Ip2cService ip2cService;

    private static final Logger LOG = Logger.getLogger(ApiController.class.getName());

    @RequestMapping(value = "/updateDatabase", produces = "application/json")
    public ResponseDto updateDatabase() {
        try {
            return ip2cService.updateDatabase();
        } catch (Exception e) {
            LOG.severe("Error updating database: " + e.getMessage());
            return new ResponseDto("Error", "Error updating database: " + e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/v1/{ip}", produces = "application/json")
    public ResponseDto getCountryByIpCsv(@PathVariable String ip) {
        try {
            LOG.info("Received request for IP: " + ip);
            return ip2cService.getCountryByIpFromCsv(ip);
        } catch (Exception e) {
            String errorMessage = "Error getting country by IP: " + e.getMessage();
            LOG.severe(errorMessage);
            return new ResponseDto("Error", errorMessage, null);
        }
    }

    @RequestMapping(value = "/v2/{ip}", produces = "application/json")
    public ResponseDto getCountryByIp(@PathVariable String ip) {
        try {
            LOG.info("Received request for IP: " + ip);
            return ip2cService.getCountryByIp(ip);
        } catch (Exception e) {
            String errorMessage = "Error getting country by IP: " + e.getMessage();
            LOG.severe(errorMessage);
            return new ResponseDto("Error", errorMessage, null);
        }
    }
}
