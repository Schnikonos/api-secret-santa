package com.santa.secret;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

@Slf4j
@SpringBootApplication
public class ApiSecretSantaApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext = SpringApplication.run(ApiSecretSantaApplication.class, args);

		// Open the default web browser
		String url = String.format("http://localhost:%s", applicationContext.getEnvironment().getProperty("server.port"));
		openBrowser(url);
	}

	private static void openBrowser(String url) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI(url));
			} catch (Exception e) {
				log.error("Failed to open browser", e);
			}
		} else {
			try {
				Runtime rt = Runtime.getRuntime();
				rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
			} catch (IOException e) {
				log.error("Failed to open browser via runtime", e);
			}
		}
	}
}
