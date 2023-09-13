package com.logfarm.engine;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import com.logfarm.model.Model;
import com.logfarm.utility.GeneratedDataHandler;
import com.logfarm.utility.Logger;


public class SimulationCore 
{
	public static void RunNewSimulation(SimulationRun simulationRun)
	{
		// Run
		System.out.println("Starting simulation.");
		
		Model model = simulationRun.getModel();
		Random rng = simulationRun.getRNG();
		SimulationParameters sp = simulationRun.getSimulationParameters();
		GeneratedDataHandler gdh = simulationRun.getGeneratedDataHandler();
		
		LocalDate warmUpDate = sp.getWarmUpDate();
		int numberOfDays = (int) ChronoUnit.DAYS.between(warmUpDate, sp.getEndDate()) + 1;
		int numberOfWarmUpDays = (int) ChronoUnit.DAYS.between(warmUpDate, sp.getStartDate()) + 1;
		
		// Prepare
		model.initialize(warmUpDate);
		
		LocalDate date = warmUpDate;
		int daysCounter = 0;
		while(daysCounter < numberOfDays)
		{
			if(Logger.isUsed) 
			{
				Logger.singleton.log("------------------------");
				Logger.singleton.log("day " + daysCounter);
				Logger.singleton.log("------------------------");
			}				
				
			boolean inWarmUp = daysCounter < numberOfWarmUpDays;
			
			// Progress in time
			// Update all agents
			model.updateAgents(date, rng, gdh, !inWarmUp);
			date = date.plusDays(1);
			daysCounter++;
		}
		System.out.println("Simulation finished");
	}
}

