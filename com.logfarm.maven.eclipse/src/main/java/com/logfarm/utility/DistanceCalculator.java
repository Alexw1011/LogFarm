package com.logfarm.utility;

import org.javatuples.Pair;

public class DistanceCalculator 
{
	public static double GetDistance(Pair<Double, Double> coordinate1, Pair<Double, Double> coordinate2)
	{
		return GetDistance(coordinate1.getValue0(), coordinate1.getValue1(), coordinate2.getValue0(), coordinate2.getValue1());
	}
	
	public static double GetDistance(double lat1, double lon1, double lat2, double lon2)
	{
		// Haversine formula
		// Radius of earth in km
		final double meanRadiusEarth = 6371;
		
		double latDistanceInRadians = (lat2 - lat1) * Math.PI / 180;
		double lonDistanceInRadians = (lon2 - lon1) * Math.PI / 180;
		
		double a = Math.sin(latDistanceInRadians / 2) * Math.sin(latDistanceInRadians / 2) + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.sin(lonDistanceInRadians / 2) * Math.sin(lonDistanceInRadians / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		
		// Distance in km
		return c * meanRadiusEarth;
		
		//tmp: 
		//return Math.sqrt(Math.pow(lat2-lat1, 2) + Math.pow(lon2-lon1, 2));
	}
}
