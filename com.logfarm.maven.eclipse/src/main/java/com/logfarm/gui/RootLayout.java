package com.logfarm.gui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

public class RootLayout extends AnchorPane
{

    public RootLayout() 
    {

        FXMLLoader fxmlLoader;
		try {
			fxmlLoader = new FXMLLoader
					(
			    getClass().getResource(new URL("file:resources/fxml/RootLayout.fxml").toExternalForm())
			);
			
	        fxmlLoader.setRoot(this);
	        fxmlLoader.setController(this);

	        try {
	            fxmlLoader.load();

	        } catch (IOException exception) {
	            throw new RuntimeException(exception);
	        }
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @FXML
    private void initialize() 
    {
    }
}
