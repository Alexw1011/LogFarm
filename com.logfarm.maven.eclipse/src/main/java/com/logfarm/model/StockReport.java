package com.logfarm.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import org.javatuples.Pair;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import com.logfarm.utility.DateNodeHandler;
import com.logfarm.utility.GeneratedDataHandler;

@NodeEntity(label="stock_report")
public class StockReport extends GeneratedData implements Serializable, IRelationshipTarget
{
	@Relationship(type="on_date", direction=Relationship.OUTGOING)
	private Date date;
	
	@Relationship(type="quantity_relationship", direction=Relationship.OUTGOING)
	private ArrayList<QuantityRelationship> skuQuantities = new ArrayList<QuantityRelationship>();
	
	@Relationship(type="for_facility", direction=Relationship.OUTGOING)
	private Facility facility;
	
	public StockReport()
	{
		
	}
	
	public StockReport(LocalDate t, ArrayList<Pair<SKU, Integer>> stocks, Facility facility)
	{
		this.facility = facility;
		this.date = DateNodeHandler.singleton.findDateNodeForDate(t);
		
		// Translate to relationships
		for(Pair<SKU, Integer> position : stocks) skuQuantities.add(new QuantityRelationship(this, position.getValue0(), position.getValue1()));
	}
	
	@Override
	public void substituteReferences(HashMap<Facility, Facility> facilityDuplicates, HashMap<SKU, SKU> skuDuplicates)
	{
		this.facility = facilityDuplicates.get(this.facility);
		for(QuantityRelationship skuQuantity : skuQuantities) skuQuantity.changeEndNode(skuDuplicates.get((SKU)skuQuantity.getEndNode()));
	}
}
