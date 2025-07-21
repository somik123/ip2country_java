package org.somik.ip2country.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.somik.ip2country.entity.IpType;

public class CommonUtils {
    private static final Logger LOG = Logger.getLogger(CommonUtils.class.getName());

    public static boolean downloadFileFromUrl(String urlStr, String savePath) {
        try {
            URL url = new URI(urlStr).toURL();
            BufferedInputStream bis = new BufferedInputStream(url.openStream());
            FileOutputStream fis = new FileOutputStream(savePath);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = bis.read(buffer, 0, 1024)) != -1) {
                fis.write(buffer, 0, count);
            }
            fis.close();
            bis.close();
            return true;
        } catch (Exception e) {
            logErrors(LOG, e);
            return false;
        }
    }

    public static void logErrors(Logger LOG, Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();
        System.out.println(sStackTrace);
        LOG.warning(sStackTrace);
    }

    public static List<String> listAllFilesInDirectory(String directoryPath) {
        try {
            java.io.File dir = new java.io.File(directoryPath);
            if (dir.isDirectory()) {
                String[] files = dir.list();
                if (files == null)
                    return null;
                return Stream.of(files)
                        .map(f -> directoryPath + f)
                        .toList();
            } else {
                LOG.warning("Provided path is not a directory: " + directoryPath);
                return null;
            }
        } catch (Exception e) {
            logErrors(LOG, e);
            return null;
        }
    }

    public static List<String> readLinesFromFile(String filePath) {
        try {
            return Files.readAllLines(Paths.get(filePath));
        } catch (Exception e) {
            logErrors(LOG, e);
            return null;
        }
    }

    public static String getIpType(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            if (address instanceof java.net.Inet6Address) {
                return IpType.IPv6.getType();
            } else if (address instanceof java.net.Inet4Address) {
                return IpType.IPv4.getType();
            } else {
                return null;
            }
        } catch (Exception e) {
            logErrors(LOG, e);
            return "Error determining IP type";
        }
    }

    public static BigInteger ipToBigInteger(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            byte[] ipBytes = address.getAddress();
            return new BigInteger(1, ipBytes);
        } catch (Exception e) {
            logErrors(LOG, e);
            return null;
        }
    }

    public static boolean downloadCsvData(String urlFile, String downloadPath) {

        // Validate input parameters
        if (urlFile == null || urlFile.isEmpty() || downloadPath == null || downloadPath.isEmpty()) {
            LOG.warning("Invalid input parameters for downloading CSV data.");
            return false;
        }
        try {
            // Read URLs from the provided file
            List<String> urls = readLinesFromFile(urlFile);
            if (urls == null || urls.isEmpty()) {
                LOG.warning("No URLs found in the file: " + urlFile);
                return false;
            }

            // Download each file from the URLs
            for (int i = 0; i < urls.size(); i++) {
                String urlStr = urls.get(i);
                String savePath = downloadPath + (i + 1) + ".csv";
                if (!downloadFileFromUrl(urlStr, savePath)) {
                    LOG.warning("Failed to download file from URL: " + urlStr);
                    return false; // Download failed
                } else {
                    LOG.info("Downloaded file from URL: " + urlStr + " to " + savePath);
                }
            }
        } catch (Exception e) {
            logErrors(LOG, e);
            return false; // Exception occurred
        }

        LOG.info("All files downloaded successfully to: " + downloadPath);
        return true; // All downloads succeeded
    }

    public static String getFilenameWithoutExtensionFromPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            LOG.warning("Invalid file path provided.");
            return null;
        }
        try {
            // Normalize the file path to handle different OS path formats
            String fileName = new File(filePath).getName();
            int dotIndex = fileName.lastIndexOf('.');
            return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
        } catch (Exception e) {
            logErrors(LOG, e);
            return null; // Return null if there's an error in getting the canonical path
        }
    }

    public static String[] getConfidence(List<String> countryList) {
        if (!countryList.isEmpty()) {
            // Step 1: Count occurrences
            Map<String, Integer> counts = new HashMap<>();
            for (String country : countryList) {
                counts.put(country, counts.getOrDefault(country, 0) + 1);
            }

            // Step 2: Get most common country and total items
            String mostCommonCountry = null;
            int maxCount = 0;

            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                if (entry.getValue() > maxCount) {
                    mostCommonCountry = entry.getKey();
                    maxCount = entry.getValue();
                }
            }

            int total = countryList.size();
            double confidence = ((double) maxCount / total) * 100;

            // Output results
            LOG.info("Most common country: " + mostCommonCountry);
            LOG.info("Confidence: " + String.format("%.2f%%", confidence));
            return new String[] { mostCommonCountry, String.format("%.2f", confidence) };
        }
        return new String[] { "", "0.00" };
    }
}
