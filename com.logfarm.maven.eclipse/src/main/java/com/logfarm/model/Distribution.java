package com.logfarm.model;

import java.io.Serializable;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Transient;

@NodeEntity(label="distribution")
public class Distribution implements Serializable, IDoETarget
{
	@Id @GeneratedValue
	private Long id;
	
	@Property(name="distribution_type")
	protected String distributionType;
	
	@Property(name="distribution_param1")
	protected String distributionParam1;
	
	@Property(name="distribution_param2")
	protected String distributionParam2;
	
	@Transient
	AbstractRealDistribution distribution;
	
	@Transient
	public enum DistributionTypes {NONE, NORMAL, GAMMA, EXPONENTIAL};
	
	
	public Distribution()
	{
		
	}
	
	public Distribution(AbstractRealDistribution distribution)
	{
		this.distribution = distribution;
		
		String type = "None";
		double param1 = 0;
		double param2 = 0;
		if(distribution instanceof NormalDistribution)
		{
			type = "Normal";
			NormalDistribution casted = (NormalDistribution) distribution;
			param1 = casted.getMean();
			param2 = casted.getStandardDeviation();
			
		}
		else if(distribution instanceof GammaDistribution)
		{
			type = "Gamma";
			GammaDistribution casted = (GammaDistribution) distribution;
			param1 = casted.getShape();
			param2 = casted.getScale();
		}	
		else if(distribution instanceof ExponentialDistribution)
		{
			type = "Exponential";
			ExponentialDistribution casted = (ExponentialDistribution) distribution;
			param1 = casted.getMean();
			param2 = 0;
		}
		
		this.distributionType = type;
		this.distributionParam1 = String.valueOf(param1);
		this.distributionParam2 = String.valueOf(param2);
	}
	
	public Distribution(String type, float param1, float param2)
	{
		this.distributionType = type;
		this.distributionParam1 = String.valueOf(param1);
		this.distributionParam2 = String.valueOf(param2);
	}
	
	public void initialize()
	{
		double param1 = Double.parseDouble(this.distributionParam1);
		double param2 = Double.parseDouble(this.distributionParam2);
		// param2 cannot be 0, use 1 instead to not let it crash
		if(param2 == 0)
		{
			param2 = 1;
			// Also, give a warning
			System.out.println("STANDARD DEVIATION OF 0 USED, CHANGED TO 1 TO PREVENT CRASHING");
		}
		
		if(distributionType.equals("Normal")) this.distribution = new NormalDistribution(param1, param2);
		else if(distributionType.equals("Gamma")) this.distribution = new GammaDistribution(param1, param2);
		else if(distributionType.equals("Exponential")) this.distribution = new ExponentialDistribution(param1);
	}
	
	public int sampleRoundedIntegerValue()
	{
		double continuousValue = distribution.sample();
		int discreteValue = (int) Math.round(continuousValue);
		return discreteValue;
	}
	
	public double sample()
	{
		return distribution.sample();
	}

	@Override
	public void setProperty(String name, float value) 
	{
		switch(name)
		{
			case "distribution_param1":
				this.distributionParam1 = String.valueOf(value);
				break;
			case "distribution_param2":
				this.distributionParam2 = String.valueOf(value);
				break;
			default:
				System.out.println("Property not found");				
		}		
	}
}
