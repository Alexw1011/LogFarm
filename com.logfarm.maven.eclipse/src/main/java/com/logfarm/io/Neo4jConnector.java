package com.logfarm.io;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

public class Neo4jConnector 
{
	public static final Neo4jConnector singleton = new Neo4jConnector();
	
	private SessionFactory sessionFactory;
	
	public boolean establishConnection(String databaseName)
	{
		// Setup the driver
		Configuration config = new Configuration.Builder().uri("bolt://localhost:7687").credentials("neo4j", "logfarmer").database(databaseName).verifyConnection(true).build();
		sessionFactory = new SessionFactory(config, "com.logfarm.model");
		return true;
	}
	
	public SessionFactory getSessionFactory()
	{
		return sessionFactory;
	}
	
	public Session getSession()
	{
		return sessionFactory.openSession();
	}
}
