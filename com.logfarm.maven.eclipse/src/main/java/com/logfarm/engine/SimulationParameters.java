package com.logfarm.engine;

import java.time.LocalDate;

public class SimulationParameters 
{
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDate warmUpDate;
	
	
	public SimulationParameters(LocalDate startDate, LocalDate endDate, LocalDate warmUpDate)
	{
		this.startDate = startDate;
		this.endDate = endDate;
		this.warmUpDate = warmUpDate;
	}
	
	public LocalDate getStartDate() { return this.startDate; };
	public LocalDate getEndDate() { return this.endDate; };
	public LocalDate getWarmUpDate() { return this.warmUpDate; };
}
