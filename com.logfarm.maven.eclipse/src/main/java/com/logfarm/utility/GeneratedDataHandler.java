package com.logfarm.utility;

import java.util.ArrayList;
import java.util.HashMap;

import com.logfarm.model.Facility;
import com.logfarm.model.GeneratedData;
import com.logfarm.model.SKU;
import com.logfarm.model.SimulationRunInfo;

public class GeneratedDataHandler 
{
	private ArrayList<GeneratedData> generatedData = new ArrayList<GeneratedData>();
	private SimulationRunInfo simulationRunInfo;
	private HashMap<Facility, Facility> facilitiesMap;
	private HashMap<SKU, SKU> skusMap;
	
	public GeneratedDataHandler(HashMap<Facility, Facility> facilitiesMap, HashMap<SKU, SKU> skusMap, SimulationRunInfo simulationRunInfo)
	{
		this.facilitiesMap = facilitiesMap;
		this.skusMap = skusMap;
		this.simulationRunInfo = simulationRunInfo;
	}
	
	public void addGeneratedData(GeneratedData generatedData)
	{
		generatedData.associateSimulationRunInfo(simulationRunInfo);
		this.generatedData.add(generatedData);
	}
	
	public void substituteReferences()
	{
		for(GeneratedData gd : generatedData) gd.substituteReferences(facilitiesMap, skusMap);
	}
	
	public ArrayList<GeneratedData> getAllGeneratedData()
	{
		return this.generatedData;
	}
	
}
