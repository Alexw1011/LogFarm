package com.logfarm.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import org.javatuples.Pair;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;

import com.logfarm.utility.GeneratedDataHandler;
import com.logfarm.utility.Logger;

@NodeEntity(label="storages_component")
public class ComponentStorages extends Component implements Serializable
{
	@Relationship(type="utilizes", direction=Relationship.OUTGOING)
	private ArrayList<Storage> storages = new ArrayList<Storage>();
	
	@Transient
	private HashMap<SKU, Storage> skuResponsibilities = new HashMap<SKU, Storage>();
	
	public ComponentStorages()
	{
		
	}
	
	public void addStorage(Storage storage)
	{
		this.storages.add(storage);
	}
	
	public ArrayList<Storage> getStorages()
	{
		return this.storages;
	}
	
	public void store(SKU sku, int quantity)
	{
		// Single responsibility only
		this.skuResponsibilities.get(sku).store(sku, quantity);
	}
	
	public void store(Pair<SKU, Integer> pair)
	{
		store(pair.getValue0(), pair.getValue1());
	}
	
	public int withdraw(SKU sku, int quantity)
	{
		return this.skuResponsibilities.get(sku).withdraw(sku, quantity);
	}
	
	public void initialize()
	{
		for(Storage storage : storages)
		{
			storage.initialize();
			// Find responsibilities
			ArrayList<SKU> skus = storage.getAllowedSKUs();
			for(SKU sku : skus) this.skuResponsibilities.put(sku, storage);
		}
	}
	
	public void makeStockReport(LocalDate t, Facility facility, GeneratedDataHandler gdh, boolean generateData)
	{
		// Collect current stock
		ArrayList<Pair<SKU, Integer>> allStocks = new ArrayList<Pair<SKU, Integer>>();
		for(Storage storage : storages)
		{
			allStocks.addAll(storage.getStocks());
			if(Logger.isUsed)
			{
				ArrayList<Pair<SKU, Integer>> stocks = storage.getStocks();
				for(Pair<SKU, Integer> pair : stocks)
				{
					Logger.singleton.log("> current storage for " + pair.getValue0().getName() + ": " + pair.getValue1() + "/" + String.valueOf(storage.getCapacity()));
				}
			}	
		}
			
		// Fill the report
		if(generateData) gdh.addGeneratedData(new StockReport(t, allStocks, facility));
	}
	
	public ArrayList<Pair<SKU, Integer>> controlInventory()
	{
		ArrayList<Pair<SKU, Integer>> overallSKUsToOrder = new ArrayList<Pair<SKU, Integer>>();
		for(int i = 0; i < this.storages.size(); i++)
		{
			Storage storage = this.storages.get(i);
			ArrayList<Pair<SKU, Integer>> skusToOrder = storage.controlInventory();
			overallSKUsToOrder.addAll(skusToOrder);
		}
		
		return overallSKUsToOrder;
		
		/*
		// Check supply and reorder levels
		for(int i = 0; i < this.storages.size(); i++)
		{
			Storage storage = this.storages.get(i);
			ArrayList<Pair<SKU, Integer>> skusToOrder = storage.controlInventory();
			// Search for suppliers (we use single sourcing) and compile lists of skus to order from the supplier
			HashMap<Facility, ArrayList<Pair<SKU, Integer>>> skusToOrderFromSupplier = new HashMap<Facility, ArrayList<Pair<SKU, Integer>>>();
			for(Pair<SKU, Integer> skuToOrder : skusToOrder)
			{
				Facility facility = this.skuSourcedBy.get(skuToOrder.getValue0());
				// Get list of supplier and add it
				ArrayList<Pair<SKU, Integer>> skusToOrderFromSupplierEntry = skusToOrderFromSupplier.getOrDefault(facility, new ArrayList<Pair<SKU, Integer>>());
				skusToOrderFromSupplierEntry.add(skuToOrder);
				// Put it back in the map
				skusToOrderFromSupplier.put(facility, skusToOrderFromSupplierEntry);
			}
			// Place orders
			for (Map.Entry<Facility, ArrayList<Pair<SKU, Integer>>> entry : skusToOrderFromSupplier.entrySet()) entry.getKey().placeOrder(new Order(this, entry.getValue()));
		}
		*/
	}
}
