package com.logfarm.engine;

import java.util.ArrayList;

import org.javatuples.Pair;

import com.logfarm.model.Facility;
import com.logfarm.model.VehicleFleet;
import com.logfarm.utility.DistanceCalculator;

public class VRPModel 
{
	public class Node
	{
		//variables
		public int index;
		public int demand;
		public int routeIndex;
		public double[] distances;
		public Facility facility;
		
		//constructor
		public Node(int i, Facility facility)
		{
			index = i;
			this.facility = facility;
		}
		
		public Node copy()
		{
			Node n	= new Node(index, facility);
			n.demand        = demand;
			n.routeIndex    = routeIndex;
			n.distances		= distances.clone();
			n.facility		= facility;
			return n;
		}		
	}
	
	private int numberOfPossibleCustomers;
	public int numberOfCustomers;
	public int numberOfVertices;
	public int numberOfVehicles;
	public int vehicleCapacity;
	public Node[] nodes;
	private Facility depot;
	
	public double[][] possibleCustomersDistanceMatrix;
	
	public VRPModel(ArrayList<Facility> possibleCustomers, Facility depot, VehicleFleet vehicleFleet)
	{
		this.numberOfPossibleCustomers = possibleCustomers.size();
		this.numberOfVehicles = vehicleFleet.getNumberOfVehicles();
		this.vehicleCapacity = vehicleFleet.getFirstVehicleCapacity();
		this.depot = depot;
		
		// Calculate distance matrix (utilize symmetric behavior)
		possibleCustomersDistanceMatrix = new double[this.numberOfPossibleCustomers + 1][this.numberOfPossibleCustomers + 1];
		for(int i = 0; i < this.numberOfPossibleCustomers + 1; i++)
		{
			for(int j = 0; j <= i; j++)
			{
				if(i == j) this.possibleCustomersDistanceMatrix[i][j] = 0;
				else
				{
					Pair<Double, Double> firstCoordinate;
					if(i == this.numberOfPossibleCustomers) firstCoordinate = depot.getCoordinate();
					else firstCoordinate = possibleCustomers.get(i).getCoordinate();				
					double distance = DistanceCalculator.GetDistance(firstCoordinate, possibleCustomers.get(j).getCoordinate());
					this.possibleCustomersDistanceMatrix[i][j] = distance;
					this.possibleCustomersDistanceMatrix[j][i] = distance;
				}
			}
		}		
	}
	
	public Boolean setDemands(ArrayList<Pair<Facility, Integer>> demands)
	{
		// Find non-zero demands
		ArrayList<Pair<Integer, Integer>> nonZeroDemands = new ArrayList<Pair<Integer, Integer>>();
		ArrayList<Facility> remainingCustomers = new ArrayList<Facility>();
		for(int i = 0; i < demands.size(); i++)
		{
			Pair<Facility, Integer> demandEntry = demands.get(i);
			int demandValue = demandEntry.getValue1();
			if(demandValue > 0)
			{
				nonZeroDemands.add(new Pair<Integer, Integer>(i, demandValue));
				remainingCustomers.add(demandEntry.getValue0());
			}
		}
		
		// Select subset of nodes and their distances
		this.numberOfCustomers = nonZeroDemands.size();
		this.numberOfVertices = this.numberOfCustomers + 1;
		this.nodes = new Node[numberOfVertices];
		int depotIndex = this.numberOfCustomers;
		
		for(int i = 0; i < numberOfCustomers; i++)
		{
			this.nodes[i] = new Node(i, remainingCustomers.get(i));
			Pair<Integer, Integer> nonZeroDemandEntry = nonZeroDemands.get(i);
			int customerIndex = nonZeroDemandEntry.getValue0();
			this.nodes[i].demand = nonZeroDemandEntry.getValue1();
			// Prepare subset of distance matrix
			double[] distances = new double[this.numberOfVertices];
			for(int j = 0; j < nonZeroDemands.size(); j++) distances[j] = this.possibleCustomersDistanceMatrix[customerIndex][nonZeroDemands.get(j).getValue0()];
			distances[depotIndex] = this.possibleCustomersDistanceMatrix[customerIndex][this.numberOfPossibleCustomers];
			this.nodes[i].distances = distances;
		}
		// Depot
		nodes[depotIndex] = new Node(depotIndex, depot);
		double[] distancesDepot = new double[this.numberOfVertices];
		for(int i = 0; i < nonZeroDemands.size(); i++) distancesDepot[i] = this.possibleCustomersDistanceMatrix[this.numberOfPossibleCustomers][nonZeroDemands.get(i).getValue0()];
		distancesDepot[depotIndex] = 0;
		this.nodes[depotIndex].distances = distancesDepot;
		
		return this.numberOfCustomers > 0;
	}
}
