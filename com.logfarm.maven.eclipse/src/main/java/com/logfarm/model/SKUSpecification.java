package com.logfarm.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label="sku_specification")
public class SKUSpecification implements Serializable 
{
	@Id @GeneratedValue
	private Long id;
	
	@Relationship(type="uses_skus", direction=Relationship.OUTGOING)
	private ArrayList<SKU> skus = new ArrayList<SKU>();
	
	@Relationship(type="uses_supplier", direction=Relationship.OUTGOING)
	private Facility supplier;
	
	public SKUSpecification()
	{
		
	}
	
	public SKUSpecification(Facility supplier, ArrayList<SKU> skus)
	{
		this.skus = skus;
		this.supplier = supplier;
	}
	
	public Facility getSupplier()
	{
		return this.supplier;
	}
	
	public ArrayList<SKU> getSKUs()
	{
		return this.skus;
	}
}
