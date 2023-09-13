package com.logfarm.engine;

import java.util.ArrayList;
import java.util.Collections;

import com.logfarm.engine.VRPModel.Node;

// Based on an implementation of Florian Arnold, Faculty of Applied Economics - University of Antwerp., Operations Research Group ANT/OR.
// Supplementary material of the paper "A critical analysis of the improved Clarke and Wright savings algorithm" by Arnold, Sörensen and Palhazi Cuervo.

public class VRPSolver 
{	
	public static class VRPSolverResults
	{
		public double savings;
		public ArrayList<Route> routes;
		
		public VRPSolverResults(double savings, ArrayList<Route> routes)
		{
			this.savings = savings;
			this.routes = routes;
		}
	}
	
	public static class Route 
	{
		public ArrayList<Node> nodes;
		public int length; //number of customers on the route
		public int load;
		public double cost;
		
		public Route()
		{
			 nodes = new ArrayList<Node>();
			 this.cost = 0;
			 this.load = 0;
			 this.length = 0;
		}
		
		public Route copy()
		{
			Route r 	= new Route();
			r.cost 		= this.cost;
			r.load 		= this.load;
			r.length 	= this.length;
			r.nodes 	= new ArrayList<Node>();
			for (int i=0; i<this.nodes.size(); i++)
				r.nodes.add(this.nodes.get(i));
			return r;
		}
	}
	
	public static class Saving implements Comparable<Saving>
	{
		public double saving;
		public int from;
		public int to;
			
		public Saving(double v, int f, int t)
		{
			saving = v;
			from = f;
			to = t;
		}

		public Saving copy()
		{
			Saving s = new Saving(saving,from,to);
			return s;
		}
		
		public int compareTo(Saving o) 
		{
			if(o.saving<this.saving)
				return -1;
			else if(o.saving == this.saving)
				return 0;
			else
				return 1;
		}
	}
	
	private static ArrayList<Saving> copySavingList(ArrayList<Saving> original)
	{
		ArrayList<Saving> copy = new ArrayList<Saving>();
		for (int i=0;i<original.size();i++)
			copy.add(original.get(i).copy() );
		return copy;
	}
	
	private static ArrayList<Route> copyRouteList(ArrayList<Route> original)
	{
		ArrayList<Route> copy = new ArrayList<Route>();
		for (int i=0;i<original.size();i++)
			copy.add(original.get(i).copy() );
		return copy;
	}
	
	public static VRPSolverResults SolveWithICW(VRPModel model, int maxIterations)
	{
		return ICW(model, maxIterations);
	}
	
	private static VRPSolverResults ICW(VRPModel model, int maxIterations)
	{
		int numberOfCustomers = model.numberOfCustomers;
		int numberOfVehicles = model.numberOfVehicles;
		Node[] nodes = model.nodes;
		// Prepare routes
		ArrayList<Route> routes = new ArrayList<Route>();
		
		//compute and order the savings for all pairs of customers
		ArrayList<Saving> bestSavingList = new ArrayList<Saving>();
		ArrayList<Integer> assignedCustomers = new ArrayList<Integer>();
		double saving;
		
		for (int i=0; i<numberOfCustomers; i++)
		{
			assignedCustomers.add(i);
			for (int j=i+1; j<numberOfCustomers; j++)
			{
				saving = nodes[i].distances[numberOfCustomers] + nodes[j].distances[numberOfCustomers] - nodes[i].distances[j];
				if (saving<0.01)
				saving = 0.01;
				Saving s = new Saving( saving, i, j );
				bestSavingList.add(s);
			}
		}
		Collections.sort(bestSavingList);

		//initialization
		ArrayList<Saving> previousSavingList = null;
		int countNotImproved = 0;
		
		//---------------------------------------------
		//compute the solution with the original Clark and Wright algorithm
		//---------------------------------------------		
		double bestCosts = clarkWrightParallel(model, routes, copySavingList(bestSavingList));
		double previousCosts       = bestCosts;
		ArrayList<Route> bestRoutes = copyRouteList(routes);
		
		if (routes.size() > numberOfVehicles )
			{previousCosts = 999999; bestCosts = 999999;}

		//---------------------------------------------
		//In each iteration, shake the savings list and either accept or ignore it
		//---------------------------------------------
		for (int it=0; it<maxIterations; it++ )
		{
			previousSavingList = copySavingList( bestSavingList );
				
			//---------------------------------------------
			//shake the bestSavingList with the randomized tournament selection
			//---------------------------------------------
			ArrayList<Saving> shakenSavingsList = new ArrayList<Saving>();
			while (!previousSavingList.isEmpty())
			{
				//select a random tournament size
				int T 	= (int)(Math.random()*7.0) + 3;
				if (T > previousSavingList.size())
					T = previousSavingList.size();
				//select a random winner from the tournament
				int winner = -1;
				int sum = 0;
				for (int i=0; i<T; i++)
					sum += (int) previousSavingList.get(i).saving;
				double	ran = Math.random() * (double)sum;
				int cdr = 0;
				for(int i=0; i<T; i++)
				{
					cdr += previousSavingList.get(i).saving;
					if ((double)cdr >= ran) 	
					{
						winner = i;
						break;
					}
				}
				if (winner == -1)
					System.out.println("Error: no winner selected");
				shakenSavingsList.add( previousSavingList.get(winner).copy() );
				previousSavingList.remove(winner);
			}
			
			double newCosts = clarkWrightParallel(model, routes, copySavingList(shakenSavingsList));
					
			//Do we accept the new savings list?
			if ( newCosts  < previousCosts)
			{
				//is the solution valid?
				if ( routes.size() <= numberOfVehicles )
				{
					previousCosts = newCosts;
				}
				else
					previousCosts = 999999;
				bestSavingList   =  copySavingList( shakenSavingsList );
				countNotImproved = 0;
			}
			else
				countNotImproved++;
			
			//abort the search after 1000 iterations without improvements
			if (countNotImproved>1000)
				break;
			
			if (newCosts < bestCosts && numberOfVehicles >= routes.size())
			{
				bestCosts = newCosts;
				bestRoutes = copyRouteList(routes);
			}
				
		}//for iteration
		
		//return bestCosts;
		return new VRPSolverResults(bestCosts, bestRoutes);
	}
	
	
	//---------------------------------------------
	//The parallel Clark and Wright algorithm
	//---------------------------------------------
	private static double clarkWrightParallel(VRPModel model, ArrayList<Route> routes, ArrayList<Saving> savings )
	{
		int numberOfCustomers = model.numberOfCustomers;
		int vehicleCapacity = model.vehicleCapacity;
		Node[] nodes = model.nodes;
		
		int depot 	= numberOfCustomers;
		routes.clear();
		
		//---------------------------------------------
		//add edges between pairs of nodes with the highest savings
		//---------------------------------------------		
		int countVisits		= 0;
		boolean[] visited	= new boolean[numberOfCustomers];
		ArrayList<Integer> extensionPoints = new ArrayList<Integer>();
		ArrayList<Integer> interiorPoints  = new ArrayList<Integer>();
		int countRoutes = 0;
			
		while ( savings.size() > 0)
		{	
				//---------------------------------------------
				// get the edge with the next highest saving
				//---------------------------------------------
				int n1 = savings.get(0).from;
				int n2 = savings.get(0).to;
				
				//---------------------------------------------
				// case 1: one of the nodes is an interior Point
				//---------------------------------------------
				if ( interiorPoints.contains(n1) || interiorPoints.contains(n2) )
				{
					//do nothing
				}
				//---------------------------------------------
				// case 2: both nodes are neither extension nor interior points
				//---------------------------------------------
				else if ( !extensionPoints.contains(n1) && !extensionPoints.contains(n2) )
				{
					if ( nodes[ n1 ].demand + nodes[ n2 ].demand <= vehicleCapacity )
					{
						//start a new route with these two nodes
						Route r = new Route();
						r.nodes.add(nodes[depot]);
						r.nodes.add(nodes[ n1 ]);
						r.nodes.add(nodes[ n2 ]);
						r.nodes.add(nodes[depot]);
						r.length 			= 2;
						r.load				= nodes[ n1 ].demand + nodes[ n2 ].demand;
						r.cost				= nodes[ n1 ].distances[ n2 ] + nodes[ n1 ].distances[ depot ] + nodes[ n2 ].distances[ depot ];
						countVisits = countVisits + 2;
						extensionPoints.add( n1 );
						extensionPoints.add( n2 );
						visited[n1]=true;
						visited[n2]=true;
						nodes[ n1 ].routeIndex = countRoutes;
						nodes[ n2 ].routeIndex = countRoutes;
						
						routes.add( r.copy() );
						countRoutes++;
					}
				}

				//---------------------------------------------
				// case 3a: the first node is an extension point 
				//---------------------------------------------
				else if ( extensionPoints.contains(n1) && !extensionPoints.contains(n2))
				{
					
					Route r = routes.get( nodes[n1].routeIndex );
					double addCost = nodes[ n1 ].distances[ n2 ] + nodes[ n2 ].distances[ depot ] - nodes[ n1 ].distances[ depot ];
					if ( r.load + nodes[ n2 ].demand <= vehicleCapacity )
					{
						//extend the route
						if ( r.nodes.get(1).index == n1 )
							r.nodes.add(1, nodes[ n2 ]);
						else
							r.nodes.add(r.length+1, nodes[ n2 ]);
						r.length++;
						r.load	+= nodes[ n2 ].demand;
						r.cost += addCost;
						nodes[ n2 ].routeIndex = nodes[n1].routeIndex;
						countVisits++;
						extensionPoints.remove(Integer.valueOf(n1));
						extensionPoints.add( n2 );
						interiorPoints.add( n1 );
						visited[n2]=true;

					}
				}
				//---------------------------------------------
				// case 3b: the second node is an extension point 
				//---------------------------------------------
				else if ( !extensionPoints.contains(n1) && extensionPoints.contains(n2) )
				{
					Route r = routes.get( nodes[n2].routeIndex );
					double addCost = nodes[ n2 ].distances[ n1 ] + nodes[ n1 ].distances[ depot ] - nodes[ n2 ].distances[ depot ];
					if ( r.load + nodes[ n1 ].demand <= vehicleCapacity )
					{
						//extend the route
						if ( r.nodes.get(1).index == n2 )
							r.nodes.add(1, nodes[ n1 ]);
						else
							r.nodes.add(r.length+1, nodes[ n1 ]);
						r.length++;
						r.load	+= nodes[ n1 ].demand;
						r.cost += addCost;
						nodes[ n1 ].routeIndex = nodes[n2].routeIndex;
						countVisits++;
						extensionPoints.remove(Integer.valueOf(n2));
						extensionPoints.add( n1 );
						interiorPoints.add( n2 );
						visited[n1]=true;
					}
				}
				//---------------------------------------------
				// case 4: both nodes are extension points; merge the two routes
				//---------------------------------------------
				else if ( extensionPoints.contains(n1) && extensionPoints.contains(n2) )
				{
					if (nodes[n1].routeIndex != nodes[n2].routeIndex )
					{
						Route r1 = routes.get( nodes[n1].routeIndex );
						Route r2 = routes.get( nodes[n2].routeIndex );
						double addCost = r1.cost + r2.cost + nodes[ n2 ].distances[ n1 ] - nodes[ n1 ].distances[ depot ] - nodes[ n2 ].distances[ depot ];
						if ( r1.load + r2.load <= vehicleCapacity )
						{
							//merge the routes
							//bring into format depot -...- n1 - n2 -...- depot
							if ( r1.nodes.get(1).index == n1 )
								Collections.reverse(r1.nodes);
							if ( r2.nodes.get(1).index != n2 )
								Collections.reverse(r2.nodes);
							//remove two depots
							r1.nodes.remove( r1.nodes.size()-1 );
							r2.nodes.remove( 0 );
							//attach route 2 to route 1
							r1.nodes.addAll(r1.nodes.size(), r2.nodes);
							r1.length 	+= r2.length;
							r1.load		+= r2.load;
							Route temp	= r1.copy();
							
							//update the routes
							//adapt the routeIndices of all other routes
							int index1 = nodes[n1].routeIndex;
							int index2 = nodes[n2].routeIndex;
							for (int i=index1+1; i<routes.size(); i++)
								for (int j=1; j<routes.get(i).length+1; j++)
									routes.get(i).nodes.get(j).routeIndex--;
							for (int i=index2+1; i<routes.size(); i++)
								for (int j=1; j<routes.get(i).length+1; j++)
									routes.get(i).nodes.get(j).routeIndex--;
							//remove the two previous routes (in the right order)
							if ( index1 < index2 )
							{
								routes.remove(index2);
								routes.remove(index1);
							}
							else
							{
								routes.remove(index1);
								routes.remove(index2);
							}	
							//insert the merged route
							for (int i=1; i<temp.length+1; i++)
								temp.nodes.get(i).routeIndex = countRoutes-2;
							temp.cost = addCost;
							routes.add(temp.copy());
							countRoutes--;
	
							extensionPoints.remove(Integer.valueOf(n1));
							extensionPoints.remove(Integer.valueOf(n2));
							interiorPoints.add( n1 );
							interiorPoints.add( n2 );
						}
					}
				}
				savings.remove(0);
		}
		
		//catch the case, that there is only one customer left that does not fit in any route
		boolean noViableSolution = false;
		if ( countVisits < numberOfCustomers)
		{
			for (int i=0; i<numberOfCustomers; i++)
			{
				if (!visited[i] && nodes[ i ].demand <= vehicleCapacity)
				{
					//create an own route for this customer
					Route r = new Route();
					r.nodes.add(nodes[depot]);
					r.nodes.add(nodes[ i ]);
					r.nodes.add(nodes[depot]);
					r.length 			= 1;
					r.load				= nodes[ i ].demand;
					r.cost = nodes[depot].distances[i] * 2;
					nodes[ i ].routeIndex = countRoutes;
					countVisits++;
					routes.add(r.copy());
				}
				else noViableSolution = true;
			}	
		}
		
		//compute the costs of the solution
		double costs = 0;
		if(noViableSolution) return 999999;
			
		for (int r=0; r<routes.size(); r++)
		{
			for (int i=1; i<routes.get(r).length+2; i++)
				 costs += routes.get(r).nodes.get(i-1).distances[routes.get(r).nodes.get(i).index];
		}
		
		return costs;
		
	}
}
