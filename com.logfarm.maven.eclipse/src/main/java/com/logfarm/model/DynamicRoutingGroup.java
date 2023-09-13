package com.logfarm.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label="dynamic_routing_group")
public class DynamicRoutingGroup implements Serializable
{
	@Id @GeneratedValue
	private Long id;
	
	@Relationship(type="has_member", direction=Relationship.OUTGOING)
	private ArrayList<Facility> member = new ArrayList<Facility>();
	
	@Relationship(type="utilizes_fleet", direction=Relationship.OUTGOING)
	private VehicleFleet fleet;
	
	public DynamicRoutingGroup()
	{
		
	}
	
	public DynamicRoutingGroup(VehicleFleet fleet)
	{
		this.fleet = fleet;
	}
	
	public void initialize()
	{
		fleet.initialize();
	}
	
	public void addMember(Facility member)
	{
		this.member.add(member);
	}
	
	public ArrayList<Facility> getMembers()
	{
		return this.member;
	}
	
	public VehicleFleet getVehicleFleet()
	{
		return this.fleet;
	}
}
