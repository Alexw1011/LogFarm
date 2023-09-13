package com.logfarm.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.annotation.Transient;

@RelationshipEntity(type = "transport_relation")
public class TransportRelation implements Serializable
{
	@Id @GeneratedValue
	private Long id;
	
	@StartNode
	ComponentDistribution supplierDistribution;
	
	@EndNode
	Facility recipient;

	@Property(name="distance")
	private double distance;
	
	public TransportRelation()
	{
		
	}
	
	public TransportRelation(ComponentDistribution supplierDistribution, Facility recipient, double distance)
	{
		this.supplierDistribution = supplierDistribution;
		this.recipient = recipient;
		this.distance = distance;
	}
	
	
	public void initialize()
	{
		
	}
	
	/*
	public boolean processOrder(Order order)
	{
		// Forward to fleet and see if it can be processed
		return true;
	}
	
	public void sendOut()
	{
		
	}
	*/
	
	public Facility getRecipient()
	{
		return recipient;
	}
	
	public double getDistance()
	{
		return this.distance;
	}
}
