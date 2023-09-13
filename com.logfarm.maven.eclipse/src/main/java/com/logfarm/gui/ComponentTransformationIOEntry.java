package com.logfarm.gui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.javatuples.Pair;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

public class ComponentTransformationIOEntry extends HBox
{
	@FXML private HBox root;
    @FXML private ComboBox<TableSKUsRow> cb;
    @FXML private TextField tf;
    
    private ObservableList<TableSKUsRow> skuEntries;
    private ListChangeListener<TableSKUsRow> skuEntriesListener;
    
    public ComponentTransformationIOEntry(ObservableList<TableSKUsRow> skuEntries)
    {
    	FXMLLoader fxmlLoader;
    	fxmlLoader = new FXMLLoader();
		try 
		{
			fxmlLoader.setLocation(new URL("file:resources/fxml/ComponentTransformationIOEntry.fxml"));
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
            public void onChanged(javafx.collections.ListChangeListener.Change<? extends TableSKUsRow> c) { update(); } 
        };
		
        this.skuEntries = skuEntries;
        this.skuEntries.addListener(skuEntriesListener);
        update();
    }
    
    @FXML
    private void initialize() 
    {    	
    	cb.setConverter(new StringConverter<TableSKUsRow>() 
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
    	
    	tf.setText("0");
    }
    
    private void update()
    {
    	if(skuEntries.size() > 0) cb.setItems(skuEntries);
    }
    
    public Pair<TableSKUsRow, Integer> getValues(boolean invertValue)
    {
    	int factor = 1;
    	if(invertValue) factor = -1;
    	return new Pair<TableSKUsRow, Integer>(cb.getValue(), Integer.parseInt(tf.getText()) * factor);
    }
    
    public void setValues(TableSKUsRow sku, Integer value)
    {
    	cb.setValue(sku);
    	tf.setText(Integer.toString(value));
    }
}
