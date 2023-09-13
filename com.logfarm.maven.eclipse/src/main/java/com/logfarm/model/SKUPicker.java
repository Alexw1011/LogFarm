package com.logfarm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import org.javatuples.Pair;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label="sku_picker")
public class SKUPicker implements Serializable 
{
	@Id @GeneratedValue
	private Long id;
	
	@Relationship(type="sku_picker_weight", direction=Relationship.OUTGOING)
	private ArrayList<SKUPickerWeight> weights  = new ArrayList<SKUPickerWeight>();
	
	public SKUPicker()
	{
		
	}
	
	public void initialize()
	{
		for(SKUPickerWeight weight : weights)
		{
			weight.getEntry().getEntryAsPair().getValue1().initialize();
		}
	}
	
	public void addWeight(SKUPickerWeight weight)
	{
		this.weights.add(weight);
	}
	
	public Pair<SKU, Distribution> sampleSKUEntry()
	{
		// Roll a dice between 0 and 1
		float roll = ThreadLocalRandom.current().nextFloat();
		Iterator<SKUPickerWeight> it = weights.iterator();
		while(it.hasNext())
		{
			SKUPickerWeight pickWeight = it.next();
			float weight = pickWeight.getWeight();
			if(roll <= weight) return pickWeight.getEntry().getEntryAsPair();
			roll -= weight;
		}
		
		return null;
	}
}
