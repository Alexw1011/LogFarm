package com.logfarm.model;

import org.neo4j.ogm.annotation.NodeEntity;

import java.io.Serializable;

import org.javatuples.Pair;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;


@NodeEntity(label="sku_source")
public class Source implements Serializable, IRelationshipTarget, IDoETarget
{
	@Id @GeneratedValue
	private Long id;
	
	@Property(name="production_per_hour")
	private float productionPerHour;
	
	@Relationship(type="spawns_sku", direction=Relationship.OUTGOING)
	private SKU sku;
	
	@Transient
	float productionRemainder = 0;
	
	public Source()
	{
		
	}
	
	public Source(SKU sku, float productionPerHour)
	{
		this.sku = sku;
		this.productionPerHour = productionPerHour;
	}
	
	public Pair<SKU, Integer> getProduction(float dth)
	{
		return new Pair<SKU, Integer>(this.sku, getNumberOfGeneratedSKUs(dth));
	}
	
	private int getNumberOfGeneratedSKUs(float dth)
	{
		float production = productionRemainder + this.productionPerHour * dth;
		int discreteProduction = (int)production;
		this.productionRemainder = production - discreteProduction;
		
		return discreteProduction;
	}
	
	public SKU getSKU()
	{
		return sku;
	}	
	
	@Override
	public void setProperty(String name, float value)
	{
		switch(name)
		{
		case "production_per_hour":
			this.productionPerHour = value;
			break;
		default:
			System.out.println("Property not found.");
		}
	}
}
