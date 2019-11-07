package net.ketone.accrptgen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class AccrptgenApplicationLocal {

	public static void main(String[] args) {
//		System.setProperty("org.apache.poi.util.POILogger", "org.apache.poi.util.SystemOutLogger" );
//		System.setProperty("poi.log.level", POILogger.DEBUG + "");
		SpringApplication.run(AccrptgenApplicationLocal.class, args);
	}

	/**
	 * to speed up boot time
	 */
	@Configuration
	@ComponentScan(lazyInit = true)
	static class LocalConfig {
	}
}