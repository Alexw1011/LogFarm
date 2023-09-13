package com.logfarm.model;

import java.io.Serializable;

import org.neo4j.graphdb.Entity;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.model.Node;

@RelationshipEntity(type = "quantity_relationship")
public class QuantityRelationship implements Serializable 
{
	@Id @GeneratedValue
	private Long id;
	
	@StartNode
	IRelationshipTarget startNode;
	
	@EndNode
	IRelationshipTarget endNode;
	
	@Property(name="quantity")
	int quantity;
	
	public QuantityRelationship()
	{
		
	}
	
	public QuantityRelationship(IRelationshipTarget startNode, IRelationshipTarget endNode, int quantity)
	{
		this.startNode = startNode;
		this.endNode = endNode;
		this.quantity = quantity;
	}
	
	public void changeEndNode(IRelationshipTarget endNode)
	{
		this.endNode = endNode;
	}
	
	public int getQuantity()
	{
		return this.quantity;
	}
	
	public Object getEndNode()
	{
		return this.endNode;
	}
}
