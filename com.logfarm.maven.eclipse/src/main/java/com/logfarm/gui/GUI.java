package com.logfarm.gui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class GUI 
{
	public GUI()
	{
		
	}
	
	public void createAppGUI(Stage stage)
	{
		// Parametrize window
        stage.setTitle("LogFarm");		
		final double windowSizeX = 1280;
		final double windowSizeY = 720;
		
		FXMLLoader loader = new FXMLLoader();   	
    	try 
    	{
    		loader.setLocation(new URL("file:resources/fxml/main.fxml"));
        } 
    	catch (MalformedURLException e) 
    	{
            e.printStackTrace();
            return;
        }
    	
    	// Prepare the root node
        AnchorPane root;
		try {
			root = loader.<AnchorPane>load();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return;
		}
		
        // Finalize
        Scene scene = new Scene(root, windowSizeX, windowSizeY);
        try {
			scene.getStylesheets().add(new URL("file:resources/fxml/application.css").toExternalForm());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        stage.setScene(scene);
        GUIController controller = loader.getController();
        controller.setStage(stage);     
        stage.show();
	}
}
