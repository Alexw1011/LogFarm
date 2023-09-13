package com.logfarm.gui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.javatuples.Pair;

import com.logfarm.engine.ExperimentManager;
import com.logfarm.engine.SimulationParameters;
import com.logfarm.gui.FacilityClipboard.ConsumptionEntryClipboard;
import com.logfarm.gui.FacilityClipboard.DemandEntryClipboard;
import com.logfarm.gui.FacilityClipboard.DistributionClipboard;
import com.logfarm.gui.FacilityClipboard.DistributionConnection;
import com.logfarm.gui.FacilityClipboard.ProductionEntryClipboard;
import com.logfarm.gui.FacilityClipboard.StorageEntryClipboard;
import com.logfarm.gui.FacilityClipboard.TransformationEntryClipboard;
import com.logfarm.gui.FacilityClipboard.VehicleOwnershipEntryClipboard;
import com.logfarm.gui.FacilityClipboard.StorageEntryClipboard.StorageSKUEntryClipboard;
import com.logfarm.io.Neo4jConnector;
import com.logfarm.io.Neo4jHandler;
import com.logfarm.model.ComponentConsumption;
import com.logfarm.model.ComponentDemandGenerator;
import com.logfarm.model.ComponentDistribution;
import com.logfarm.model.ComponentProduction;
import com.logfarm.model.ComponentStorages;
import com.logfarm.model.ComponentTransformation;
import com.logfarm.model.Distribution;
import com.logfarm.model.DoECoupler;
import com.logfarm.model.DoELink;
import com.logfarm.model.DynamicRoutingGroup;
import com.logfarm.model.Facility;
import com.logfarm.model.Malfunction;
import com.logfarm.model.Model;
import com.logfarm.model.SKU;
import com.logfarm.model.SKUPicker;
import com.logfarm.model.SKUPickerEntry;
import com.logfarm.model.SKUPickerWeight;
import com.logfarm.model.SKUStorageDetails;
import com.logfarm.model.Sink;
import com.logfarm.model.Source;
import com.logfarm.model.Storage;
import com.logfarm.model.TransformationProcess;
import com.logfarm.model.TransportRelation;
import com.logfarm.model.VehicleClass;
import com.logfarm.model.VehicleFleet;
import com.logfarm.utility.DistanceCalculator;
import com.logfarm.utility.Logger;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GUIController implements Initializable
{
	@FXML private AnchorPane panePlaceableArea;
	@FXML private GridPane gridAddBlocks;
	@FXML private AnchorPane paneRoot;
	
	@FXML private TableView<TableSKUsRow> tableSKUs;
	@FXML private TableColumn<TableSKUsRow, String> tableSKUsNames;
	@FXML private TableColumn<TableSKUsRow, Button> tableSKUsRemoveButtons;
	@FXML private Label labelNumberOfSKUs;
	@FXML private Button buttonAddSKU;	
	ObservableList<TableSKUsRow> tableSKUsItems;
	
	@FXML private TableView<TableVehicleClassesRow> tableVehicleClasses;
	@FXML private TableColumn<TableVehicleClassesRow, String> tableVehicleClassesNames;
	@FXML private TableColumn<TableVehicleClassesRow, Integer> tableVehicleClassesCapacities;
	@FXML private TableColumn<TableVehicleClassesRow, Button> tableVehicleClassesRemoveButtons;
	@FXML private Label labelNumberOfVehicleClasses;
	@FXML private Button btnAddVehicleClass;	
	ObservableList<TableVehicleClassesRow> tableVehicleClassesItems;
	
	// General
	@FXML private TextField tfName;
	@FXML private TextField tfLat;
	@FXML private TextField tfLon;
	@FXML private TextField tfMon;
	@FXML private TextField tfTue;
	@FXML private TextField tfWed;
	@FXML private TextField tfThr;
	@FXML private TextField tfFri;
	@FXML private TextField tfSat;
	@FXML private TextField tfSun;
	@FXML private HBox hboxCustomerGroup;
	@FXML private TextField tfNumberOfCustomers;
	@FXML private TextField tfLatMin;
	@FXML private TextField tfLatMax;
	@FXML private TextField tfLonMin;
	@FXML private TextField tfLonMax;
	
	// Production
	@FXML private CheckBox checkProductionComponent;
	@FXML private VBox vboxProduction;
	@FXML private VBox vboxProductionEntries;
	@FXML private CheckBox cbProductionMalfunction;
	@FXML private TextField tfProductionMalfunctionChance;
	@FXML private ComboBox<String> cbProductionMalfunctionDistribution;
	@FXML private TextField tfProductionMalfunctionParam1;
	@FXML private TextField tfProductionMalfunctionParam2;
	@FXML private HBox hboxProductionMalfunctionChance;
	@FXML private HBox hboxProductionMalfunctionDistribution;
	
	// Transform
	@FXML private CheckBox checkTransformComponent;
	@FXML private VBox vboxTransform;
	@FXML private VBox vboxTransformationEntries;
	@FXML private CheckBox cbTransformMalfunction;
	@FXML private TextField tfTransformMalfunctionChance;
	@FXML private ComboBox<String> cbTransformMalfunctionDistribution;
	@FXML private TextField tfTransformMalfunctionParam1;
	@FXML private TextField tfTransformMalfunctionParam2;
	@FXML private HBox hboxTransformMalfunctionChance;
	@FXML private HBox hboxTransformMalfunctionDistribution;
	
	// Sinks
	@FXML private CheckBox checkSinkComponent;
	@FXML private VBox vboxSinks;
	
	// Storage
	@FXML private CheckBox checkStorageComponent;
	@FXML private VBox vboxStorage;
	
	// Distribution
	@FXML private CheckBox checkDistributionComponent;
	@FXML private VBox vboxDistribution;
	@FXML private VBox vboxVehicleFleetStatic;
	@FXML private VBox vboxVehicleFleetDynamic;
	@FXML private VBox vboxConnectionEntries;	
	
	// Demand
	@FXML private CheckBox checkDemandComponent; 
	@FXML private VBox vboxDemand;
	@FXML private ComboBox<String> cbDemandDistribution;
	@FXML private TextField tfDemandDistributionParam1;
	@FXML private TextField tfDemandDistributionParam2;
	@FXML private VBox vboxDemandEntries;	
	
	// Experiments
	@FXML private ProgressBar pbExperiment;
	@FXML private Label labelExperimentProgress;
	@FXML private Label labelExperimentTime;
	@FXML private DatePicker dpStart;
	@FXML private DatePicker dpWarmUp;
	@FXML private DatePicker dpEnd;
	@FXML private TextField tfThreadPoolSize;
	@FXML private TextField tfSeed;
	@FXML private TextField tfReplications;
	@FXML private ImageView iconStatus;
	
	// Tabs
	@FXML private TabPane tabPaneParametrize;
	@FXML private Tab tabGeneral;
	@FXML private Tab tabProduction;
	@FXML private Tab tabTransform;
	@FXML private Tab tabSink;
	@FXML private Tab tabStorage;
	@FXML private Tab tabDistribution;
	@FXML private Tab tabDemand;
	@FXML private Tab tabSourcing;
	
	// Sourcing
	@FXML private VBox vboxSourcingEntries;
	
	@FXML private Label labelExperimentPlanFile;
	@FXML private Button startButton;
	
	Stage stage;
	
	private DragIcon dragOverIcon = null;
	
	private EventHandler<DragEvent> iconDragOverRoot = null;
	private EventHandler<DragEvent> iconDragDropped = null;
	private EventHandler<DragEvent> iconDragOverRightPane = null;
	
	ArrayList<DraggableNode> placedNodes = new ArrayList<DraggableNode>();
	DraggableNode lastSelectedNode = null;
	HashMap<DraggableNode, FacilityClipboard> facilityClipboards = new HashMap<DraggableNode, FacilityClipboard>();
	FacilityClipboard activeClipboard = null;
	
	ArrayList<ProductionProduceSKU> productionProduceSKUEntries = new ArrayList<ProductionProduceSKU>();
	ArrayList<ComponentConsumptionEntry> componentSinksEntries = new ArrayList<ComponentConsumptionEntry>();
	ArrayList<ComponentStorageParameters> storageParameters = new ArrayList<ComponentStorageParameters>();
	ArrayList<ComponentDistributionConnectionEntry> distributionConnectionEntries = new ArrayList<ComponentDistributionConnectionEntry>();
	ArrayList<ComponentDistributionVehicleEntry> compomentDistributionStaticVehicleFleetEntries = new ArrayList<ComponentDistributionVehicleEntry>();
	ArrayList<ComponentDistributionVehicleEntry> compomentDistributionDynamicVehicleFleetEntries = new ArrayList<ComponentDistributionVehicleEntry>();
	ArrayList<ComponentDemandEntry> componentDemandEntries = new ArrayList<ComponentDemandEntry>();
	ArrayList<ComponentTransformationEntry> componentTransformationEntries = new ArrayList<ComponentTransformationEntry>();
	ArrayList<SourcingEntry> sourcingEntries = new ArrayList<SourcingEntry>();
	
	private final int maxSKUs = 10;
	
	Timeline timeline;
	
	SimpleBooleanProperty ctrlPressed = new SimpleBooleanProperty();
	String doeStyle = "-fx-background-color:orange;";
	
	// Experiment parameters
	Model loadedModel = null;
	File loadedExperimentPlanFile;
	int loadedThreadPoolSize = 0;
	int loadedSeed = 0;
	int loadedReplications = 0;
	boolean logRun = false;
	SimulationParameters loadedSimulationParameters = null;
	
	@Override
	public void initialize(URL _url, ResourceBundle _rb)
	{
		// This is the dummy for drag and drop operations
		dragOverIcon = new DragIcon();		 
		dragOverIcon.setVisible(false);
		dragOverIcon.setOpacity(0.65);
		paneRoot.getChildren().add(dragOverIcon); 

	    // Populate grid with icons
	    for (int i = 0; i < DragIconType.values().length; i++) 
	    { 
	        DragIcon icn = new DragIcon();
	        addDragDetection(icn);
	 
	        icn.setType(DragIconType.values()[i]);
	        int column = i % 2;
	        int row = i / 2;
	        gridAddBlocks.add(icn, column, row);
	    }
	    
	    buildDragHandlers();
	    
	    tableSKUsNames.setCellValueFactory(new PropertyValueFactory<TableSKUsRow, String>("name"));
		tableSKUsRemoveButtons.setCellValueFactory(new PropertyValueFactory<TableSKUsRow, Button>("removeButton"));		
		tableSKUs.setEditable(true);
		tableSKUsItems = FXCollections.observableArrayList();
		updateTableSKUs();
		
		tableVehicleClassesNames.setCellValueFactory(new PropertyValueFactory<TableVehicleClassesRow, String>("name"));
		tableVehicleClassesCapacities.setCellValueFactory(new PropertyValueFactory<TableVehicleClassesRow, Integer>("capacity"));
		tableVehicleClassesRemoveButtons.setCellValueFactory(new PropertyValueFactory<TableVehicleClassesRow, Button>("removeButton"));		
		tableVehicleClasses.setEditable(true);
		tableVehicleClassesItems = FXCollections.observableArrayList();
		updateTableVehicleClasses();
		
		ObservableList<String> options = FXCollections.observableArrayList("Normal", "Gamma", "Exponential");
        cbDemandDistribution.setItems(options);
        cbProductionMalfunctionDistribution.setItems(options);
        cbTransformMalfunctionDistribution.setItems(options);
        
        tfDemandDistributionParam1.setText("0.0");
        tfDemandDistributionParam2.setText("0.0");
        
        updateAllowedTabs();
	}
	
	@FXML
	private void changedName()
	{
		lastSelectedNode.changeCaption(tfName.getText());
	}
	
	public void setStage(Stage stage)
	{
		this.stage = stage;
		
		// Listen to CTRL
		stage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
            	ctrlPressed.setValue(true);
            }
        });

		stage.getScene().setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
            	ctrlPressed.setValue(false);
            }
        });
	}
	
	private void buildDragHandlers() 
	{
		//drag over transition to move widget form left pane to right pane
		iconDragOverRoot = new EventHandler <DragEvent>() 
		{
	        @Override
	        public void handle(DragEvent event) 
	        {
	            Point2D p = panePlaceableArea.sceneToLocal(event.getSceneX(), event.getSceneY());

	            if (!panePlaceableArea.boundsInLocalProperty().get().contains(p)) 
	            {
	            	dragOverIcon.relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));
	                return;
	            }
	            event.consume();
	        }
	    };
	 
	    iconDragOverRightPane = new EventHandler <DragEvent> () 
	    {
	        @Override
	        public void handle(DragEvent event) 
	        {

	            event.acceptTransferModes(TransferMode.ANY);
	 
	            dragOverIcon.relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));

	            event.consume();
	        }
	    };
	 
	    iconDragDropped = new EventHandler <DragEvent> () 
	    {
	        @Override
	        public void handle(DragEvent event) 
	        {
	        	DragContainer container = (DragContainer) event.getDragboard().getContent(DragContainer.AddNode);

                container.addData("scene_coords", new Point2D(event.getSceneX(), event.getSceneY()));

                ClipboardContent content = new ClipboardContent();
                content.put(DragContainer.AddNode, container);

                event.getDragboard().setContent(content);
                event.setDropCompleted(true);
	        }
	    };

	    paneRoot.setOnDragDone (new EventHandler<DragEvent>()
	    {     
		    @Override
		    public void handle (DragEvent event) 
		    {
		    	panePlaceableArea.removeEventHandler(DragEvent.DRAG_OVER, iconDragOverRightPane);
		        panePlaceableArea.removeEventHandler(DragEvent.DRAG_DROPPED, iconDragDropped);
		        paneRoot.removeEventHandler(DragEvent.DRAG_OVER, iconDragOverRoot);
		                
		        dragOverIcon.setVisible(false);
		        
		        DragContainer container = (DragContainer) event.getDragboard().getContent(DragContainer.AddNode);
	
		        if (container != null) 
		        {
		        	if (container.getValue("scene_coords") != null) 
		        	{ 
			            DraggableNode node = new DraggableNode();
			                                
			            node.setType(DragIconType.valueOf(container.getValue("type").toString()));
			            panePlaceableArea.getChildren().add(node);
		
			            Point2D cursorPoint = container.getValue("scene_coords");
		
			            node.relocateToPoint(new Point2D(cursorPoint.getX() - 50, cursorPoint.getY() - 67.5), false);
			            
			            // Add node to placed list
			            placedNodes.add(node);
			            // Add an empty clipboard
			            FacilityClipboard newFacilityClipboard = new FacilityClipboard();
			            newFacilityClipboard.type = node.getType();
			            newFacilityClipboard.name = node.getType().toString();
			            facilityClipboards.put(node, newFacilityClipboard);
			            activateNeededComponents(newFacilityClipboard, node.getType());
			            if(newFacilityClipboard.type == DragIconType.customerGroup) newFacilityClipboard.isCustomerGroup = true;
			            // Bind its selection to this controller
			            node.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent> () 
			            {
			                @Override
			                public void handle(MouseEvent event) 
			                {
			                	DraggableNode node = (DraggableNode)event.getSource();
			                	
			                	if(lastSelectedNode != null) lastSelectedNode.setSelected(false);
			                	node.setSelected(true);
			                	setActiveClipboard(facilityClipboards.get(node));
			                	lastSelectedNode = node;
			                    event.consume();
			                    // Update
			                    updateAllowedTabs();
			                }                    
			            });     
		            }
		         }
		                
		        //AddLink drag operation
                container = (DragContainer) event.getDragboard().getContent(DragContainer.AddLink);
				
				if (container != null) 
				{
					//bind the ends of our link to the nodes whose id's are stored in the drag container
					String sourceId = container.getValue("source");
					String targetId = container.getValue("target");

					if (sourceId != null && targetId != null) 
					{
					
						//	System.out.println(container.getData());
						NodeLink link = new NodeLink();
						
						//add our link at the top of the rendering order so it's rendered first
						panePlaceableArea.getChildren().add(0,link);
						
						DraggableNode source = null;
						DraggableNode target = null;
					
						for (Node n: panePlaceableArea.getChildren()) 
						{
							if (n.getId() == null) continue;
							if (n.getId().equals(sourceId)) source = (DraggableNode) n;
							if (n.getId().equals(targetId)) target = (DraggableNode) n;
						}
					
						if (source != null && target != null)
						{
							link.bindEnds(source, target);
							// Add links to clipboards
							facilityClipboards.get(source).AddConnection(target, true);
							facilityClipboards.get(target).AddConnection(source, true);
							newLinkEstablished();
						}
					}
					
				}
        
	        event.consume();
		    }
	    });
	}
	
	private void addDragDetection(DragIcon dragIcon) {

	    dragIcon.setOnDragDetected (new EventHandler <MouseEvent> () 
	    {
	        @Override
	        public void handle(MouseEvent event) 
	        {
	            // set the other drag event handles on their respective objects
	        	paneRoot.setOnDragOver(iconDragOverRoot);
	        	panePlaceableArea.setOnDragOver(iconDragOverRightPane);
	        	panePlaceableArea.setOnDragDropped(iconDragDropped);
	        
	            // get a reference to the clicked DragIcon object
	            DragIcon icn = (DragIcon) event.getSource();

	            //begin drag ops
	            dragOverIcon.setType(icn.getType());
	            dragOverIcon.relocateToPoint(new Point2D (event.getSceneX(), event.getSceneY()));

	            ClipboardContent content = new ClipboardContent();
	            DragContainer container = new DragContainer();

	            container.addData ("type", dragOverIcon.getType().toString());
	            content.put(DragContainer.AddNode, container);

	            dragOverIcon.startDragAndDrop (TransferMode.ANY).setContent(content);
	            dragOverIcon.setVisible(true);
	            dragOverIcon.setMouseTransparent(true);
	            event.consume();
	        }
	    });
	}
	
	@FXML
	private void addSKU()
	{
		if(tableSKUs.getItems().size() >= maxSKUs) return;
		
		Button button = createRemoveSKUButton();
		tableSKUsItems.add(new TableSKUsRow(new TextField("new_sku"), button));
		tableSKUs.setItems(tableSKUsItems);
		updateTableSKUs();
	}
	
	private Button createRemoveSKUButton()
	{
		Button button = new Button();
		EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() 
		{
            public void handle(ActionEvent e)
            {
        		// find in list
        		int itemOfButtonIndex = -1;
        		int c = 0;
        		for(TableSKUsRow item : tableSKUsItems)
        		{
        			if(item.getRemoveButton() == e.getSource())
        			{
        				itemOfButtonIndex = c;
        				break;
        			}
        			c++;
        		}
        		
        		if(itemOfButtonIndex > -1)
        		{
        			tableSKUsItems.remove(itemOfButtonIndex);
        			tableSKUs.setItems(tableSKUsItems);
        			updateTableSKUs();
        		}
            }
        };
        button.setOnAction(event);
		return button;
	}

	
	private void updateTableSKUs()
	{
		int numberOfItems = tableSKUs.getItems().size();
		this.labelNumberOfSKUs.setText(Integer.toString(numberOfItems) + "/" + Integer.toString(maxSKUs) + " SKUs");
	}
	
	private void updateTableVehicleClasses()
	{
		int numberOfItems = tableVehicleClasses.getItems().size();
		this.labelNumberOfVehicleClasses.setText(Integer.toString(numberOfItems) + "/" + Integer.toString(maxSKUs) + " Vehicle Classes");
	}
	
	@FXML
	private void addVehicleClass()
	{
		if(tableVehicleClasses.getItems().size() >= maxSKUs) return;
		
		Button button = createRemoveVehicleClassButton();
		tableVehicleClassesItems.add(new TableVehicleClassesRow(new TextField("new_vehicle"), new TextField("0"), button));
		tableVehicleClasses.setItems(tableVehicleClassesItems);
		updateTableVehicleClasses();
	}
	
	private Button createRemoveVehicleClassButton()
	{
		Button button = new Button();
		EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() 
		{
            public void handle(ActionEvent e)
            {
            	// find in list
        		int itemOfButtonIndex = -1;
        		int c = 0;
        		for(TableVehicleClassesRow item : tableVehicleClassesItems)
        		{
        			if(item.getRemoveButton() == e.getSource())
        			{
        				itemOfButtonIndex = c;
        				break;
        			}
        			c++;
        		}
        		
        		if(itemOfButtonIndex > -1)
        		{
        			tableVehicleClassesItems.remove(itemOfButtonIndex);
        			tableVehicleClasses.setItems(tableVehicleClassesItems);
        			updateTableVehicleClasses();
        		}
            }
        };
        button.setOnAction(event);
		return button;
	}
	
	public HashMap<DraggableNode, FacilityClipboard> getNodeToFacilityClipboardsMap() { return facilityClipboards; };
	
	@FXML
	private void tabParametrizeSelected()
	{
		tabProductionSelected();
		tabStorageSelected();
	}
	
	@FXML
	private void tabProductionSelected()
	{
		vboxProduction.setVisible(checkProductionComponent.isSelected());
	}
	
	@FXML
	private void tabTransformSelected()
	{
		vboxTransform.setVisible(checkTransformComponent.isSelected());
	}
	
	@FXML
	private void tabSinksSelected()
	{
		vboxSinks.setVisible(checkSinkComponent.isSelected());
	}
	
	@FXML
	private void tabStorageSelected()
	{
		vboxStorage.setVisible(checkStorageComponent.isSelected());
	}
	
	@FXML
	private void addProductionProcess()
	{
		ObservableList<Node> children = vboxProductionEntries.getChildren();
		ProductionProduceSKU node = new ProductionProduceSKU(tableSKUsItems, this);
		children.add(node);
		productionProduceSKUEntries.add(node);	
	}
	
	@FXML
	private void removeProductionProcess()
	{
		if(productionProduceSKUEntries.size() > 0)
		{
			ObservableList<Node> children = vboxProductionEntries.getChildren();
			children.remove(children.size() - 1);
			productionProduceSKUEntries.remove(productionProduceSKUEntries.size() - 1);
		}
	}
	
	@FXML
	private void addTransformationProcess()
	{
		ObservableList<Node> children = vboxTransformationEntries.getChildren();
		ComponentTransformationEntry node = new ComponentTransformationEntry(tableSKUsItems, this);
		children.add(node);
		componentTransformationEntries.add(node);
	}
	
	@FXML
	private void removeTransformationProcess()
	{
		if(componentTransformationEntries.size() > 0)
		{
			ObservableList<Node> children = vboxTransformationEntries.getChildren();
			children.remove(children.size() - 1);
			componentTransformationEntries.remove(componentTransformationEntries.size() - 1);
		}
	}
	
	@FXML
	private void buttonStorageAdd()
	{
		ObservableList<Node> children = vboxStorage.getChildren();
		if(children.size() < 6)
		{
			ComponentStorageParameters node = new ComponentStorageParameters(tableSKUsItems, this);
			children.add(children.size() - 1, node);
			storageParameters.add(node);
		}
	}
	
	@FXML
	private void buttonStorageRemove()
	{
		ObservableList<Node> children = vboxStorage.getChildren();
		if(children.size() > 1)
		{
			children.remove(children.size() - 2);
			storageParameters.remove(storageParameters.size() - 1);
		}	
	}
	
	@FXML
	private void tabDistributionSelected()
	{
		vboxDistribution.setVisible(checkDistributionComponent.isSelected());
		updateConnectionsDisplay();
	}
	
	@FXML
	private void addVehicleStatic()
	{
		ObservableList<Node> children = vboxVehicleFleetStatic.getChildren();
		ComponentDistributionVehicleEntry node = new ComponentDistributionVehicleEntry(tableVehicleClassesItems);
		children.add(node);
		compomentDistributionStaticVehicleFleetEntries.add(node);
	}
	
	@FXML
	private void removeVehicleStatic()
	{
		if(compomentDistributionStaticVehicleFleetEntries.size() > 0)
		{
			ObservableList<Node> children = vboxVehicleFleetStatic.getChildren();
			children.remove(children.size() - 1);
			compomentDistributionStaticVehicleFleetEntries.remove(compomentDistributionStaticVehicleFleetEntries.size() - 1);
		}
	}
	
	@FXML
	private void addVehicleDynamic()
	{
		ObservableList<Node> children = vboxVehicleFleetDynamic.getChildren();
		ComponentDistributionVehicleEntry node = new ComponentDistributionVehicleEntry(tableVehicleClassesItems);
		children.add(node);
		compomentDistributionDynamicVehicleFleetEntries.add(node);
	}
	
	@FXML
	private void removeVehicleDynamic()
	{
		if(compomentDistributionDynamicVehicleFleetEntries.size() > 0)
		{
			ObservableList<Node> children = vboxVehicleFleetDynamic.getChildren();
			children.remove(children.size() - 1);
			compomentDistributionDynamicVehicleFleetEntries.remove(compomentDistributionDynamicVehicleFleetEntries.size() - 1);
		}
	}
	
	@FXML
	private void tabDemandSelected()
	{
		vboxDemand.setVisible(checkDemandComponent.isSelected());
	}

	@FXML
	private void tabGeneralSelected()
	{
		
	}
	
	@FXML
	private void toggledCbProductionMalfunction()
	{
		boolean active = cbProductionMalfunction.isSelected();
		hboxProductionMalfunctionChance.setVisible(active);
		hboxProductionMalfunctionDistribution.setVisible(active);
		if(active)
		{
			if(activeClipboard.productionMalfunction == null) activeClipboard.productionMalfunction = activeClipboard.new MalfunctionClipboard();
			tfProductionMalfunctionChance.setText(Float.toString(activeClipboard.productionMalfunction.dailyChance));
			cbProductionMalfunctionDistribution.setValue(activeClipboard.productionMalfunction.distribution.type);
			tfProductionMalfunctionParam1.setText(Float.toString(activeClipboard.productionMalfunction.distribution.param1));
			tfProductionMalfunctionParam2.setText(Float.toString(activeClipboard.productionMalfunction.distribution.param2));	
		}
		else activeClipboard.productionMalfunction = null;
	}
	
	@FXML
	private void toggledCbTransformMalfunction()
	{
		boolean active = cbTransformMalfunction.isSelected();
		hboxTransformMalfunctionChance.setVisible(active);
		hboxTransformMalfunctionDistribution.setVisible(active);
		if(active)
		{
			if(activeClipboard.transformationMalfunction == null) activeClipboard.transformationMalfunction = activeClipboard.new MalfunctionClipboard();
			tfTransformMalfunctionChance.setText(Float.toString(activeClipboard.transformationMalfunction.dailyChance));
			cbTransformMalfunctionDistribution.setValue(activeClipboard.transformationMalfunction.distribution.type);
			tfTransformMalfunctionParam1.setText(Float.toString(activeClipboard.transformationMalfunction.distribution.param1));
			tfTransformMalfunctionParam2.setText(Float.toString(activeClipboard.transformationMalfunction.distribution.param2));	
		}
		else activeClipboard.transformationMalfunction = null;
	}
	
	
	private void setActiveClipboard(FacilityClipboard newActiveClipboard)
	{
		saveToClipboard();
		activeClipboard = newActiveClipboard;
		loadFromClipboard();
		
		updateCheckBoxVisibilities();
	}
	
	private void updateCheckBoxVisibilities()
	{
		vboxProduction.setVisible(checkProductionComponent.isSelected());
		vboxTransform.setVisible(checkTransformComponent.isSelected());
		vboxSinks.setVisible(checkSinkComponent.isSelected());
		vboxStorage.setVisible(checkStorageComponent.isSelected());
		vboxDistribution.setVisible(checkDistributionComponent.isSelected());
		vboxDemand.setVisible(checkDemandComponent.isSelected());
	}
	
	private void saveToClipboard()
	{
		if(activeClipboard == null) return;
		
		// General
		activeClipboard.name = tfName.getText();
		activeClipboard.latitude = Float.parseFloat(tfLat.getText());
		activeClipboard.longitude = Float.parseFloat(tfLon.getText());
		activeClipboard.workingHours = new float[] {Float.parseFloat(tfMon.getText()), Float.parseFloat(tfTue.getText()), Float.parseFloat(tfWed.getText()), Float.parseFloat(tfThr.getText()), Float.parseFloat(tfFri.getText()), Float.parseFloat(tfSat.getText()), Float.parseFloat(tfSun.getText())};
		if(activeClipboard.isCustomerGroup)
		{
			activeClipboard.customerGroupNumberOfCustomer = Integer.parseInt(tfNumberOfCustomers.getText());
			activeClipboard.customerGroupMinLon = Float.parseFloat(tfLonMin.getText());
			activeClipboard.customerGroupMaxLon = Float.parseFloat(tfLonMax.getText());
			activeClipboard.customerGroupMinLat = Float.parseFloat(tfLatMin.getText());
			activeClipboard.customerGroupMaxLat = Float.parseFloat(tfLatMax.getText());
		}
		
		// Production
		activeClipboard.hasComponentProduction = checkProductionComponent.isSelected();
		activeClipboard.productionEntries.clear();
		for(ProductionProduceSKU entry : productionProduceSKUEntries) entry.saveToClipboard(activeClipboard);
		activeClipboard.productionMalfunction = null;
		if(cbProductionMalfunction.isSelected())
		{
			activeClipboard.productionMalfunction = activeClipboard.new MalfunctionClipboard();
			activeClipboard.productionMalfunction.dailyChance = Float.parseFloat(tfProductionMalfunctionChance.getText());
			activeClipboard.productionMalfunction.DoE = factorFieldIsActive(tfProductionMalfunctionChance);
			activeClipboard.productionMalfunction.distribution = activeClipboard.new DistributionClipboard();
			activeClipboard.productionMalfunction.distribution.type = cbProductionMalfunctionDistribution.getValue();
			activeClipboard.productionMalfunction.distribution.param1 = Float.parseFloat(tfProductionMalfunctionParam1.getText());
			activeClipboard.productionMalfunction.distribution.param1DoE = factorFieldIsActive(tfProductionMalfunctionParam1);
			activeClipboard.productionMalfunction.distribution.param2 = Float.parseFloat(tfProductionMalfunctionParam2.getText());
			activeClipboard.productionMalfunction.distribution.param2DoE = factorFieldIsActive(tfProductionMalfunctionParam2);
		}
		
		// Transform
		activeClipboard.hasComponentTransformation = checkTransformComponent.isSelected();
		activeClipboard.transformationEntries.clear();
		for(ComponentTransformationEntry entry : componentTransformationEntries) entry.saveToClipboard(activeClipboard);
		activeClipboard.transformationMalfunction = null;
		if(cbTransformMalfunction.isSelected())
		{
			activeClipboard.transformationMalfunction = activeClipboard.new MalfunctionClipboard();
			activeClipboard.transformationMalfunction.dailyChance = Float.parseFloat(tfTransformMalfunctionChance.getText());
			activeClipboard.transformationMalfunction.DoE = factorFieldIsActive(tfTransformMalfunctionChance);
			activeClipboard.transformationMalfunction.distribution = activeClipboard.new DistributionClipboard();
			activeClipboard.transformationMalfunction.distribution.type = cbTransformMalfunctionDistribution.getValue();
			activeClipboard.transformationMalfunction.distribution.param1 = Float.parseFloat(tfTransformMalfunctionParam1.getText());
			activeClipboard.transformationMalfunction.distribution.param1DoE = factorFieldIsActive(tfTransformMalfunctionParam1);
			activeClipboard.transformationMalfunction.distribution.param2 = Float.parseFloat(tfTransformMalfunctionParam2.getText());
			activeClipboard.transformationMalfunction.distribution.param2DoE = factorFieldIsActive(tfTransformMalfunctionParam2);
		}
		
		// Sinks
		activeClipboard.hasComponentConsumption = checkSinkComponent.isSelected();
		activeClipboard.consumptionEntries.clear();
		for(ComponentConsumptionEntry entry : componentSinksEntries) entry.saveToClipboard(activeClipboard);
		
		// Storage
		activeClipboard.hasComponentStorage = checkStorageComponent.isSelected();
		activeClipboard.storageEntries.clear();
		for(ComponentStorageParameters entry : storageParameters) entry.saveToClipboard(activeClipboard);
		
		// Distribution
		activeClipboard.hasComponentDistribution = checkDistributionComponent.isSelected();
		activeClipboard.connectionEntries.clear();
		for(ComponentDistributionConnectionEntry entry : distributionConnectionEntries) entry.saveToClipboard(activeClipboard);
		activeClipboard.vehicleFleetStaticEntries.clear();
		for(ComponentDistributionVehicleEntry entry : compomentDistributionStaticVehicleFleetEntries) entry.saveToClipboard(activeClipboard, true);
		activeClipboard.vehicleFleetDynamicEntries.clear();
		for(ComponentDistributionVehicleEntry entry : compomentDistributionDynamicVehicleFleetEntries) entry.saveToClipboard(activeClipboard, false);
		
		// Demand
		activeClipboard.hasComponentDemand = checkDemandComponent.isSelected();
		activeClipboard.timeBetweenDemands.type = cbDemandDistribution.getValue();
		activeClipboard.timeBetweenDemands.param1 = Float.valueOf(tfDemandDistributionParam1.getText());
		activeClipboard.timeBetweenDemands.param1DoE = factorFieldIsActive(tfDemandDistributionParam1);
		activeClipboard.timeBetweenDemands.param2 = Float.valueOf(tfDemandDistributionParam2.getText());
		activeClipboard.timeBetweenDemands.param2DoE = factorFieldIsActive(tfDemandDistributionParam2);
		activeClipboard.demandEntries.clear();
		for(ComponentDemandEntry entry : componentDemandEntries) entry.saveToClipboard(activeClipboard);
		
		// Sourced by
		activeClipboard.sourcedBy = new ArrayList<Pair<TableSKUsRow, DraggableNode>>();
		for(SourcingEntry entry : sourcingEntries) entry.saveToClipboard(activeClipboard);
	}
	
	private void loadFromClipboard()
	{
		if(activeClipboard == null) return;
		
		// General
		tfName.setText(activeClipboard.name);
		tfLat.setText(Float.toString(activeClipboard.latitude));
		tfLon.setText(Float.toString(activeClipboard.longitude));
		tfMon.setText(Float.toString(activeClipboard.workingHours[0]));
		tfTue.setText(Float.toString(activeClipboard.workingHours[1]));
		tfWed.setText(Float.toString(activeClipboard.workingHours[2]));
		tfThr.setText(Float.toString(activeClipboard.workingHours[3]));
		tfFri.setText(Float.toString(activeClipboard.workingHours[4]));
		tfSat.setText(Float.toString(activeClipboard.workingHours[5]));
		tfSun.setText(Float.toString(activeClipboard.workingHours[6]));
		if(activeClipboard.isCustomerGroup)
		{
			tfNumberOfCustomers.setText(Integer.toString(activeClipboard.customerGroupNumberOfCustomer));
			tfLonMin.setText(Float.toString(activeClipboard.customerGroupMinLon));
			tfLonMax.setText(Float.toString(activeClipboard.customerGroupMaxLon));
			tfLatMin.setText(Float.toString(activeClipboard.customerGroupMinLat));
			tfLatMax.setText(Float.toString(activeClipboard.customerGroupMaxLat));
		}
		
		// Production
		checkProductionComponent.setSelected(activeClipboard.hasComponentProduction);
		prepareNumberOfProductionEntries(activeClipboard.productionEntries.size());
		for(int i = 0; i < activeClipboard.productionEntries.size(); i++) productionProduceSKUEntries.get(i).loadFromClipboard(activeClipboard.productionEntries.get(i));
		boolean productionMalfunctionActive = activeClipboard.productionMalfunction != null;		
		cbProductionMalfunction.setSelected(productionMalfunctionActive);
		if(activeClipboard.productionMalfunction != null)
		{
			setFactorField(tfProductionMalfunctionChance, activeClipboard.productionMalfunction.DoE);
			setFactorField(tfProductionMalfunctionParam1, activeClipboard.productionMalfunction.distribution.param1DoE);
			setFactorField(tfProductionMalfunctionParam2, activeClipboard.productionMalfunction.distribution.param2DoE);
		}
		else
		{
			setFactorField(tfProductionMalfunctionChance, false);
			setFactorField(tfProductionMalfunctionParam1, false);
			setFactorField(tfProductionMalfunctionParam2, false);
		}
		toggledCbProductionMalfunction();
		
		// Transform
		checkTransformComponent.setSelected(activeClipboard.hasComponentTransformation);
		prepareNumberOfTransformationEntries(activeClipboard.transformationEntries.size());
		for(int i = 0; i < activeClipboard.transformationEntries.size(); i++) componentTransformationEntries.get(i).loadFromClipboard(activeClipboard.transformationEntries.get(i));
		boolean transformationMalfunctionActive = activeClipboard.transformationMalfunction != null;		
		cbTransformMalfunction.setSelected(transformationMalfunctionActive);
		if(activeClipboard.transformationMalfunction != null)
		{
			setFactorField(tfTransformMalfunctionChance, activeClipboard.transformationMalfunction.DoE);
			setFactorField(tfTransformMalfunctionParam1, activeClipboard.transformationMalfunction.distribution.param1DoE);
			setFactorField(tfTransformMalfunctionParam2, activeClipboard.transformationMalfunction.distribution.param2DoE);
		}
		else
		{
			setFactorField(tfTransformMalfunctionChance, false);
			setFactorField(tfTransformMalfunctionParam1, false);
			setFactorField(tfTransformMalfunctionParam2, false);
		}
		toggledCbTransformMalfunction();
		
		// Sinks
		checkSinkComponent.setSelected(activeClipboard.hasComponentConsumption);
		prepareNumberOfSinkEntries(activeClipboard.consumptionEntries.size());
		for(int i = 0; i < activeClipboard.consumptionEntries.size(); i++) componentSinksEntries.get(i).loadFromClipboard(activeClipboard.consumptionEntries.get(i));
		
		// Storage
		checkStorageComponent.setSelected(activeClipboard.hasComponentStorage);
		prepareNumberOfStorageEntries(activeClipboard.storageEntries.size());
		for(int i = 0; i < activeClipboard.storageEntries.size(); i++) storageParameters.get(i).loadFromClipboard(activeClipboard.storageEntries.get(i));
		
		// Distribution
		checkDistributionComponent.setSelected(activeClipboard.hasComponentDistribution);
		updateConnectionsDisplay();
		prepareNumberOfVehicleFleetStatic(activeClipboard.vehicleFleetStaticEntries.size());
		for(int i = 0; i < activeClipboard.vehicleFleetStaticEntries.size(); i++) compomentDistributionStaticVehicleFleetEntries.get(i).loadFromClipboard(activeClipboard.vehicleFleetStaticEntries.get(i), true);
		prepareNumberOfVehicleFleetDynamic(activeClipboard.vehicleFleetDynamicEntries.size());
		for(int i = 0; i < activeClipboard.vehicleFleetDynamicEntries.size(); i++) compomentDistributionDynamicVehicleFleetEntries.get(i).loadFromClipboard(activeClipboard.vehicleFleetDynamicEntries.get(i), false);
		
		// Demand
		checkDemandComponent.setSelected(activeClipboard.hasComponentDemand);
		cbDemandDistribution.setValue(activeClipboard.timeBetweenDemands.type);
		tfDemandDistributionParam1.setText(Float.toString(activeClipboard.timeBetweenDemands.param1));
		setFactorField(tfDemandDistributionParam1, activeClipboard.timeBetweenDemands.param1DoE);
		tfDemandDistributionParam2.setText(Float.toString(activeClipboard.timeBetweenDemands.param2));
		setFactorField(tfDemandDistributionParam2, activeClipboard.timeBetweenDemands.param2DoE);
		prepareNumberOfDemandEntries(activeClipboard.demandEntries.size());
		for(int i = 0; i < activeClipboard.demandEntries.size(); i++) componentDemandEntries.get(i).loadFromClipboard(activeClipboard.demandEntries.get(i));
		
		// Sourcing
		prepareNumberOfSourcingEntries(activeClipboard.sourcedBy.size());
		for(int i = 0; i < activeClipboard.sourcedBy.size(); i++) sourcingEntries.get(i).loadFromClipboard(activeClipboard.sourcedBy.get(i));
	}
	
	private void prepareNumberOfProductionEntries(int number)
	{
		while(productionProduceSKUEntries.size() > 0) removeProductionProcess();
		while(productionProduceSKUEntries.size() < number) addProductionProcess();
	}
	
	private void prepareNumberOfTransformationEntries(int number)
	{
		while(componentTransformationEntries.size() > 0) removeTransformationProcess();
		while(componentTransformationEntries.size() < number) addTransformationProcess();
	}
	
	private void prepareNumberOfSourcingEntries(int number)
	{
		while(sourcingEntries.size() > 0) buttonSourcingRemove();
		while(sourcingEntries.size() < number) buttonSourcingAdd();
	}
	
	private void prepareNumberOfSinkEntries(int number)
	{
		while(componentSinksEntries.size() != number)
		{
			if(componentSinksEntries.size() < number) btnAddSinkAction();
			else btnRemoveSinkAction();
		}
	}
	
	private void prepareNumberOfVehicleFleetStatic(int number)
	{
		while(compomentDistributionStaticVehicleFleetEntries.size() != number)
		{
			if(compomentDistributionStaticVehicleFleetEntries.size() < number) addVehicleStatic();
			else removeVehicleStatic();
		}
	}
	
	private void prepareNumberOfVehicleFleetDynamic(int number)
	{
		while(compomentDistributionDynamicVehicleFleetEntries.size() != number)
		{
			if(compomentDistributionDynamicVehicleFleetEntries.size() < number) addVehicleDynamic();
			else removeVehicleDynamic();
		}
	}
	
	private void prepareNumberOfStorageEntries(int number)
	{
		while(storageParameters.size() != number)
		{
			if(storageParameters.size() < number) buttonStorageAdd();
			else buttonStorageRemove();
		}
	}
	
	private void prepareNumberOfDemandEntries(int number)
	{
		while(componentDemandEntries.size() != number)
		{
			if(componentDemandEntries.size() < number) buttonDemandAdd();
			else buttonDemandRemove();
		}
	}
	
	@FXML
	private void buttonDemandAdd()
	{
		ObservableList<Node> children = vboxDemandEntries.getChildren();
		ComponentDemandEntry node = new ComponentDemandEntry(tableSKUsItems, this);
		children.add(node);
		componentDemandEntries.add(node);
	}
	
	@FXML
	private void buttonDemandRemove()
	{
		if(componentDemandEntries.size() > 0)
		{
			ObservableList<Node> children = vboxDemandEntries.getChildren();
			children.remove(children.size() - 1);
			componentDemandEntries.remove(componentDemandEntries.size() - 1);
		}
	}
	
	@FXML void buttonSourcingAdd()
	{
		ObservableList<Node> children = vboxSourcingEntries.getChildren();
		SourcingEntry node = new SourcingEntry(tableSKUsItems);
		node.setConnectedNodes(activeClipboard.connectedNodes);
		children.add(node);
		sourcingEntries.add(node);
	}
	
	@FXML void buttonSourcingRemove()
	{
		if(sourcingEntries.size() > 0)
		{
			ObservableList<Node> children = vboxSourcingEntries.getChildren();
			children.remove(children.size() - 1);
			sourcingEntries.remove(sourcingEntries.size() - 1);
		}
	}

	
	@FXML
	private void btnAddSinkAction()
	{
		ObservableList<Node> children = vboxSinks.getChildren();
		if(children.size() < 6)
		{
			ComponentConsumptionEntry node = new ComponentConsumptionEntry(tableSKUsItems, this);
			children.add(children.size() - 1, node);
			componentSinksEntries.add(node);
		}
	}
	
	@FXML
	private void btnRemoveSinkAction()
	{
		ObservableList<Node> children = vboxSinks.getChildren();
		if(children.size() > 1)
		{
			children.remove(children.size() - 2);
			componentSinksEntries.remove(componentSinksEntries.size() - 1);
		}	
	}
	
	private void updateConnectionsDisplay()
	{
		if(activeClipboard == null) return;
		ArrayList<FacilityClipboard.DistributionConnection> connections = activeClipboard.connectionEntries;
		distributionConnectionEntries.clear();
		vboxConnectionEntries.getChildren().clear();
		// Construct new
		for(int i = 0; i < connections.size(); i++)
		{
			ComponentDistributionConnectionEntry newEntry = new ComponentDistributionConnectionEntry(this);
			newEntry.loadFromClipboard(connections.get(i));
			distributionConnectionEntries.add(newEntry);
			vboxConnectionEntries.getChildren().add(newEntry);
		}
	}
	
	private void updateSourcingEntries()
	{
		for(SourcingEntry entry : sourcingEntries) entry.setConnectedNodes(activeClipboard.connectedNodes);
	}
	
	private void newLinkEstablished()
	{
		if(tabDistribution.isSelected() && checkDistributionComponent.isSelected())
		{
			updateConnectionsDisplay();
		}
		
		updateSourcingEntries();
	}
	
	private void checkStartButtonRequirements()
	{
		if(loadedExperimentPlanFile != null && loadedThreadPoolSize > 0 && loadedModel != null)
		{
			startButton.setDisable(false);
		}
	}
	
	@FXML
	private void startExperiment()
	{
		
		long startLoadingModel = System.currentTimeMillis();
		Neo4jHandler neo4jHandler = new Neo4jHandler();
        loadedModel = neo4jHandler.loadModel(Neo4jConnector.singleton);
        long finishedLoadingModel = System.currentTimeMillis();
        long timeElapsed = finishedLoadingModel - startLoadingModel;
        System.out.println("Time needed to load the model: " + (timeElapsed / 1000f) + "s");

        if(logRun)
        {
        	Logger.singleton.enable();
            loadedThreadPoolSize = 1;
            loadedReplications = 1;
        }      
		
		Task<Void> task = new ExperimentManager(loadedModel, loadedSimulationParameters, loadedExperimentPlanFile, loadedReplications, loadedThreadPoolSize, loadedSeed, logRun);
		
        // This method allows us to handle any Exceptions thrown by the task
        task.setOnFailed(wse -> {
            wse.getSource().getException().printStackTrace();
        });

        // If the task completed successfully, perform other updates here
        task.setOnSucceeded(wse -> {
        	experimentFinished();
        	iconStatus.setImage(new Image("file:resources/icons/status_finished.png"));
        });

        // Before starting our task, we need to bind our UI values to the properties on the task
        pbExperiment.progressProperty().bind(task.progressProperty());
        labelExperimentProgress.textProperty().bind(task.messageProperty());
        
        long startTime = System.currentTimeMillis();
        DateFormat timeFormat = new SimpleDateFormat( "HH:mm.ss" );
        timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        timeline = new Timeline(
            new KeyFrame(
                Duration.millis( 500 ),
                event -> {
                    final long diff = System.currentTimeMillis() - startTime;
                    if ( diff < 0 ) {
                        labelExperimentTime.setText( timeFormat.format( 0 ) );
                    } else {
                    	labelExperimentTime.setText( timeFormat.format( diff ) );
                    }
                }
            )
        );
        timeline.setCycleCount( Animation.INDEFINITE );
        timeline.play();
        iconStatus.setImage(new Image("file:resources/icons/status_listening.png"));

        // Now, start the task on a background thread
        new Thread(task).start();
	}
	
	private void experimentFinished()
	{
		timeline.stop();
		System.out.println("Done!");
	}
	
	private void updateAllowedTabs()
	{
		// Get currently active tab
		Tab previouslyActiveTab = tabPaneParametrize.getSelectionModel().getSelectedItem();
		
		// Deactivate all but General
		tabGeneral.setDisable(false);
		tabSourcing.setDisable(true);
		tabProduction.setDisable(true);
		tabTransform.setDisable(true);
		tabSink.setDisable(true);
		tabStorage.setDisable(true);
		tabDistribution.setDisable(true);
		tabDemand.setDisable(true);
		checkProductionComponent.setDisable(true);
		checkTransformComponent.setDisable(true);
		checkSinkComponent.setDisable(true);
		checkStorageComponent.setDisable(true);
		checkDistributionComponent.setDisable(true);
		checkDemandComponent.setDisable(true);
		hboxCustomerGroup.setVisible(false);
		tfLat.setDisable(false);
		tfLon.setDisable(false);
		
		if(lastSelectedNode == null)
		{
			tabGeneral.setDisable(true);
			return;
		}
		DragIconType type = lastSelectedNode.getType();
		// Activate suitable
		switch(type)
		{
			case supplier:
				tabProduction.setDisable(false);
				tabTransform.setDisable(false);
				tabStorage.setDisable(false);
				tabDistribution.setDisable(false);
				checkTransformComponent.setDisable(false);
				break;
			case manufacturer:
				tabProduction.setDisable(false);
				tabTransform.setDisable(false);
				tabSink.setDisable(false);
				tabStorage.setDisable(false);
				tabDistribution.setDisable(false);
				tabDemand.setDisable(false);
				checkProductionComponent.setDisable(false);
				checkSinkComponent.setDisable(false);
				checkDemandComponent.setDisable(false);
				tabSourcing.setDisable(false);
				break;
			case warehouse:
				tabStorage.setDisable(false);
				tabDistribution.setDisable(false);
				tabDemand.setDisable(false);
				checkDemandComponent.setDisable(false);
				tabSourcing.setDisable(false);
				break;
			case customer:
				tabSink.setDisable(false);
				tabDemand.setDisable(false);
				tabSourcing.setDisable(false);
				break;
			case customerGroup:
				tabSink.setDisable(false);
				tabDemand.setDisable(false);
				tabSourcing.setDisable(false);
				hboxCustomerGroup.setVisible(true);
				tfLat.setDisable(true);
				tfLon.setDisable(true);
				break;
		}
		
		// Also, switch back to previously selected or general
		if(!previouslyActiveTab.isDisabled()) tabPaneParametrize.getSelectionModel().select(previouslyActiveTab);
		else tabPaneParametrize.getSelectionModel().select(tabGeneral);
	}
	
	private void activateNeededComponents(FacilityClipboard clipboard, DragIconType type)
	{
		switch(type)
		{
			case supplier:
				clipboard.hasComponentProduction = true;
				clipboard.hasComponentStorage = true;
				clipboard.hasComponentDistribution = true;
				break;
			case manufacturer:
				clipboard.hasComponentTransformation = true;
				clipboard.hasComponentStorage = true;
				clipboard.hasComponentDistribution = true;
				break;
			case warehouse:
				clipboard.hasComponentStorage = true;
				clipboard.hasComponentDistribution = true;
				break;
			case customer:
				clipboard.hasComponentConsumption = true;
				clipboard.hasComponentDemand = true;
				break;
			case customerGroup:
				clipboard.hasComponentConsumption = true;
				clipboard.hasComponentDemand = true;
				break;
		}
	}
	
	@FXML
	private void buildModel()
	{
		Random rng = new Random(0);
		
		// Save current clipboard
		saveToClipboard();
		
		// Prepare DoECoupler
		DoECoupler doeCoupler = new DoECoupler();
		
		System.out.println("Checking model ...");
		// Create SKUs
		HashMap<TableSKUsRow, SKU> skusMap = new HashMap<TableSKUsRow, SKU>();
		for(TableSKUsRow row : tableSKUsItems) skusMap.put(row, new SKU(row.getNameString()));
		
		// Create vehicles
		HashMap<TableVehicleClassesRow, VehicleClass> vehicleMap = new HashMap<TableVehicleClassesRow, VehicleClass>();
		for(TableVehicleClassesRow row : tableVehicleClassesItems) vehicleMap.put(row, new VehicleClass(row.getNameString(), Integer.parseInt(row.getCapacity().getText())));
		
		// Create agents
		ArrayList<Pair<Facility, FacilityClipboard>> allFacilities = new ArrayList<Pair<Facility, FacilityClipboard>>();
		HashMap<Node, ArrayList<Facility>> mapFacilities = new HashMap<Node, ArrayList<Facility>>();
		
		for(DraggableNode node : placedNodes)
		{
			// Create object
			FacilityClipboard clipboard = facilityClipboards.get(node);
			// See if it is a customer group
			if(clipboard.type != DragIconType.customerGroup)
			{
				Facility newFacility = new Facility(clipboard.name, clipboard.latitude, clipboard.longitude, clipboard.workingHours);
				allFacilities.add(new Pair<Facility, FacilityClipboard>(newFacility, clipboard));
				mapFacilities.put(node, new ArrayList<Facility>(Arrays.asList(newFacility)));
			}
			else
			{
				// Create facilities according to the customer group
				ArrayList<Facility> customersInCustomerGroup = new ArrayList<Facility>();
				for(int i = 0; i < clipboard.customerGroupNumberOfCustomer; i++)
				{
					String name = clipboard.name + "_" + Integer.toString(i + 1);
					float randomLat = (float) (clipboard.customerGroupMinLat + rng.nextFloat() * (clipboard.customerGroupMaxLat - clipboard.customerGroupMinLat));
		    		float randomLon = (float) (clipboard.customerGroupMinLon + rng.nextFloat() * (clipboard.customerGroupMaxLon - clipboard.customerGroupMinLon));
		    		Facility generatedCustomer = new Facility(name , randomLat, randomLon, clipboard.workingHours);
		    		
		    		// Make a new clipboard with a deep copy
		    		FacilityClipboard deepCopy = null;
		    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    		try {
						new ObjectOutputStream(baos).writeObject(clipboard);
						ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			    		deepCopy = (FacilityClipboard) new ObjectInputStream(bais).readObject();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	    		
		    		
		    		// Fill info
		    		deepCopy.name = name;
		    		deepCopy.latitude = randomLat;
		    		deepCopy.longitude = randomLon;
		    		
		    		// Sourced by must be exactly the same
		    		deepCopy.sourcedBy = clipboard.sourcedBy;
		    		
		    		allFacilities.add(new Pair<Facility, FacilityClipboard>(generatedCustomer, deepCopy));
		    		customersInCustomerGroup.add(generatedCustomer);
				}
				mapFacilities.put(node, customersInCustomerGroup);
			}
		}
		
		
		// Create components
		for(Pair<Facility, FacilityClipboard> pair : allFacilities)
		{
			FacilityClipboard clipboard = pair.getValue1();
			Facility facility = pair.getValue0();
	
			// Check integrity NOT IMPLEMENTED
			/*
			// Must have more than one connection
			if(clipboard.connectionEntries.size() == 0)
			{
				System.out.println("No connections for node");
				return;
			}
			*/
			
			// Production Component
			if(clipboard.hasComponentProduction)
			{
				ComponentProduction productionComponent = new ComponentProduction();
				for(ProductionEntryClipboard entry : clipboard.productionEntries)
				{
					Source newSource = new Source(skusMap.get(entry.sku), entry.rate);
					productionComponent.addSource(newSource);
					if(entry.DoE) doeCoupler.addLink(new DoELink(doeCoupler, newSource, (int)entry.rate, "production_per_hour"));
				}
					
					
				facility.addComponent(productionComponent);
				
				// Malfunction
				if(clipboard.productionMalfunction != null)
				{
					DistributionClipboard distributionClipboard = clipboard.productionMalfunction.distribution;
					Distribution distribution = new Distribution(distributionClipboard.type, distributionClipboard.param1, distributionClipboard.param2);
					Malfunction malfunction = new Malfunction(clipboard.productionMalfunction.dailyChance, distribution);
					productionComponent.enableMalfunction(malfunction);
					
					if(clipboard.productionMalfunction.DoE) doeCoupler.addLink(new DoELink(doeCoupler, malfunction, (int)clipboard.productionMalfunction.dailyChance, "daily_chance"));
					if(distributionClipboard.param1DoE) doeCoupler.addLink(new DoELink(doeCoupler, distribution, (int)distributionClipboard.param1, "distribution_param1"));
					if(distributionClipboard.param2DoE) doeCoupler.addLink(new DoELink(doeCoupler, distribution, (int)distributionClipboard.param2, "distribution_param2"));
				}
			}
			
			// Transformation Component
			if(clipboard.hasComponentTransformation)
			{
				ComponentTransformation transformationComponent = new ComponentTransformation();
				
				for(TransformationEntryClipboard entry : clipboard.transformationEntries)
				{
					// Translate SKUs
					ArrayList<Pair<SKU, Integer>> transformationRule = new ArrayList<Pair<SKU, Integer>>();
					for(Pair<TableSKUsRow, Integer> skuChange : entry.skuChanges) transformationRule.add(new Pair<SKU, Integer>(skusMap.get(skuChange.getValue0()), skuChange.getValue1()));
					
					TransformationProcess process = new TransformationProcess(transformationRule, entry.rate);
					transformationComponent.addTransformProcess(process);
					
					if(entry.DoE) doeCoupler.addLink(new DoELink(doeCoupler, process, (int)entry.rate, "completions_per_hour"));
				}
				
				facility.addComponent(transformationComponent);
				
				// Malfunction
				if(clipboard.transformationMalfunction != null)
				{
					DistributionClipboard distributionClipboard = clipboard.transformationMalfunction.distribution;
					Distribution distribution = new Distribution(distributionClipboard.type, distributionClipboard.param1, distributionClipboard.param2);
					Malfunction malfunction = new Malfunction(clipboard.transformationMalfunction.dailyChance, distribution);
					transformationComponent.enableMalfunction(malfunction);
					
					if(clipboard.productionMalfunction.DoE) doeCoupler.addLink(new DoELink(doeCoupler, malfunction, (int)clipboard.productionMalfunction.dailyChance, "daily_chance"));
					if(distributionClipboard.param1DoE) doeCoupler.addLink(new DoELink(doeCoupler, distribution, (int)distributionClipboard.param1, "distribution_param1"));
					if(distributionClipboard.param2DoE) doeCoupler.addLink(new DoELink(doeCoupler, distribution, (int)distributionClipboard.param2, "distribution_param2"));
				}
			}
			
			// Consumption Component
			if(clipboard.hasComponentConsumption)
			{
				ComponentConsumption consumptionComponent = new ComponentConsumption();
				for(ConsumptionEntryClipboard entry : clipboard.consumptionEntries)
				{
					Sink newSink = new Sink(skusMap.get(entry.sku), entry.rate);
					consumptionComponent.addSink(newSink);
					if(entry.DoE) doeCoupler.addLink(new DoELink(doeCoupler, newSink, (int)entry.rate, "sinking_per_hour"));
				}
					
					
				facility.addComponent(consumptionComponent);
			}
			
			// Demand Component
			if(clipboard.hasComponentDemand)
			{
				Distribution timeBetweenDemands = new Distribution(clipboard.timeBetweenDemands.type, clipboard.timeBetweenDemands.param1, clipboard.timeBetweenDemands.param2);
				
				if(clipboard.timeBetweenDemands.param1DoE) doeCoupler.addLink(new DoELink(doeCoupler, timeBetweenDemands, (int)clipboard.timeBetweenDemands.param1, "distribution_param1"));
				if(clipboard.timeBetweenDemands.param2DoE) doeCoupler.addLink(new DoELink(doeCoupler, timeBetweenDemands, (int)clipboard.timeBetweenDemands.param2, "distribution_param2"));
				
				SKUPicker skuPicker = new SKUPicker();
				for(DemandEntryClipboard entry : clipboard.demandEntries)
				{
					Distribution quantityDistribution = new Distribution(entry.quantityDistribution.type, entry.quantityDistribution.param1, entry.quantityDistribution.param2);
		    		SKUPickerEntry skuPickerEntry = new SKUPickerEntry(skusMap.get(entry.sku), quantityDistribution);
		    		SKUPickerWeight pickWeight = new SKUPickerWeight(skuPicker, skuPickerEntry, entry.weight);
		    		skuPicker.addWeight(pickWeight);
		    		
		    		if(entry.quantityDistribution.param1DoE) doeCoupler.addLink(new DoELink(doeCoupler, quantityDistribution, (int)entry.quantityDistribution.param1, "distribution_param1"));
					if(entry.quantityDistribution.param2DoE) doeCoupler.addLink(new DoELink(doeCoupler, quantityDistribution, (int)entry.quantityDistribution.param2, "distribution_param2"));
				}
				facility.addComponent(new ComponentDemandGenerator(timeBetweenDemands, skuPicker));
			}
			
			// Storage Component
			if(clipboard.hasComponentStorage)
			{
				ComponentStorages storageComponent = new ComponentStorages();
				for(StorageEntryClipboard entry : clipboard.storageEntries)
				{
					Storage newStorage = new Storage((int)entry.capacity, entry.initialFill);
					
					for(StorageSKUEntryClipboard skuEntry : entry.storageSKUEntries)
					{
						SKUStorageDetails storageDetails = new SKUStorageDetails(skusMap.get(skuEntry.sku), skuEntry.policy, skuEntry.param1, skuEntry.param2);
						newStorage.addAllowedSKUDetails(storageDetails);
						
						if(skuEntry.param1DoE) doeCoupler.addLink(new DoELink(doeCoupler, storageDetails, (int)skuEntry.param1, "strategy_param1"));
						if(skuEntry.param2DoE) doeCoupler.addLink(new DoELink(doeCoupler, storageDetails, (int)skuEntry.param2, "strategy_param2"));
					}
					
					storageComponent.addStorage(newStorage);
				}
				facility.addComponent(storageComponent);
			}
			
			if(clipboard.hasComponentDistribution)
			{
				// Make fleets
				VehicleFleet staticFleet = new VehicleFleet();
				for(VehicleOwnershipEntryClipboard vehicleEntry : clipboard.vehicleFleetStaticEntries) staticFleet.addAccumulatedVehicles(vehicleMap.get(vehicleEntry.vehicleClass), vehicleEntry.numberOfVehicles);
				
				VehicleFleet dynamicFleet = new VehicleFleet();
				for(VehicleOwnershipEntryClipboard vehicleEntry : clipboard.vehicleFleetDynamicEntries) dynamicFleet.addAccumulatedVehicles(vehicleMap.get(vehicleEntry.vehicleClass), vehicleEntry.numberOfVehicles);
				
				ComponentDistribution distributionComponent = new ComponentDistribution();
				DynamicRoutingGroup customerGroup = new DynamicRoutingGroup(dynamicFleet);
				distributionComponent.setDynamicRoutingGroup(customerGroup);
				
				for(DistributionConnection connection : clipboard.connectionEntries)
				{
					ArrayList<Facility> customers = mapFacilities.get(connection.node);
					
					for(Facility customer : customers)
					{
						if(connection.staticConnection)
						{
							// Make a transport relation
					    	TransportRelation newTR = new TransportRelation(distributionComponent, customer, DistanceCalculator.GetDistance(facility.getCoordinate(), customer.getCoordinate()));
					    	distributionComponent.addTransportRelation(newTR);
						}
						else
						{
							// Add to dynamic group
					    	customerGroup.addMember(customer);
						}
					}
				}
				facility.addComponent(distributionComponent);
			}
			
			// Add sourced by
			for(Pair<TableSKUsRow, DraggableNode> entry : clipboard.sourcedBy)
			{
				ArrayList<Facility> mappedFacilities = mapFacilities.get(entry.getValue1());
				for(Facility facilityEntry : mappedFacilities) facility.addSupplier(facilityEntry, new ArrayList<SKU>(Arrays.asList(skusMap.get(entry.getValue0()))));
			}
		}
		
		org.neo4j.ogm.session.Session session = Neo4jConnector.singleton.getSession();
    	org.neo4j.ogm.transaction.Transaction tx = session.beginTransaction();
    	session.purgeDatabase();

    	// Save all facilites
    	for(Pair<Facility, FacilityClipboard> pair : allFacilities)
    	{
    		session.save(pair.getValue0());
    	}
    	
    	// Make and save DoECoupler
    	
    	// doeCoupler.addLink(new DoELink(doeCoupler, berlinSinkSpeakerSystems, 1, "sinking_per_hour"));
    	// Save DoECoupler
    	session.save(doeCoupler);
    	
        tx.commit();
    	tx.close();
    	
    	System.out.println("New Model built!");
    	
    	// Now load it
		long startLoadingModel = System.currentTimeMillis();
		Neo4jHandler neo4jHandler = new Neo4jHandler();
        loadedModel = neo4jHandler.loadModel(Neo4jConnector.singleton);
        long finishedLoadingModel = System.currentTimeMillis();
        long timeElapsed = finishedLoadingModel - startLoadingModel;
        System.out.println("Time needed to load the model: " + (timeElapsed / 1000f) + "s");
        
        loadedThreadPoolSize = Integer.parseInt(tfThreadPoolSize.getText());
        loadedSeed = Integer.parseInt(tfSeed.getText());
        loadedReplications = Integer.parseInt(tfReplications.getText());
        LocalDate startDate = dpStart.getValue();
        LocalDate endDate = dpEnd.getValue();
        LocalDate warmUpDate = dpWarmUp.getValue();
        
        // Make the simulation parameters
        loadedSimulationParameters = new SimulationParameters(startDate, endDate, warmUpDate); 	
        
        // TEST
        loadedExperimentPlanFile = new File("resources\\scenario2plan.csv");
    	
    	checkStartButtonRequirements();
	}
	
	@FXML
	public void loadExperimentPlanFromHardDrive()
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Factors File");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
		File file = fileChooser.showOpenDialog(stage);
		
		if (file != null) 
		{
			labelExperimentPlanFile.setText(file.getName());
			loadedExperimentPlanFile = file;
        }
	}
	
	@FXML void aboutLogFarm()
	{
		Alert informationDialog = new Alert(AlertType.INFORMATION);
		informationDialog.setTitle("About LogFarm");
		informationDialog.setHeaderText("Information About LogFarm");
		String s ="LogFarm - created by Alexander Wuttke \r\n"
				+ "Department of IT in Production and Logistics \r\n"
				+ "TU Dortmund University \r\n"
				+ "v1.0";
		informationDialog.setContentText(s);
		informationDialog.show();
	}
	
	@FXML void factorFieldClicked(MouseEvent e)
	{
		// Together with CTRL?
		if(ctrlPressed.getValue())
		{
			Object source = e.getSource();
			Control control = (Control) source;
			boolean state = factorFieldIsActive(control);
			setFactorField(control, !state);
		}
	}
	
	public boolean factorFieldIsActive(Control control)
	{		
		return control.getStyle().equals(doeStyle);
	}
	
	public void setFactorField(Control control, boolean state)
	{		
		if(state) control.setStyle(doeStyle);
		else control.setStyle("");
	}
	
	@FXML private void startLogRun()
	{
		this.logRun = true;
        startExperiment();
	}
	
	@FXML private void loadScenario1()
	{
    	// Enable logger
		loadedExperimentPlanFile = new File("resources\\scenario1plan.csv");
    	
    	float[] workingHours = new float[]{8,8,8,8,8,0,0};
    	
    	// SKUs
    	SKU plank = new SKU("Brett");
    	SKU mount = new SKU("Wandhalterung");
    	SKU electronics = new SKU("Elektronik");
    	SKU speaker = new SKU("Lautsprecher");
    	SKU speakerSystem = new SKU("Lautsprechersystem");
    	
    	VehicleClass vehicle = new VehicleClass("LKW", 500);
    	
    	Facility wallmounts = new Facility("Wandhalterungen", 50.6717, 10.9799, workingHours); 
    	ComponentProduction wallmountProductionComponent = new ComponentProduction();
    	Source wallmountProductionComponentSource = new Source(mount, 0f);
    	wallmountProductionComponent.addSource(wallmountProductionComponentSource);
    	wallmounts.addComponent(wallmountProductionComponent);
    	ComponentStorages wallmountsStorageComponent = new ComponentStorages();
    	SKUStorageDetails wallmountsStorageComponentSKU1Details = new SKUStorageDetails(mount, 0, 0, 0);
    	Storage wallmountsStorage = new Storage(200000, 0f);
    	wallmountsStorage.addAllowedSKUDetails(wallmountsStorageComponentSKU1Details);
    	wallmountsStorageComponent.addStorage(wallmountsStorage);
    	wallmounts.addComponent(wallmountsStorageComponent);
    	VehicleFleet wallmountsFleet = new VehicleFleet();
    	wallmountsFleet.addAccumulatedVehicles(vehicle, 50);
    	ComponentDistribution wallmountsDistributionComponent = new ComponentDistribution();
    	wallmountsDistributionComponent.setVehicleFleetForStaticRouting(wallmountsFleet);
    	wallmounts.addComponent(wallmountsDistributionComponent);
    	
    	Facility sawmill = new Facility("Saegewerk", 51.4539, 9.1159, workingHours); 
    	ComponentProduction sawmillProductionComponent = new ComponentProduction();
    	Source sawmillProductionComponentSource = new Source(plank, 0f);
    	sawmillProductionComponent.addSource(sawmillProductionComponentSource);
    	sawmill.addComponent(sawmillProductionComponent);
    	ComponentStorages sawmillStorageComponent = new ComponentStorages();
    	SKUStorageDetails sawmillStorageComponentSKU1Details = new SKUStorageDetails(plank, 0, 0, 0);
    	Storage sawmillStorage = new Storage(200000, 0f);
    	sawmillStorage.addAllowedSKUDetails(sawmillStorageComponentSKU1Details);
    	sawmillStorageComponent.addStorage(sawmillStorage);
    	sawmill.addComponent(sawmillStorageComponent);
    	VehicleFleet sawmillFleet = new VehicleFleet();
    	sawmillFleet.addAccumulatedVehicles(vehicle, 50);
    	ComponentDistribution sawmillDistributionComponent = new ComponentDistribution();
    	sawmillDistributionComponent.setVehicleFleetForStaticRouting(sawmillFleet);
    	sawmill.addComponent(sawmillDistributionComponent);
    	Distribution sawmillMalfunctionDistribution = new Distribution(new NormalDistribution(0.5f, 0.3f));
    	Malfunction sawmillMalfunction = new Malfunction(0.2f, sawmillMalfunctionDistribution);
    	sawmillProductionComponent.enableMalfunction(sawmillMalfunction);
    	
    	Facility semifinished = new Facility("Halbfabrikate", 52.4760, 10.1021, workingHours); 
    	ComponentProduction semifinishedProductionComponent = new ComponentProduction();
    	Source semifinishedProductionComponentSource1 = new Source(electronics, 0f);
    	Source semifinishedProductionComponentSource2 = new Source(speaker, 0f);
    	semifinishedProductionComponent.addSource(semifinishedProductionComponentSource1);
    	semifinishedProductionComponent.addSource(semifinishedProductionComponentSource2);
    	semifinished.addComponent(semifinishedProductionComponent);
    	ComponentStorages semifinishedStorageComponent = new ComponentStorages();
    	SKUStorageDetails semifinishedStorageComponentSKU1Details = new SKUStorageDetails(electronics, 0, 0, 0);
    	SKUStorageDetails semifinishedStorageComponentSKU2Details = new SKUStorageDetails(speaker, 0, 0, 0);
    	Storage semifinishedStorage = new Storage(200000, 0f);
    	semifinishedStorage.addAllowedSKUDetails(semifinishedStorageComponentSKU1Details);
    	semifinishedStorage.addAllowedSKUDetails(semifinishedStorageComponentSKU2Details);
    	semifinishedStorageComponent.addStorage(semifinishedStorage);
    	semifinished.addComponent(semifinishedStorageComponent);
    	VehicleFleet semifinishedFleet = new VehicleFleet();
    	semifinishedFleet.addAccumulatedVehicles(vehicle, 50);
    	ComponentDistribution semifinishedDistributionComponent = new ComponentDistribution();
    	semifinishedDistributionComponent.setVehicleFleetForStaticRouting(semifinishedFleet);
    	semifinished.addComponent(semifinishedDistributionComponent);
    	Distribution semifinishedMalfunctionDistribution = new Distribution(new NormalDistribution(0.8f, 0.2f));
    	Malfunction semifinishedMalfunction = new Malfunction(0.05f, semifinishedMalfunctionDistribution);
    	semifinishedProductionComponent.enableMalfunction(semifinishedMalfunction);
    	
    	Facility speakersystem = new Facility("Lautsprechersysteme", 51.8532, 10.7905, workingHours); 
    	ArrayList<Pair<SKU, Integer>> skuChanges = new ArrayList<Pair<SKU, Integer>>();
    	skuChanges.add(new Pair<SKU, Integer>(speaker, -2));
    	skuChanges.add(new Pair<SKU, Integer>(electronics, -1));
    	skuChanges.add(new Pair<SKU, Integer>(plank, -1));
    	skuChanges.add(new Pair<SKU, Integer>(speakerSystem, 1));
    	ComponentTransformation speakersystemTransformationComponent = new ComponentTransformation();
    	TransformationProcess speakersystemTransformationProcess = new TransformationProcess(skuChanges, 0f);
    	speakersystemTransformationComponent.addTransformProcess(speakersystemTransformationProcess);
    	speakersystem.addComponent(speakersystemTransformationComponent);
    	ComponentStorages speakersystemStorageComponent = new ComponentStorages();
    	SKUStorageDetails speakersystemStorageComponentSKU1Details = new SKUStorageDetails(speakerSystem, 0, 0, 0);
    	SKUStorageDetails speakersystemStorageComponentSKU2Details = new SKUStorageDetails(plank, 1, 1000, 500);
    	SKUStorageDetails speakersystemStorageComponentSKU3Details = new SKUStorageDetails(electronics, 1, 1000, 500);
    	SKUStorageDetails speakersystemStorageComponentSKU4Details = new SKUStorageDetails(speaker, 1, 2000, 500);
    	Storage speakersystemStorage = new Storage(200000, 0f);
    	speakersystemStorage.addAllowedSKUDetails(speakersystemStorageComponentSKU1Details);
    	speakersystemStorage.addAllowedSKUDetails(speakersystemStorageComponentSKU2Details);
    	speakersystemStorage.addAllowedSKUDetails(speakersystemStorageComponentSKU3Details);
    	speakersystemStorage.addAllowedSKUDetails(speakersystemStorageComponentSKU4Details);
    	speakersystemStorageComponent.addStorage(speakersystemStorage);
    	speakersystem.addComponent(speakersystemStorageComponent);
    	VehicleFleet speakersystemFleet = new VehicleFleet();
    	speakersystemFleet.addAccumulatedVehicles(vehicle, 50);
    	ComponentDistribution speakersystemDistributionComponent = new ComponentDistribution();
    	speakersystemDistributionComponent.setVehicleFleetForStaticRouting(speakersystemFleet);
    	speakersystem.addComponent(speakersystemDistributionComponent);
    	speakersystem.addSupplier(sawmill, new ArrayList<SKU>(Arrays.asList(plank)));
    	speakersystem.addSupplier(semifinished, new ArrayList<SKU>(Arrays.asList(electronics, speaker)));
    	
    	Facility warehouse = new Facility("Warenhaus", 51.1459, 9.5630, workingHours); 
    	ComponentStorages warehouseStorageComponent = new ComponentStorages();
    	SKUStorageDetails warehouseStorageComponentSKU1Details = new SKUStorageDetails(speakerSystem, 1, 2000, 500);
    	SKUStorageDetails warehouseStorageComponentSKU2Details = new SKUStorageDetails(mount, 1, 400, 500);
    	Storage warehouseStorage = new Storage(200000, 0f);
    	warehouseStorage.addAllowedSKUDetails(warehouseStorageComponentSKU1Details);
    	warehouseStorage.addAllowedSKUDetails(warehouseStorageComponentSKU2Details);
    	warehouseStorageComponent.addStorage(warehouseStorage);
    	warehouse.addComponent(warehouseStorageComponent);
    	VehicleFleet warehouseFleet = new VehicleFleet();
    	warehouseFleet.addAccumulatedVehicles(vehicle, 50);
    	ComponentDistribution warehouseDistributionComponent = new ComponentDistribution();
    	warehouseDistributionComponent.setVehicleFleetForStaticRouting(warehouseFleet);
    	warehouse.addComponent(warehouseDistributionComponent);
    	warehouse.addSupplier(speakersystem, new ArrayList<SKU>(Arrays.asList(speakerSystem)));
    	warehouse.addSupplier(wallmounts, new ArrayList<SKU>(Arrays.asList(mount)));
    	
    	Facility dortmund = new Facility("Dortmund", 51.4920, 7.3735, workingHours);
    	ComponentConsumption dortmundConsumptionComponent = new ComponentConsumption();
		Sink dortmundSinkSpeakerSystems = new Sink(speakerSystem, 0f);
		Sink dortmundSinkMounts = new Sink(mount, 0f);
		dortmundConsumptionComponent.addSink(dortmundSinkSpeakerSystems);
		dortmundConsumptionComponent.addSink(dortmundSinkMounts);
		dortmund.addComponent(dortmundConsumptionComponent);
		dortmund.addSupplier(warehouse, new ArrayList<SKU>(Arrays.asList(speakerSystem, mount)));
		ComponentStorages dortmundStorageComponent = new ComponentStorages();
    	SKUStorageDetails dortmundStorageComponentSKU1Details = new SKUStorageDetails(speakerSystem, 1, 900, 250);
    	SKUStorageDetails dortmundStorageComponentSKU2Details = new SKUStorageDetails(mount, 1, 180, 250);
		Storage dortmundStorage = new Storage(200000, 0f);
		dortmundStorage.addAllowedSKUDetails(dortmundStorageComponentSKU1Details);
		dortmundStorage.addAllowedSKUDetails(dortmundStorageComponentSKU2Details);
		dortmundStorageComponent.addStorage(dortmundStorage);
		dortmund.addComponent(dortmundStorageComponent);
		
    	Facility frankfurt = new Facility("Frankfurt", 50.1672, 8.6770, workingHours);
    	ComponentConsumption frankfurtConsumptionComponent = new ComponentConsumption();
		Sink frankfurtSinkSpeakerSystems = new Sink(speakerSystem, 0f);
		Sink frankfurtSinkMounts = new Sink(mount, 0f);
		frankfurtConsumptionComponent.addSink(frankfurtSinkSpeakerSystems);
		frankfurtConsumptionComponent.addSink(frankfurtSinkMounts);
		frankfurt.addComponent(frankfurtConsumptionComponent);
		frankfurt.addSupplier(warehouse, new ArrayList<SKU>(Arrays.asList(speakerSystem, mount)));
		ComponentStorages frankfurtStorageComponent = new ComponentStorages();
    	SKUStorageDetails frankfurtStorageComponentSKU1Details = new SKUStorageDetails(speakerSystem, 1, 600, 250);
    	SKUStorageDetails frankfurtStorageComponentSKU2Details = new SKUStorageDetails(mount, 1, 120, 250);
		Storage frankfurtStorage = new Storage(200000, 0f);
		frankfurtStorage.addAllowedSKUDetails(frankfurtStorageComponentSKU1Details);
		frankfurtStorage.addAllowedSKUDetails(frankfurtStorageComponentSKU2Details);
		frankfurtStorageComponent.addStorage(frankfurtStorage);
		frankfurt.addComponent(frankfurtStorageComponent);
    	
    	Facility berlin = new Facility("Berlin", 52.4152, 13.4018, workingHours);
		ComponentConsumption berlinConsumptionComponent = new ComponentConsumption();
		Sink berlinSinkSpeakerSystems = new Sink(speakerSystem, 0f);
		Sink berlinSinkMounts = new Sink(mount, 0f);
		berlinConsumptionComponent.addSink(berlinSinkSpeakerSystems);
		berlinConsumptionComponent.addSink(berlinSinkMounts);
		berlin.addComponent(berlinConsumptionComponent);
		berlin.addSupplier(warehouse, new ArrayList<SKU>(Arrays.asList(speakerSystem, mount)));
		ComponentStorages berlinStorageComponent = new ComponentStorages();
    	SKUStorageDetails berlinStorageComponentSKU1Details = new SKUStorageDetails(speakerSystem, 1, 500, 250);
    	SKUStorageDetails berlinStorageComponentSKU2Details = new SKUStorageDetails(mount, 1, 100, 250);
    	Storage berlinStorage = new Storage(200000, 0f);
    	berlinStorage.addAllowedSKUDetails(berlinStorageComponentSKU1Details);
    	berlinStorage.addAllowedSKUDetails(berlinStorageComponentSKU2Details);
    	berlinStorageComponent.addStorage(berlinStorage);
    	berlin.addComponent(berlinStorageComponent);
    	
    	// TR
    	TransportRelation wallmountsToWarehouse = new TransportRelation(wallmountsDistributionComponent, warehouse, DistanceCalculator.GetDistance(wallmounts.getCoordinate(), warehouse.getCoordinate()));
    	wallmountsDistributionComponent.addTransportRelation(wallmountsToWarehouse);
    	TransportRelation sawmillToSpeakersystems = new TransportRelation(sawmillDistributionComponent, speakersystem, DistanceCalculator.GetDistance(sawmill.getCoordinate(), speakersystem.getCoordinate()));
    	sawmillDistributionComponent.addTransportRelation(sawmillToSpeakersystems);
    	TransportRelation semifinishedToSpeakersystems = new TransportRelation(semifinishedDistributionComponent, speakersystem, DistanceCalculator.GetDistance(semifinished.getCoordinate(), speakersystem.getCoordinate()));
    	semifinishedDistributionComponent.addTransportRelation(semifinishedToSpeakersystems);
    	TransportRelation speakersystemToWarehouse = new TransportRelation(speakersystemDistributionComponent, warehouse, DistanceCalculator.GetDistance(speakersystem.getCoordinate(), warehouse.getCoordinate()));
    	speakersystemDistributionComponent.addTransportRelation(speakersystemToWarehouse);
    	TransportRelation warehouseToBerlin = new TransportRelation(warehouseDistributionComponent, berlin, DistanceCalculator.GetDistance(warehouse.getCoordinate(), berlin.getCoordinate()));
    	warehouseDistributionComponent.addTransportRelation(warehouseToBerlin);
    	TransportRelation warehouseToFrankfurt = new TransportRelation(warehouseDistributionComponent, frankfurt, DistanceCalculator.GetDistance(warehouse.getCoordinate(), frankfurt.getCoordinate()));
    	warehouseDistributionComponent.addTransportRelation(warehouseToFrankfurt);
    	TransportRelation warehouseToDortmund = new TransportRelation(warehouseDistributionComponent, dortmund, DistanceCalculator.GetDistance(warehouse.getCoordinate(), dortmund.getCoordinate()));
    	warehouseDistributionComponent.addTransportRelation(warehouseToDortmund);

    	DoECoupler doeCoupler = new DoECoupler();
    	doeCoupler.addLink(new DoELink(doeCoupler, berlinSinkSpeakerSystems, 1, "sinking_per_hour"));
    	doeCoupler.addLink(new DoELink(doeCoupler, berlinSinkMounts, 2, "sinking_per_hour"));   	
    	doeCoupler.addLink(new DoELink(doeCoupler, frankfurtSinkSpeakerSystems, 3, "sinking_per_hour"));
    	doeCoupler.addLink(new DoELink(doeCoupler, frankfurtSinkMounts, 4, "sinking_per_hour"));   
    	doeCoupler.addLink(new DoELink(doeCoupler, dortmundSinkSpeakerSystems, 5, "sinking_per_hour"));
    	doeCoupler.addLink(new DoELink(doeCoupler, dortmundSinkMounts, 6, "sinking_per_hour"));   
    	doeCoupler.addLink(new DoELink(doeCoupler, sawmillMalfunction, 7, "daily_chance"));
    	doeCoupler.addLink(new DoELink(doeCoupler, sawmillMalfunctionDistribution, 8, "distribution_param1"));
    	doeCoupler.addLink(new DoELink(doeCoupler, sawmillMalfunctionDistribution, 9, "distribution_param2"));
    	doeCoupler.addLink(new DoELink(doeCoupler, sawmillProductionComponentSource, 10, "production_per_hour"));
    	doeCoupler.addLink(new DoELink(doeCoupler, wallmountProductionComponentSource, 11, "production_per_hour"));
    	doeCoupler.addLink(new DoELink(doeCoupler, semifinishedProductionComponentSource1, 12, "production_per_hour"));
    	doeCoupler.addLink(new DoELink(doeCoupler, semifinishedProductionComponentSource2, 13, "production_per_hour"));
    	doeCoupler.addLink(new DoELink(doeCoupler, semifinishedMalfunction, 14, "daily_chance"));
    	doeCoupler.addLink(new DoELink(doeCoupler, semifinishedMalfunctionDistribution, 15, "distribution_param1"));
    	doeCoupler.addLink(new DoELink(doeCoupler, semifinishedMalfunctionDistribution, 16, "distribution_param2"));
    	doeCoupler.addLink(new DoELink(doeCoupler, speakersystemTransformationProcess, 17, "completions_per_hour"));

    	org.neo4j.ogm.session.Session session = Neo4jConnector.singleton.getSession();
    	org.neo4j.ogm.transaction.Transaction tx = session.beginTransaction();
    	session.purgeDatabase();

    	session.save(sawmill);
    	session.save(semifinished);
    	session.save(speakersystem);
    	session.save(wallmounts);
    	session.save(warehouse);
    	session.save(dortmund);
    	session.save(frankfurt);
    	session.save(berlin);
    	session.save(doeCoupler);
    	
        tx.commit();
    	tx.close();
    	
    	loadedSeed = 0;
        loadedSimulationParameters = new SimulationParameters(LocalDate.of(2014, 7, 1), LocalDate.of(2015, 7, 1), LocalDate.of(2014, 6, 24));
        
        System.out.println("Scenario 1 loaded");
        
        // Delete this later
        loadedThreadPoolSize = 2;
        loadedReplications = 4;
        
        startExperiment();
	}
	
	@FXML private void loadScenario2()
	{
		loadedExperimentPlanFile = new File("resources\\scenario2plan.csv");
    	
    	Random rng = new Random(0);
    	
    	float[] workingHours = new float[]{8,8,8,8,8,0,0};
    	float[] workingHoursCustomers = new float[]{12,12,12,12,12,12,0};
    	
    	// SKUs
    	SKU sku1 = new SKU("sku1");
    	SKU sku2 = new SKU("sku2");
    	SKU sku3 = new SKU("sku3");
    	SKU sku4 = new SKU("sku4");
    	SKU sku5 = new SKU("sku5");
    	ArrayList<SKU> allSKUs = new ArrayList<SKU>(Arrays.asList(sku1, sku2, sku3, sku4, sku5));
    	
    	VehicleClass van = new VehicleClass("van", 750);
    	VehicleClass truck = new VehicleClass("truck", 1650);
    	
    	Facility hub = new Facility("hub", 37.991, 23.692, workingHours); 
    	ComponentStorages hubStorageComponent = new ComponentStorages();
    	SKUStorageDetails hubStorageComponentSKU1Details = new SKUStorageDetails(sku1, 1, 0, 0);
    	SKUStorageDetails hubStorageComponentSKU2Details = new SKUStorageDetails(sku2, 1, 0, 0);
    	SKUStorageDetails hubStorageComponentSKU3Details = new SKUStorageDetails(sku3, 1, 0, 0);
    	SKUStorageDetails hubStorageComponentSKU4Details = new SKUStorageDetails(sku4, 1, 0, 0);
    	SKUStorageDetails hubStorageComponentSKU5Details = new SKUStorageDetails(sku5, 1, 0, 0);
    	Storage hubStorage = new Storage(200000, 0f);
    	hubStorage.addAllowedSKUDetails(hubStorageComponentSKU1Details);
    	hubStorage.addAllowedSKUDetails(hubStorageComponentSKU2Details);
    	hubStorage.addAllowedSKUDetails(hubStorageComponentSKU3Details);
    	hubStorage.addAllowedSKUDetails(hubStorageComponentSKU4Details);
    	hubStorage.addAllowedSKUDetails(hubStorageComponentSKU5Details);
    	hubStorageComponent.addStorage(hubStorage);
    	hub.addComponent(hubStorageComponent);
    	VehicleFleet hubFleet = new VehicleFleet();
    	hubFleet.addAccumulatedVehicles(van, 50);
    	ComponentDistribution hubDistributionComponent = new ComponentDistribution();
    	hubDistributionComponent.setVehicleFleetForStaticRouting(hubFleet);
    	hub.addComponent(hubDistributionComponent);
    	
    	// Prepare dynamic group
    	DynamicRoutingGroup customerGroup = new DynamicRoutingGroup(hubFleet);
    	hubDistributionComponent.setDynamicRoutingGroup(customerGroup);
    	// Create some random customers
    	int numberOfCustomers = 100;
    	Facility[] customers = new Facility[numberOfCustomers];
    	float minLat = 37.9421f;
    	float maxLat = 38.0237f;
    	float minLon = 23.6730f;
    	float maxLon = 23.7702f;
    	ArrayList<Distribution> demandFrequencies = new ArrayList<Distribution>();
    	ArrayList<Distribution> demandQuantities = new ArrayList<Distribution>();
    	for(int i = 0; i < numberOfCustomers; i++)
    	{
    		float randomLat = (float) (minLat + rng.nextFloat() * (maxLat - minLat));
    		float randomLon = (float) (minLon + rng.nextFloat() * (maxLon - minLon));
    		customers[i] = new Facility("customer" + Integer.toString(i+1), randomLat, randomLon, workingHoursCustomers);
    		
    		// Add a demand generator
    		Distribution demandFrequency = new Distribution(new NormalDistribution(0, 1));
    		SKUPicker skuPicker = new SKUPicker();
    		Distribution quantity1 = new Distribution(new NormalDistribution(0, 1));
    		SKUPickerEntry skuPickerEntry1 = new SKUPickerEntry(sku1, quantity1);
    		SKUPickerWeight pickWeight1 = new SKUPickerWeight(skuPicker, skuPickerEntry1, 0.2f);
    		Distribution quantity2 = new Distribution(new NormalDistribution(0, 1));
    		SKUPickerEntry skuPickerEntry2 = new SKUPickerEntry(sku2, quantity2);
    		SKUPickerWeight pickWeight2 = new SKUPickerWeight(skuPicker, skuPickerEntry2, 0.2f);
    		Distribution quantity3 = new Distribution(new NormalDistribution(0, 1));
    		SKUPickerEntry skuPickerEntry3 = new SKUPickerEntry(sku3, quantity3);
    		SKUPickerWeight pickWeight3 = new SKUPickerWeight(skuPicker, skuPickerEntry3, 0.2f);
    		Distribution quantity4 = new Distribution(new NormalDistribution(0, 1));
    		SKUPickerEntry skuPickerEntry4 = new SKUPickerEntry(sku4, quantity4);
    		SKUPickerWeight pickWeight4 = new SKUPickerWeight(skuPicker, skuPickerEntry4, 0.2f);
    		Distribution quantity5 = new Distribution(new NormalDistribution(0, 1));
    		SKUPickerEntry skuPickerEntry5 = new SKUPickerEntry(sku5, quantity5);
    		SKUPickerWeight pickWeight5 = new SKUPickerWeight(skuPicker, skuPickerEntry5, 0.2f);
    		skuPicker.addWeight(pickWeight1);
    		skuPicker.addWeight(pickWeight2);
    		skuPicker.addWeight(pickWeight3);
    		skuPicker.addWeight(pickWeight4);
    		skuPicker.addWeight(pickWeight5);
    		
    		ComponentDemandGenerator demandGeneratorComponent = new ComponentDemandGenerator(demandFrequency, skuPicker);
    		customers[i].addComponent(demandGeneratorComponent);
    		customers[i].addSupplier(hub, allSKUs);
    		customerGroup.addMember(customers[i]);
    		
    		demandFrequencies.add(demandFrequency);
    		demandQuantities.add(quantity1);
    		demandQuantities.add(quantity2);
    		demandQuantities.add(quantity3);
    		demandQuantities.add(quantity4);
    		demandQuantities.add(quantity5);
    	}
    	
    	ArrayList<Facility> suppliers = new ArrayList<Facility>();
    	ArrayList<ComponentProduction> supplierProductionComponents = new ArrayList<ComponentProduction>();
    	double[] supplierLat = {38.059, 38.090, 38.031, 38.065, 38.284};
    	double[] supplierLon = {23.522, 23.562, 23.609, 23.762, 23.679};
    	for(int i = 0; i < 5; i++)
    	{
    		SKU sku = allSKUs.get(i);
        	Facility supplier = new Facility("supplier" + i, supplierLat[i], supplierLon[i], workingHours);
        	ComponentProduction supplierProductionComponent = new ComponentProduction();
        	supplierProductionComponent.addSource(sku, 10000f);
        	supplier.addComponent(supplierProductionComponent);
        	ComponentStorages supplierStorageComponent = new ComponentStorages();
        	SKUStorageDetails supplierStorageComponentSKUDetails = new SKUStorageDetails(sku, 0, 0, 0);
        	Storage supplierStorage = new Storage(200000, 0f);
        	supplierStorage.addAllowedSKUDetails(supplierStorageComponentSKUDetails);
        	supplierStorageComponent.addStorage(supplierStorage);
        	supplier.addComponent(supplierStorageComponent);
        	VehicleFleet supplierFleet = new VehicleFleet();
        	supplierFleet.addAccumulatedVehicles(truck, 500);
        	ComponentDistribution supplierDistributionComponent = new ComponentDistribution();
        	supplierDistributionComponent.setVehicleFleetForStaticRouting(supplierFleet);
        	supplier.addComponent(supplierDistributionComponent);
        	TransportRelation supplierToHub = new TransportRelation(supplierDistributionComponent, hub, DistanceCalculator.GetDistance(supplier.getCoordinate(), hub.getCoordinate()));
        	supplierDistributionComponent.addTransportRelation(supplierToHub);
        	hub.addSupplier(supplier, new ArrayList<SKU>(Arrays.asList(sku)));
        	suppliers.add(supplier);
        	
        	supplierProductionComponents.add(supplierProductionComponent);
    	}
    	
    	DoECoupler doeCoupler = new DoECoupler();
    	doeCoupler.addLink(new DoELink(doeCoupler, hubStorageComponentSKU1Details, 1, "strategy_param1"));
    	doeCoupler.addLink(new DoELink(doeCoupler, hubStorageComponentSKU2Details, 1, "strategy_param1"));
    	doeCoupler.addLink(new DoELink(doeCoupler, hubStorageComponentSKU3Details, 1, "strategy_param1"));
    	doeCoupler.addLink(new DoELink(doeCoupler, hubStorageComponentSKU4Details, 1, "strategy_param1"));
    	doeCoupler.addLink(new DoELink(doeCoupler, hubStorageComponentSKU5Details, 1, "strategy_param1"));
    	doeCoupler.addLink(new DoELink(doeCoupler, hubStorageComponentSKU1Details, 2, "strategy_param2"));
    	doeCoupler.addLink(new DoELink(doeCoupler, hubStorageComponentSKU2Details, 2, "strategy_param2"));
    	doeCoupler.addLink(new DoELink(doeCoupler, hubStorageComponentSKU3Details, 2, "strategy_param2"));
    	doeCoupler.addLink(new DoELink(doeCoupler, hubStorageComponentSKU4Details, 2, "strategy_param2"));
    	doeCoupler.addLink(new DoELink(doeCoupler, hubStorageComponentSKU5Details, 2, "strategy_param2"));
    	
    	for(Distribution distribution : demandFrequencies)
    	{
    		doeCoupler.addLink(new DoELink(doeCoupler, distribution, 3, "distribution_param1"));
    		doeCoupler.addLink(new DoELink(doeCoupler, distribution, 4, "distribution_param2"));
    	}
    	
    	for(Distribution distribution : demandQuantities)
    	{
    		doeCoupler.addLink(new DoELink(doeCoupler, distribution, 5, "distribution_param1"));
    		doeCoupler.addLink(new DoELink(doeCoupler, distribution, 6, "distribution_param2"));
    	}
    	
    	org.neo4j.ogm.session.Session session = Neo4jConnector.singleton.getSession();
    	org.neo4j.ogm.transaction.Transaction tx = session.beginTransaction();
    	session.purgeDatabase();

    	for(Facility supplier : suppliers) session.save(supplier);
    	session.save(hub);
    	for(Facility customer : customers) session.save(customer);
    	session.save(doeCoupler);
    	
        tx.commit();
    	tx.close();
    	
    	loadedSeed = 0;
        loadedSimulationParameters = new SimulationParameters(LocalDate.of(2014, 7, 1), LocalDate.of(2015, 7, 1), LocalDate.of(2014, 6, 24));
        loadedThreadPoolSize = 2;
        loadedReplications = 8;
        
        System.out.println("Scenario 2 loaded");
        startExperiment();
	}
}
