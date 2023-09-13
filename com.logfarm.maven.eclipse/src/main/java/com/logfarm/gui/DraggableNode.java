package com.logfarm.gui;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;

public class DraggableNode extends AnchorPane implements Serializable
{
    @FXML private transient AnchorPane paneRoot;
    @FXML private transient Label labelCaption;
    @FXML private transient AnchorPane paneMove;
    @FXML private transient AnchorPane paneConnect;
    @FXML private transient ImageView imageIcon;
    
    private transient EventHandler<DragEvent> parentNodeDragOver;
    private transient EventHandler<DragEvent> parentNodeDragDropped;
    private transient EventHandler<DragEvent> parentNodeLinkDragOver;
    private transient EventHandler<DragEvent> parentNodeLinkDragDropped;
    
    private transient EventHandler<MouseEvent> thisNodeLinkDragDetected;
    private transient EventHandler<DragEvent> thisNodeHandleDragDropped;

    private final DraggableNode self;
    
    private DragIconType mType = null;

    private transient Point2D mDragOrigin = new Point2D(12.5, 110);
    private transient Point2D mDragOffset = new Point2D(0.0, 0.0);
    private final List<String> mLinkIds = new ArrayList<String>();
      
    private transient NodeLink dragLink = null;
    private transient AnchorPane panePlaceableArea = null;
    
    public DraggableNode() 
    {
    	self = this;
    	
    	FXMLLoader fxmlLoader;
    	fxmlLoader = new FXMLLoader();
		try 
		{
			fxmlLoader.setLocation(new URL("file:resources/fxml/DraggableNode.fxml"));
			
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

		setId(UUID.randomUUID().toString());
    }

    @FXML
    private void initialize() 
    {
    	buildNodeDragHandlers();
    	buildLinkDragHandlers();
    	
	    paneConnect.setOnDragDetected(thisNodeLinkDragDetected);
	    paneRoot.setOnDragDropped(thisNodeHandleDragDropped);
	    
	    dragLink = new NodeLink();
	    dragLink.setVisible(false);
	                
	    parentProperty().addListener(new ChangeListener() 
	    {
	        @Override
	        public void changed(ObservableValue observable, Object oldValue, Object newValue) 
	        {
	        	panePlaceableArea = (AnchorPane) getParent();
	        }
	    });
    }

    public void relocateToPoint (Point2D p, boolean useDragOrigin) 
    {
        Point2D localCoords = getParent().sceneToLocal(p);
        if(useDragOrigin) relocate((int) (localCoords.getX() - mDragOrigin.getX() - mDragOffset.getX()), (int) (localCoords.getY() - mDragOrigin.getY() - mDragOffset.getY()));
        else relocate((int) (localCoords.getX() - mDragOffset.getX()), (int) (localCoords.getY() - mDragOffset.getY()));
    }

    public DragIconType getType () { return mType; }
    
    public String getCaption() { return labelCaption.getText(); };

    public void setType (DragIconType type) {

        mType = type;

        getStyleClass().clear();
        getStyleClass().add("dragicon");

        switch (mType) 
        {  
	        case supplier:
	        	labelCaption.setText("Supplier");
	        	try 
				{
					imageIcon.setImage(new Image((new URL("file:resources/icons/facility_supplier.png").toString())));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        break;
	        
	        case manufacturer:
	        	labelCaption.setText("Manufacturer");
	        	try 
				{
					imageIcon.setImage(new Image((new URL("file:resources/icons/facility_manufacturer.png").toString())));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        break;
	        
	        case warehouse:
	        	labelCaption.setText("Warehouse");
	        	try 
				{
					imageIcon.setImage(new Image((new URL("file:resources/icons/facility_warehouse.png").toString())));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        break;
	        
	        case customer:
	        	labelCaption.setText("Customer");
	        	try 
				{
					imageIcon.setImage(new Image((new URL("file:resources/icons/facility_customer.png").toString())));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        break;
	
	        case customerGroup:
	        	labelCaption.setText("Customer Group");
				try 
				{
					imageIcon.setImage(new Image((new URL("file:resources/icons/facility_customers.png").toString())));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        break;	
	            
	        default:
	        break;
        }
    }
    
    public void buildNodeDragHandlers() 
    {
    	parentNodeDragOver = new EventHandler<DragEvent> () 
    	{
    	    //dragover to handle node dragging in the right pane view
    	    @Override
    	    public void handle(DragEvent event) 
    	    {       
    	        event.acceptTransferModes(TransferMode.ANY);                
    	        relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()), true);
    	        event.consume();
    	    }
    	};
    	        
    	//dragdrop for node dragging
    	parentNodeDragDropped = new EventHandler<DragEvent> () 
    	{        
    	    @Override
    	    public void handle(DragEvent event) 
    	    {
	    	    getParent().setOnDragOver(null);
	    	    getParent().setOnDragDropped(null);             
	    	    event.setDropCompleted(true);
	    	                    
	    	    event.consume();
    	    }
    	};
    	
        //drag detection for node dragging
        paneMove.setOnDragDetected (new EventHandler<MouseEvent> () 
        {
            @Override
            public void handle(MouseEvent event) 
            {
                getParent().setOnDragOver (parentNodeDragOver);
                getParent().setOnDragDropped (parentNodeDragDropped);

                //begin drag ops
                mDragOffset = new Point2D(event.getX(), event.getY());
                        
                relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()), true);
                        
                ClipboardContent content = new ClipboardContent();
                DragContainer container = new DragContainer();
                        
                container.addData ("type", mType.toString());
                content.put(DragContainer.DragNode, container);
                        
                startDragAndDrop (TransferMode.ANY).setContent(content);                  
                        
                event.consume();                    
            }                    
        });     
    }
    
    private void buildLinkDragHandlers() 
    {
        
    	thisNodeLinkDragDetected = new EventHandler <MouseEvent> () 
    	   {
    		   @Override
    		   public void handle(MouseEvent event) 
    		   {   		                    
	    		   getParent().setOnDragOver(parentNodeLinkDragOver);
	    		   getParent().setOnDragDropped(parentNodeLinkDragDropped);
	    		                    
	    		   //Set up user-draggable link
	    		   panePlaceableArea.getChildren().add(0,dragLink);                  
	    		                    
	    		   dragLink.setVisible(false);
	
	    		   Point2D p = new Point2D(getLayoutX() + (getWidth() / 2.0), getLayoutY() + (getHeight() / 2.0));
	
	    		   dragLink.setStart(p);                  
	    		                    
	    		   //Drag content code
	    		   ClipboardContent content = new ClipboardContent();
	    		   DragContainer container = new DragContainer ();
	    		                    
	    		   container.addData("source", getId());
	    		   content.put(DragContainer.AddLink, container);
	    		                
	    		   startDragAndDrop (TransferMode.ANY).setContent(content);    
	
	    		   event.consume();
    		   }
    	    };
    	        
    	            
    	    parentNodeLinkDragOver = new EventHandler <DragEvent> () 
    	    {
    	    	@Override
    	        public void handle(DragEvent event) 
    	    	{
    	            event.acceptTransferModes(TransferMode.ANY);
    	                        
    	            //Relocate user-draggable link
    	            if (!dragLink.isVisible()) dragLink.setVisible(true);
    	                        
    	            dragLink.setEnd(new Point2D(event.getX(), event.getY()));

    	            event.consume();
    	        }
    	    };

    	    parentNodeLinkDragDropped = new EventHandler <DragEvent> () 
    	    {
    	        @Override
    	        public void handle(DragEvent event) 
    	        {
    	        	System.out.println("Link not dropped on Node");
    	            getParent().setOnDragOver(null);
	    	        getParent().setOnDragDropped(null);
	    	        
	    	        //hide the draggable NodeLink and remove it from the right-hand AnchorPane's children
	    	        dragLink.setVisible(false);
	    	        panePlaceableArea.getChildren().remove(0);
	    	                    
	    	        event.setDropCompleted(true);
	    	        event.consume();
    	       }        
    	    };
    	    
    	    thisNodeHandleDragDropped = new EventHandler <DragEvent> () 
    	    {
    	        @Override
    	        public void handle(DragEvent event) 
    	        {
    	        	System.out.println("Link dropped on node");

	    	        getParent().setOnDragOver(null);
	    	        getParent().setOnDragDropped(null);
	    	                                            
	    	        //get the drag data.  If it's null, abort.  
	    	        //This isn't the drag event we're looking for.
	    	        DragContainer container = (DragContainer) event.getDragboard().getContent(DragContainer.AddLink);
	    	                                    
	    	        if (container == null) return;
	    	                        
	    	        DraggableNode sourceNode = (DraggableNode) event.getSource();
	    	        
	    	        //AnchorPane link_handle = (AnchorPane) event.getSource();
	    	        //DraggableNode parent = (DraggableNode) link_handle.getParent();
	    	                        
	    	        ClipboardContent content = new ClipboardContent();
	    	                        
	    	        container.addData("target", getId());                       
	    	        content.put(DragContainer.AddLink, container);
	    	                        
	    	        event.getDragboard().setContent(content);               
	    	        event.setDropCompleted(true);
	    	        
	    	        //hide the draggable NodeLink and remove it from the right-hand AnchorPane's children
	    	        dragLink.setVisible(false);
	    	        panePlaceableArea.getChildren().remove(0);
	
	    	        event.consume();                
    	        }
    	    };
    	}
    
    public void registerLink(String linkId) 
    {
        mLinkIds.add(linkId);
    }
    
    public void setSelected(boolean state)
    {
    	if(state)
    	{
    		getStyleClass().add("selected");
    	}
    	else
    	{
    		getStyleClass().remove("selected");
    	}
    }
    
    public void changeCaption(String newCaption)
    {
    	labelCaption.setText(newCaption);
    }
}