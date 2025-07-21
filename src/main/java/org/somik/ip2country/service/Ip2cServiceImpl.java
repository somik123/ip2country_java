package org.somik.ip2country.service;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.somik.ip2country.dto.ResponseDto;
import org.somik.ip2country.dto.ResultDto;
import org.somik.ip2country.entity.Ip2c;
import org.somik.ip2country.entity.Ip2cV4;
import org.somik.ip2country.entity.Ip2cV6;
import org.somik.ip2country.entity.IpType;
import org.somik.ip2country.repo.Ip2cV4Repo;
import org.somik.ip2country.repo.Ip2cV6Repo;
import org.somik.ip2country.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class Ip2cServiceImpl implements Ip2cService {

    private static final Logger LOG = Logger.getLogger(Ip2cServiceImpl.class.getName());

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DbService dbService;
    @Autowired
    private Ip2cV4Repo ip2cV4Repo;
    @Autowired
    private Ip2cV6Repo ip2cV6Repo;

    private static final String csv_urls_ipv4 = "./data/csv_ipv4.list";
    private static final String csv_urls_ipv6 = "./data/csv_ipv6.list";

    private static final String csv_ipv4_path = "./data/db/csv_v4/";
    private static final String csv_ipv6_path = "./data/db/csv_v6/";

    @Override
    public ResponseDto getCountryByIp(String ip) {
        long startTime = System.currentTimeMillis();
        BigInteger ipBigInt = CommonUtils.ipToBigInteger(ip);
        if (ipBigInt == null) {
            String errorMessage = "Invalid IP address: " + ip;
            LOG.warning(errorMessage);
            return new ResponseDto("Error", errorMessage, null);
        }
        LOG.info("Searching for IP address: " + ip + " (Decimal: " + ipBigInt + ")");

        String ipType = CommonUtils.getIpType(ip);

        List<String> responseCountries = new ArrayList<>();

        List<Ip2c> ip2cList;
        List<Ip2cV4> ip2cV4List = new ArrayList<>();
        List<Ip2cV6> ip2cV6List = new ArrayList<>();
        // Check in the database
        if (ipType.equals(IpType.IPv4.getType())) {
            LOG.info("Searching in IPv4 database...");
            ip2cV4List = ip2cV4Repo.findCountryByIpRange(ipBigInt);
        } else if (ipType.equals(IpType.IPv6.getType())) {
            LOG.info("Searching in IPv6 database...");
            ip2cV6List = ip2cV6Repo.findCountryByIpRange(ipBigInt);
        } else {
            String errorMessage = "Unknown IP type for address: " + ip;
            LOG.warning(errorMessage);
            return new ResponseDto("Error", errorMessage, null);
        }

        ip2cList = ipType.equals(IpType.IPv4.getType()) ? new ArrayList<>(ip2cV4List) : new ArrayList<>(ip2cV6List);

        if (ip2cList != null && !ip2cList.isEmpty()) {
            LOG.info("Found " + ip2cList.size() + " matching records in the database for IP: " + ip);

            responseCountries.addAll(
                    ip2cList.stream()
                            .map(Ip2c::getCountryCode)
                            .toList());
            String[] confidence = CommonUtils.getConfidence(responseCountries);

            ResultDto resultDto = new ResultDto(ip, ipBigInt, confidence[0], confidence[1],
                    responseCountries.toArray(new String[0]), (System.currentTimeMillis() - startTime) + " ms");

            ResponseDto responseDto = new ResponseDto("OK", "", resultDto);
            return responseDto;
        } else {
            LOG.info("No matching records found in the database for IP: " + ip);
            // If not found in the database, search in CSV files
            LOG.info("Searching in CSV files for IP: " + ip);
            // Call the method to search in CSV files
            return getCountryByIpFromCsv(ip);
        }
    }

    @Override
    public ResponseDto getCountryByIpFromCsv(String ip) {

        long startTime = System.currentTimeMillis();

        BigInteger ipBigInt = CommonUtils.ipToBigInteger(ip);
        if (ipBigInt == null) {
            LOG.warning("Invalid IP address: " + ip);
            return null;
        }

        String csvDir = null;
        String ipType = CommonUtils.getIpType(ip);
        if (ipType.equals(IpType.IPv4.getType())) {
            csvDir = csv_ipv4_path;
        } else if (ipType.equals(IpType.IPv6.getType())) {
            csvDir = csv_ipv6_path;
        } else {
            String errorMessage = "Unknown IP type for address: " + ip;
            LOG.warning(errorMessage);
            return new ResponseDto("Error", errorMessage, null);
        }
        LOG.info("Searching for IP address in directory: " + csvDir);

        List<String> responseCountries = new ArrayList<>();

        // Search in CSV files for the IP
        List<String> csvFiles = CommonUtils.listAllFilesInDirectory(csvDir);
        if (csvFiles != null) {
            for (String csvFile : csvFiles) {
                LOG.info("Checking CSV file: " + csvFile);

                // Read lines from the CSV file
                List<String> lines = CommonUtils.readLinesFromFile(csvFile);
                if (lines != null) {
                    for (String line : lines) {
                        String[] parts = line.split(",");
                        if (parts.length >= 3) {
                            BigInteger ipFrom = new BigInteger(parts[0]);
                            BigInteger ipTo = new BigInteger(parts[1]);
                            if (ipFrom != null && ipTo != null && ipBigInt.compareTo(ipFrom) >= 0
                                    && ipBigInt.compareTo(ipTo) <= 0) {
                                LOG.info("Found IP address in CSV file: " + csvFile);
                                // Return the country associated with the IP
                                responseCountries.add(parts[2]); // Add country to response list
                            }
                        }
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        String timeTaken = (endTime - startTime) + " ms";

        String[] confidence = CommonUtils.getConfidence(responseCountries);

        ResultDto resultDto = new ResultDto(ip, ipBigInt, confidence[0], confidence[1],
                responseCountries.toArray(new String[0]), timeTaken);
        ResponseDto responseDto = new ResponseDto("OK", "", resultDto);
        LOG.info("Response for IP " + ip + ": " + responseDto);
        return responseDto;
    }

    @Override
    public ResponseDto updateDatabase() {
        // Download CSV data if not already present
        boolean downloaded = downloadCsvData();
        if (!downloaded) {
            String errorMessage = "Failed to download CSV data. Database update aborted.";
            LOG.warning(errorMessage);
            return new ResponseDto("Error", errorMessage, null);
        }

        // Clear existing data in the database
        LOG.info("Clearing existing data in the database...");
        ip2cV4Repo.deleteAll();
        ip2cV6Repo.deleteAll();
        LOG.info("Existing data cleared from the database.");

        // Populate the database from the downloaded CSV files
        LOG.info("Populating database from CSV files...");

        // Get the list of CSV files from the specified directories
        List<String> csvFilesIpv4 = CommonUtils.listAllFilesInDirectory(csv_ipv4_path);
        if (csvFilesIpv4 != null) {
            for (String csvFile : csvFilesIpv4) {
                populateDatabaseFromCsv(csvFile, IpType.IPv4.getType());
            }
        }

        List<String> csvFilesIpv6 = CommonUtils.listAllFilesInDirectory(csv_ipv6_path);
        if (csvFilesIpv6 != null) {
            for (String csvFile : csvFilesIpv6) {
                populateDatabaseFromCsv(csvFile, IpType.IPv6.getType());
            }
        }

        LOG.info("Database populated successfully from CSV files.");

        // Export the database to a file
        LOG.info("Exporting database to file...");
        dbService.exportDatabase();
        String message = "Database updated successfully.";
        LOG.info(message);
        return new ResponseDto("OK", message, null);
    }

    public boolean downloadCsvData() {
        boolean ipv4Downloaded = CommonUtils.downloadCsvData(csv_urls_ipv4, csv_ipv4_path);
        boolean ipv6Downloaded = CommonUtils.downloadCsvData(csv_urls_ipv6, csv_ipv6_path);
        return ipv4Downloaded && ipv6Downloaded;
    }

    public void populateDatabaseFromCsv(String csvFilePath, String ipType) {
        try {
            List<Ip2cV4> ip2cV4List = new ArrayList<>();
            List<Ip2cV6> ip2cV6List = new ArrayList<>();

            LOG.info("Populating database from CSV file: " + csvFilePath);
            final String dbId = CommonUtils.getFilenameWithoutExtensionFromPath(csvFilePath);
            List<String> lines = CommonUtils.readLinesFromFile(csvFilePath);
            if (lines != null) {
                for (String line : lines) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        BigInteger ipFrom = new BigInteger(parts[0]);
                        BigInteger ipTo = new BigInteger(parts[1]);
                        String country = parts[2];

                        // Prepare the Ip2c object
                        if (ipType.equals(IpType.IPv4.getType())) {
                            ip2cV4List.add(new Ip2cV4(ipFrom, ipTo, country, dbId));
                        } else if (ipType.equals(IpType.IPv6.getType())) {
                            ip2cV6List.add(new Ip2cV6(ipFrom, ipTo, country, dbId));
                        }
                    }
                }
                if (ipType.equals(IpType.IPv4.getType()) && !ip2cV4List.isEmpty()) {
                    LOG.info("Total records to insert: " + ip2cV4List.size());
                    // ip2cV4Repo.saveAll(ip2cV4List);
                    batchInsert(new ArrayList<>(ip2cV4List), ipType);
                } else if (ipType.equals(IpType.IPv6.getType()) && !ip2cV6List.isEmpty()) {
                    LOG.info("Total records to insert: " + ip2cV6List.size());
                    // ip2cV6Repo.saveAll(ip2cV6List);
                    batchInsert(new ArrayList<>(ip2cV6List), ipType);
                }
                LOG.info("Database populated successfully from CSV file: " + csvFilePath);
            }
        } catch (Exception e) {
            CommonUtils.logErrors(LOG, e);
        }
    }

    public void batchInsert(List<Ip2c> ip2cList, String ipType) {
        if (ip2cList == null || ip2cList.isEmpty()) {
            LOG.warning("No IP2C records to insert.");
            return;
        }

        String tableName = ipType.equals(IpType.IPv4.getType()) ? "IP2CV4" : "IP2CV6";
        LOG.info("Batch inserting " + ip2cList.size() + " records into " + tableName + " table.");

        try {
            jdbcTemplate.batchUpdate(
                    "INSERT INTO " + tableName + " (ip_from, ip_to, country_code, db_id) VALUES (?, ?, ?, ?)",
                    new BatchPreparedStatementSetter() {
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Ip2c ip2c = ip2cList.get(i);
                            ps.setObject(1, ip2c.getIpFrom());
                            ps.setObject(2, ip2c.getIpTo());
                            ps.setString(3, ip2c.getCountryCode());
                            ps.setString(4, ip2c.getDbId());
                        }

                        public int getBatchSize() {
                            return ip2cList.size();
                        }
                    });
        } catch (Exception e) {
            LOG.severe("Error during batch insert: " + e.getMessage());
            CommonUtils.logErrors(LOG, e);
        }
    }
}
