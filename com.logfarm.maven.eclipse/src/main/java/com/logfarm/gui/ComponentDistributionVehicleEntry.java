package com.logfarm.gui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.logfarm.gui.FacilityClipboard.VehicleOwnershipEntryClipboard;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

public class ComponentDistributionVehicleEntry extends HBox
{
	@FXML private HBox paneRoot;
    @FXML private ComboBox<TableVehicleClassesRow> cbVehicleClass;
    @FXML private TextField tfQuantity;
    
    private ObservableList<TableVehicleClassesRow> vehicleClassEntries;
    private ListChangeListener<TableVehicleClassesRow> vehicleClassEntriesListener;
    
    public ComponentDistributionVehicleEntry(ObservableList<TableVehicleClassesRow> vehicleClassEntries)
    {
    	FXMLLoader fxmlLoader;
    	fxmlLoader = new FXMLLoader();
		try 
		{
			fxmlLoader.setLocation(new URL("file:resources/fxml/ComponentDistributionVehicles.fxml"));
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
		
		vehicleClassEntriesListener = new ListChangeListener<TableVehicleClassesRow>() 
		{
            @Override
            public void onChanged(javafx.collections.ListChangeListener.Change<? extends TableVehicleClassesRow> c) { updateVehicleClasses(); } 
        };
		
        this.vehicleClassEntries = vehicleClassEntries;
        this.vehicleClassEntries.addListener(vehicleClassEntriesListener);
        updateVehicleClasses();
    }
    
    @FXML
    private void initialize() 
    {    	
    	cbVehicleClass.setConverter(new StringConverter<TableVehicleClassesRow>() 
    	{
		    @Override
		    public String toString(TableVehicleClassesRow object) {
		    	if(object == null) return "";
		        return object.getNameString();
		    }

		    @Override
		    public TableVehicleClassesRow fromString(String string) {
		        return null;
		    }
		});
    	
    	tfQuantity.setText("0");
    }
    
    private void updateVehicleClasses()
    {
    	if(vehicleClassEntries.size() > 0) cbVehicleClass.setItems(vehicleClassEntries);
    }
    
    public void saveToClipboard(FacilityClipboard facilityClipboard, boolean forStatic)
    {    	
    	VehicleOwnershipEntryClipboard clipboard = facilityClipboard.new VehicleOwnershipEntryClipboard();
    	
    	clipboard.vehicleClass = cbVehicleClass.getValue();
    	clipboard.numberOfVehicles = Integer.valueOf(tfQuantity.getText());
    	
    	if(forStatic) facilityClipboard.vehicleFleetStaticEntries.add(clipboard);
    	else facilityClipboard.vehicleFleetDynamicEntries.add(clipboard);
    }
    
    public void loadFromClipboard(VehicleOwnershipEntryClipboard clipboard, boolean forStatic)
    {
    	cbVehicleClass.setValue(clipboard.vehicleClass);
    	tfQuantity.setText(Integer.toString(clipboard.numberOfVehicles));
    }
}
