package com.logfarm.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;

@NodeEntity(label="vehicle_fleet")
public class VehicleFleet implements Serializable 
{
	@Id @GeneratedValue
	private Long id;
	
	@Relationship(type = "vehicle_ownership", direction = Relationship.OUTGOING)
	ArrayList<VehicleOwnership> vehiclesAccumulated = new ArrayList<VehicleOwnership>();
	
	@Transient
	ArrayList<Integer> vehicleCapacities = new ArrayList<Integer>();
	
	@Transient
	int overallCapacity;
	
	public VehicleFleet()
	{
		
	}
	
	public void addAccumulatedVehicles(VehicleClass vehicleClass, int quantity)
	{
		vehiclesAccumulated.add(new VehicleOwnership(this, vehicleClass, quantity));
	}
	
	public void initialize()
	{
		overallCapacity = 0;
		for(int i = 0; i < vehiclesAccumulated.size(); i++)
		{
			int capacity = vehiclesAccumulated.get(i).getVehicleClass().getCapacity();
			for(int u = 0; u < vehiclesAccumulated.get(i).getQuantity(); u++)
			{
				vehicleCapacities.add(capacity);
				overallCapacity += capacity;
			}
		}
		
		for(int i = 0; i < vehicleCapacities.size(); i++) overallCapacity += vehicleCapacities.get(i);
	}
	
	public int getOverallCapacity()
	{
		return this.overallCapacity;
	}
	
	public ArrayList<Integer> getVehicleCapacities()
	{
		return this.vehicleCapacities;
	}
	
	public int getNumberOfVehicles()
	{
		return this.vehicleCapacities.size();
	}
	
	public int getFirstVehicleCapacity()
	{
		return this.vehicleCapacities.get(0);
	}
}
