package com.logfarm.gui;

import java.io.Serializable;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class TableVehicleClassesRow implements Serializable
{
	private transient TextField name;
	private transient TextField capacity;
	private transient Button removeButton;
	
	public TableVehicleClassesRow(TextField name, TextField capacity, Button removeButton)
	{
		this.name = name;
		this.capacity = capacity;
		this.removeButton = removeButton;
		
		Image img = new Image("file:resources/icons/abort.png");
	    ImageView view = new ImageView(img);
	    view.setFitHeight(18);
	    view.setPreserveRatio(true);
		this.removeButton.setGraphic(view);
	}
	
	public TextField getName()
	{
		return this.name;
	}
	
	public String getNameString()
	{
		return this.name.getText();
	}
	
	public TextField getCapacity()
	{
		return this.capacity;
	}
	
	public Button getRemoveButton()
	{
		return this.removeButton;
	}
}
