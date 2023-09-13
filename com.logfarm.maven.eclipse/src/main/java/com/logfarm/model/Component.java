package com.logfarm.model;

import java.io.Serializable;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label="component")
public class Component implements Serializable
{
	@Id @GeneratedValue
	protected Long id;
}
