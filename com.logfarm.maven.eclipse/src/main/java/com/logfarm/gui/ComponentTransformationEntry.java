package com.logfarm.gui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.javatuples.Pair;

import com.logfarm.gui.FacilityClipboard.TransformationEntryClipboard;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class ComponentTransformationEntry extends VBox
{
	@FXML private VBox root;
    @FXML private TextField tfRate;
    @FXML private VBox vboxInputs;
    @FXML private VBox vboxOutputs;
    
    private ObservableList<TableSKUsRow> skuEntries;
    
    private ArrayList<ComponentTransformationIOEntry> inputs = new ArrayList<ComponentTransformationIOEntry>();
    private ArrayList<ComponentTransformationIOEntry> outputs = new ArrayList<ComponentTransformationIOEntry>();
    
    private GUIController guic;
    
    public ComponentTransformationEntry(ObservableList<TableSKUsRow> skuEntries, GUIController guic)
    {
    	this.guic = guic;
    	
    	FXMLLoader fxmlLoader;
    	fxmlLoader = new FXMLLoader();
		try 
		{
			fxmlLoader.setLocation(new URL("file:resources/fxml/ComponentTransformProcessMask.fxml"));
			
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
    	
    }
    
    @FXML
    private ComponentTransformationIOEntry addInput()
    {
    	ObservableList<Node> children = vboxInputs.getChildren();
    	ComponentTransformationIOEntry node = new ComponentTransformationIOEntry(skuEntries);
		children.add(node);
		inputs.add(node);
		return node;
    }
    
    @FXML
    private void removeInput()
    {
    	if(inputs.size() > 0)
		{
			ObservableList<Node> children = vboxInputs.getChildren();
			children.remove(children.size() - 1);
			inputs.remove(inputs.size() - 1);
		}
    }
    
    @FXML
    private ComponentTransformationIOEntry addOutput()
    {
    	ObservableList<Node> children = vboxOutputs.getChildren();
    	ComponentTransformationIOEntry node = new ComponentTransformationIOEntry(skuEntries);
		children.add(node);
		outputs.add(node);
		return node;
    }
    
    @FXML
    private void removeOutput()
    {
    	if(outputs.size() > 0)
		{
			ObservableList<Node> children = vboxOutputs.getChildren();
			children.remove(children.size() - 1);
			outputs.remove(outputs.size() - 1);
		}
    }
    
    public void saveToClipboard(FacilityClipboard facilityClipboard)
    {    	
    	TransformationEntryClipboard transformationEntryClipboard = facilityClipboard.new TransformationEntryClipboard();
    	transformationEntryClipboard.rate = Float.parseFloat(tfRate.getText());
    	transformationEntryClipboard.DoE = guic.factorFieldIsActive(tfRate);
    	transformationEntryClipboard.skuChanges = new ArrayList<Pair<TableSKUsRow, Integer>>();
    	for(ComponentTransformationIOEntry change : inputs) transformationEntryClipboard.skuChanges.add(change.getValues(true));
    	for(ComponentTransformationIOEntry change : outputs) transformationEntryClipboard.skuChanges.add(change.getValues(false));
    	
    	facilityClipboard.transformationEntries.add(transformationEntryClipboard);
    }
    
    public void loadFromClipboard(TransformationEntryClipboard clipboard)
    {
    	tfRate.setText(Float.toString(clipboard.rate));
    	guic.setFactorField(tfRate, clipboard.DoE);
    	for(Pair<TableSKUsRow, Integer> change : clipboard.skuChanges)
    	{
    		if(change.getValue1() >= 0)
    		{
    			ComponentTransformationIOEntry entry = addInput();
    			entry.setValues(change.getValue0(), change.getValue1());
    		}
    		else
    		{
    			ComponentTransformationIOEntry entry = addOutput();
    			entry.setValues(change.getValue0(), -1 * change.getValue1());
    		}
    	}
    }
    
    @FXML void factorFieldClicked(MouseEvent e)
	{
		guic.factorFieldClicked(e);
	}
}
