package com.logfarm.model;

import java.io.Serializable;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity(label="vehicle_class")
public class VehicleClass implements Serializable
{
	@Id @GeneratedValue
	private Long id;
	
	@Property(name="name")
	private String name;
	
	@Property(name="capacity")
	private int capacity;
	
	public VehicleClass()
	{
		
	}
	
	public VehicleClass(String name, int capacity)
	{
		this.name = name;
		this.capacity = capacity;
	}
	
	public int getCapacity()
	{
		return this.capacity;
	}
}
