package com.logfarm.model;

import java.io.Serializable;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;

@NodeEntity(label="sku_storage_details")
public class SKUStorageDetails implements Serializable, IDoETarget
{
	@Id @GeneratedValue
	private Long id;
	
	@Property(name = "strategy_id")
	private int strategyId;
	
	@Property(name = "strategy_param1")
	private int strategyParam1;
	
	@Property(name = "strategy_param2")
	private int strategyParam2;
	
	@Relationship(type="specifies_sku", direction=Relationship.OUTGOING)
	private SKU sku;
	
	
	@Transient
	public enum Strategies
	{
		NONE(0), 
		SQ(1), 
		RS(2), 
		SS(3);
		
		private final int value;
	    private Strategies(int value) 
	    {
	        this.value = value;
	    }

	    public int getValue() 
	    {
	        return value;
	    }
	    
	    public static Strategies fromInt(int i) 
	    {
	        for (Strategies b : Strategies.values()) 
	        {
	            if (b.getValue() == i) { return b; }
	        }
	        return null;
	    }
	    
	    public static int getStrategyId(String string)
	    {
	    	switch(string)
	    	{
	    	case "(s,q)":
	    		return 1;
	    	case "(r,s)":
	    		return 2;
	    	case "(s,S)":
	    		return 3;
	    	default:
	    		return 0;
	    	}
	    }
	}
	
	public SKUStorageDetails()
	{
	}
	
	public SKUStorageDetails(SKU sku, int strategyId, int strategyParam1, int strategyParam2)
	{
		this.sku = sku;
		this.strategyId = strategyId;
		this.strategyParam1 = strategyParam1;
		this.strategyParam2 = strategyParam2;
	}
	
	public SKUStorageDetails(SKU sku, String strategyString, int strategyParam1, int strategyParam2)
	{
		this.sku = sku;
		this.strategyId = Strategies.getStrategyId(strategyString);
		this.strategyParam1 = strategyParam1;
		this.strategyParam2 = strategyParam2;
	}
	
	public SKUStorageDetails(SKU sku)
	{
		this.sku = sku;
	}
	
	public SKU getSKU()
	{
		return this.sku;
	}
	
	public int getStrategyId()
	{
		return this.strategyId;
	}
	
	public int getStrategyParam1()
	{
		return this.strategyParam1;
	}
	
	public int getStrategyParam2()
	{
		return this.strategyParam2;
	}

	@Override
	public void setProperty(String name, float value) 
	{
		switch(name)
		{
			case "strategy_id":
				this.strategyId = (int)value;
				break;
			case "strategy_param1":
				this.strategyParam1 = (int)value;
				break;
			case "strategy_param2":
				this.strategyParam2 = (int)value;
				break;
			default:
				System.out.println("Property not found");				
		}
	}
}
