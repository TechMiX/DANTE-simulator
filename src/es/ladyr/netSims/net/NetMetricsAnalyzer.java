/*
 * Copyright 2008 Luis Rodero Merino.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Luis Rodero Merino if you need additional information or
 * have any questions. Contact information:
 * Email: lrodero AT gsyc.es
 * Webpage: http://gsyc.es/~lrodero
 * Phone: +34 91 488 8107; Fax: +34 91 +34 91 664 7494
 * Postal address: Desp. 121, Departamental II,
 *                 Universidad Rey Juan Carlos
 *                 C/Tulipán s/n, 28933, Móstoles, Spain 
 *       
 */

package es.ladyr.netSims.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Stack;

import es.ladyr.util.commandLine.CommandLineParser;
import es.ladyr.util.dataStructs.SortedArrayList;

public class NetMetricsAnalyzer {
    
    private static NetMetricsAnalyzer _instance = null;

    private final static String HELP_ARG_NAME = CommandLineParser.DEFAULT_ARGUMENT_NAMES_HEADER + "help";
    private final static String CLUSTER_ARG_NAME = CommandLineParser.DEFAULT_ARGUMENT_NAMES_HEADER + "clus";
    private final static String DIAMETER_ARG_NAME = CommandLineParser.DEFAULT_ARGUMENT_NAMES_HEADER + "diam";
    private final static String PRINTDISTMATRIX_ARG_NAME = CommandLineParser.DEFAULT_ARGUMENT_NAMES_HEADER + "printDistMat";
    private final static String OUTPUTFILE_ARG_NAME = CommandLineParser.DEFAULT_ARGUMENT_NAMES_HEADER + "outFile";
    
    private static final String[] commandLineArgsNames = {HELP_ARG_NAME, CLUSTER_ARG_NAME, DIAMETER_ARG_NAME, PRINTDISTMATRIX_ARG_NAME, OUTPUTFILE_ARG_NAME};
    
    private static final String USAGE = "java " + NetMetricsAnalyzer.class.getName()
                                        + " {" + HELP_ARG_NAME + "}"
                                        + " {" + CLUSTER_ARG_NAME + "}"
                                        + " {" + DIAMETER_ARG_NAME + "}"
                                        + " {" + PRINTDISTMATRIX_ARG_NAME + "}"
                                        + " {" + OUTPUTFILE_ARG_NAME + " <fileName>}"
                                        + " { <netFile1> <netFile2> ... }"
                                        + CLUSTER_ARG_NAME + ", " + DIAMETER_ARG_NAME + " and " + PRINTDISTMATRIX_ARG_NAME + " expect pajek format files.";
    
    private static void printUsage(){
        System.out.println("Usage: " + USAGE + "\n");
    }   
    
    public static void main(String args[]){  
        
        CommandLineParser commandLineParser = new CommandLineParser();
        
        String unknownArg = commandLineParser.lookForUnknownArguments(args, commandLineArgsNames);
        if(unknownArg != null){
            System.out.println("Unknown argument " + unknownArg);
            printUsage();
            return;
        }
        
        boolean printHelp = commandLineParser.isArgPresent(args, HELP_ARG_NAME);
        if(printHelp){
            printUsage();
            return;
        }
        
        boolean computeClusterizationDegree = commandLineParser.isArgPresent(args, CLUSTER_ARG_NAME);
        boolean computeDiameter = commandLineParser.isArgPresent(args, DIAMETER_ARG_NAME);
        boolean printDistMatrix = commandLineParser.isArgPresent(args, PRINTDISTMATRIX_ARG_NAME);

        if(!computeClusterizationDegree && !computeDiameter && !printDistMatrix){
            System.out.println("Nothing to do.");
            printUsage();
            return;
        }
        
        String outputFileName = commandLineParser.getArgVal(args, OUTPUTFILE_ARG_NAME);
        
        if((outputFileName == null) && (commandLineParser.isArgPresent(args, OUTPUTFILE_ARG_NAME))){
            System.out.println("Could not find name for output file");
            printUsage();
            return;            
        }            
        
        PrintWriter outputFileWriter = null;
        if(outputFileName != null) {
            try {
                outputFileWriter = new PrintWriter(new File(outputFileName));
            } catch (FileNotFoundException exception) {
                throw new Error("This FileNotFoundException should not happen", exception);
            }
            outputFileWriter.print("# NET");
            if(computeClusterizationDegree)
                outputFileWriter.print("\tClusterDeg");
            if(computeDiameter)
                outputFileWriter.print("\tDiameter");
            outputFileWriter.println();
            outputFileWriter.flush();
        }
        
        args = commandLineParser.removeArg(args, HELP_ARG_NAME);
        args = commandLineParser.removeArg(args, CLUSTER_ARG_NAME);
        args = commandLineParser.removeArg(args, DIAMETER_ARG_NAME);
        args = commandLineParser.removeArg(args, PRINTDISTMATRIX_ARG_NAME);
        args = commandLineParser.removeArgAndVal(args, OUTPUTFILE_ARG_NAME);
                        
        if(args.length == 0)
            args = new String[]{null};
        
        for(int fileIndex = 0; fileIndex < args.length; fileIndex++){

            Node[] network = null;
            
            if(args[fileIndex] == null) {
                System.out.print("Building test network... ");
                network = buildTestNetwork();      
                System.out.println("done");
                if(outputFileWriter != null) outputFileWriter.print("Test Net");          
            } else {
                File pajekFile = new File(args[fileIndex]);
                System.out.print("Reading pajek file " + pajekFile.getAbsolutePath() + "... ");
                network = getInstance().readNetworkFromPajekFile(pajekFile);
                System.out.println("done");
                if(outputFileWriter != null) outputFileWriter.print("NetFile-" + pajekFile.getAbsolutePath()); 
            }
            
            if(!computeClusterizationDegree && !computeDiameter && !printDistMatrix)
                System.out.println("Ummm... nothing else to do");
            
            if(computeClusterizationDegree){
                System.out.print("Computing Clustering Coefficient... ");
                double clusteringCoefficient = getInstance().networkClusteringCoefficient(network);
                System.out.println("done: " + clusteringCoefficient + "\n");
                if(outputFileWriter != null) outputFileWriter.print("\t"+clusteringCoefficient); 
            }
            
            if(computeDiameter || printDistMatrix){
                System.out.print("Computing distances matrix... ");
                int[][] distancesMatrix = getInstance().distancesMatrix3(network, false);
                System.out.println(" done");
                if(printDistMatrix)
                    getInstance().printMatrix(distancesMatrix);
                if(computeDiameter) {
                    System.out.print("Computing diameter... ");
                    int diameter = getInstance().maxDistance(distancesMatrix);
                    System.out.println("done, diameter is " + diameter);    
                    if(outputFileWriter != null) outputFileWriter.print("\t"+diameter);                 
                }
            }
            if(outputFileWriter != null) {outputFileWriter.println();outputFileWriter.flush();}           
        }
        
        if(outputFileWriter != null) outputFileWriter.close();
        
    }
    
    public static NetMetricsAnalyzer getInstance(){
        if(_instance == null)
            _instance = new NetMetricsAnalyzer();
        return _instance;
    }
    
    private static Node[] buildTestNetwork(){
        
        Node[] network = new Node[15];
        
        // Creating nodes
        for(int nodeIndex = 0; nodeIndex < network.length; nodeIndex++)
            network[nodeIndex] = new Node(nodeIndex);
        
        // Connecting nodes
        
        // 0 <-> 1
        connect(network[0], network[1]);
        
        // 0 <-> 3
        connect(network[0], network[3]);
        
        // 1 <-> 2
        connect(network[1], network[2]);
        
        // 2 <-> 3
        connect(network[2], network[3]);
        
        // 2 <-> 4
        connect(network[2], network[4]);
        
        // 2 <-> 6
        connect(network[2], network[6]);
        
        // 3 <-> 4
        connect(network[3], network[4]);
        
        // 5 <-> 8
        connect(network[5], network[8]);
        
        // 6 <-> 5
        connect(network[6], network[5]);
        
        // 6 <-> 7
        connect(network[6], network[7]);
        
        // 6 <-> 8
        connect(network[6], network[8]);
        
        // 7 <-> 9
        connect(network[7], network[9]);
        
        // 8 <-> 9
        connect(network[8], network[9]);
        
        // 9 <-> 10
        connect(network[9], network[10]);
        
        // 10 <-> 11
        connect(network[10], network[11]);
        
        // 11 <-> 12
        connect(network[11], network[12]);
        
        // 12 <-> 13
        connect(network[12], network[13]);
        
        // 12 <-> 14
        connect(network[12], network[14]);
        
        // 13 <-> 14
        connect(network[13], network[14]);
        
        return network;
    }
    
    private static void connect(Node node1, Node node2){        
        node1.neighbors.add(node2); node2.neighbors.add(node1);        
    }
    
    public double nodeClusteringCoefficient(Node node){
        
        if(node == null)
            throw new IllegalArgumentException("Node can not be null");
        
        if(node.degree() <= 1)
            return 0.0;
        
        long possibleConnsBetNeighs = node.degree() * (node.degree() - 1);
        
        SortedArrayList neighbors = node.neighbors();
        
        int connsBetNeighs = 0;
        
        for(int neighIndex = 0; neighIndex < neighbors.size(); neighIndex++){
            Node neighbor = (Node)neighbors.get(neighIndex);
            SortedArrayList neighborsOfNeighbor = neighbor.neighbors();
            for(int restOfNeighIndex = neighIndex+1; restOfNeighIndex < neighbors.size(); restOfNeighIndex++)
                if(neighborsOfNeighbor.contains(neighbors.get(restOfNeighIndex)))
                    connsBetNeighs++;
        }
        
        double clusteringCoefficient = ((double)2*connsBetNeighs)/possibleConnsBetNeighs;
        //System.out.println("Clustering coefficient of node " + node + ": " + clusteringCoefficient);
        
        return clusteringCoefficient;
    }
    
    public double networkClusteringCoefficient(Node[] network){
        
        if(network == null)
            throw new IllegalArgumentException("Network can not be null");
        
        if(network.length == 0)
            throw new IllegalArgumentException("Network can not be empty");
        
        double sumOfNodesClustCoeff = 0.0;
        for(int nodeIndex = 0; nodeIndex < network.length; nodeIndex++)
            sumOfNodesClustCoeff += nodeClusteringCoefficient(network[nodeIndex]);
        
        if(sumOfNodesClustCoeff == 0)
            throw new Error("0 value computed as sum of all nodes clustering coefficient");

        double clusteringCoefficient = sumOfNodesClustCoeff/network.length;
        
        //System.out.println("Clustering coefficient of network: " + clusteringCoefficient);
        
        return clusteringCoefficient;
    }
    
    public int maxDistance(int[][] distancesMatrix){
        int maxDistance = 0;
        
        if(distancesMatrix == null)
            throw new IllegalArgumentException("Distances matrix can not be null");
        
        if(distancesMatrix.length == 0)
            throw new IllegalArgumentException("Distances matrix can not be empty");
        
        for(int row = 0; row < distancesMatrix.length; row++)
            if(distancesMatrix.length != distancesMatrix[row].length)
                throw new IllegalArgumentException("Distances matrix must be a square matrix");
        
        for(int nodeIndex = 0; nodeIndex < distancesMatrix.length; nodeIndex++) {
            for(int restOfNodesIndex = nodeIndex+1; restOfNodesIndex < distancesMatrix.length; restOfNodesIndex++){
                int distance = distancesMatrix[nodeIndex][restOfNodesIndex];
                if(distance != distancesMatrix[restOfNodesIndex][nodeIndex])
                    throw new IllegalArgumentException("Distances matrix must be simmetric");
                if(distancesMatrix[nodeIndex][restOfNodesIndex] > maxDistance)
                    maxDistance = distance;
            }
        }
        
        return maxDistance;
    }
    
    public int[][] distancesMatrix3(Node[] network, boolean specialCheck){
        
        if(network == null)
            throw new IllegalArgumentException("Network can not be null");
        
        if(network.length == 0)
            throw new IllegalArgumentException("Network can not be empty");
        
        // Cloning network, nodes now will store their index in the matrix (which is quite useful)
        NodeWithIndex[] networkWithIndexes = NodeWithIndex.cloneNetwork(network);
        
        SortedArrayList allNodesSorted = new SortedArrayList(networkWithIndexes, false);
        
        int[][] distances = new int[network.length][network.length];
        
        // Initiating distances matrix
        for(int rowIndex = 0; rowIndex < network.length; rowIndex++)
            for(int columnIndex = 0; columnIndex < network.length; columnIndex++)
                distances[rowIndex][columnIndex] = -1;
        
        // Main diagonal
        for(int nodeIndex = 0; nodeIndex < network.length; nodeIndex++)
            distances[nodeIndex][nodeIndex] = 0;
        
        for(int originNodeIndex = 0; originNodeIndex < network.length; originNodeIndex++){
            
            NodeWithIndex originNode = networkWithIndexes[originNodeIndex];

            //System.out.println("At origin node " + originNode);
            
            int originNodeIndexInMatrix = originNode.index();

            SortedArrayList nodesPendingToVisit = new SortedArrayList(allNodesSorted);
            nodesPendingToVisit.remove(originNode);
            
            Stack floodedNodesPerHop = new Stack();
            SortedArrayList floodedAtHop0 = new SortedArrayList();
            floodedAtHop0.add(originNode);
            floodedNodesPerHop.push(floodedAtHop0);
            while(!((SortedArrayList)floodedNodesPerHop.peek()).isEmpty()){
                //System.out.println("\tHop " + floodedNodesPerHop.size());
                nextFloodingHop(floodedNodesPerHop, distances, originNode, originNodeIndexInMatrix);
            }
            
            // Checking all distances are set
            for(int column = 0; column < network.length; column++)
                if(distances[column][originNodeIndexInMatrix] == -1)
                    throw new Error("Just flooded from node " + originNode + ", but not all distances from this node are set");
            
            
            // Updating distances matrix with flooded nodes at each hop
            if(specialCheck)
                for(int hop = 1; (hop < floodedNodesPerHop.size() && hop <= 1); hop++){
                    SortedArrayList nodesFloodedAtHop = (SortedArrayList)floodedNodesPerHop.get(hop);
                        
                    for(int nodesAtHopIndex = 0; nodesAtHopIndex < nodesFloodedAtHop.size(); nodesAtHopIndex++){
                        NodeWithIndex node = (NodeWithIndex)nodesFloodedAtHop.get(nodesAtHopIndex);
                        int nodeIndexInDistancesMatrix = node.index();
                        for(int nodesAtHopIndex_b = nodesAtHopIndex+1; nodesAtHopIndex_b < nodesFloodedAtHop.size(); nodesAtHopIndex_b++){
                            NodeWithIndex nodeAtHop_b = (NodeWithIndex)nodesFloodedAtHop.get(nodesAtHopIndex_b);
                            int nodeAtPostHop_bIndexInDistancesMatrix = nodeAtHop_b.index();
                            
                            int distance = distances[nodeIndexInDistancesMatrix][nodeAtPostHop_bIndexInDistancesMatrix];
                            if((distance > 2*hop) || (distance == -1)){
                                distances[nodeIndexInDistancesMatrix][nodeAtPostHop_bIndexInDistancesMatrix] = 2*hop;
                                distances[nodeAtPostHop_bIndexInDistancesMatrix][nodeIndexInDistancesMatrix] = 2*hop;
                            }                            
                        }
                    }
                }
            
        }
        
        return distances;
    }
    
    private void nextFloodingHop(Stack floodedNodesPerHop, int[][] distances, NodeWithIndex originNode, int originNodeIndexInMatrix){
        
        if(floodedNodesPerHop == null)
            throw new IllegalArgumentException("Stack of visited nodes can not be null");
        
        if(originNode == null)
            throw new IllegalArgumentException("Origin node can not be null");
        
        int hop = floodedNodesPerHop.size();
        
        // Performing hop, reading list of visited nodes at previous hop
        SortedArrayList nodesFloodedAtPrevHop = (SortedArrayList)floodedNodesPerHop.peek();
        // For each neighbor of each node visited at previous hop, we check if it has not been already visited, or the distance to it from the origin node
        // known so far is greater than the number of hops. If so, we add them as a node to visit.
        SortedArrayList floodedNodes = new SortedArrayList();
        for(int nodeFloodedPrevHopIndex = 0; nodeFloodedPrevHopIndex < nodesFloodedAtPrevHop.size(); nodeFloodedPrevHopIndex++){
            Node prevFloodedNode = (Node)nodesFloodedAtPrevHop.get(nodeFloodedPrevHopIndex);
            SortedArrayList neighbors = prevFloodedNode.neighbors();
            for(int neighborIndex = 0; neighborIndex < neighbors.size(); neighborIndex++) {
                NodeWithIndex neighbor = (NodeWithIndex)neighbors.get(neighborIndex);
                
                int neighborIndexInMatrix = neighbor.index();
                
                if(distances[originNodeIndexInMatrix][neighborIndexInMatrix] != distances[neighborIndexInMatrix][originNodeIndexInMatrix])
                    throw new Error("Inconsistent distances matrix state, value at [" + originNodeIndexInMatrix +"]["+ neighborIndexInMatrix+"] = " + 
                                    distances[originNodeIndexInMatrix][neighborIndexInMatrix] + ", but value at [" + neighborIndexInMatrix +"]["+ originNodeIndexInMatrix+"] = " + 
                                        distances[neighborIndexInMatrix][originNodeIndexInMatrix]);
                
                int distanceToOriginNode = distances[originNodeIndexInMatrix][neighborIndexInMatrix];
                
                if((distanceToOriginNode < 0) || (distanceToOriginNode >= hop)) {
                    // Found a new or shorter way to node, must explore further
                    distances[originNodeIndexInMatrix][neighborIndexInMatrix] = hop;
                    distances[neighborIndexInMatrix][originNodeIndexInMatrix] = hop;
                    floodedNodes.add(neighbor);
                }
            }
        }        
        
        floodedNodesPerHop.push(floodedNodes);
    }
    
    public int[][] distancesMatrix2(Node[] network){
        
        if(network == null)
            throw new IllegalArgumentException("Network can not be null");
        
        if(network.length == 0)
            throw new IllegalArgumentException("Network can not be empty");
        
        // Cloning network, nodes now will store their index on the matrix (which is quite useful)
        NodeWithIndex[] networkWithIndexes = NodeWithIndex.cloneNetwork(network);
        
        int[][] distances = new int[network.length][network.length];
        
        // Initiating distances matrix
        for(int rowIndex = 0; rowIndex < network.length; rowIndex++)
            for(int columnIndex = 0; columnIndex < network.length; columnIndex++)
                distances[rowIndex][columnIndex] = -1;
        
        // Main diagonal
        for(int nodeIndex = 0; nodeIndex < network.length; nodeIndex++)
            distances[nodeIndex][nodeIndex] = 0;

        Stack exploredNodes = new Stack();
        
        for(int nodeIndex = 0; nodeIndex < networkWithIndexes.length; nodeIndex++) {
            explore(distances, exploredNodes, networkWithIndexes[nodeIndex]);
            if(!exploredNodes.isEmpty())
                throw new Error("Stack of explored nodes should be empty");
        }
        
        return distances;
    }
    
    private void explore(int[][] distances, Stack exploredNodes, NodeWithIndex presentNode){
        
        exploredNodes.push(presentNode);
        
        /*System.out.print(" Explored nodes so far:");
        for(int exploredNodesIndex = 0 ; exploredNodesIndex < exploredNodes.size(); exploredNodesIndex++)
            System.out.print(" " + ((Integer)nodesIndexes.get(exploredNodes.get(exploredNodesIndex))).intValue());
        System.out.println();*/
        
        SortedArrayList neighbors = presentNode.neighbors();
        
        Stack neighborsToExplore = new Stack();
        
        for(int neighIndex = 0; neighIndex < neighbors.size(); neighIndex++){
            
            NodeWithIndex neighbor = (NodeWithIndex)neighbors.get(neighIndex);
            int neighborIndexInDistancesMatrix = neighbor.index();
            
            //System.out.println("\tNeighbor "+ (Integer)nodesIndexes.get(neighbor));
            
            if(exploredNodes.contains(neighbor)) // Neighbor already explored
                continue;
            
            for(int visitedIndex = 0; visitedIndex < exploredNodes.size(); visitedIndex++){
                
                NodeWithIndex visitedNode = (NodeWithIndex)exploredNodes.get(visitedIndex);
                int visitedNodeIndexInDistancesMatrix = visitedNode.index();
                
                int hopsToNeighbor = exploredNodes.size() - visitedIndex;
                
                //System.out.println("\tChecking with visited node " + (Integer)nodesIndexes.get(visitedNode) + ", hops to neighbor " + hopsToNeighbor);
                
                if(distances[neighborIndexInDistancesMatrix][visitedNodeIndexInDistancesMatrix] == -1) {
                    // This node has not been explored yet
                    //System.out.println("\tDistance is -1, distance not set yet, setting to "  + hopsToNeighbor);
                    distances[neighborIndexInDistancesMatrix][visitedNodeIndexInDistancesMatrix] = hopsToNeighbor;    
                    distances[visitedNodeIndexInDistancesMatrix][neighborIndexInDistancesMatrix] = hopsToNeighbor;    
                    if(!neighborsToExplore.contains(neighbor))
                        neighborsToExplore.push(neighbor);
                } else if(distances[neighborIndexInDistancesMatrix][visitedNodeIndexInDistancesMatrix] > hopsToNeighbor){
                    //System.out.println("\tDistance is " + distances[neighborIndexInDistancesMatrix][visitedNodeIndexInDistancesMatrix] + 
                    //                   ", distance shorther found, updating to "  + hopsToNeighbor);
                    // We have found a shorter way to this node
                    distances[neighborIndexInDistancesMatrix][visitedNodeIndexInDistancesMatrix] = hopsToNeighbor;    
                    distances[visitedNodeIndexInDistancesMatrix][neighborIndexInDistancesMatrix] = hopsToNeighbor;    
                    //System.out.println("\tUpdating previous explored nodes");
                    for(int prevVisitIndex = visitedIndex-1; prevVisitIndex >= 0; prevVisitIndex--){
                        NodeWithIndex prevVisitedNode = (NodeWithIndex)exploredNodes.get(prevVisitIndex);  
                        //System.out.println("\t\tUpdating previous explored node " + (Integer)nodesIndexes.get(prevVisitedNode) +
                        //                   " to " + (exploredNodes.size() - prevVisitIndex));
                        int prevVisitNodeIndexInDistancesMatrix = prevVisitedNode.index();
                        distances[prevVisitNodeIndexInDistancesMatrix][neighborIndexInDistancesMatrix] = exploredNodes.size() - prevVisitIndex;    
                        distances[neighborIndexInDistancesMatrix][prevVisitNodeIndexInDistancesMatrix] = exploredNodes.size() - prevVisitIndex;                                
                    }                      
                } else if(distances[neighborIndexInDistancesMatrix][visitedNodeIndexInDistancesMatrix] == hopsToNeighbor) {
                    //System.out.println("\tDistance is " + distances[neighborIndexInDistancesMatrix][visitedNodeIndexInDistancesMatrix] + 
                    //                   ", we must explore neighbor");
                    if(!neighborsToExplore.contains(neighbor))
                        neighborsToExplore.push(neighbor);
                } else {
                    // There is a shorter way, already explored, to neighbor
                    //System.out.println("\tDistance is " + distances[neighborIndexInDistancesMatrix][visitedNodeIndexInDistancesMatrix] + 
                    //", that is shorther, we ignore neighbor");
                    
                }
                
            }
            
        }
        
        /*System.out.print(" Neighbors to explore:");
        for(int neighsToExpIndex = 0 ; neighsToExpIndex < neighborsToExplore.size(); neighsToExpIndex++)
            System.out.print(" " + ((Integer)nodesIndexes.get(neighborsToExplore.get(neighsToExpIndex))).intValue());
        System.out.println();*/
        
        while(!neighborsToExplore.isEmpty()){
            NodeWithIndex neighborToExplore = (NodeWithIndex)neighborsToExplore.pop();
            explore(distances, exploredNodes, neighborToExplore);
        }
        
        if(!exploredNodes.pop().equals(presentNode))
            throw new Error("Explored nodes stack in non-consistent state");
        
    }
    
    public int[][] distancesMatrix(Node[] network){
        
        if(network == null)
            throw new IllegalArgumentException("Network can not be null");
        
        if(network.length == 0)
            throw new IllegalArgumentException("Network can not be empty");
        
        int[][] distances = new int[network.length][network.length];
        
        // Initiating distances matrix
        for(int rowIndex = 0; rowIndex < network.length; rowIndex++)
            for(int columnIndex = 0; columnIndex < network.length; columnIndex++)
                distances[rowIndex][columnIndex] = -1;
        
        // Main diagonal
        for(int nodeIndex = 0; nodeIndex < network.length; nodeIndex++)
            distances[nodeIndex][nodeIndex] = 0;
        
        for(int nodeIndex = 0; nodeIndex < network.length; nodeIndex++) {
            for(int restOfNodesIndex = nodeIndex+1; restOfNodesIndex < network.length; restOfNodesIndex++){
                distances[nodeIndex][restOfNodesIndex] = distanceBetweenNodes(network[nodeIndex], network[restOfNodesIndex]);
                distances[restOfNodesIndex][nodeIndex] = distances[nodeIndex][restOfNodesIndex];
            }
        }
        
        return distances;
    }
    
    public int distanceBetweenNodes(Node node1, Node node2){
        
        if((node1 == null) ||(node2 == null))
            throw new IllegalArgumentException("Node can not be null");
        
        if(node1.equals(node2))
            return 0;
        
        SortedArrayList toBeChecked = new SortedArrayList();
        SortedArrayList checked = new SortedArrayList();
        checked.add(node1);        
        toBeChecked.addAll(node1.neighbors());
        
        int distance = 0;
        
        while(true){
            distance++;
            if(toBeChecked.contains(node2))
                 break;
            SortedArrayList justChecked = new SortedArrayList(toBeChecked);
            checked.addAll(toBeChecked);
            toBeChecked.clear();
            for(int nodeIndex = 0; nodeIndex < justChecked.size(); nodeIndex++){
                Node nodeChecked = (Node)justChecked.get(nodeIndex);
                SortedArrayList neighbors = nodeChecked.neighbors();
                for(int neighIndex = 0; neighIndex < neighbors.size(); neighIndex++){
                    Node neighbor = (Node)neighbors.get(neighIndex);
                    if(!checked.contains(neighbor))
                        toBeChecked.add(neighbor);
                }
            }
            if(toBeChecked.isEmpty())
                throw new Error("Nodes are not connected!");
        }
        
        return distance;
    }
    
    public Node[] readNetworkFromPajekFile(File pajekFile){
    
        if(pajekFile == null)
            throw new IllegalArgumentException("File can not be null");

        String verticesInitMark = "*Vertices";
        String arcsInitMark = "*Arcs";
        String edgesInitMark = "*Edges";
        
        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new FileReader(pajekFile));
        } catch (FileNotFoundException exception) {
            throw new Error("FileNotFoundException caught when opening file " + pajekFile.getAbsolutePath(), exception);
        }

        // Looking for '* Vertices' line
        String line = null;
        do {
            line = getLine(fileReader);
            if(line == null)
                throw new Error("EOF reached before '" + verticesInitMark + "' was found in file " + pajekFile.getAbsolutePath());
        } while(!line.startsWith(verticesInitMark));
        
        String[] lineParts = line.split("[ ]+");
        if(lineParts.length != 2)
            throw new Error("Error parsing Vertices line " + line + ", can not split in two parts (separated by white spaces)");
        int expectedNumberOfNodes = -1;
        try {
            expectedNumberOfNodes = Integer.parseInt(lineParts[1]);
        } catch(NumberFormatException exception){
            throw new Error("Could not parse " + lineParts[1] + " to a valid integer at Vertices line");            
        }
        
        
        // Reading vertices and building node objects, until '* Arcs' line is found
        SortedArrayList nodes = new SortedArrayList();
        do {
            
            line = getLine(fileReader);

            if(line == null)
                throw new Error("EOF reached before '" + arcsInitMark + "' was found in file");
            
            if(line.startsWith(arcsInitMark))
                break;
            
            lineParts = line.split("[ ]+");
            if(lineParts.length != 2)
                throw new Error("Error parsing node line " + line + ", can not split in two parts (separated by white spaces)");
            
            int nodeID = -1;
            try {
                nodeID = Integer.parseInt(lineParts[0]);                
            } catch(NumberFormatException exception){
                throw new Error("Could not parse " + lineParts[0] + " to a valid integer");
            }
            if(nodeID < 0)
                throw new Error("Node ID can not be negative!, found " + nodeID + " value");
            
            Node node = new Node(nodeID);
            if(nodes.contains(node))
                throw new Error("Node with ID " + nodeID + " already created!, ID " + nodeID + " found more than once in file");
            
            nodes.add(node);
            
        } while(true);

        if(nodes.size() != expectedNumberOfNodes)
            throw new Error("Expected " + expectedNumberOfNodes + " nodes, but only " + nodes.size() + " were found");
        
        // Reading arcs, until '* Edges' line is found
        HashMap nodesArcs = new HashMap();
        do {            

            line = getLine(fileReader);
            
            if(line == null)
                throw new Error("EOF reached before '" + edgesInitMark + "' was found in file");
            
            if(line.startsWith(edgesInitMark))
                break;
            
            lineParts = line.split("[ ]+");

            if(lineParts.length != 3)
                throw new Error("Error parsing arcs line " + line + ", can not split in three parts (separated by white spaces)");
            
            int originNodeID = -1;
            int destinationNodeID = -1; 
            try {
                originNodeID = Integer.parseInt(lineParts[0]);                
            } catch(NumberFormatException exception){
                throw new Error("Could not parse " + lineParts[0] + " to a valid integer");
            }
            try {
                destinationNodeID = Integer.parseInt(lineParts[1]);                
            } catch(NumberFormatException exception){
                throw new Error("Could not parse " + lineParts[1] + " to a valid integer");
            }
            int originNodeIndex = nodes.indexOf(new Node(originNodeID));
            int destinationNodeIndex = nodes.indexOf(new Node(destinationNodeID));
            if(originNodeIndex < 0)
                throw new Error("Found node " + originNodeID + " as arc origin, but not defined in Vertices");
            if(destinationNodeIndex < 0)
                throw new Error("Found node " + destinationNodeID + " as arc destination, but not defined in Vertices");
            
            Node originNode = (Node)nodes.get(originNodeIndex);
            Node destinationNode = (Node)nodes.get(destinationNodeIndex);
            SortedArrayList originNeighbors = (SortedArrayList)nodesArcs.get(originNode);
            if(originNeighbors == null)
                originNeighbors = new SortedArrayList();
            SortedArrayList destinationNeighbors = (SortedArrayList)nodesArcs.get(destinationNode);
            if(destinationNeighbors == null)
                destinationNeighbors = new SortedArrayList();
            
            originNeighbors.add(destinationNode);
            destinationNeighbors.add(originNode);
            
            nodesArcs.put(originNode, originNeighbors);
            nodesArcs.put(destinationNode, destinationNeighbors);

        } while(true);
        
        
        // Can close file now
        try {
            fileReader.close();
        } catch (IOException exception) {
            throw new Error("IOException caught when closing file " + pajekFile.getAbsolutePath(), exception);
        }
        
        // Setting neighbors
        Node[] allNodes = (Node[])nodes.toArray(new Node[0]);
        for(int nodeIndex = 0; nodeIndex < allNodes.length; nodeIndex++){
            Node node = allNodes[nodeIndex];
            SortedArrayList neighbors = (SortedArrayList)nodesArcs.get(node);
            if(neighbors != null)
                node.neighbors().addAll(neighbors);
        }
        
        
        // Checking consistency
        for(int nodeIndex = 0; nodeIndex < allNodes.length; nodeIndex++){
            Node node = allNodes[nodeIndex];
            SortedArrayList neighbors = node.neighbors();
            if(neighbors == null)
                continue;
            for(int neighborIndex = 0; neighborIndex < neighbors.size(); neighborIndex++){
                Node neighbor = (Node)neighbors.get(neighborIndex);
                if(!neighbor.neighbors().contains(node))
                    throw new Error("Error!, neighbor "+ neighbor.id() + " does not have node " + node.id() + " in its own list of neighbors");
            }
        }
        
        return allNodes;
    }

    private String getLine(BufferedReader fileReader){
        
        if(fileReader == null)
            throw new Error("File reader can not be null");
    
        String line = null;
        do{
            try {
                line = fileReader.readLine();
            } catch (IOException exception) {
                throw new Error("IOException caught when reading line from BufferedReader", exception);
            }            
            if(line == null)
                return null;           
            line = line.trim();
        } while((line.startsWith("#")) || (line == "")); // Avoiding empty and commented lines in file
    
        return line;
    }
    
    private void printMatrix(int[][] distancesMatrix){
        
        if(distancesMatrix == null)
            throw new IllegalArgumentException("Distances matrix can not be null");
        
        if(distancesMatrix.length == 0)
            throw new IllegalArgumentException("Distances matrix can not be empty");
        
        for(int row = 0; row < distancesMatrix.length; row++)
            if(distancesMatrix.length != distancesMatrix[row].length)
                throw new IllegalArgumentException("Distances matrix must be a square matrix");
        
        char upperLeftCorner = '\u23A1';
        char upperRightCorner = '\u23A4';
        char leftSide = '\u23A2';
        char rightSide = '\u23A5';
        char lowerLeftCorner = '\u23A3';
        char lowerRightCorner = '\u23A6';
        
        int maxDistanceStringLength = 0;
        for(int row = 0; row < distancesMatrix.length; row++)
            for(int column = 0; column < distancesMatrix.length; column++) {
                int distanceStringLength = (distancesMatrix[row][column]+"").length();
                if(distanceStringLength > maxDistanceStringLength)
                    maxDistanceStringLength = distanceStringLength;
            }
            
        int maxIndexStringLength = (distancesMatrix.length +"").length();
        
        int maxStringLength = (maxIndexStringLength >= maxDistanceStringLength) ? maxIndexStringLength : maxDistanceStringLength;
        
        System.out.print(addSpacesUntilMinLength("", maxIndexStringLength) + "  ");
        for(int column = 0; column < distancesMatrix.length; column++)
            System.out.print(" " + addSpacesUntilMinLength(column+"", maxStringLength));
        
        System.out.println();
        
        for(int row = 0; row < distancesMatrix.length; row++) {
            if(row == 0)
                System.out.print(addSpacesUntilMinLength(row+"", maxIndexStringLength) + " " + upperLeftCorner);
            else if(row == distancesMatrix.length - 1)
                System.out.print(addSpacesUntilMinLength(row+"", maxIndexStringLength) + " " + lowerLeftCorner);
            else
                System.out.print(addSpacesUntilMinLength(row+"", maxIndexStringLength) + " " + leftSide);
                
            for(int column = 0; column < distancesMatrix.length; column++)
                System.out.print(" " + addSpacesUntilMinLength(distancesMatrix[row][column]+"", maxStringLength));

            if(row == 0)
                System.out.println(upperRightCorner);
            else if(row == distancesMatrix.length - 1)
                System.out.println(lowerRightCorner);
            else
                System.out.println(rightSide);            
                        
        }        
        
    }
    
    private String addSpacesUntilMinLength(String string, int minLength){
        while(string.length() < minLength)
            string = " " + string;
        return string;
    }

}

class NodeWithIndex extends Node {
    
    public static NodeWithIndex[] cloneNetwork(Node[] network){
        
        if(network == null)
            throw new IllegalArgumentException("Network to clone can not be null");
        
        NodeWithIndex[] newNetwork = new NodeWithIndex[network.length];
        
        HashMap nodesTable = new HashMap();
        for(int nodeIndex = 0; nodeIndex < network.length; nodeIndex++) {
            newNetwork[nodeIndex] = new NodeWithIndex(network[nodeIndex].id(), nodeIndex);
            nodesTable.put(network[nodeIndex], newNetwork[nodeIndex]);
        }
        
        for(int nodeIndex = 0; nodeIndex < network.length; nodeIndex++) {            
            SortedArrayList neighbors = network[nodeIndex].neighbors();
            
            for(int neighborIndex = 0; neighborIndex < neighbors.size(); neighborIndex++){
                NodeWithIndex neighborWithIndex = (NodeWithIndex)nodesTable.get(neighbors.get(neighborIndex));
                newNetwork[nodeIndex].neighbors().add(neighborWithIndex);
            }            
        }
        
        return newNetwork;
    }
    
    private int index = -1;
    
    public NodeWithIndex(int id, int index){
        super(id);
        this.index = index;
    }
    
    public int index(){
        return index;
    }
}
