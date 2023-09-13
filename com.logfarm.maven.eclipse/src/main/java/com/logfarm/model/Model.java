package com.logfarm.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import com.logfarm.utility.GeneratedDataHandler;

public class Model implements Serializable
{
	// Environment
	ArrayList<SKU> skus;
	ArrayList<VehicleClass> vehicleClasses;
	ArrayList<Facility> facilities;
	
	DoECoupler doeCoupler;
	
	public void setDoECoupler(DoECoupler doeCoupler)
	{
		this.doeCoupler = doeCoupler;
	}
	
	public ArrayList<SKU> getSKUs() 
	{
		return skus;
	}
	
	public void setSKUs(ArrayList<SKU> skus) 
	{
		this.skus = skus;
	}
	
	public void setVehicleClassess(ArrayList<VehicleClass> vehicleClasses) { this.vehicleClasses = vehicleClasses; };
	public ArrayList<VehicleClass> getVehicleClassess() { return this.vehicleClasses; };
	
	public DoECoupler getDoECoupler() { return doeCoupler; };
	
	public void setFacilites(ArrayList<Facility> facilities)
	{
		this.facilities = facilities;
	}
	
	public ArrayList<Facility> getFacilities()
	{
		return this.facilities;
	}
	
	public void updateAgents(LocalDate t, Random rng, GeneratedDataHandler gdh, boolean generateData)
	{
		Iterator<Facility> it = this.facilities.iterator();
        while(it.hasNext())
        {
            it.next().update(t, rng, gdh, generateData);
        }
        // Second iteration
        it = this.facilities.iterator();
        while(it.hasNext())
        {
            it.next().updateDistribution(t, rng, gdh, generateData);
        }
	}
	
	public void initialize(LocalDate startDate)
	{
		Iterator<Facility> it = this.facilities.iterator();
		while(it.hasNext())
        {
            it.next().awake();
        }
		
		it = this.facilities.iterator();
        while(it.hasNext())
        {
            it.next().initialize(startDate);
        }
	}
	
	public static Model makeWorkingCopy(Model model, float[] factorValues)
	{
		Model workingCopy = null;
		try {
			workingCopy = deepCopy(model);
		} catch (Exception e) { e.printStackTrace(); }
		
		if(workingCopy != null)
		{
			// Set factor values
			DoECoupler doeCoupler = workingCopy.getDoECoupler();
			ArrayList<DoELink> doeLinks = doeCoupler.getLinks();
			
			for(DoELink link : doeLinks) link.applyFactorValue(factorValues);
		}
		
		return workingCopy;
	}
	
	public static Model deepCopy( Object o ) throws Exception
	{
	  ByteArrayOutputStream baos = new ByteArrayOutputStream();
	  new ObjectOutputStream( baos ).writeObject( o );

	  ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() );

	  return (Model) new ObjectInputStream(bais).readObject();
	}
}
