package com.logfarm.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.javatuples.Pair;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label="component_sinks")
public class ComponentConsumption extends Component implements Serializable
{
	@Relationship(type="utilizes", direction=Relationship.OUTGOING)
	private ArrayList<Sink> sinks = new ArrayList<Sink>();
	
	@Relationship(type="may_have_malfunction", direction=Relationship.OUTGOING)
	private Malfunction malfunction = null;
	
	public ComponentConsumption()
	{

	}
	
	public void addSink(Sink sink)
	{
		this.sinks.add(sink);
	}
	
	public void addSink(SKU sku, float rate)
	{
		this.sinks.add(new Sink(sku, rate));
	}
	
	public void initialize()
	{
		if(malfunction != null) malfunction.initialize();
	}
	
	public ArrayList<Sink> getSinks()
	{
		return this.sinks;
	}
	
	public ArrayList<Pair<SKU, Integer>> getConsumption(float dth, float rn)
	{
		ArrayList<Pair<SKU, Integer>> consumption = new ArrayList<Pair<SKU, Integer>>();
		
		for(Sink sink : sinks) consumption.add(sink.getConsumption(dth));
		
		// See to malfunction
		if(malfunction != null)
		{
			float operabilityLevel = malfunction.getRemainingOperability(rn);
			if(operabilityLevel == 0f) return null;
			else if(operabilityLevel < 1f)
			{
				// Modify each pair
				for(int i = 0; i < consumption.size(); i++)
				{
					Pair<SKU, Integer> original = consumption.get(i);
					consumption.set(i, new Pair<SKU, Integer>(original.getValue0(), (int)Math.round((float)original.getValue1() * operabilityLevel)));
				}
			}
		}	
		
		return consumption;
	}
	
	public void enableMalfunction(Malfunction malfunction)
	{
		this.malfunction = malfunction;
	}
}
