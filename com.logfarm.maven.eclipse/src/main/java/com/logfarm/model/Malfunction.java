package com.logfarm.model;

import java.io.Serializable;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label="malfunction")
public class Malfunction implements Serializable, IDoETarget
{
	@Id @GeneratedValue
	private Long id;
	
	@Property(name="daily_chance")
	protected Float dailyChance;
	
	@Relationship(type = "utilizes_distribution", direction = Relationship.OUTGOING)
	protected Distribution distributionOperability;
	
	public Malfunction()
	{
		
	}
	
	public Malfunction(float dailyChance, Distribution intensity)
	{
		this.dailyChance = dailyChance;
		this.distributionOperability = intensity;
	}
	
	public void initialize()
	{
		distributionOperability.initialize();
	}
	
	public float getRemainingOperability(float randomNumber)
	{
		// Does the random number trigger the malfunction?
		if(randomNumber <= dailyChance)
		{
			// Find the intensity value
			float operabilityValue = (float) distributionOperability.sample();
			// Clamp
			if(operabilityValue <= 0f) return 0f;
			else if(operabilityValue >= 1f) return 1f;
			return operabilityValue;
		}
		return 1f;
	}

	@Override
	public void setProperty(String name, float value) {
		switch(name)
		{
		case "daily_chance":
			this.dailyChance = value;
			break;
		default:
			System.out.println("Property not found.");
		}
	}
}
