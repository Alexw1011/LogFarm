package com.logfarm.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import org.javatuples.Pair;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;

import com.logfarm.utility.DateNodeHandler;
import com.logfarm.utility.GeneratedDataHandler;

@NodeEntity(label="order")
public class Order extends GeneratedData implements Serializable, IRelationshipTarget
{
	@Relationship(type="order_date", direction=Relationship.OUTGOING)
	private Date orderDate;
	
	@Relationship(type="delivery_date", direction=Relationship.OUTGOING)
	private Date deliveryDate;
	
	@Relationship(type="order_supplier", direction=Relationship.OUTGOING)
	private Facility supplier;
	
	@Relationship(type="order_recipient", direction=Relationship.OUTGOING)
	private Facility recipient;
	
	@Relationship(type="quantity_relationship", direction=Relationship.OUTGOING)
	private ArrayList<QuantityRelationship> skuQuantities = new ArrayList<QuantityRelationship>();
	
	@Transient
	ArrayList<Pair<SKU, Integer>> positions;
	
	@Transient
	int overallQuantity;
	
	@Transient
	static long internalOrderIdCounter = 0;
	
	@Transient
	long internalOrderId;
	
	public Order()
	{
		internalOrderId = ++internalOrderIdCounter;
	}
	
	public Order(Facility supplier, Facility recipient, ArrayList<Pair<SKU, Integer>> positions, LocalDate orderDate)
	{
		this.supplier = supplier;
		this.recipient = recipient;
		this.positions = positions;
		this.orderDate = DateNodeHandler.singleton.findDateNodeForDate(orderDate);
		
		// Remove negative or zero positions
		for(int i = this.positions.size() - 1; i >= 0; i--) if(this.positions.get(i).getValue1() <= 0) this.positions.remove(i);
		
		this.overallQuantity = 0;
		for(Pair<SKU, Integer> position : positions) this.overallQuantity += position.getValue1();
		
		// Translate to relationships
		for(Pair<SKU, Integer> position : positions)
		{
			skuQuantities.add(new QuantityRelationship(this, position.getValue0(), position.getValue1()));
		}
		
		internalOrderId = ++internalOrderIdCounter;
	}
	
	public long getInternalOrderId() { return internalOrderId; };
	
	public ArrayList<Pair<SKU, Integer>> getPositions()
	{
		return this.positions;
	}
	
	public Facility getSupplier() { return supplier; };
	
	public Facility getRecipient() { return recipient; };
	
	public int getOverallQuantity()
	{
		return this.overallQuantity;
	}
	
	public void markAsDelivered(LocalDate deliveryDate)
	{
		this.deliveryDate = DateNodeHandler.singleton.findDateNodeForDate(deliveryDate);
	}
	
	@Override
	public void substituteReferences(HashMap<Facility, Facility> facilityDuplicates, HashMap<SKU, SKU> skuDuplicates)
	{
		this.supplier = facilityDuplicates.get(this.supplier);
		this.recipient = facilityDuplicates.get(this.recipient);
		for(QuantityRelationship skuQuantity : skuQuantities) skuQuantity.changeEndNode(skuDuplicates.get((SKU)skuQuantity.getEndNode()));
	}
}
