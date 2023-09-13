package com.logfarm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.javatuples.Pair;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;

import com.logfarm.utility.Logger;

@NodeEntity(label="storage")
public class Storage implements Serializable
{
	@Id @GeneratedValue
	private Long id;
	
	@Property(name="capacity")
	private int capacity;
	
	@Property(name = "initial_fill")
	private float initialFill;
	
	@Relationship(type="utilizes_sku_storage_details", direction=Relationship.OUTGOING)
	private ArrayList<SKUStorageDetails> skuStorageDetails;
	
	@Transient
	private HashMap<SKU, Integer> stocks = new HashMap<SKU, Integer>();
	
	@Transient
	private int stock = 0;
	
	@Transient
	private int numberOfSKUs;
	
	@Transient
	private HashMap<SKU, SKUStorageDetails> skuDetails = new HashMap<SKU, SKUStorageDetails>();
	
	public Storage()
	{
		
	}
	
	public Storage(int capacity, float initialFill)
	{
		this.skuStorageDetails = new ArrayList<SKUStorageDetails>();
		this.capacity = capacity;
		this.initialFill = initialFill;
	}
	
	public void addAllowedSKUDetails(SKUStorageDetails skuDetails)
	{
		this.skuStorageDetails.add(skuDetails);
	}
	
	public int getCapacity() { return capacity; };
	
	public ArrayList<SKU> getAllowedSKUs()
	{
		ArrayList<SKU> allowedSKUs = new ArrayList<SKU>();
		for(int i = 0; i < this.skuStorageDetails.size(); i++) allowedSKUs.add(this.skuStorageDetails.get(i).getSKU());
		return allowedSKUs;
	}
	
	public void initialize()
	{
		// Initialize stock to initial fill
		int initialStockLevel = (int)(this.capacity * this.initialFill);
		this.numberOfSKUs = this.skuStorageDetails.size();
		for(int i = 0; i < this.numberOfSKUs; i++)
		{
			SKUStorageDetails details = this.skuStorageDetails.get(i);
			stocks.put(details.getSKU(), initialStockLevel);
			skuDetails.put(details.getSKU(), details);
		}
	}
	
	public void store(SKU sku, int quantity)
	{
		int remainingStock = this.capacity - this.stock;
		int storableQuantity = Math.min(quantity, remainingStock);
		this.stock += storableQuantity;
		stocks.put(sku, stocks.get(sku) + storableQuantity);		
		if(Logger.isUsed) Logger.singleton.log("<?> stored " + sku.getName() + " with quantity of " + storableQuantity);
	}
	
	public ArrayList<Pair<SKU, Integer>> getStocks()
	{
		ArrayList<Pair<SKU, Integer>> stocksList = new ArrayList<Pair<SKU, Integer>>();
		for(HashMap.Entry<SKU, Integer> entry : stocks.entrySet()) stocksList.add(new Pair<SKU, Integer>(entry.getKey(), entry.getValue()));
		return stocksList;
	}
	
	// return: withdrawn SKUs
	public int withdraw(SKU sku, int quantity)
	{
		int stockOfSKU = stocks.get(sku);
		int withdrawableQuantity = Math.min(quantity, stockOfSKU);
		this.stock -= withdrawableQuantity;
		stocks.put(sku, stockOfSKU - withdrawableQuantity);
		return withdrawableQuantity;
	}
	
	public ArrayList<Pair<SKU, Integer>> controlInventory()
	{
		ArrayList<Pair<SKU, Integer>> orders = new ArrayList<Pair<SKU, Integer>>();
		for (Entry<SKU, Integer> pair : stocks.entrySet())
		{
			SKUStorageDetails details = skuDetails.get(pair.getKey());
			int stockLevel = pair.getValue();
			// Rest depends on strategy
			int strategyId = details.getStrategyId();
			int strategyParam1 = details.getStrategyParam1();
			int strategyParam2 = details.getStrategyParam2();
			if(strategyId == 1)
			{
				if(stockLevel < strategyParam1)
				{
					int undershot = strategyParam1 - stockLevel;
					int numberOfOrders = (int)Math.ceil((double)undershot / (double)strategyParam2);
					// Initiate orders
					for(int u = 0; u < numberOfOrders; u++) orders.add(new Pair<SKU, Integer>(details.getSKU(), strategyParam2));
				}
			}
		}
		return orders;
	}
}
