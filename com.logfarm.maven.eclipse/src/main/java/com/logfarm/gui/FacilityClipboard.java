package com.logfarm.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.javatuples.Tuple;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FacilityClipboard implements Serializable
{
	public class ProductionEntryClipboard implements Serializable
	{
		public TableSKUsRow sku;
		public float rate;
		public boolean DoE;
	}
	
	public class TransformationEntryClipboard implements Serializable
	{
		public float rate;
		public boolean DoE;
		ArrayList<Pair<TableSKUsRow, Integer>> skuChanges;
	}
	
	public class ConsumptionEntryClipboard implements Serializable
	{
		public TableSKUsRow sku;
		public float rate;
		public boolean DoE;
	}
	
	public class StorageEntryClipboard implements Serializable
	{
		public class StorageSKUEntryClipboard implements Serializable
		{
			public TableSKUsRow sku;
			public String policy;
			public int param1;
			public boolean param1DoE;
			public int param2;
			public boolean param2DoE;
		}
		
		public float capacity;
		public boolean capacityDoE;
		public float initialFill;
		public boolean initialFillDoE;
		ArrayList<StorageSKUEntryClipboard> storageSKUEntries = new ArrayList<StorageSKUEntryClipboard>();
	}
	
	public class VehicleOwnershipEntryClipboard implements Serializable
	{
		public TableVehicleClassesRow vehicleClass;
		public int numberOfVehicles;
		public boolean DoE;
	}
	
	public class DistributionClipboard implements Serializable
	{
		public String type;
		public float param1 = 0f;
		public boolean param1DoE;
		public float param2 = 0f;
		public boolean param2DoE;
	}
	
	public class DemandEntryClipboard implements Serializable
	{
		public TableSKUsRow sku;
		public float weight;
		public DistributionClipboard quantityDistribution = new DistributionClipboard();
	}
	
	public class MalfunctionClipboard implements Serializable
	{
		public float dailyChance = 0f;
		public boolean DoE;
		public DistributionClipboard distribution = new DistributionClipboard();
	}
	
	public class DistributionConnection implements Serializable
	{
		public DraggableNode node;
		public boolean staticConnection;
	}
	
	public void AddConnection(DraggableNode newNode, boolean isStatic)
	{
		DistributionConnection newEntry = new DistributionConnection();
		newEntry.node = newNode;
		newEntry.staticConnection = isStatic;
		connectionEntries.add(newEntry);
		connectedNodes.add(newNode);
	}
	
	public void RemoveConnection(DraggableNode nodeToRemove)
	{
		for(int i = connectionEntries.size() - 1; i >= 0; i--)
		{
			if(connectionEntries.get(i).node == nodeToRemove)
			{
				connectionEntries.remove(i);
				connectedNodes.remove(i);
				return;
			}
		}
	}
	
	public DragIconType type;
	public String name = "";
	public float latitude = 0;
	public float longitude = 0;
	public float[] workingHours = new float[]{0, 0, 0, 0, 0, 0, 0};
	
	public transient ArrayList<Pair<TableSKUsRow, DraggableNode>> sourcedBy = new ArrayList<Pair<TableSKUsRow, DraggableNode>>();
	public transient ArrayList<DraggableNode> connectedNodes = new ArrayList<DraggableNode>();
	
	public boolean hasComponentProduction = false;
	ArrayList<ProductionEntryClipboard> productionEntries = new ArrayList<ProductionEntryClipboard>();
	public MalfunctionClipboard productionMalfunction;
	
	public boolean hasComponentTransformation = false;
	ArrayList<TransformationEntryClipboard> transformationEntries = new ArrayList<TransformationEntryClipboard>();
	public MalfunctionClipboard transformationMalfunction;
	
	public boolean hasComponentConsumption = false;
	ArrayList<ConsumptionEntryClipboard> consumptionEntries = new ArrayList<ConsumptionEntryClipboard>();
	
	public boolean hasComponentStorage = false;
	ArrayList<StorageEntryClipboard> storageEntries = new ArrayList<StorageEntryClipboard>();
	
	public boolean hasComponentDistribution = false;
	ArrayList<DistributionConnection> connectionEntries = new ArrayList<DistributionConnection>();
	ArrayList<VehicleOwnershipEntryClipboard> vehicleFleetStaticEntries = new ArrayList<VehicleOwnershipEntryClipboard>();
	ArrayList<VehicleOwnershipEntryClipboard> vehicleFleetDynamicEntries = new ArrayList<VehicleOwnershipEntryClipboard>();
	
	public boolean hasComponentDemand = false;
	DistributionClipboard timeBetweenDemands = new DistributionClipboard();
	ArrayList<DemandEntryClipboard> demandEntries = new ArrayList<DemandEntryClipboard>();
	
	public boolean isCustomerGroup = false;
	public int customerGroupNumberOfCustomer = 0;
	public float customerGroupMinLat = 0;
	public float customerGroupMaxLat = 0;
	public float customerGroupMinLon = 0;
	public float customerGroupMaxLon = 0;
}
