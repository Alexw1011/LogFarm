package com.logfarm.gui;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class DragIcon extends AnchorPane implements Serializable
{
	@FXML Label labelCaption;
	@FXML ImageView imageIcon;
	
	private DragIconType mType;
	
    public DragIcon() 
    {

        FXMLLoader fxmlLoader;
		try {
			fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(new URL("file:resources/fxml/DragIcon.fxml"));
			
	        fxmlLoader.setRoot(this);
	        fxmlLoader.setController(this);

	        try 
	        {
	            fxmlLoader.load();

	        } catch (IOException exception) 
	        {
	            throw new RuntimeException(exception);
	        }
		} catch (MalformedURLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @FXML
    private void initialize() 
    {
    }
    
    public DragIconType getType() { return mType;}

    public void setType(DragIconType type) {

        mType = type;

        getStyleClass().clear();
        getStyleClass().add("dragicon");
        switch (mType) 
        {  
	        case supplier:
	        	labelCaption.setText("Supplier");
	        	try 
				{
					imageIcon.setImage(new Image((new URL("file:resources/icons/facility_supplier.png").toString())));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        break;
	        
	        case manufacturer:
	        	labelCaption.setText("Manufacturer");
	        	try 
				{
					imageIcon.setImage(new Image((new URL("file:resources/icons/facility_manufacturer.png").toString())));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        break;
	        
	        case warehouse:
	        	labelCaption.setText("Warehouse");
	        	try 
				{
					imageIcon.setImage(new Image((new URL("file:resources/icons/facility_warehouse.png").toString())));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        break;
	        
	        case customer:
	        	labelCaption.setText("Customer");
	        	try 
				{
					imageIcon.setImage(new Image((new URL("file:resources/icons/facility_customer.png").toString())));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        break;
	
	        case customerGroup:
	        	labelCaption.setText("Customer Group");
				try 
				{
					imageIcon.setImage(new Image((new URL("file:resources/icons/facility_customers.png").toString())));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        break;	
	            
	        default:
	        break;
        }
    }
    
    public void relocateToPoint (Point2D p) 
    {

    	Point2D localCoords = getParent().sceneToLocal(p);
        //Point2D localCoords = new Point2D(getParent().sceneToLocal(p));

        relocate ((int) (localCoords.getX() - (getBoundsInLocal().getWidth() / 2)), (int) (localCoords.getY() - (getBoundsInLocal().getHeight() / 2)));
    }
}
