package com.logfarm.model;

import java.io.Serializable;

import org.javatuples.Pair;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label="sku_picker_entry")
public class SKUPickerEntry implements Serializable 
{
	@Id @GeneratedValue
	private Long id;
	
	@Relationship(type="utilizes_sku", direction=Relationship.OUTGOING)
	private SKU sku;
	
	@Relationship(type="utilizes_distribution", direction=Relationship.OUTGOING)
	private Distribution quantityDistribution;
	
	public SKUPickerEntry()
	{
		
	}
	
	public SKUPickerEntry(SKU sku, Distribution quantityDistribution)
	{
		this.sku = sku;
		this.quantityDistribution = quantityDistribution;
	}
	
	public Pair<SKU, Distribution> getEntryAsPair()
	{
		return new Pair<SKU, Distribution>(sku, quantityDistribution);
	}
}
