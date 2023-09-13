package com.logfarm.model;

import java.io.Serializable;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type = "vehicle_ownership")
public class VehicleOwnership implements Serializable 
{
	@Id @GeneratedValue
	private Long id;
	
	@StartNode
	VehicleFleet vehicleFleet;
	
	@EndNode
	VehicleClass vehicleClass;
	
	@Property(name="quantity")
	int quantity;
	
	public VehicleOwnership()
	{
		
	}
	
	public VehicleOwnership(VehicleFleet vehicleFleet, VehicleClass vehicleClass, int quantity)
	{
		this.vehicleFleet = vehicleFleet;
		this.vehicleClass = vehicleClass;
		this.quantity = quantity;
	}
	
	public int getQuantity()
	{
		return this.quantity;
	}
	
	public VehicleClass getVehicleClass()
	{
		return this.vehicleClass;
	}
}
