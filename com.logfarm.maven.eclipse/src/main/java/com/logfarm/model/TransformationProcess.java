package com.logfarm.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.javatuples.Pair;
import org.neo4j.graphdb.Entity;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.model.Node;

@NodeEntity(label="transformation_process")
public class TransformationProcess implements Serializable, IRelationshipTarget, IDoETarget
{
	@Id @GeneratedValue
	private Long id;
	
	@Property(name="completions_per_hour")
	private float completionsPerHour;
	
	@Relationship(type="quantity_relationship", direction=Relationship.OUTGOING)
	private ArrayList<QuantityRelationship> skuChangeDefinitions = new ArrayList<QuantityRelationship>();
	
	@Transient
	float processRemainder = 0;
	
	public TransformationProcess()
	{
		
	}
	
	public TransformationProcess(ArrayList<Pair<SKU, Integer>> skuChanges, float completionsPerHour)
	{
		this.completionsPerHour = completionsPerHour;
		// Make a list of quantity relationships from it
		for(Pair<SKU, Integer> skuChange : skuChanges)
		{
			skuChangeDefinitions.add(new QuantityRelationship(this, skuChange.getValue0(), skuChange.getValue1()));
		}
	}
	
	public ArrayList<Pair<SKU, Integer>> getSKUChange(float dth)
	{
		ArrayList<Pair<SKU, Integer>> skuChanges = new ArrayList<Pair<SKU, Integer>>();
		int numberOfCompletions = getNumberOfCompletions(dth);
		
		for(QuantityRelationship skuChange : skuChangeDefinitions)
		{
			SKU sku = (SKU) skuChange.getEndNode();
			int quantity = skuChange.getQuantity() * numberOfCompletions;
			
			skuChanges.add(new Pair<SKU, Integer>(sku, quantity));
		}
		
		return skuChanges;
	}
	
	private int getNumberOfCompletions(float dth)
	{
		float production = processRemainder + this.completionsPerHour * dth;
		int discreteProduction = (int)production;
		this.processRemainder = production - discreteProduction;
		
		return discreteProduction;
	}

	@Override
	public void setProperty(String name, float value) 
	{
		switch(name)
		{
		case "completions_per_hour":
			this.completionsPerHour = value;
			break;
		default:
			System.out.println("Property not found.");
		}
	}
}
