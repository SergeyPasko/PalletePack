package org.innovecs;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.innovecs.models.Box;
import org.innovecs.models.BoxType;
import org.innovecs.services.FileService;
import org.innovecs.services.OptimalPackStrategy;
import org.innovecs.services.PackService;
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
	private static final Logger LOG = LogManager.getLogger(Main.class);

	@Autowired
	private FileService fileService;
	@Autowired
	private PackService packService;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(Main.class);
		app.run(args);
	}

	@Override
	public void run(String... args) {
		List<Box> boxs = fileService.readBoxFile("src\\main\\resources\\input.csv");
		Map<String, List<Box>> boxesByDestinations = boxs.stream().collect(Collectors.groupingBy(Box::getDestination));
		boxesByDestinations.values().forEach(packService::calculatePack);
	}
}
