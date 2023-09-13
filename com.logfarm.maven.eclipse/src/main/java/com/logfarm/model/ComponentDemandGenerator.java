package com.logfarm.model;

import java.io.Serializable;
import java.time.LocalDate;

import org.javatuples.Pair;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;

@NodeEntity(label="component_demand_generator")
public class ComponentDemandGenerator extends Component implements Serializable
{
	@Relationship(type = "utilizes_distribution", direction = Relationship.OUTGOING)
	protected Distribution orderFrequencyDistribution;

	@Relationship(type = "utilizes_sku_picker", direction = Relationship.OUTGOING)
	protected SKUPicker skuPicker;
	
	@Transient
	LocalDate nextOrderOnDate = null;
	
	public ComponentDemandGenerator()
	{
		
	}
	
	public ComponentDemandGenerator(Distribution orderFrequencyDistribution, SKUPicker skuPicker)
	{
		this.orderFrequencyDistribution = orderFrequencyDistribution;
		this.skuPicker = skuPicker;
	}
	
	public void initialize(LocalDate startDate)
	{
		orderFrequencyDistribution.initialize();
		skuPicker.initialize();
		// Find start for nextOrderOnDay
		int daysUntilNextOrder = orderFrequencyDistribution.sampleRoundedIntegerValue();
		nextOrderOnDate = startDate.plusDays(daysUntilNextOrder);
	}
	
	public Pair<SKU, Integer> sampleDemand(LocalDate t)
	{
		if(nextOrderOnDate != null)
		{
			if(t.equals(nextOrderOnDate) || t.isAfter(nextOrderOnDate))
			{
				// Place order
				// At first, pick a sku and get its corresponding distribution
				Pair<SKU, Distribution> skuEntry = skuPicker.sampleSKUEntry();
				int orderQuantity = skuEntry.getValue1().sampleRoundedIntegerValue();
				
				// Reroll up to 5 times if quantity is zero or negative
				int reroll = 0;
				while(orderQuantity <= 0f && reroll < 5) orderQuantity = skuEntry.getValue1().sampleRoundedIntegerValue();
				
				Pair<SKU, Integer> onlyOrderPosition = new Pair<SKU, Integer>(skuEntry.getValue0(), orderQuantity);	
				
				// Find new date for consecutive order
				int daysUntilNextOrder = orderFrequencyDistribution.sampleRoundedIntegerValue();
				// Reroll up to 5 times if quantity is zero or negative
				reroll = 0;
				while(daysUntilNextOrder <= 0 && reroll < 5) daysUntilNextOrder = orderFrequencyDistribution.sampleRoundedIntegerValue();
				
				nextOrderOnDate = t.plusDays(daysUntilNextOrder);
				
				if (orderQuantity <= 0) return null;
				return onlyOrderPosition;
			}
		}
		return null;
	}
}
