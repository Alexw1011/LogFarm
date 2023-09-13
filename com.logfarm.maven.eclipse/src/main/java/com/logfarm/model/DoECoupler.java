package com.logfarm.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label="doe_coupler")
public class DoECoupler implements Serializable
{
	@Id @GeneratedValue
	private Long id;
	
	@Relationship(type="doe_link", direction=Relationship.OUTGOING)
	private ArrayList<DoELink> links = new ArrayList<DoELink>();
	
	public DoECoupler()
	{
		
	}
	
	public void addLink(DoELink newLink)
	{
		links.add(newLink);
	}
	
	public ArrayList<DoELink> getLinks() { return links; };
}
