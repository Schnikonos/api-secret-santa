package com.santa.secret;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@Slf4j
@SpringBootApplication
public class ApiSecretSantaApplication {

	public static void main(String[] args) {
		new Thread(() -> {
			SpringApplication.run(ApiSecretSantaApplication.class, args);

			// Open the default web browser
			openBrowser("http://localhost:8080");
		}).start();

		// Create a small GUI window with a taskbar icon
		// Create and show the Swing GUI
		SwingUtilities.invokeLater(ApiSecretSantaApplication::createAndShowGUI);
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

	private static void createAndShowGUI() {
		// Ensure the app is taskbar-aware
		JFrame.setDefaultLookAndFeelDecorated(true);

		// Create a simple JFrame
		JFrame frame = new JFrame("Secret Santa App");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 200);
		frame.setLocationRelativeTo(null); // Center the frame on the screen

		// Set the JFrame icon (this affects the taskbar in Windows)
		try (InputStream iconStream = ApiSecretSantaApplication.class.getResourceAsStream("/secret-santa.png")) {
			if (iconStream != null) {
				Image icon = ImageIO.read(iconStream);
				frame.setIconImage(icon);
			} else {
				log.error("Window icon not found!");
			}
		} catch (IOException e) {
			log.error("Failed to load icon", e);
		}

		// Add a label to the frame
		JLabel label = new JLabel("The application is running. Close this window to exit.", SwingConstants.CENTER);
		frame.getContentPane().add(label, BorderLayout.CENTER);

		// Add a shutdown hook to close the Spring Boot app when the GUI closes
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0); // Terminate the application
			}
		});

		// Make the frame visible
		frame.setVisible(true);

		// Set a custom taskbar icon (optional)
		setTaskbarIcon();
	}

	private static void setTaskbarIcon() {
		Taskbar taskbar = Taskbar.getTaskbar();
		if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
			try (InputStream iconStream = ApiSecretSantaApplication.class.getResourceAsStream("/secret-santa.png")) {
				if (iconStream != null) {
					Image icon = ImageIO.read(iconStream);
					taskbar.setIconImage(icon);
				} else {
					log.error("Taskbar icon not found!");
				}
			} catch (IOException e) {
				log.error("Failed to load icon", e);
			}
		}
	}
}
