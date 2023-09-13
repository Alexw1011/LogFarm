package com.logfarm.utility;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

import com.logfarm.model.Date;

public class DateNodeHandler 
{
	public static final DateNodeHandler singleton = new DateNodeHandler();
	private HashMap<LocalDate, Date> dateNodes = new HashMap<LocalDate, Date>();
	
	public void prepareNodes(LocalDate startDate, LocalDate endDate)
	{
		int numberOfNeededDays = (int) (ChronoUnit.DAYS.between(startDate, endDate) + 1);
		LocalDate currentDate = startDate;
		for(int i = 0; i < numberOfNeededDays; i++)
		{
			dateNodes.put(currentDate, new Date(currentDate));
			currentDate = currentDate.plusDays(1);
		}
	}
	
	public Date findDateNodeForDate(LocalDate date)
	{
		return dateNodes.get(date);
	}
	
	public ArrayList<Date> getAllDateNodes()
	{
		return new ArrayList<Date>(dateNodes.values());
	}
}
