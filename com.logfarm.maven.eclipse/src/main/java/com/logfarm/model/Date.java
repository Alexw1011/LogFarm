package com.logfarm.model;

import java.io.Serializable;
import java.time.LocalDate;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity(label="date")
public class Date implements Serializable
{
	@Id @GeneratedValue
	private Long id;
	
	@Property(name = "date")
	private LocalDate date;
	
	public Date()
	{
		
	}
	
	public Date(LocalDate date)
	{
		this.date = date;
	}
}
