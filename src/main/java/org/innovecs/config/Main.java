package org.innovecs.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.innovecs.models.Box;
import org.innovecs.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author spasko
 */
@SpringBootApplication
@ComponentScan("org.innovecs")
public class Main implements CommandLineRunner {
	@Autowired
	private FileService fileService;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(Main.class);
		app.run(args);
	}

	@Override
	public void run(String... args) {
		List<Box> boxs = fileService.readBoxFile("src\\main\\resources\\input.csv");
		Map<String, List<Box>> boxesByDestinations = boxs.stream()
				.collect(Collectors.groupingBy(d -> d.getDestination()));
		boxesByDestinations.forEach((k, v) -> {
			System.out.println("Dest " + k);
			v.forEach(System.out::println);
		});
	}
}
