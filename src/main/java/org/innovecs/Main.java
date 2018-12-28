package org.innovecs;

import org.innovecs.view.BoxPackagingSampleApp;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javafx.application.Application;

/**
 * @author spasko
 */
@SpringBootApplication
@ComponentScan("org.innovecs")
public class Main extends BoxPackagingSampleApp {
	public static void main(String[] args) {
		Application.launch(args);
	}
}
