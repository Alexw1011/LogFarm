package com.logfarm.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.javatuples.Pair;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;

import com.logfarm.engine.VRPModel;
import com.logfarm.engine.VRPSolver;
import com.logfarm.engine.VRPModel.Node;
import com.logfarm.engine.VRPSolver.Route;
import com.logfarm.engine.VRPSolver.VRPSolverResults;
import com.logfarm.utility.Logger;

@NodeEntity(label="component_distribution")
public class ComponentDistribution extends Component implements Serializable
{
	@Relationship(type="transport_relation", direction=Relationship.OUTGOING)
	private ArrayList<TransportRelation> transportRelations = new ArrayList<TransportRelation>();
	
	@Relationship(type="utilizes_dynamic_routing_group", direction=Relationship.OUTGOING)
	private DynamicRoutingGroup dynamicRoutingGroup;
	
	@Relationship(type="utilizes_fleet", direction=Relationship.OUTGOING)
	private VehicleFleet vehicleFleetForStaticRouting;
	
	@Transient
	HashMap<Facility, Object> recipientResponsibilities = new HashMap<Facility, Object>();
	
	@Transient 
	VRPModel vrpModel;
	
	@Transient
	int dynamicVehicleFleetHighestCapacity;
	
	public ComponentDistribution()
	{
		
	}
	
	public void initialize(Facility depot)
	{
		if(this.vehicleFleetForStaticRouting != null) this.vehicleFleetForStaticRouting.initialize();
		
		for(TransportRelation transportRelation : transportRelations)
		{
			transportRelation.initialize();
			this.recipientResponsibilities.put(transportRelation.getRecipient(), transportRelation);
		}
		
		if(dynamicRoutingGroup != null)
		{
			dynamicRoutingGroup.initialize();
			ArrayList<Facility> customersInGroup = dynamicRoutingGroup.getMembers();			
			vrpModel = new VRPModel(customersInGroup, depot, dynamicRoutingGroup.getVehicleFleet());
			for(Facility customer : customersInGroup) this.recipientResponsibilities.put(customer, dynamicRoutingGroup);
			dynamicVehicleFleetHighestCapacity = dynamicRoutingGroup.getVehicleFleet().getFirstVehicleCapacity();
		}
	}
	
	
	public void addTransportRelation(TransportRelation transportRelation)
	{
		this.transportRelations.add(transportRelation);
	}
	
	public void setVehicleFleetForStaticRouting(VehicleFleet fleet)
	{
		this.vehicleFleetForStaticRouting = fleet;
	}
	
	public void setDynamicRoutingGroup(DynamicRoutingGroup dynamicRoutingGroup)
	{
		this.dynamicRoutingGroup = dynamicRoutingGroup;
	}
	
	public void update(LocalDate t, LinkedList<Order> outstandingOrders)
	{
		// Test
		LocalDate test = LocalDate.of(2014, 8, 1);
		if(t.isAfter(test))
		{
			int z = 1;
		}
		
		// Separate orders
		ArrayList<Order> ordersForStaticRouting = new ArrayList<Order>();
		ArrayList<Order> ordersForDynamicRouting = new ArrayList<Order>();
		for(Order order : outstandingOrders)
		{
			Facility recipient = order.getRecipient();
			
			// Works on day?
			if(!recipient.worksOnDate(t)) continue;
			
			Object responsibleObject = this.recipientResponsibilities.get(recipient);
			if(responsibleObject instanceof TransportRelation)
			{
				ordersForStaticRouting.add(order);
				
				if(Logger.isUsed) Logger.singleton.log(order.getSupplier().getName() + " has outstanding static order for recipient " + order.getRecipient().getName() + " with id " + order.getInternalOrderId());
				ArrayList<Pair<SKU, Integer>> positions = order.getPositions();
				if(Logger.isUsed) for(Pair<SKU, Integer> entry : positions) Logger.singleton.log(order.getSupplier().getName() + " " + entry.getValue1() + " of sku " + entry.getValue0().getName());
			}
			else
			{
				ordersForDynamicRouting.add(order);
				
				if(Logger.isUsed) Logger.singleton.log(order.getSupplier().getName() + " has outstanding dynamic order for recipient " + order.getRecipient().getName() + " with id " + order.getInternalOrderId());
				ArrayList<Pair<SKU, Integer>> positions = order.getPositions();
				if(Logger.isUsed) for(Pair<SKU, Integer> entry : positions) Logger.singleton.log(order.getSupplier().getName() + " " + entry.getValue1() + " of sku " + entry.getValue0().getName());
			}	
		}
		
		// Process
		ArrayList<Order> servingOrders = new ArrayList<Order>();
		// Static routing
		if(ordersForStaticRouting.size() > 0)
		{
			servingOrders.addAll(staticRouting(ordersForStaticRouting));	
		}
		// Dynamic routing
		if(ordersForDynamicRouting.size() > 0)
		{
			servingOrders.addAll(dynamicRouting(ordersForDynamicRouting));
		}
		
		// Remove served orders
		if(servingOrders.size() >= 0)
		{
			for(int i = 0; i < servingOrders.size(); i++)
			{
				Order order = servingOrders.get(i);
				
				// Remove stock
				ArrayList<Pair<SKU, Integer>> positions = order.getPositions();
				for(Pair<SKU, Integer> position : positions) order.getSupplier().removeStock(position.getValue0(), position.getValue1());	
				
				// Dispatch vehicles
				order.getRecipient().acceptShipment(order);
				if(Logger.isUsed) Logger.singleton.log(order.getSupplier().getName() + " served order " + order.getInternalOrderId());
				order.markAsDelivered(t);
				outstandingOrders.remove(order);
			}
		}	
	}
	
	public ArrayList<Order> staticRouting(ArrayList<Order> outstandingOrders)
	{
		ArrayList<Order> servingOrders = new ArrayList<Order>();
		
		ArrayList<Integer> vehicleCapacities = this.vehicleFleetForStaticRouting.getVehicleCapacities();
		ArrayList<Integer> vehicleLoadThisDay = new ArrayList<Integer>(vehicleCapacities.size());
		for(int i = 0; i < vehicleCapacities.size(); i++) vehicleLoadThisDay.add(0);
		ArrayList<Facility> vehicleBoundToRecipientThisDay  = new ArrayList<Facility>(vehicleCapacities.size());
		for(int i = 0; i < vehicleCapacities.size(); i++) vehicleBoundToRecipientThisDay.add(null);
		int remainingCapacity = this.vehicleFleetForStaticRouting.getOverallCapacity();
		int unusedCapacityByUnboundVehicles = remainingCapacity;
		// This is the additional space created by vehicles, bound to a transport relation, but not fully loaded
		HashMap<Facility, Integer> remainingSpaceByBoundVehicles = new HashMap<Facility, Integer>();
		
		// Try to fit each order
		Iterator<Order> it = outstandingOrders.iterator();
		while(it.hasNext() && remainingCapacity > 0)
		{
			// Probably we can fit more
			Order order = it.next();
			Facility recipient = order.getRecipient();
			// Calculate total size of order
			int orderTotalSize = 0;
			ArrayList<Pair<SKU, Integer>> orderPositions = order.getPositions();
			for(int i = 0; i < orderPositions.size(); i++) orderTotalSize += orderPositions.get(i).getValue1();
			
			// Check available size
			int availableCapacity = unusedCapacityByUnboundVehicles + remainingSpaceByBoundVehicles.getOrDefault(recipient, 0);
			if(availableCapacity >= orderTotalSize)
			{
				// We know that we can serve the order. Now, allocate enough space in the vehicles
				it.remove();
				servingOrders.add(order);
				int remainingOrderSize = orderTotalSize;
				for(int i = 0; i < vehicleCapacities.size(); i++)
				{
					// Is the vehicle free or at least already bound to the requesting TR?
					if(vehicleBoundToRecipientThisDay.get(i) == null || vehicleBoundToRecipientThisDay.get(i) == recipient)
					{
						int capacityThisVehicle = vehicleCapacities.get(i);
						// Use it
						int spaceOnThisVehicle = capacityThisVehicle - vehicleLoadThisDay.get(i);
						int spaceAfterLoading = Math.max(0, spaceOnThisVehicle - remainingOrderSize);
						int deltaLoad = spaceOnThisVehicle - spaceAfterLoading;
						remainingOrderSize -= deltaLoad;
						remainingCapacity -= deltaLoad;
						
						if(vehicleBoundToRecipientThisDay.get(i) == null) unusedCapacityByUnboundVehicles -= capacityThisVehicle;
						vehicleLoadThisDay.set(i, capacityThisVehicle - spaceAfterLoading);
						vehicleBoundToRecipientThisDay.set(i, recipient);
						remainingSpaceByBoundVehicles.put(recipient, spaceAfterLoading);
					}
				}
			}
		}
		return servingOrders;
	}
	
	public ArrayList<Order> dynamicRouting(ArrayList<Order> outstandingOrders)
	{
		// Collect demands before appling the vrp model
		ArrayList<Pair<Facility, Integer>> demands = new ArrayList<Pair<Facility, Integer>>();
		ArrayList<Facility> members = this.dynamicRoutingGroup.getMembers();
		// Maintain a dict of orders per member
		HashMap<Facility, ArrayList<Order>> ordersPerMember = new HashMap<Facility, ArrayList<Order>>();
		// Prepare the members
		for(int i = 0; i < members.size(); i++) demands.add(new Pair<Facility, Integer>(members.get(i), 0));
		// Fill demand
		for(int i = 0; i < outstandingOrders.size(); i++)
		{
			Order order = outstandingOrders.get(i);
			Facility customer = order.getRecipient();
			// find in demand
			for(int j = 0; j < demands.size(); j++)
			{
				if(customer == demands.get(j).getValue0())
				{
					int previousValue = demands.get(j).getValue1();
					int additionalValue = order.getOverallQuantity();
					
					if(previousValue + additionalValue <= dynamicVehicleFleetHighestCapacity)
					{
						demands.set(j, new Pair<Facility, Integer>(customer, previousValue + additionalValue));
						if(!ordersPerMember.containsKey(customer)) ordersPerMember.put(customer, new ArrayList<Order>());
						ordersPerMember.get(customer).add(order);
					}
					break;
				}			
			}
		}
		// Set up vrp model
		boolean hasNonZeroDemand = vrpModel.setDemands(demands);
		VRPSolverResults vrpSolverResults = null;
		if(hasNonZeroDemand) vrpSolverResults = VRPSolver.SolveWithICW(this.vrpModel, 5);
		
		// Extract served orders
		ArrayList<Order> servedOrders = new ArrayList<Order>();
		if(vrpSolverResults != null)
		{
			// Process results
			// Find all served facilities and their orders
			for(int i = 0; i < vrpSolverResults.routes.size(); i++)
			{
				Route route = vrpSolverResults.routes.get(i);
				for(int u = 1; u < route.nodes.size() - 1; u++) 
				{
					Node node = route.nodes.get(u);
					servedOrders.addAll(ordersPerMember.get(node.facility));
					if(Logger.isUsed) Logger.singleton.log("> route " + i + " serves " + node.facility.getName() + " with " + node.demand);
				}
			}
		}
		
		return servedOrders;
	}
}
