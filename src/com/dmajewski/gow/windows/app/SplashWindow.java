package com.dmajewski.gow.windows.app;

import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashWindow extends Window{

	public SplashWindow(Frame owner) {
		super(owner);
	}
	
	public static JFrame FRAME_INSTANCE;
	
	public static long START_TIME = System.currentTimeMillis();

	private static final long serialVersionUID = 4709302141240481574L;

	public static void main(String[] args) {
		FRAME_INSTANCE = new JFrame();
		FRAME_INSTANCE.setUndecorated(true);
		java.awt.Label label = new java.awt.Label("Application is loading. Please wait...");
		
//label.getFont()
		label.setFont(new Font("Serif", Font.BOLD, 72));
//		label.setSize(300, 200);
//		frame.getContentPane().add(label, BorderLayout.CENTER);
		JPanel contentPanel = new JPanel();
		contentPanel.add(label);

		Border padding = BorderFactory.createEmptyBorder(40, 40, 40, 40);

		contentPanel.setBorder(padding);

		FRAME_INSTANCE.setContentPane(contentPanel);		
		FRAME_INSTANCE.pack();
		
//		frame.setSize(new Dimension(300, 200));
		FRAME_INSTANCE.setLocationRelativeTo(null);
		FRAME_INSTANCE.setVisible(true);
		FRAME_INSTANCE.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		try {
			Class.forName(MainWindow.class.getName()).getMethod("main", new Class[] { String[].class }).invoke(null,
					new Object[] {new String[0]});
		} catch (Exception e) {
			InternalError error = new InternalError("Failed to invoke main method");
			error.initCause(e);
			throw error;
		}
	}


	public void start(Stage initStage) throws Exception {
		Label label = new Label("Application is loading. Please wait...");
		label.setMinWidth(400);
		label.setAlignment(Pos.CENTER);
		label.setTextAlignment(TextAlignment.CENTER);
		Scene splashScene = new Scene(label, 300, 200);
		initStage.initStyle(StageStyle.UNDECORATED);
		initStage.setScene(splashScene);
		initStage.show();
		try {
			Class.forName(MainWindow.class.getName()).getMethod("main", new Class[] { String[].class }).invoke(null,
					new Object[] {new String[0]});
		} catch (Exception e) {
			InternalError error = new InternalError("Failed to invoke main method");
			error.initCause(e);
			throw error;
		}
		initStage.close();
	}

}
