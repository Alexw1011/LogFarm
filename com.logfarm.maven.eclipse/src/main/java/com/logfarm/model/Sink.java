package com.logfarm.model;

import java.io.Serializable;

import org.javatuples.Pair;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;

@NodeEntity(label="sku_sink")
public class Sink implements Serializable, IDoETarget
{
	@Id @GeneratedValue
	private Long id;
	
	@Property(name="sinking_per_hour")
	private float sinkingPerHour;
	
	@Relationship(type="sinks_sku", direction=Relationship.OUTGOING)
	private SKU sku;
	
	@Transient
	float remainderSinking = 0;
	
	public Sink()
	{
		
	}
	
	public Sink(SKU sku, float sinkingPerHour)
	{
		this.sku = sku;
		this.sinkingPerHour = sinkingPerHour;
	}
	
	public Pair<SKU, Integer> getConsumption(float dth)
	{
		return new Pair<SKU, Integer>(this.sku, getNumberOfSKUsToSink(dth));
	}
	
	private int getNumberOfSKUsToSink(float dth)
	{
		float sinking = remainderSinking + this.sinkingPerHour * dth;
		int discreteSinked = (int)sinking;
		this.remainderSinking = sinking - discreteSinked;
		
		return discreteSinked;
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
		case "sinking_per_hour":
			this.sinkingPerHour = value;
			break;
		default:
			System.out.println("Property not found.");
		}
	}
}
