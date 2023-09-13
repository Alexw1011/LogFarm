package com.logfarm.model;

import java.io.Serializable;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type = "sku_quantity")
public class SKUQuantity implements Serializable
{
	@Id @GeneratedValue
	private Long id;
	
	@StartNode
	GeneratedData start;
	
	@EndNode
	SKU sku;
	
	@Property(name="quantity")
	int quantity;
	
	public SKUQuantity()
	{
		
	}
	
	public SKUQuantity(GeneratedData start, SKU sku, int quantity)
	{
		this.start = start;
		this.sku = sku;
		this.quantity =quantity;
	}
	
	public float getQuantity()
	{
		return this.quantity;
	}

	public SKU getSKU()
	{ 
		return this.sku;
	}
	
	public void changeSKU(SKU sku)
	{
		this.sku = sku;
	}
}
