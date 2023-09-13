package com.logfarm.io;

import java.util.ArrayList;
import java.util.HashMap;

import org.neo4j.driver.exceptions.TransientException;
import org.neo4j.ogm.session.Session;

import com.logfarm.model.Date;
import com.logfarm.model.DoECoupler;
import com.logfarm.model.DoELink;
import com.logfarm.model.Facility;
import com.logfarm.model.GeneratedData;
import com.logfarm.model.Model;
import com.logfarm.model.SKU;
import com.logfarm.model.SKUPickerEntry;
import com.logfarm.model.SKUPickerWeight;
import com.logfarm.model.SimulationRunInfo;
import com.logfarm.model.TransportRelation;
import com.logfarm.model.VehicleClass;
import com.logfarm.utility.GeneratedDataHandler;

public class Neo4jHandler 
{
	public Model loadModel(Neo4jConnector connector)
	{
		// Setup a session	
		Session session = connector.getSessionFactory().openSession();
		
		ArrayList<Facility> facilities = (ArrayList<Facility>) session.loadAll(Facility.class, 5);		
		ArrayList<SKU> skus = (ArrayList<SKU>) session.loadAll(SKU.class);
		ArrayList<VehicleClass> vehicles = (ArrayList<VehicleClass>) session.loadAll(VehicleClass.class); 
        session.loadAll(SKUPickerWeight.class);
        session.loadAll(SKUPickerEntry.class);
        session.loadAll(TransportRelation.class);
        session.loadAll(DoELink.class);
        
        DoECoupler doeCoupler = ((ArrayList<DoECoupler>) session.loadAll(DoECoupler.class)).get(0);
		
		// Insert to model
		Model model = new Model();
		model.setSKUs(skus);
		model.setVehicleClassess(vehicles);
		model.setFacilites(facilities);
		model.setDoECoupler(doeCoupler);
		
		return model;
	}
	
	public void saveModel(Neo4jConnector connector)
	{
		
	}
	
	public static void PurgeResults(Neo4jConnector connector)
	{
		Session session = connector.getSessionFactory().openSession();
		System.out.println("Drop DB");
		session.query("DROP DATABASE results;", new HashMap<>());
		System.out.println("Create DB");
		session.query("CREATE DATABASE results", new HashMap<>());
		//session.purgeDatabase();
	}
	
	public static void SaveGeneratedData(Neo4jConnector connector, SimulationRunInfo simulationRunInfo, GeneratedDataHandler generatedDataHandler)
	{
		Session session = connector.getSessionFactory().openSession();
		SaveWithRetries(session, simulationRunInfo, 1, 1000);
		ArrayList<GeneratedData> generatedData = generatedDataHandler.getAllGeneratedData();
		SaveWithRetries(session, generatedData, 1, 1000);
	}
	
	private static boolean SaveWithRetries(Session session, Object o, int depth, int retries)
	{
		boolean success = false;
		int tries = 0;
		while(!success && tries < retries)
		{
			try
			{
				session.save(o, depth);
				success = true;
			}
			catch (TransientException e)
			{
				// Retry a little later (500ms)
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			tries++;
		}
			
		if(!success) System.out.println("THREAD FAILED TO WRITE");
		return success;
	}
	
	public static void PrepareExperimentResults(Neo4jConnector connector, ArrayList<Facility> facilities, ArrayList<SKU> skus, ArrayList<Date> dates)
	{
		Session session = connector.getSessionFactory().openSession();
		// Save nodes
		session.save(facilities, 1);
		session.save(skus, 1);
		session.save(dates);
	}
}
