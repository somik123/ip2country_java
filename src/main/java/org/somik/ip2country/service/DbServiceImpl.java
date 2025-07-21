package org.somik.ip2country.service;

import java.io.File;
import java.sql.Connection;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DbServiceImpl implements DbService {

    private final static String DB_NAME = "./data/db/ip2country.sql";

    private final static Logger LOG = Logger.getLogger(DbServiceImpl.class.getName());

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DbServiceImpl(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void exportDatabase() {
        String sql = String.format("SCRIPT TO '%s'", DB_NAME.replace("'", "''"));
        jdbcTemplate.execute(sql);
    }

    @Override
    public void importDatabase() {
        try {
            if (!new File(DB_NAME).exists()) {
                LOG.warning("Database file not found: " + DB_NAME);
                return;
            }
            // Clear existing database
            clearDatabase();

            // Import the database from the SQL file
            LOG.info("Importing database from file: " + DB_NAME);
            String sql = String.format("RUNSCRIPT FROM '%s'", DB_NAME.replace("'", "''"));
            jdbcTemplate.execute(sql);
            LOG.info("Database imported successfully from file: " + DB_NAME);
        } catch (Exception e) {
            LOG.severe("Error importing database: " + e.getMessage());
        }
    }

    @Override
    public void clearDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().execute("DROP ALL OBJECTS");
            LOG.info("Database cleared successfully.");
        } catch (Exception e) {
            LOG.severe("Error clearing database: " + e.getMessage());
        }
    }
}
