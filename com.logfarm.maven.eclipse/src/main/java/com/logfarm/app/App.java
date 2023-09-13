package com.logfarm.app;

import javafx.application.Application;
import javafx.stage.Stage;

import com.logfarm.engine.AutomaticSimulationHandler;
import com.logfarm.gui.GUI;
import com.logfarm.io.Neo4jConnector;

public class App extends Application implements AutoCloseable
{
    public static void main(String[] args)
    {
    	// No splash screen for now
    	
    	// Launch will call the start method
        launch(App.class, args);
    }
    
    @Override
    public void start(Stage stage) 
    {
    	String[] args = getParameters().getUnnamed().toArray(new String[getParameters().getUnnamed().size()]);

    	// Initialize connector
    	Neo4jConnector connector = Neo4jConnector.singleton;
        boolean result = connector.establishConnection("neo4j");
        if(!result)
        {
        	System.out.println("Could not establish a connection to the specified neo4j database.");
        	return;
        }
        
        if(args.length == 0 || !("-a".equals(args[0])))
        {
        	// Start GUI
        	GUI gui = new GUI();
        	gui.createAppGUI(stage);	
        }
        else if("-a".equals(args[0]))
        {
        	System.out.println("automatic processing requested");
        	try {
        		AutomaticSimulationHandler ash = new AutomaticSimulationHandler();
				ash.handleAutomaticSimulation(args[1]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
    }    
    
    public void close() throws Exception
    {
		
    }
}
