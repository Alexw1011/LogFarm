package com.logfarm.engine;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.logfarm.io.Neo4jConnector;
import com.logfarm.io.Neo4jHandler;
import com.logfarm.model.Facility;
import com.logfarm.model.Model;
import com.logfarm.model.SKU;
import com.logfarm.model.SimulationRunInfo;
import com.logfarm.utility.DateNodeHandler;
import com.logfarm.utility.GeneratedDataHandler;
import com.logfarm.utility.Logger;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import javafx.concurrent.Task;

public class ExperimentManager extends Task<Void>
{
	private final Model model;
	private final SimulationParameters simulationParameters;
	private final File experimentPlanFile;
	private final int numberOfThreads;
	private final int seed;
	private final int replications;
	private final boolean logRun;
	
	public ExperimentManager(Model model, SimulationParameters simulationParameters, File experimentPlanFile, int replications, int numberOfThreads, int seed, boolean logRun)
	{
		this.model = model;
		this.simulationParameters = simulationParameters;
		this.experimentPlanFile = experimentPlanFile;
		this.replications = replications;
		this.numberOfThreads = numberOfThreads;
		this.seed = seed;
		this.logRun = logRun;
		
		// Initial state
    	updateProgress(0, 1);
        updateMessage(String.valueOf(0));
	}
   
    @Override
    protected Void call() throws Exception
	{
    	//PrintStream out = new PrintStream(new FileOutputStream("output.txt", false), false);
        //System.setOut(out);
    	
    	long startPurging = System.currentTimeMillis();
		// Purge results db
		// Switch so system to handle dbs
        Neo4jConnector.singleton.establishConnection("system");
		Neo4jHandler.PurgeResults(Neo4jConnector.singleton);
		// Now switch to purged results
		Neo4jConnector.singleton.establishConnection("results");
		long finishedPurging = System.currentTimeMillis();
    	
    	long startExperimentManager = System.currentTimeMillis();
    	long startSetup = System.currentTimeMillis();
    	
		float[][] experimentPlan = null;
		Random seedRNG = new Random(seed);
		
		try 
		{
			experimentPlan = readExperimentPlan(experimentPlanFile);
		} catch (IOException | CsvException e) { e.printStackTrace(); }
		
		// Prepare results db
		// We will need facilities, skus and dates
		ArrayList<Facility> facilities = model.getFacilities();
		for(Facility facility : facilities) facility.resetId();
		ArrayList<SKU> skus = model.getSKUs();
		for(SKU sku : skus) sku.resetId();
		DateNodeHandler.singleton.prepareNodes(simulationParameters.getStartDate(), simulationParameters.getEndDate());
		
		Neo4jHandler.PrepareExperimentResults(Neo4jConnector.singleton, facilities, skus, DateNodeHandler.singleton.getAllDateNodes());
		
		// Run it and wait for it to finish
		System.out.println("Started threading loop");
		//int repetitions = 4;
        int numberOfRuns = experimentPlan.length * replications;
		//int numberOfRuns = 4;
        int finishedNumberOfRuns = 0;
        int startedNumberOfRuns = 0;
        Thread[] runningThreads = new Thread[numberOfThreads];
        
        if(this.logRun)
        {
        	numberOfRuns = 1;
        }
        
        long finishedSetup = System.currentTimeMillis();
        while(finishedNumberOfRuns < numberOfRuns)
        {
        	for(int i = 0; i < numberOfThreads; i++)
        	{
        		// Finished?
        		if(runningThreads[i] != null && !runningThreads[i].isAlive())
        		{
        			finishedNumberOfRuns++;
        			System.out.println("Run " + Integer.toString(finishedNumberOfRuns) + " finished! <------------------------------------------");
        			runningThreads[i] = null;
        		}
        		
        		if(runningThreads[i] == null && startedNumberOfRuns < numberOfRuns)
        		{
        			int designPoint = (int)((float)startedNumberOfRuns / replications);
        			int designPointRepetition = (startedNumberOfRuns % replications) + 1;
        			
        			float[] factorValues = null;
        			factorValues = experimentPlan[designPoint];
        			
        			long startCopying = System.currentTimeMillis();
        			Model workingCopy = Model.makeWorkingCopy(model, factorValues);
        			long finishedCopying = System.currentTimeMillis();
        			//System.out.println("id " + (startedNumberOfRuns + 1) + " copying time is " + ((finishedCopying - startCopying) / 1000f) + "s");
        			System.out.println((startedNumberOfRuns + 1) + ",c," + ((finishedCopying - startCopying) / 1000f));
        			
        			// Make the facilities and skus mappings
        			HashMap<Facility, Facility> facilitiesMapping = makeFacilitiesMapping(facilities, workingCopy);
        			HashMap<SKU, SKU> skusMapping = makeSKUsMapping(skus, workingCopy);       			
        					
        			SimulationRunInfo simulationRunInfo = new SimulationRunInfo((long)(startedNumberOfRuns + 1), (long)(designPoint + 1), (long)designPointRepetition);
        			GeneratedDataHandler generatedDataHandler = new GeneratedDataHandler(facilitiesMapping, skusMapping, simulationRunInfo);
        			
        			// Start a new thread
        			runningThreads[i] = new SimulationRunThread(Neo4jConnector.singleton.getSessionFactory(), new SimulationRun(seedRNG.nextInt(), workingCopy, simulationParameters, simulationRunInfo, generatedDataHandler));
        			runningThreads[i].start();
        			startedNumberOfRuns++;
        		}
        	}
        	
        	// Update our progress and message properties
            updateProgress(finishedNumberOfRuns, numberOfRuns);
            updateMessage(String.valueOf(finishedNumberOfRuns));
        }
        
        if(Logger.isUsed) Logger.singleton.close();
        long finishedExperimentManager = System.currentTimeMillis();
        
        System.out.println("Purging time: " + ((finishedPurging - startPurging) / 1000f) + "s");
        System.out.println("Preparation time: " + ((finishedSetup - startSetup) / 1000f) + "s");
        System.out.println("Experiment Manager time: " + ((finishedExperimentManager - startExperimentManager) / 1000f) + "s");
        System.out.println("Purging time: " + ((finishedPurging - startPurging) / 1000f) + "s");
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");    
        java.util.Date resultdate = new java.util.Date(startExperimentManager);
        System.out.println("started: " + sdf.format(resultdate));

        //out.close();
        return null;
	}
	
	
	private static float[][] readExperimentPlan(File experimentPlanFile) throws IOException, CsvException
	{
		// We assume its a csv
		if (experimentPlanFile == null) return null; 
		
		CSVReader csvReader = new CSVReader(new FileReader(experimentPlanFile));
	    List<String[]> list = new ArrayList<>();
	    list = csvReader.readAll();
	    csvReader.close();
	    
		// The experiment plan will be a two dimensional array of floats. first dimension will be the run (number of lines in csv) and second will be factor values (rows in csv)
	    int numberOfDesignPoints = list.size();
	    int numberOfFactors = list.get(0).length;
		float[][] experimentPlan = new float[numberOfDesignPoints][numberOfFactors];
		
		// Sample the read String rows
		for(int i = 0; i < numberOfDesignPoints; i++)
		{
			for(int u = 0; u < numberOfFactors; u++)
			{
				experimentPlan[i][u] = Float.valueOf(list.get(i)[u]);
			}
		}
		
		return experimentPlan;
	}
	
	private HashMap<Facility, Facility> makeFacilitiesMapping(ArrayList<Facility> resultFacilites, Model workingCopy)
	{
		ArrayList<Facility> workingCopyFacilites = workingCopy.getFacilities();
		HashMap<Facility, Facility> facilitiesMapping = new HashMap<Facility, Facility>();
		for(Facility facilityWC : workingCopyFacilites)
		{
			// Find equivalent
			for(Facility resultFacility : resultFacilites)
			{
				if(facilityWC.getName().equals(resultFacility.getName()))
				{
					facilitiesMapping.put(facilityWC, resultFacility);
					break;
				}
			}
		}
		return facilitiesMapping;
	}
	
	private HashMap<SKU, SKU> makeSKUsMapping(ArrayList<SKU> resultSKUs, Model workingCopy)
	{
		ArrayList<SKU> workingCopySKUs = workingCopy.getSKUs();
		HashMap<SKU, SKU> skusMapping = new HashMap<SKU, SKU>();
		for(SKU skuWC : workingCopySKUs)
		{
			// Find equivalent
			for(SKU resultSKU : resultSKUs)
			{
				if(skuWC.getName().equals(resultSKU.getName()))
				{
					skusMapping.put(skuWC, resultSKU);
					break;
				}
			}
		}
		return skusMapping;
	}
	
	@Override
    protected void succeeded() {
        super.succeeded();
    }

    @Override
    protected void failed() {
        super.failed();
    }
}