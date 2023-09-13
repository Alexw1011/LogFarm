package com.logfarm.model;

import java.io.Serializable;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type = "sku_picker_weight")
public class SKUPickerWeight implements Serializable 
{
	@Id @GeneratedValue
	private Long id;
	
	@StartNode
	SKUPicker skuPicker;
	
	@EndNode
	SKUPickerEntry entry;
	
	@Property(name="weight")
	float weight;
	
	public SKUPickerWeight()
	{
		
	}
	
	public SKUPickerWeight(SKUPicker skuPicker, SKUPickerEntry entry, float weight)
	{
		this.skuPicker = skuPicker;
		this.entry = entry;
		this.weight = weight;
	}
	
	public float getWeight()
	{
		return this.weight;
	}
	
	public SKUPickerEntry getEntry()
	{
		return this.entry;
	}
}
