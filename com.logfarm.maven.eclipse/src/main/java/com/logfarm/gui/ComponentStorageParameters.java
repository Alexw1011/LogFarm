package com.logfarm.gui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.logfarm.gui.FacilityClipboard.StorageEntryClipboard;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class ComponentStorageParameters extends VBox
{
	@FXML private VBox paneRoot;
	@FXML private TextField tfCapacity;
	@FXML private TextField tfInitialFill;
    
    @FXML private VBox vboxSKUDetails;
    
    private ArrayList<ComponentStorageSKUParameters> skuDetails = new ArrayList<ComponentStorageSKUParameters>();
    private ObservableList<TableSKUsRow> skuEntries;
    
    GUIController guic;
    
    public ComponentStorageParameters(ObservableList<TableSKUsRow> skuEntries, GUIController guic) 
    {
    	this.guic = guic;
    	
    	FXMLLoader fxmlLoader;
    	fxmlLoader = new FXMLLoader();
		try 
		{
			fxmlLoader.setLocation(new URL("file:resources/fxml/Storage.fxml"));
			
			fxmlLoader.setRoot(this);
	        fxmlLoader.setController(this);
	        
	        this.skuEntries = skuEntries;

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
		tfCapacity.setText("0");
		tfInitialFill.setText("0");
    }
    
    @FXML
    private void addSKU()
    {
    	ObservableList<Node> children = vboxSKUDetails.getChildren();
		if(children.size() < 5)
		{
			ComponentStorageSKUParameters node = new ComponentStorageSKUParameters(skuEntries, guic);
			children.add(children.size(), node);
			skuDetails.add(node);
		}
    }
    
    @FXML
    private void removeSKU()
    {
    	ObservableList<Node> children = vboxSKUDetails.getChildren();
		if(children.size() > 0)
		{
			children.remove(children.size() - 1);
			skuDetails.remove(skuDetails.size() - 1);
		}	
    }
    
    public void saveToClipboard(FacilityClipboard facilityClipboard)
    {    	
    	StorageEntryClipboard clipboard = facilityClipboard.new StorageEntryClipboard();
    	
    	clipboard.capacity = Float.valueOf(tfCapacity.getText());
    	clipboard.capacityDoE = guic.factorFieldIsActive(tfCapacity);
    	clipboard.initialFill = Float.valueOf(tfInitialFill.getText());
    	clipboard.initialFillDoE = guic.factorFieldIsActive(tfInitialFill);
    	
    	clipboard.storageSKUEntries.clear();
		for(ComponentStorageSKUParameters entry : skuDetails) entry.saveToClipboard(clipboard);
    	
    	facilityClipboard.storageEntries.add(clipboard);
    }
    
    public void loadFromClipboard(StorageEntryClipboard clipboard)
    {
    	tfCapacity.setText(Float.toString(clipboard.capacity));
    	guic.setFactorField(tfCapacity, clipboard.capacityDoE);
    	tfInitialFill.setText(Float.toString(clipboard.initialFill));
    	guic.setFactorField(tfInitialFill, clipboard.initialFillDoE);
    	
    	prepareNumberOfSKUDetailsEntries(clipboard.storageSKUEntries.size());
		for(int i = 0; i < clipboard.storageSKUEntries.size(); i++) skuDetails.get(i).loadFromClipboard(clipboard.storageSKUEntries.get(i));
    } 
    
	private void prepareNumberOfSKUDetailsEntries(int number)
	{
		while(skuDetails.size() != number)
		{
			if(skuDetails.size() < number) addSKU();
			else removeSKU();
		}
	}
	
	@FXML
    private void factorFieldClicked(MouseEvent e)
    {
    	guic.factorFieldClicked(e);
    }
}
