package com.logfarm.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import org.javatuples.Pair;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;

import com.logfarm.utility.GeneratedDataHandler;
import com.logfarm.utility.Logger;

@NodeEntity(label="facility")
public class Facility implements Serializable
{	
	@Id @GeneratedValue
	private Long id;
	
	@Property(name="name")
	private String name;
	
	@Property(name="lon")
	private double lon;
	
	@Property(name="lat")
	private double lat;
	
	@Property(name="working_hours")
	private float[] workingHours;
	
	@Relationship(type="has_component", direction=Relationship.OUTGOING)
	private ArrayList<Component> components = new ArrayList<Component>();
	
	@Relationship(type="orders_from", direction=Relationship.OUTGOING)
	private ArrayList<SKUSpecification> ordersFrom = new ArrayList<SKUSpecification>();

	@Transient
	ComponentProduction componentSources;
	
	@Transient
	ComponentTransformation componentTransformation;
	
	@Transient
	ComponentConsumption componentSinks;
	
	@Transient
	ComponentStorages componentStorages;
	
	@Transient
	ComponentDistribution componentDistribution;
	
	@Transient
	ComponentDemandGenerator componentDemandGenerator;
	
	@Transient
	LinkedList<Order> outstandingOrders = new LinkedList<Order>();
	
	@Transient
	ArrayList<Order> pendingOrdersThisDay = new ArrayList<Order>();
	
	@Transient
	ArrayList<Order> pendingOrdersNextDay = new ArrayList<Order>();
	
	@Transient
	HashMap<SKU, Facility> skuSourcedBy = new HashMap<SKU, Facility>();
	
	@Transient
	Pair<Double, Double> coordinate;
	
	public Facility()
	{
		this.name = "default";
	}
	
	public Facility(String name, double lat, double lon, float[] workingHours)
	{
		this.name = name;
		this.lon = lon;
		this.lat = lat;
		coordinate = new Pair<Double, Double>(lat, lon);
		this.workingHours = workingHours;
	}
	
	public void resetId()
	{
		this.id = null;
	}

	public String getName()
	{
		return this.name;
	}
	
	public Pair<Double, Double> getCoordinate()
	{
		return coordinate;
	}
	
	public void addComponent(Component component)
	{
		components.add(component);
	}
	
	public void addSupplier(Facility supplier, ArrayList<SKU> skus)
	{
		this.ordersFrom.add(new SKUSpecification(supplier, skus));	
	}
	
	public void placeOrder(Order order)
	{
		pendingOrdersNextDay.add(order);
		
		//System.out.println("Received an order!");
		//System.out.println("distance:" + DistanceCalculator.GetDistance(this.getCoordinate(), order.getRecipient().getCoordinate()));
	}
	
	public void acceptShipment(Order order)
	{
		//System.out.println("Received a shipment!");
		
		if(this.componentStorages != null)
		{
			// Store the SKUs
			ArrayList<Pair<SKU, Integer>> orderPositions = order.getPositions();
			for(int i = 0; i < orderPositions.size(); i++)
			{
				componentStorages.store(orderPositions.get(i).getValue0(), orderPositions.get(i).getValue1());
				
				if(Logger.isUsed) Logger.singleton.log(name + " received " + orderPositions.get(i).getValue1() + " of sku " + orderPositions.get(i).getValue0().getName());
			}
		}
		else
		{
			// Burn the SKUs
			//System.out.println("Burnt incoming SKUs");
			ArrayList<Pair<SKU, Integer>> orderPositions = order.getPositions();
			for(int i = 0; i < orderPositions.size(); i++)
			{
				if(Logger.isUsed) Logger.singleton.log("<> " + name + " consumed " + orderPositions.get(i).getValue1() + " of sku " + orderPositions.get(i).getValue0().getName());
			}
		}	
	}
	
	public void awake()
	{
		coordinate = new Pair<Double, Double>(lat, lon);
	}
	
	public void initialize(LocalDate startDate)
	{
		// Find suppliers
		for(SKUSpecification skuSpecification : this.ordersFrom)
		{
			Facility supplier = skuSpecification.getSupplier();
			ArrayList<SKU> skus = skuSpecification.getSKUs();
			for(SKU sku : skus) this.skuSourcedBy.put(sku, supplier);			
		}
		
		// Initialize all components
		for(int i = 0; i < this.components.size(); i++)
		{
			Component component = this.components.get(i);
			if(component instanceof ComponentProduction)
			{
				this.componentSources = (ComponentProduction) component;
				this.componentSources.initialize();
			}
			else if(component instanceof ComponentTransformation)
			{
				this.componentTransformation = (ComponentTransformation) component;
				this.componentTransformation.initialize();
			}
			else if(component instanceof ComponentConsumption)
			{
				this.componentSinks = (ComponentConsumption) component;
				this.componentSinks.initialize();
			}
			else if(component instanceof ComponentStorages)
			{
				this.componentStorages = (ComponentStorages) component;
				this.componentStorages.initialize();
			}
			else if(component instanceof ComponentDistribution)
			{
				this.componentDistribution = (ComponentDistribution) component;
				this.componentDistribution.initialize(this);
			}
			else if(component instanceof ComponentDemandGenerator)
			{
				this.componentDemandGenerator = (ComponentDemandGenerator) component;
				this.componentDemandGenerator.initialize(startDate);
			}
		}
	}


	public void update(LocalDate date, Random rng, GeneratedDataHandler gdh, boolean generateData) 
	{	
		float dth = workingHours[date.getDayOfWeek().ordinal()];
		if(Logger.isUsed) Logger.singleton.log(name + ": works for " + dth + " hours");
		if(dth == 0) return;
		
		// See sinking
		if(this.componentSinks != null)
		{
			ArrayList<Pair<SKU, Integer>> consumption = this.componentSinks.getConsumption(dth, rng.nextFloat());
			// Withdraw everything
			for(Pair<SKU, Integer> entry : consumption)
			{
				int sinked = this.componentStorages.withdraw(entry.getValue0(), entry.getValue1());
				//if(sinked != entry.getValue1()) System.out.println("Could not sink all SKUs!");
				
				if(Logger.isUsed) Logger.singleton.log("> sinked " + sinked + " of " + entry.getValue1() + " from sku " + entry.getValue0().getName());
			}
		}
		
		// See production
		if(this.componentSources != null)
		{
			ArrayList<Pair<SKU, Integer>> production = this.componentSources.getProduction(dth, rng.nextFloat());
			// Store everything
			if(production != null)
			{
				for(Pair<SKU, Integer> entry : production)
				{				
					this.componentStorages.store(entry);
					if(Logger.isUsed) Logger.singleton.log("> produced " + String.valueOf(entry.getValue1()) + " units of " + entry.getValue0().getName());
				}
			}
		}
		
		// See transformation
		if(this.componentTransformation != null)
		{
			ArrayList<Pair<SKU, Integer>> skuChanges = this.componentTransformation.getSKUChanges(dth, rng.nextFloat());
			// Perform changes
			if(skuChanges != null)
			{
				for(Pair<SKU, Integer> entry : skuChanges)
				{
					int quantity = entry.getValue1();
					if(quantity > 0f) this.componentStorages.store(entry);
					else if(quantity < 0f) this.componentStorages.withdraw(entry.getValue0(), -quantity);
					
					//System.out.println("changed " + entry.getValue0().getName() + " with " + String.valueOf(entry.getValue1()));
					if(Logger.isUsed) Logger.singleton.log("> changed " + entry.getValue0().getName() + " with " + String.valueOf(entry.getValue1()));
				}
			}
		}
		
		// See storages
		if(this.componentStorages != null)
		{
			ArrayList<Pair<SKU, Integer>> skusToOrder = this.componentStorages.controlInventory();
			orderSKUs(date, skusToOrder, gdh, generateData);
			componentStorages.makeStockReport(date, this, gdh, generateData);
			
			if(Logger.isUsed)
			{
				for(Pair<SKU, Integer> entry : skusToOrder) Logger.singleton.log("> reordered " + entry.getValue1() + " of sku " + entry.getValue0().getName());
			}
		}
		
		// See demand
		if(this.componentDemandGenerator != null)
		{
			// For now, only 1 SKU per order
			Pair<SKU, Integer> position = componentDemandGenerator.sampleDemand(date);
			if(position != null)
			{
				ArrayList<Pair<SKU, Integer>> skusToOrder = new ArrayList<Pair<SKU, Integer>>();
				skusToOrder.add(position);				
				orderSKUs(date, skusToOrder, gdh, generateData);
				
				if(Logger.isUsed)
				{
					for(Pair<SKU, Integer> entry : skusToOrder) Logger.singleton.log("> generated demand with quantity " + entry.getValue1() + " of sku " + entry.getValue0().getName());
				}
			}
		}
	}
	
	public void updateDistribution(LocalDate date, Random rng, GeneratedDataHandler gdh, boolean generateData)
	{
		if(!worksOnDate(date)) return;
		
		if(this.componentDistribution != null)
		{			
			outstandingOrders.addAll(pendingOrdersThisDay);
			pendingOrdersThisDay.clear();
			pendingOrdersThisDay.addAll(pendingOrdersNextDay);
			pendingOrdersNextDay.clear();
			
			this.componentDistribution.update(date, outstandingOrders);
		}
	}
	
	private void orderSKUs(LocalDate t, ArrayList<Pair<SKU, Integer>> skusToOrder, GeneratedDataHandler gdh, boolean generateData)
	{
		for(Pair<SKU, Integer> pair : skusToOrder)
		{
			// Place a new order (for now, no pooling)
			ArrayList<Pair<SKU, Integer>> positions = new ArrayList<Pair<SKU, Integer>>();
			positions.add(pair);
			Facility supplier = this.skuSourcedBy.get(pair.getValue0());
			Order newOrder = new Order(supplier, this, positions, t);
			if(generateData) gdh.addGeneratedData(newOrder);
			supplier.placeOrder(newOrder);
		}
	}
	
	public boolean worksOnDate(LocalDate date)
	{
		float dth = workingHours[date.getDayOfWeek().ordinal()];
		return dth > 0f;
	}
	
	public void removeStock(SKU sku, int quantity)
	{
		componentStorages.withdraw(sku, quantity);
	}
}
