package com.logfarm.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.javatuples.Pair;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label="component_transformation")
public class ComponentTransformation  extends Component implements Serializable
{
	@Relationship(type="utilizes", direction=Relationship.OUTGOING)
	private ArrayList<TransformationProcess> processes = new ArrayList<TransformationProcess>();
	
	@Relationship(type="may_have_malfunction", direction=Relationship.OUTGOING)
	private Malfunction malfunction = null;
	
	public ComponentTransformation()
	{

	}
	
	public void addTransformProcess(TransformationProcess newProcess)
	{
		processes.add(newProcess);
	}
	
	public ArrayList<TransformationProcess> getTransformationProcesses()
	{
		return this.processes;
	}
	
	public void initialize()
	{
		if(malfunction != null) malfunction.initialize();
	}
	
	public ArrayList<Pair<SKU, Integer>> getSKUChanges(float dth, float rn)
	{
		// The change might be positive (production) or negative (consumption)
		ArrayList<Pair<SKU, Integer>> skuChange = new ArrayList<Pair<SKU, Integer>>();
		
		for(TransformationProcess process : processes) skuChange.addAll(process.getSKUChange(dth));
		
		// See to malfunction
		if(malfunction != null)
		{
			float operabilityLevel = malfunction.getRemainingOperability(rn);
			if(operabilityLevel == 0f) return null;
			else if(operabilityLevel < 1f)
			{
				// Modify each pair
				for(int i = 0; i < skuChange.size(); i++)
				{
					Pair<SKU, Integer> original = skuChange.get(i);
					skuChange.set(i, new Pair<SKU, Integer>(original.getValue0(), (int)Math.round((float)original.getValue1() * operabilityLevel)));
					
					//System.out.println("original: " + original.getValue1().toString() + ", new: " + production.get(i).getValue1().toString() + ", oper: " + Float.toString(operabilityLevel));
				}
			}
		}	
		
		return skuChange;
	}
	
	public void enableMalfunction(Malfunction malfunction)
	{
		this.malfunction = malfunction;
	}
}
