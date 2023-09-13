package com.logfarm.gui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.logfarm.gui.FacilityClipboard.DemandEntryClipboard;
import com.logfarm.gui.FacilityClipboard.DistributionClipboard;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class ComponentDemandEntry extends VBox
{
	@FXML private VBox paneRoot;
    @FXML private ComboBox<TableSKUsRow> cbSKU;
    @FXML private TextField tfWeight;
    @FXML private ComboBox<String> cbDistribution;
    @FXML private TextField tfParameter1;
    @FXML private TextField tfParameter2;
    
    private ObservableList<TableSKUsRow> skuEntries;
    private ListChangeListener<TableSKUsRow> skuEntriesListener;
    
    GUIController guic;
    
    public ComponentDemandEntry(ObservableList<TableSKUsRow> skuEntries, GUIController guic)
    {
    	this.guic = guic;
    	
    	FXMLLoader fxmlLoader;
    	fxmlLoader = new FXMLLoader();
		try 
		{
			fxmlLoader.setLocation(new URL("file:resources/fxml/ComponentDemandSKUEntry.fxml"));
			
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
        
        ObservableList<String> options = FXCollections.observableArrayList("Normal", "Gamma", "Exponential");
        cbDistribution.setItems(options);
        
        tfParameter1.setText("0.0");
        tfParameter2.setText("0.0");
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
    	
    	tfWeight.setText("0.0");
    }
    
    private void updateSKUs()
    {
    	if(skuEntries.size() > 0) cbSKU.setItems(skuEntries);
    }
    
    public void saveToClipboard(FacilityClipboard facilityClipboard)
    {    	
    	DemandEntryClipboard clipboard = facilityClipboard.new DemandEntryClipboard();
    	DistributionClipboard clipboardDistribution = clipboard.quantityDistribution;
    	
    	clipboard.sku = cbSKU.getValue();
    	clipboard.weight = Float.valueOf(tfWeight.getText());
    	clipboardDistribution.type = cbDistribution.getValue();
    	clipboardDistribution.param1 = Float.valueOf(tfParameter1.getText());
    	clipboardDistribution.param1DoE = guic.factorFieldIsActive(tfParameter1);
    	clipboardDistribution.param2 = Float.valueOf(tfParameter2.getText());
    	clipboardDistribution.param2DoE = guic.factorFieldIsActive(tfParameter2);
    	
    	facilityClipboard.demandEntries.add(clipboard);
    }
    
    public void loadFromClipboard(DemandEntryClipboard clipboard)
    {
    	DistributionClipboard clipboardDistribution = clipboard.quantityDistribution;
    	
    	cbSKU.setValue(clipboard.sku);
    	tfWeight.setText(Float.toString(clipboard.weight));
    	cbDistribution.setValue(clipboardDistribution.type);
    	tfParameter1.setText(Float.toString(clipboardDistribution.param1));
    	guic.setFactorField(tfParameter1, clipboardDistribution.param1DoE);
    	tfParameter2.setText(Float.toString(clipboardDistribution.param2));
    	guic.setFactorField(tfParameter2, clipboardDistribution.param2DoE);
    }
    
    @FXML
    private void factorFieldClicked(MouseEvent e)
    {
    	guic.factorFieldClicked(e);
    }
}
