package com.logfarm.model;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity(label="simulation_run_info")
public class SimulationRunInfo 
{
	@Id @GeneratedValue
	private Long id;
	
	@Property(name = "run_id")
	private Long runId;
	
	@Property(name = "design_point")
	private Long designPoint;
	
	@Property(name = "repitition")
	private Long repitition;
	
	public SimulationRunInfo()
	{
		
	}
	
	public SimulationRunInfo(Long runId, Long designPoint, Long repitition)
	{
		this.runId = runId;
		this.designPoint = designPoint;
		this.repitition = repitition;
	}
	
	public Long getRunId() { return runId; };
}
