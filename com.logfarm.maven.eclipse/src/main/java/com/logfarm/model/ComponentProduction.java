package com.logfarm.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.javatuples.Pair;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label="component_production")
public class ComponentProduction extends Component implements Serializable
{
	@Relationship(type="utilizes", direction=Relationship.OUTGOING)
	private ArrayList<Source> sources = new ArrayList<Source>();
	
	@Relationship(type="may_have_malfunction", direction=Relationship.OUTGOING)
	private Malfunction malfunction = null;
	
	public ComponentProduction()
	{

	}
	
	public void addSource(Source source)
	{
		sources.add(source);
	}
	
	public void addSource(SKU sku, float rate)
	{
		sources.add(new Source(sku, rate));
	}
	
	public ArrayList<Source> getSources()
	{
		return this.sources;
	}
	
	public void initialize()
	{
		if(malfunction != null) malfunction.initialize();
	}
	
	public ArrayList<Pair<SKU, Integer>> getProduction(float dth, float rn)
	{
		ArrayList<Pair<SKU, Integer>> production = new ArrayList<Pair<SKU, Integer>>();
		// Compile a list of each source's production
		for(Source source : sources) production.add(source.getProduction(dth));
		
		// See to malfunction
		if(malfunction != null)
		{
			float operabilityLevel = malfunction.getRemainingOperability(rn);
			if(operabilityLevel == 0f) return null;
			else if(operabilityLevel < 1f)
			{
				// Modify each pair
				for(int i = 0; i < production.size(); i++)
				{
					Pair<SKU, Integer> original = production.get(i);
					production.set(i, new Pair<SKU, Integer>(original.getValue0(), (int)Math.round((float)original.getValue1() * operabilityLevel)));
					
					//System.out.println("original: " + original.getValue1().toString() + ", new: " + production.get(i).getValue1().toString() + ", oper: " + Float.toString(operabilityLevel));
				}
			}
		}	
		
		return production;
	}
	
	public void enableMalfunction(Malfunction malfunction)
	{
		this.malfunction = malfunction;
	}
}
