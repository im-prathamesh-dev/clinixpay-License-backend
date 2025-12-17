package Clinixpay.ClinicPaykeyGeneration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClinicPaykeyGenerationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClinicPaykeyGenerationApplication.class, args);
	}

}
