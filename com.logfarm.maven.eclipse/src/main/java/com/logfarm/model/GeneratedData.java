package com.logfarm.model;

import java.io.Serializable;
import java.util.HashMap;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label="generated_data")
public abstract class GeneratedData implements Serializable 
{
	@Id @GeneratedValue
	protected Long id;
	
	@Relationship(type="generated_in", direction=Relationship.OUTGOING)
	protected SimulationRunInfo simulationRunInfo;
	
	public abstract void substituteReferences(HashMap<Facility, Facility> facilityDuplicates, HashMap<SKU, SKU> skuDuplicates);
	
	public void associateSimulationRunInfo(SimulationRunInfo simulationRunInfo)
	{
		this.simulationRunInfo = simulationRunInfo;
	}
}
