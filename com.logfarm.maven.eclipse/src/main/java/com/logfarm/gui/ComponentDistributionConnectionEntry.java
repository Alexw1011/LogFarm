package com.logfarm.gui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.logfarm.gui.FacilityClipboard.DistributionConnection;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

public class ComponentDistributionConnectionEntry extends GridPane
{
	@FXML private GridPane paneRoot;
    @FXML private Label labelName;
    @FXML private RadioButton rbStatic;
    @FXML private RadioButton rbDynamic;
    
    private DraggableNode nodeReference = null;
    private ToggleGroup toggleGroup = new ToggleGroup();
    
    private GUIController guic;
    private HashMap<DraggableNode, FacilityClipboard> facilityClipboards;
    
    public ComponentDistributionConnectionEntry(GUIController guic)
    {
    	this.guic = guic;
    	facilityClipboards = guic.getNodeToFacilityClipboardsMap();
    	
    	FXMLLoader fxmlLoader;
    	fxmlLoader = new FXMLLoader();
		try 
		{
			fxmlLoader.setLocation(new URL("file:resources/fxml/ComponentDistributionConnectionEntry.fxml"));
			
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
    	rbStatic.setToggleGroup(toggleGroup);
    	rbDynamic.setToggleGroup(toggleGroup);
    	
    	rbStatic.setSelected(true);
    }
    
    public DraggableNode getNodeReference()
    {
    	return nodeReference;
    }
    
    public void saveToClipboard(FacilityClipboard facilityClipboard)
    {    	
    	DistributionConnection clipboard = facilityClipboard.new DistributionConnection();
    	
    	clipboard.node = nodeReference;
    	clipboard.staticConnection = rbStatic.isSelected();
    	
    	facilityClipboard.connectionEntries.add(clipboard);
    }
    
    public void loadFromClipboard(DistributionConnection clipboard)
    {
    	nodeReference = clipboard.node;
    	//labelName.setText(nodeReference.getId().toString());
    	labelName.setText(facilityClipboards.get(nodeReference).name);
    	rbStatic.setSelected(clipboard.staticConnection);
    	rbDynamic.setSelected(!clipboard.staticConnection);
    }
}
