package com.logfarm.engine;

import org.neo4j.ogm.session.SessionFactory;

import com.logfarm.io.Neo4jConnector;
import com.logfarm.io.Neo4jHandler;

public class SimulationRunThread extends Thread
{
	SessionFactory sf;
	SimulationRun simulationRun;
	
	public SimulationRunThread(SessionFactory sf, SimulationRun simulationRun)
	{
		this.sf = sf;
		this.simulationRun = simulationRun;
	}
	
	@Override
	public void run() 
	{
		// Run the simulation
		long startSimulation = System.currentTimeMillis();
		SimulationCore.RunNewSimulation(this.simulationRun);
		long finishedSimulation = System.currentTimeMillis();
		
		long startWriting = System.currentTimeMillis();
		// Prepare results
		this.simulationRun.getGeneratedDataHandler().substituteReferences();
		// Save Results
        Neo4jHandler.SaveGeneratedData(Neo4jConnector.singleton, simulationRun.getSimulationRunInfo(), simulationRun.getGeneratedDataHandler());
        long finishedWriting = System.currentTimeMillis();
        	
        System.out.println(simulationRun.getSimulationRunInfo().getRunId() + ",s," + ((finishedSimulation - startSimulation) / 1000f));
        System.out.println(simulationRun.getSimulationRunInfo().getRunId() + ",w," + ((finishedWriting - startWriting) / 1000f));
    }  
}
