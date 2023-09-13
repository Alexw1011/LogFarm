package com.logfarm.gui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.javatuples.Pair;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class SourcingEntry extends VBox
{
	@FXML private VBox paneRoot;
    @FXML private ComboBox<TableSKUsRow> cbSKU;
    @FXML private ComboBox<String> cbFacility;
    
    private ObservableList<TableSKUsRow> skuEntries;
    private ListChangeListener<TableSKUsRow> skuEntriesListener;
    
    private ArrayList<DraggableNode> currentlyDisplayedNodes;
    private ObservableList<String> options = FXCollections.observableArrayList();
    
    public SourcingEntry(ObservableList<TableSKUsRow> skuEntries)
    {
    	FXMLLoader fxmlLoader;
    	fxmlLoader = new FXMLLoader();
		try 
		{
			fxmlLoader.setLocation(new URL("file:resources/fxml/SourcingEntry.fxml"));
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
            public void onChanged(javafx.collections.ListChangeListener.Change<? extends TableSKUsRow> c) { updateSKUClasses(); } 
        };
		
        this.skuEntries = skuEntries;
        this.skuEntries.addListener(skuEntriesListener);
        updateSKUClasses();
        
        cbFacility.setItems(options);
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
    }
    
    private void updateSKUClasses()
    {
    	if(skuEntries.size() > 0) cbSKU.setItems(skuEntries);
    }
    
    public void saveToClipboard(FacilityClipboard facilityClipboard)
    {    
    	if(cbFacility.getSelectionModel().getSelectedIndex() == -1) return; // Dont save
    	facilityClipboard.sourcedBy.add(new Pair<TableSKUsRow, DraggableNode>(cbSKU.getValue(), currentlyDisplayedNodes.get(cbFacility.getSelectionModel().getSelectedIndex())));
    }
    
    public void loadFromClipboard(Pair<TableSKUsRow, DraggableNode> clipboardPair)
    {
    	cbSKU.setValue(clipboardPair.getValue0());
    	int indexOfOption = currentlyDisplayedNodes.indexOf(clipboardPair.getValue1());
    	cbFacility.getSelectionModel().select(indexOfOption);
    }
    
    public void setConnectedNodes(ArrayList<DraggableNode> nodes)
    {
    	currentlyDisplayedNodes = nodes;
    	options.clear();
    	for(DraggableNode node : nodes) options.add(node.getCaption());
    }
}
