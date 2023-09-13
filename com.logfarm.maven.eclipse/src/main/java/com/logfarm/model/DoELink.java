package com.logfarm.model;

import java.io.Serializable;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type = "doe_link")
public class DoELink implements Serializable
{
	@Id @GeneratedValue
	private Long id;
	
	@StartNode
	DoECoupler doeCoupler;
	
	@EndNode
	IDoETarget linkedEntity;
	
	@Property(name="factor_id")
	int factorId;
	
	@Property(name="property_name")
	String propertyName;
	
	public DoELink()
	{
		
	}
	
	public DoELink(DoECoupler doeCoupler, IDoETarget linkedEntity, int factorId, String propertyName)
	{
		this.doeCoupler = doeCoupler;
		this.linkedEntity = linkedEntity;
		this.factorId = factorId;
		this.propertyName = propertyName;
	}
	
	public void applyFactorValue(float[] factorValues)
	{
		linkedEntity.setProperty(propertyName, factorValues[factorId - 1]);
	}
}
