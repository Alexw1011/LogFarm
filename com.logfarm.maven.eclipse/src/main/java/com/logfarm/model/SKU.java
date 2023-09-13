package com.logfarm.model;

import org.neo4j.ogm.annotation.NodeEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.model.Node;

@NodeEntity(label="sku")
public class SKU implements Serializable, IRelationshipTarget
{
	@Id @GeneratedValue
	private Long id;
	
	@Property(name="name")
	private String name;
	
	public SKU()
	{
		this.name = "default";
	}
	
	public SKU(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public Long getId()
	{
		return this.id;
	}
	
	public void resetId()
	{
		this.id = null;
	}
}
