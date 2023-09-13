# LogFarm
A Graph-based Simulator for Logistics Networks coded in Java

## General
Experimental build of the Software LogFarm. The software has only limited functionality and serves as a proof of concept. There is no detailed documentation for LogFarm available.

For more information on how the software works, please see the following thesis:
https://www.itpl.mb.tu-dortmund.de/publikationen/files/MA_2022_Wuttke.pdf

or contact Alexander Wuttke (alexander2.wuttke@tu-dortmund.de) from TU Dortmund University.

## Requirements
 - An installed and running Neo4j database (https://neo4j.com/)
 - Adjust the username and password in the neo4jconnector.java file according to the running neo4j database (the name of the database doesn't matter)

## Remarks

 - Models cannot be loaded into the editor. However, models can be created by code like in the methods loadScenario1() and loadScenario2() in the GUIController.java file
