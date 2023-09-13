package com.logfarm.engine;

import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

import com.logfarm.io.Neo4jConnector;
import com.logfarm.io.Neo4jHandler;
import com.logfarm.model.Model;

public class AutomaticSimulationHandler 
{
	private long startedSimulationAt;
	private long loadingTime;
	
	public void handleAutomaticSimulation(String arg) throws Exception
    {	
		long startLoadingModel = System.currentTimeMillis();
		Neo4jHandler neo4jHandler = new Neo4jHandler();
        Model loadedModel = neo4jHandler.loadModel(Neo4jConnector.singleton);
        long finishedLoadingModel = System.currentTimeMillis();
        loadingTime = (finishedLoadingModel - startLoadingModel) / 1000l;
        
        // Read config
        String filepath = arg.substring(1);
        File automaticConfigFile = new File(filepath);
        if (automaticConfigFile == null) return;
        
        BufferedReader br = new BufferedReader(new FileReader(automaticConfigFile));
	    String lineBuffer;
	    lineBuffer  = br.readLine();
	    LocalDate startDate = LocalDate.of(Integer.parseInt(lineBuffer.substring(17, 21)), Integer.parseInt(lineBuffer.substring(14, 16)), Integer.parseInt(lineBuffer.substring(11, 13)));
	    lineBuffer  = br.readLine();
	    LocalDate warmUpDate = LocalDate.of(Integer.parseInt(lineBuffer.substring(19, 23)), Integer.parseInt(lineBuffer.substring(16, 18)), Integer.parseInt(lineBuffer.substring(13, 15)));
	    lineBuffer  = br.readLine();
	    LocalDate endDate = LocalDate.of(Integer.parseInt(lineBuffer.substring(15, 19)), Integer.parseInt(lineBuffer.substring(12, 14)), Integer.parseInt(lineBuffer.substring(9, 11)));
	    lineBuffer  = br.readLine();
	    int threadPoolSize = Integer.parseInt(lineBuffer.substring(17));
	    lineBuffer  = br.readLine();
	    int seed = Integer.parseInt(lineBuffer.substring(5));
	    lineBuffer  = br.readLine();
	    int replications = Integer.parseInt(lineBuffer.substring(13));
	    lineBuffer  = br.readLine();
	    String experimentFilePath = lineBuffer.substring(16);
	    
	    // Open experiment plan
	    File experimentPlanFile = new File(experimentFilePath);
	    if (experimentPlanFile == null) return;
	    
	    // Combine to simulation parameters struct
	    SimulationParameters simulationParameters = new SimulationParameters(startDate, warmUpDate, endDate);
		
		Task<Void> task = new ExperimentManager(loadedModel, simulationParameters, experimentPlanFile, replications, threadPoolSize, seed, false);
		
        // This method allows us to handle any Exceptions thrown by the task
        task.setOnFailed(tFailed -> {
            tFailed.getSource().getException().printStackTrace();
        });

        // If the task completed successfully, perform other updates here
        task.setOnSucceeded(t -> { simulationFinished(); });
        
        startedSimulationAt = System.currentTimeMillis();
        
        // Now, start the task on a background thread
        new Thread(task).start();
        
        while(task.isRunning())
        {
        	// do nothing
        }
    }
    
	private void simulationFinished()
	{
		System.out.println("Done!");
		long finishedSimulationAt = System.currentTimeMillis();
		long simulationTime = (finishedSimulationAt - startedSimulationAt) / 1000l;
		
		// Write both to a result file
		File outputLog = new File("automaticLog.txt");
		try {
			FileWriter myWriter = new FileWriter(outputLog);
			myWriter.write("Model loading time: " + String.valueOf(loadingTime) + "s\n");
			myWriter.write("Simulation took: " + String.valueOf(simulationTime) + "s\n");
			myWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
