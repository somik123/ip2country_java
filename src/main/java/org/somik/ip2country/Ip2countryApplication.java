package org.somik.ip2country;

import java.io.File;
import java.util.logging.Logger;

import org.somik.ip2country.service.DbService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Ip2countryApplication {

	private static Logger LOG = Logger.getLogger(Ip2countryApplication.class.getName());

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(Ip2countryApplication.class, args);

		initializeDirectories();
		LOG.info("Directories initialized successfully.");

		// Initialize the database
		DbService dbService = context.getBean(DbService.class);
		dbService.importDatabase();
		LOG.info("Database imported successfully.");
	}

	private static void initializeDirectories() {
		createDirectory("./data/db");
		createDirectory("./data/logs");
		createDirectory("./data/db/csv_v4");
		createDirectory("./data/db/csv_v6");
	}

	private static boolean createDirectory(String dir) {
		File file = new File(dir);
		if (file.exists()) {
			if (file.isDirectory())
				return true;
			else
				file.delete();
		}
		if (file.mkdirs())
			return true;
		else
			return false;
	}

}
