package com.logfarm.engine;

import java.util.Random;

import com.logfarm.model.Model;
import com.logfarm.model.SimulationRunInfo;
import com.logfarm.utility.GeneratedDataHandler;

public class SimulationRun 
{
	private int seed;
	private Model model;
	private SimulationParameters simulationsParameters;
	private Random rng;
	private GeneratedDataHandler generatedDataHandler;
	private SimulationRunInfo simulationRunInfo;
	
	public Model getModel() { return this.model; };
	public Random getRNG() { return this.rng; };
	public SimulationParameters getSimulationParameters() { return this.simulationsParameters; };
	public GeneratedDataHandler getGeneratedDataHandler() { return this.generatedDataHandler; };
	public SimulationRunInfo getSimulationRunInfo() { return this.simulationRunInfo; };
	
	public SimulationRun(int seed, Model model, SimulationParameters simulationsParameters, SimulationRunInfo simulationRunInfo, GeneratedDataHandler generatedDataHandler)
	{
		this.seed = seed;
		this.model = model;
		this.simulationsParameters = simulationsParameters;
		this.generatedDataHandler = generatedDataHandler;
		this.simulationRunInfo = simulationRunInfo;
		
		rng = new Random(this.seed);
	}
}
