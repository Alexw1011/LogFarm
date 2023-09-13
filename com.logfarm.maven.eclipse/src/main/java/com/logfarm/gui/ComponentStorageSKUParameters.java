package com.logfarm.gui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.logfarm.gui.FacilityClipboard.StorageEntryClipboard;
import com.logfarm.gui.FacilityClipboard.StorageEntryClipboard.StorageSKUEntryClipboard;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

public class ComponentStorageSKUParameters extends HBox
{
	@FXML private HBox paneRoot;
	@FXML private ComboBox<TableSKUsRow> cbSKU;
	@FXML private ComboBox<String> cbPolicy;
	@FXML private TextField textParam1;
	@FXML private TextField textParam2;
	
	private ObservableList<TableSKUsRow> skuEntries;
	private ListChangeListener<TableSKUsRow> skuEntriesListener;
	
	private GUIController guic;
	
	public ComponentStorageSKUParameters(ObservableList<TableSKUsRow> skuEntries, GUIController guic) 
    {
		this.guic = guic;
		
    	FXMLLoader fxmlLoader;
    	fxmlLoader = new FXMLLoader();
		try 
		{
			fxmlLoader.setLocation(new URL("file:resources/fxml/StorageSKUDetails.fxml"));
			
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
		
		
		skuEntriesListener = new ListChangeListener<TableSKUsRow>() 
		{
            @Override
            public void onChanged(javafx.collections.ListChangeListener.Change<? extends TableSKUsRow> c) { updateSKUs(); } 
        };
		
        this.skuEntries = skuEntries;
        this.skuEntries.addListener(skuEntriesListener);
        updateSKUs();
    }
    
    @FXML
    private void initialize() 
    {    	
    	cbSKU.setConverter(new StringConverter<TableSKUsRow>() 
    	{
		    @Override
		    public String toString(TableSKUsRow object) {
		    	if(object == null) return "";
		        return object.getNameString();
		    }

		    @Override
		    public TableSKUsRow fromString(String string) {
		        return null;
		    }
		});
    	
    	ObservableList<String> options = FXCollections.observableArrayList("(s,q)", "(r,s)", "(s,S)");
    	cbPolicy.setItems(options);
    }
    
    private void updateSKUs()
    {
    	if(skuEntries.size() > 0) cbSKU.setItems(skuEntries);
    }
    
    public void saveToClipboard(StorageEntryClipboard storageClipboard)
    {    	
    	StorageSKUEntryClipboard clipboard = storageClipboard.new StorageSKUEntryClipboard();
    	
    	clipboard.sku = cbSKU.getValue();
    	clipboard.policy = cbPolicy.getValue();
    	clipboard.param1 = Integer.parseInt(textParam1.getText());
    	clipboard.param1DoE = guic.factorFieldIsActive(textParam1);
    	clipboard.param2 = Integer.parseInt(textParam2.getText());
    	clipboard.param2DoE = guic.factorFieldIsActive(textParam2);
    	
    	storageClipboard.storageSKUEntries.add(clipboard);
    }
    
    public void loadFromClipboard(StorageSKUEntryClipboard clipboard)
    {
    	cbSKU.setValue(clipboard.sku);
    	cbPolicy.setValue(clipboard.policy);
    	textParam1.setText(Integer.toString(clipboard.param1));
    	guic.setFactorField(textParam1, clipboard.param1DoE);
    	textParam2.setText(Integer.toString(clipboard.param2));
    	guic.setFactorField(textParam2, clipboard.param2DoE);
    }  
    
    @FXML
    private void factorFieldClicked(MouseEvent e)
    {
    	guic.factorFieldClicked(e);
    }
}
