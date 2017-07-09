package com.dmajewski.gow.windows.app;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.dmajewski.gow.game.WindowsPlayer;
import com.dmajewski.gow.windows.driver.WinRobot;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainWindow extends Application {

	public static WindowsPlayer windowsPlayer = null;
	public static Thread playerThread = null;
	
	public static Alert alert = null;
	
	public static long start = System.currentTimeMillis();
	
	public static String BUILD_TIMESTAMP;
	public static String VERSION;
	

	public static void main(String[] args) {
		Properties props = new Properties();
		try {
			props.load(MainWindow.class.getClassLoader().getResourceAsStream("project.properties"));
			BUILD_TIMESTAMP = props.getProperty("buildtime");
			VERSION = props.getProperty("version");
		} catch (IOException e) {
			e.printStackTrace();
		}
		launch(args);
	}


	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("GOW treasuer hunt player");
		Button btn = new Button();
		btn.setText("Play");
		TextArea textArea = new TextArea("Press play to start game\n");
		textArea.setWrapText(true);
		
        Label spinnerLabel = new Label("Number of games to play:");
        final Spinner<Integer> spinner = new Spinner<Integer>();		
		
        configureWindowsPlayer(btn, textArea, spinner);
		
		btn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (playerThread == null) {

					playerThread = new Thread(windowsPlayer);
					playerThread.start();
					btn.setText("Pause");
				} else {
					if (playerThread.isAlive()) {
						windowsPlayer.suspended = true;
						btn.setText("Play");
					} else {
						windowsPlayer.suspended = true;
						playerThread = new Thread(windowsPlayer);
						windowsPlayer.suspended = false;
						playerThread.start();
						btn.setText("Pause");
					}

				}
			}
		});

		AtomicBoolean infiniteRun = new AtomicBoolean(true);
		
		primaryStage.setOnCloseRequest(e -> {
			if (playerThread != null && playerThread.isAlive())
				playerThread.interrupt();
			infiniteRun.set(false);
			Platform.exit();
			System.exit(0);
		});
		MenuBar menuBar = new MenuBar();
		Region spacer = new Region();
        spacer.getStyleClass().add("menu-bar");
        HBox.setHgrow(spacer, Priority.SOMETIMES);		
		HBox toolbar = new HBox(spacer, menuBar);
		
        
        SpinnerValueFactory<Integer> valueFactory = //
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, windowsPlayer.gamesToPlay);
        
        spinner.setValueFactory(valueFactory);
        
        FlowPane spinnerPane = new FlowPane();
        spinnerPane.getChildren().addAll(spinnerLabel, spinner);       
        
        spinner.valueProperty().addListener(new ChangeListener<Integer>() {
        	 
            @Override
            public void changed(ObservableValue<? extends Integer> observable,//
            		Integer oldValue, Integer newValue) {
            	windowsPlayer.gamesToPlay = newValue;
            }
        });
        
        Button buyMapsButton = new Button("Buy maps");
        buyMapsButton.setOnAction(e->{
        	List<Integer> options = IntStream.iterate(1, i -> i + 1).limit(20).boxed().collect(Collectors.toList());;
        	ChoiceDialog<Integer> dialog = new ChoiceDialog<Integer>(options.get(0), options);
        	dialog.setTitle("Buy maps");
        	dialog.setHeaderText("How many maps would you like to buy?");
        	dialog.showAndWait().ifPresent(response -> {
				try {
					WinRobot.takeScreenshot();
					WindowsPlayer.buyMaps(dialog.getResult());
				} catch (Exception e1) {
					Platform.runLater(() -> {
						textArea.appendText(e1.getMessage() + "\n");
						textArea.appendText(ExceptionUtils.getStackTrace(e1) + "\n");
					});
				}      		
        	});

        });
        
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< 1000; i++){
        	sb.append("Click to stop the app!! ");
        }
        
        Button stopButton = new Button(sb.toString());
        stopButton.setTextAlignment(TextAlignment.LEFT);
        stopButton.setContentDisplay(ContentDisplay.TOP);
        stopButton.wrapTextProperty().setValue(true);
        stopButton.setOnAction(event -> {
        	primaryStage.getOnCloseRequest().handle(null);
        });
        
        Scene bigButtonScene = new Scene(stopButton, 300, 200);
        
        Button playForeverButton = new Button("Play forever");
        playForeverButton.setOnAction(event ->{
        	Thread t = new Thread(){
        		public void run() {
        			boolean noExceptions = true;
                	while (noExceptions && infiniteRun.get()) {
                		windowsPlayer.suspended = false;
                		windowsPlayer.gamesToPlay = 1;
                		Thread wp = new Thread(windowsPlayer);
                		wp.start();
                		try {
							wp.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
							Platform.exit();
							noExceptions = false;
        					Platform.runLater(() -> {
        						textArea.appendText(e.getMessage() + "\n");
        						textArea.appendText(ExceptionUtils.getStackTrace(e) + "\n");
        					});
						}
                		try {
        					WindowsPlayer.buyMaps(1);
        				} catch (Exception e1) {
        					noExceptions = false;
        					e1.printStackTrace();
							Platform.exit();
        					Platform.runLater(() -> {
        						textArea.appendText(e1.getMessage() + "\n");
        						textArea.appendText(ExceptionUtils.getStackTrace(e1) + "\n");
        					});
        				}
                		try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
        					e.printStackTrace();
							Platform.exit();							
						}
        			}
				}
        	};
        	WinRobot.focusOnWindow();
        	primaryStage.setScene(bigButtonScene);
        	primaryStage.setAlwaysOnTop(false);
        	primaryStage.setMaximized(true);
        	t.start();
        });
        
        HBox btnBox = new HBox(btn, buyMapsButton, playForeverButton);
        btnBox.setSpacing(5);
		
		
		VBox vbox = new VBox(toolbar, btnBox, spinnerPane, textArea);
		Menu infoMenu = new Menu("Info");
		menuBar.getMenus().add(infoMenu);
		MenuItem aboutItem = new MenuItem("About");
		infoMenu.getItems().add(aboutItem);		
		
		Alert alert = createAboutMeAlert();
		aboutItem.setOnAction(event->{
			alert.showAndWait().ifPresent(rs -> {
//			    if (rs == ButtonType.OK) {
//			        System.out.println("Pressed OK.");
//			    }
			});
		});
		
		javafx.geometry.Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		primaryStage.setX(primaryScreenBounds.getMinX() + primaryScreenBounds.getWidth() - 300);
		primaryStage.setY(primaryScreenBounds.getMinY() + primaryScreenBounds.getHeight() - 300);
//		primaryStage.setX(10);
//		primaryStage.setY(10);
		Scene mainScene = new Scene(vbox, 300, 200);
		primaryStage.setScene(mainScene);
		primaryStage.setAlwaysOnTop(true);
		primaryStage.show();
//		window.hide();
		//WinRobot.findZoom(primaryStage);
		if(SplashWindow.FRAME_INSTANCE != null){
			SplashWindow.FRAME_INSTANCE.dispose();
		}
	}

	private void configureWindowsPlayer(Button btn, TextArea textArea, Spinner<Integer> spinner) {
		windowsPlayer = new WindowsPlayer();
		
		windowsPlayer.setStateChangeCallback(state ->{
//			textArea.setText(state.printBoard());
		});
		windowsPlayer.setFinishCallback(p ->{
			Platform.runLater(()->{
				btn.setText("Play");
				textArea.appendText("Press play to start game\n");
				windowsPlayer.gamesToPlay = 1;
				spinner.getValueFactory().setValue(windowsPlayer.gamesToPlay);
			});						
			
		});
		windowsPlayer.setSingleGameFinishCallback(wp -> {
			spinner.getValueFactory().setValue(wp.gamesToPlay);
		});
		windowsPlayer.setBestMoveCallback(m ->{
			textArea.appendText("Best move: " + m + "\n");
		});
		windowsPlayer.setExceptionCallback(e ->{
			windowsPlayer.suspended = true;
			Platform.runLater(()->{
				textArea.appendText(e.getMessage()+ "\n");
				textArea.appendText(ExceptionUtils.getStackTrace(e)+ "\n");
			});
		});
		windowsPlayer.setInfoCallback(m -> {
			Platform.runLater(()->{
				textArea.appendText(m);
			});
		});		

		
	}

	private Alert createAboutMeAlert() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("About me...");
		alert.setHeaderText(null);
		FlowPane fp = new FlowPane();
	    Label lbl1 = new Label("Tiny app to play");
	    Hyperlink link = new Hyperlink("Gems of War");
	    link.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
            	getHostServices().showDocument("http://gemsofwar.com");
            }
        });
	    Label lbl2 = new Label("treasure hunt mini game.");
	    fp.getChildren().addAll( lbl1, link, lbl2);
	    FlowPane fp2 = new FlowPane();
	    Label lbl3 = new Label("Author:");
	    Hyperlink link2 = new Hyperlink("dariuszmajewskius@gmail.com");
	    link2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
            	getHostServices().showDocument("mailto:dariuszmajewskius@gmail.com?subject=gowth%20message");
            }
        });	 
	    fp2.getChildren().addAll(lbl3, link2);
	    Label versionInfo = new Label(MessageFormat.format("Vesion: {0}, Built on: {1}", VERSION, BUILD_TIMESTAMP));
	    VBox vboxalert = new VBox(versionInfo, fp, fp2);
	    alert.getDialogPane().contentProperty().set(vboxalert);
	    return alert;
	}
}