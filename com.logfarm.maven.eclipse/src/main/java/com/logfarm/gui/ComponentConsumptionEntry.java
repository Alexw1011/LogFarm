package com.logfarm.gui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.logfarm.gui.FacilityClipboard.ConsumptionEntryClipboard;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

public class ComponentConsumptionEntry extends GridPane
{
	@FXML private GridPane paneRoot;
    @FXML private Label labelRate;
    @FXML private ComboBox<TableSKUsRow> cbSKUs;
    @FXML private TextField tfRate;
    
    private ObservableList<TableSKUsRow> skuEntries;
    private ListChangeListener<TableSKUsRow> skuEntriesListener;
    
    GUIController guic;
    
    public ComponentConsumptionEntry(ObservableList<TableSKUsRow> skuEntries, GUIController guic)
    {
    	this.guic = guic;
    	
    	FXMLLoader fxmlLoader;
    	fxmlLoader = new FXMLLoader();
		try 
		{
			fxmlLoader.setLocation(new URL("file:resources/fxml/ComponentSinksEntry.fxml"));
			
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
    	cbSKUs.setConverter(new StringConverter<TableSKUsRow>() 
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
    	
    	tfRate.setText("0.0");
    }
    
    private void updateSKUs()
    {
    	if(skuEntries.size() > 0) cbSKUs.setItems(skuEntries);
    }
    
    public void saveToClipboard(FacilityClipboard facilityClipboard)
    {    	
    	ConsumptionEntryClipboard clipboard = facilityClipboard.new ConsumptionEntryClipboard();
    	
    	clipboard.sku = cbSKUs.getValue();
    	clipboard.rate = Float.valueOf(tfRate.getText());
    	clipboard.DoE = guic.factorFieldIsActive(tfRate);
    	
    	facilityClipboard.consumptionEntries.add(clipboard);
    }
    
    public void loadFromClipboard(ConsumptionEntryClipboard clipboard)
    {
    	cbSKUs.setValue(clipboard.sku);
    	tfRate.setText(Float.toString(clipboard.rate));
    	guic.setFactorField(tfRate, clipboard.DoE);
    }
    
    @FXML
    private void factorFieldClicked(MouseEvent e)
    {
    	guic.factorFieldClicked(e);
    }
}
