/*
 * Copyright 2007 Luis Rodero Merino.  All Rights Reserved.
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
 *                 C/TulipÃ¡n s/n, 28933, MÃ³stoles, Spain 
 *       
 */

package es.ladyr.dante.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import es.ladyr.dante.externalEvents.EventForwarder;
import es.ladyr.dante.monitoring.searchesStats.NodeSearchesStats;
import es.ladyr.dante.protocol.AcceptMessage;
import es.ladyr.dante.protocol.ConnectMessage;
import es.ladyr.dante.protocol.ReplicationMessage;
import es.ladyr.dante.protocol.ProtocolMessage;
import es.ladyr.dante.protocol.DisconnectMessage;
import es.ladyr.dante.protocol.LookForNodeMessage;
import es.ladyr.dante.protocol.LookForResourceMessage;
import es.ladyr.dante.protocol.NodesFoundMessage;
import es.ladyr.dante.protocol.RejectMessage;
import es.ladyr.dante.protocol.ResourceFoundMessage;
import es.ladyr.dante.protocol.ResourceNotFoundMessage;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.netSims.net.Node;
import es.ladyr.simulator.Event;
import es.ladyr.simulator.Simulator;
import es.ladyr.util.dataStructs.SortedArrayList;
import es.ladyr.util.math.ExponentialDistribution;
import es.ladyr.util.math.RandomGenerator;


public class DanteNode extends Node { 
    
    protected static int totalPendingConnectsPetsInSystem = 0;
    protected static int totalPendingDisconnectsPetsInSystem = 0;
    protected static int totalPendingReconnectionsInSystem = 0;
    
    protected static SortedArrayList allNodesInSystem = new SortedArrayList();    
    public static SortedArrayList allNodesInSystem(){
        return allNodesInSystem;
    }
    protected static SortedArrayList allActiveNodesInSystem = new SortedArrayList();
    public static SortedArrayList allActiveNodesInSystem(){
        return allActiveNodesInSystem;
    }
    protected static SortedArrayList attackedNodes = new SortedArrayList();
    public static SortedArrayList attackedNodes(){
        return attackedNodes;
    }
    
    // To compute how much each search lasts
    //protected static double logBase2D = Math.log(DNTConfiguration.getResourcesByNode())/Math.log(2);
    
    protected ConnectedNodesSet connections = new ConnectedNodesSet(this);
    
    protected MessagesQueuesSet messagesQueues = new MessagesQueuesSet(this);
    
    protected NodeSearchesStats searchesStats = new NodeSearchesStats(this);
    
    protected Reconnecter reconnecter = new Reconnecter(this);
    
    protected PendingLocalSearchesQueue pendingLocalSearchsQueue = new PendingLocalSearchesQueue(this);
    
    protected PetsCounter petsCounter = null;
    
    protected ArrayList connectPendingPetitions = new ArrayList();
    protected ArrayList disconnectPendingPetitions = new ArrayList();
    protected int reconnectPendingPetitions = 0;
    
    protected HashMap nodesToDisconnectFrom = new HashMap();
    
    protected Resources resources = null;
    
    protected NodeState nodeStats = new NodeState(this);
    
    protected double capacity = 0; // Processing capacity (set in constructor)
    protected double bandwidth = 0; // Node bandwidth (set in constructor)
    protected long timeToSendSomePacket = 0; // Time it takes to send some packet (set in constructor)
    
    protected int nextNodeSearchID = 1;
    
    protected long busyUntil = 0;
    
    protected long lastTaskFinishedEventTime = -1; // To check events are thrown at proper times
    protected long lastMessageEventTime = -1;
    
    protected boolean nodeIsActive = true;
    protected int nodeGeneration = 0;
    
    protected int looksIgnoredBecauseNodeNotActive = 0;
    protected int reconnectsIgnoredBecauseNodeNotActive = 0;
    protected int eventsIgnoredBecauseNodeNotActive = 0;
    
    public static int totalPendingConnectsPetsInSystem(){
        return totalPendingConnectsPetsInSystem;
    }
    
    public static int totalPendingDisconnectsPetsInSystem(){
        return totalPendingDisconnectsPetsInSystem;
    }
    
    public static int totalPendingReconnectionsInSystem(){
        return totalPendingReconnectionsInSystem;
    }
    
    public static void resetGlobalCounters(){
        totalPendingConnectsPetsInSystem = 0;
        totalPendingDisconnectsPetsInSystem = 0;
        totalPendingReconnectionsInSystem = 0;
        Node.resetMessagesCounter();
        NodeSearchesStats.resetGlobalCounters();

        MessagesQueuesSet.resetCounters();
        PendingLocalSearchesQueue.resetPendLocSearches();
    }

    public DanteNode(int nodeID, double capacity, double bandwidth, boolean nodeIsActive, Resources resources) {
        super(nodeID);
        this.capacity = capacity;
        this.bandwidth = bandwidth;
        this.nodeIsActive = nodeIsActive;
        this.resources = resources;

        allNodesInSystem.add(this);
        if(nodeIsActive){
            nodeGeneration = 1;
            allActiveNodesInSystem.add(this);
            reconnecter.nodeActivated();            
        }

        if(bandwidth > 0)
            // 0 bandwidth means infinite bandwidth (timeToSendSomePacket would remain 0)
            timeToSendSomePacket = (long)Math.ceil((double)DanteConf.getPresentSimConf().maxPacketsSize() / bandwidth);     

        petsCounter = new PetsCounter();
                
        if(DanteConf.getPresentSimConf().generateEvents())
            EventForwarder.getInstance().nodeAdded(this);
    }
    
    public void deactiveNode(){
        
        if(!nodeIsActive)
            throw new Error("Trying to disactive a non active node");
        
        nodeIsActive = false;
        
        allActiveNodesInSystem.remove(this);

        totalPendingReconnectionsInSystem -= reconnectPendingPetitions;
        reconnectPendingPetitions = 0;
        totalPendingConnectsPetsInSystem -= connectPendingPetitions.size();
        connectPendingPetitions.clear();
        totalPendingDisconnectsPetsInSystem -= disconnectPendingPetitions.size();
        disconnectPendingPetitions.clear();
        
        if(DanteConf.getPresentSimConf().generateEvents()){
            
            DanteNode[] outConns = (DanteNode[])connections.outgoingConnections().toArray(new DanteNode[0]);
            
            for(int connIndex = 0; connIndex < outConns.length; connIndex++)
                EventForwarder.getInstance().connectionDeleted(this, outConns[connIndex]);
            
            DanteNode[] inConns = (DanteNode[])connections.incomingConnections().toArray(new DanteNode[0]);
            for(int connIndex = 0; connIndex < inConns.length; connIndex++)
                EventForwarder.getInstance().connectionDeleted(inConns[connIndex], this);
            
            EventForwarder.getInstance().nodeDeleted(this);
        }
        
                
        // Clear messages queues
        messagesQueues.nodeWasDeactivated();
        // Clear searchs stats
        searchesStats.nodeWasDeactivated();
        // Petitions counter
        petsCounter.nodeWasDeactivated();
        // pendingLocalSearchsQueue ?
        pendingLocalSearchsQueue.nodeWasDeactivated();
        // reconnecter ?
        reconnecter.nodeDeactivated();
        // nodesToDisconnectFrom ?
        nodesToDisconnectFrom.clear();
        // neighbors?
        connections.nodeWasDeactivated();
        
    }
    
    public void reactivateNode(){
        
        if(nodeIsActive)
            throw new Error("Trying to reactive an already active node");
        
        if(allActiveNodesInSystem.contains(this))
            throw new Error("Node is registered as active");
        
        busyUntil = 0;
        
        nodeIsActive = true;
        
        nodeGeneration++;
        
        reconnecter.nodeActivated();
        
        if(DanteConf.getPresentSimConf().generateEvents())
            EventForwarder.getInstance().nodeAdded(this);
        
        // Choosing new nodes at random
        SortedArrayList initialNeighbors = new SortedArrayList();
        if(allActiveNodesInSystem.size() <= DanteConf.getPresentSimConf().outConns())
            initialNeighbors.addAll(allActiveNodesInSystem);
        else 
            while(initialNeighbors.size() < DanteConf.getPresentSimConf().outConns())
                initialNeighbors.add( allActiveNodesInSystem.get(RandomGenerator.randomIntValue(allActiveNodesInSystem.size())) );

        allActiveNodesInSystem.add(this);        
        
        DanteNode[] newConns = (DanteNode[])initialNeighbors.toArray(new DanteNode[0]);
        for(int connIndex = 0; connIndex < newConns.length; connIndex++)
            connectTo(newConns[connIndex]);
    }
    
    public void nodeUnderAttack(){
        
        if(!nodeIsActive)
            throw new Error("Only active nodes should be attacked");

        attackedNodes.add(this);
        deactiveNode();
    }
    
    public void recoverFromAttack(){
        
        if(!attackedNodes.contains(this))
            throw new Error("Trying to recover a non attacked node");
        
        attackedNodes.remove(this);        
        reactivateNode();        
    }
    
    public boolean nodeIsActive(){
        return nodeIsActive;
    }
    
    public int nodeGeneration(){
        return nodeGeneration;
    }
    
    public int resourcesInNode(){
        return resources.numberOfRes();
    }
    
    public NodeState getNodeStats(){
        return nodeStats;
    }
    
    // New search for resource started locally  
    public void lookForResource(int resource){

        // Is node active??
        if(!nodeIsActive){            
            looksIgnoredBecauseNodeNotActive++;            
            return;
        }
        
        // Assigning search ID and registering search
        int searchID = searchesStats.registerNewSearch(resource, Simulator.simulator().getSimulationTime());
        
        // Registering search also in petitions counter
        petsCounter.registerPet(Simulator.simulator().getSimulationTime());
        
        // Must enqueue petition?         
        if(Simulator.simulator().getSimulationTime() < busyUntil)
            pendingLocalSearchsQueue.addSearch(searchID, resource);
        else
            processLocalSearch(searchID, resource);
        
    }
    
    public void processLocalSearch(int searchID, int resource){
    	
        if(busyUntil > Simulator.simulator().getSimulationTime()){
            throw new Error(id() + ": In processLocalSearch(), busyUntil " + busyUntil + " is greater than simulationTime " + Simulator.simulator().getSimulationTime());
        }
        
        ResourceLookingUpResult resourceLookingUpResult = resourceIsKnown(resource, this);
        
        //System.out.println("Search results: ");
        //System.out.println("\tNode: " + (resourceLookingUpResult.getNodeResourceIs() == null ? "NOT FOUND" : resourceLookingUpResult.getNodeResourceIs().id()) );
        //System.out.println("\tResourcesChecked: " + resourceLookingUpResult.getResourcesChecked());
        
        // Computing the total time it took to search the resource
        long timeInSearch = resourceLookingUpResult.getTimeInSearch();
        
        // Registering processed petition for stats. Param is the time the petition was started, second the time it took to process it.
        petsCounter.petProcessed(Simulator.simulator().getSimulationTime(), timeInSearch, 0);
        
        if(resourceLookingUpResult.getNodeResourceIs() == null){            
            
            
            // Resource is unknown, can we send a query to some neighbor?
            DanteNode nodeToForwardTo = DanteConf.getPresentSimConf().fwdSearchChooser().nodeToForwardTo(neighbors, null);
            if(nodeToForwardTo == null){
                
                // Updating busyUntil, depending on the total resources checked
                busyUntil = Simulator.simulator().getSimulationTime() + timeInSearch;   
                
                // Programming TaskFinished event
                Simulator.simulator().registerEvent(new NodeTaskFinishedEvent(busyUntil, this));
                                
                searchesStats.searchFinished(searchID, busyUntil, 0, false);
                return;
            } 

            
            // Updating busyUntil, depending on the total resources checked
            busyUntil = Simulator.simulator().getSimulationTime() + Math.max(timeInSearch, timeToSendSomePacket);   
            
            // Programming TaskFinished event
            Simulator.simulator().registerEvent(new NodeTaskFinishedEvent(busyUntil, this));
            
            // Creating and sending new LOOK_FOR_RESOURCE message
            LookForResourceMessage lookForResourceMessage = new LookForResourceMessage(this,  // <- message sender
                                                                                       searchID, 
                                                                                       resource, 
                                                                                       DanteConf.getPresentSimConf().ttlEstimator().ttl(this), // instead of DNTConfiguration.getSearchsTTL()
                                                                                       this); // <- search origin

            sendMessageToNode(busyUntil, nodeToForwardTo, lookForResourceMessage); // -> inherited from node
            
        } else {
            
            // Updating busyUntil, depending on the total resources checked
            busyUntil = Simulator.simulator().getSimulationTime() + timeInSearch;   
            
            // Programming TaskFinished event
            Simulator.simulator().registerEvent(new NodeTaskFinishedEvent(busyUntil, this));
			
            searchesStats.searchFinished(searchID, busyUntil, 0, true);
            
        }     
        
    }
    
    // Check if resource is known
    protected ResourceLookingUpResult resourceIsKnown(int resource, DanteNode searchSender){

        // If searches processing time is computed by exponential
        // distribution, we need to known the number of all known resources.
        if(DanteConf.getPresentSimConf().procTimesByExpDis()){
            
            int totalKnownResources = 0;
            DanteNode nodeWhereResIs = null;
            
            totalKnownResources += resources.numberOfRes();
            if(resources.containsResource(resource))
                nodeWhereResIs = this;

            // Checking neighbors
            DanteNode[] allNeighbors = (DanteNode[])neighbors.toArray(new DanteNode[0]);
            for(int neighIndex = 0; neighIndex < allNeighbors.length; neighIndex++){
                totalKnownResources += allNeighbors[neighIndex].getResources().numberOfRes();
                if(allNeighbors[neighIndex].getResources().containsResource(resource))
                    nodeWhereResIs = allNeighbors[neighIndex];
            }
            
            long timeInSearch = (capacity > 0) ? ExponentialDistribution.nextLongForMean(totalKnownResources/capacity) : 0;
            timeInSearch = (timeInSearch > 0) ? timeInSearch : 1; 
            
            return new ResourceLookingUpResult(nodeWhereResIs, timeInSearch);
            
        }
        
        // Searches processing time is computed using the number of checked resources at each search
        int totalCheckedResources = 0;
        DanteNode nodeWhereResIs = null;
        
        // Checking resources
        if(resources.containsResource(resource)){
            totalCheckedResources = resources.numberOfRes() / 2;
            nodeWhereResIs = this;
        } else {
            totalCheckedResources = resources.numberOfRes();
        }

        // Checking extra resources
        if (DanteConf.getPresentSimConf().onlineReplication() && nodeWhereResIs == null){
        	
			if (resources.containsExtraResource(resource))
			{
				totalCheckedResources += resources.numberOfExtraRes() / 2;
				nodeWhereResIs = this;
			} else {
				totalCheckedResources += resources.numberOfExtraRes();
			}

        }

        // Checking neighbors resources
        DanteNode[] allNeighbors = (DanteNode[])neighbors.toArray(new DanteNode[0]);
        for(int neighIndex = 0; neighIndex < allNeighbors.length && nodeWhereResIs == null; neighIndex++){            
            if(allNeighbors[neighIndex].getResources().containsResource(resource)){
                totalCheckedResources += allNeighbors[neighIndex].getResources().numberOfRes() / 2;
                nodeWhereResIs = allNeighbors[neighIndex];

            } else {
                totalCheckedResources += allNeighbors[neighIndex].getResources().numberOfRes();                
            }
        }

		// Checking neighbors extra resources
        if (DanteConf.getPresentSimConf().onlineReplication() && nodeWhereResIs == null){

			for(int neighIndex = 0; neighIndex < allNeighbors.length && nodeWhereResIs == null; neighIndex++){            
				if(allNeighbors[neighIndex].getResources().containsExtraResource(resource)){
					totalCheckedResources += allNeighbors[neighIndex].getResources().numberOfExtraRes() / 2;
					nodeWhereResIs = allNeighbors[neighIndex];

				} else {
					totalCheckedResources += allNeighbors[neighIndex].getResources().numberOfExtraRes();                
				}
			}
        }
        
        long timeInSearch = (capacity > 0) ? (long)Math.ceil(((double)totalCheckedResources)/capacity) : 0;
        timeInSearch = (timeInSearch > 0) ? timeInSearch : 1; 
        
        return new ResourceLookingUpResult(nodeWhereResIs, timeInSearch);
        
    }
    
    public void connectTo(DanteNode node){
        
        if(!nodeIsActive)            
            return;
        
        if(this == node)            
            return;
        
        if(connectPendingPetitions.contains(node))
            return;
        
        // Must enqueue petition?          
        if(Simulator.simulator().getSimulationTime() < busyUntil){
            
            totalPendingConnectsPetsInSystem++;
            connectPendingPetitions.add(node);
            
        } else
            startConnectProcess(node);
        
    }
    
    public void disconnectFrom(DanteNode node){
        
        if(!nodeIsActive)            
            return;
        
        if(this == node)
            return;            
        
        if(disconnectPendingPetitions.contains(node))
            return;

        // Must enqueue petition?     
        if(Simulator.simulator().getSimulationTime() < busyUntil){            
            totalPendingDisconnectsPetsInSystem++;
            disconnectPendingPetitions.add(node);            
        } else {            
            // Connection process can be started now            
            startDisconnectProcess(node);
        }
        
    }
    
    // Method to start reconnection. Used when no global knowledge is available.
    public void reconnect(){
        
        if(!nodeIsActive){            
            reconnectsIgnoredBecauseNodeNotActive++;            
            return;
        }
        
        // Must 'enqueue' petition?        
        if(Simulator.simulator().getSimulationTime() < busyUntil){                   
            reconnectPendingPetitions++;
            totalPendingReconnectionsInSystem++;            
        } else {            
            // Connection process can be started now            
            startReconnectionProcess();
        }
        
    }

    // Method to start reconnection. Used when no global knowledge is available.
    protected void startReconnectionProcess(){
        
        if(!nodeIsActive){            
            reconnectsIgnoredBecauseNodeNotActive++;            
            return;
        }

        // Updating busy until
        busyUntil = Simulator.simulator().getSimulationTime() + timeToSendSomePacket;
        
        // Programming TaskFinished event
        Simulator.simulator().registerEvent(new NodeTaskFinishedEvent(busyUntil, this));
        
        // Must create and send a LOOK_FOR_NODES message
        LookForNodeMessage lookForNodeMessage = new LookForNodeMessage(this, nextNodeSearchID, DanteConf.getPresentSimConf().nodeSearchesTTL(), this);
        
        // Choosing a random neighbor
        DanteNode neighbor = DanteConf.getPresentSimConf().fwdChooser().nodeToForwardTo(neighbors, null);
        
        if(neighbor == null)
            // Then, choosing one at random
            neighbor = (DanteNode)allActiveNodesInSystem.get(RandomGenerator.randomIntValue(allActiveNodesInSystem.size()));
        
        sendMessageToNode(busyUntil, neighbor, lookForNodeMessage);
        
        nextNodeSearchID++;
    }
    
    // This function disconnects all present outgoing connections and connects to all nodes present
    // in the list 'nodes'. It is called at each reconnection.
    public void redoOutgoingConnections(List nodesToConnectTo){        
        
        if(!nodeIsActive){
            reconnectsIgnoredBecauseNodeNotActive++;            
            return;
        }
        
        if(nodesToConnectTo.isEmpty())
            return;        
        
        if(nodesToConnectTo.size() > DanteConf.getPresentSimConf().connsChangedAtRec())            
            throw new Error("The list of nodes to connect to has more nodes than the number of conns to change???");        
        
        // Removing already connected nodes from list of nodes to connect to
        nodesToConnectTo.removeAll(connections.outgoingConnections());
        
        // Removing list of pending connects
        nodesToConnectTo.removeAll(connectPendingPetitions);
        
        if(nodesToConnectTo.isEmpty())
            return;

        nodesToDisconnectFrom = DanteConf.getPresentSimConf().disChooser().nodesToDisconnectFrom(nodesToConnectTo,
                                                                                                  connections.outgoingConnections());
        
        DanteConf.getPresentSimConf().newConnsFilter().filterNewConnections(nodesToDisconnectFrom, nodesToConnectTo);

        // Finally, calling to connect commands
        DanteNode[] newConns = (DanteNode[])nodesToConnectTo.toArray(new DanteNode[0]);
        for(int connIndex = 0; connIndex < newConns.length; connIndex++)            
            connectTo(newConns[connIndex]);
        
    }
    
    protected void startConnectProcess(DanteNode node){
        
        if(busyUntil > Simulator.simulator().getSimulationTime())
            throw new Error(id() + ": In startConnectProcess(), busyUntil " + busyUntil + " is greater than simulationTime " + Simulator.simulator().getSimulationTime());
        
        connections.connectToNode(node, Simulator.simulator().getSimulationTime(), timeToSendSomePacket);
        
        busyUntil = Simulator.simulator().getSimulationTime() + timeToSendSomePacket;
        
        // Programming TaskFinished event
        Simulator.simulator().registerEvent(new NodeTaskFinishedEvent(busyUntil, this));
    }   
    
    protected void startDisconnectProcess(DanteNode node){
        
        if(busyUntil > Simulator.simulator().getSimulationTime())
            throw new Error(id() + ": In startDisconnectProcess(), busyUntil " + busyUntil + " is greater than simulationTime " + Simulator.simulator().getSimulationTime());
        
        connections.disconnectFromNode(node, Simulator.simulator().getSimulationTime(), timeToSendSomePacket);
        busyUntil = Simulator.simulator().getSimulationTime() + timeToSendSomePacket;
        
        // Programming TaskFinished event
        Simulator.simulator().registerEvent(new NodeTaskFinishedEvent(busyUntil, this));
    }
    
    public List outgoingConnections(){
        return connections.outgoingConnections();
    }
    
    public List incomingConnections(){
        return connections.incomingConnections();
    }

    // Function called by the parent class Node 
    public void newMessage(Object data) {
        
        // New message arrived to node
        
        // Checking message content
        if(DanteConf.getPresentSimConf().beParanoid())
            if(!(data instanceof ProtocolMessage))
                throw new Error(id() + ": Message arrived that is not a P2P message???");
        
        
        ProtocolMessage message = (ProtocolMessage)data;
        
        // Is node active?
        if(!nodeIsActive){            
            // But we 'warn' the origin node, in case is a look_for_resource message, for statistics.
            if(message.getMessageType() == ProtocolMessage.LOOK_FOR_RESOURCE){
                LookForResourceMessage lfrMessage = (LookForResourceMessage)message;
                lfrMessage.getSearchOrigin().searchArrivedToDeactivatedNode(lfrMessage.getSearchID());                
            }            
            
            return;            
        }
        
        searchesStats.newMessageArrived(message);
        
        if(message.getMessageType() == ProtocolMessage.LOOK_FOR_RESOURCE)
            // Registering petition for statistics. Param is the time the petition is started.
            petsCounter.registerPet(Simulator.simulator().getSimulationTime());
        
        // Must enqueue message?        
        if(Simulator.simulator().getSimulationTime() < busyUntil){
            // Must enqueue it
            message.setTimeWasInsertedInQueue(Simulator.simulator().getSimulationTime());
            messagesQueues.addMessage(message);
        } else {
            // Message can be processed now. We know there is not any TaskFinishedEvent events to be fired
            // at this moment because those events have bigger priority and should have been triggered before
            // the MessageReceivingEvent
            message.setTimeWasInsertedInQueue(-1); // <- so it is clear the message does not come from queues
            processMessage(message);
        }
    }
    
    protected void processMessage(ProtocolMessage message) {
        
        
        if(busyUntil > Simulator.simulator().getSimulationTime())
            throw new Error(id() + ": In processMessage(), busyUntil " + busyUntil + " is greater than simulationTime " +
                            Simulator.simulator().getSimulationTime());
        
        
        if(lastMessageEventTime > Simulator.simulator().getSimulationTime())
            throw new Error(id() + ": Processing message at " + Simulator.simulator().getSimulationTime() + 
                            " and previous message was at " + lastMessageEventTime);
        else
            lastMessageEventTime = Simulator.simulator().getSimulationTime();    
        
        
        switch(message.getMessageType()){
        
            case ProtocolMessage.CONNECT:
                
                // New connection petition arrived
                ConnectMessage connectMessage = (ConnectMessage) message;

                // Updating busyUntil
                busyUntil = Simulator.simulator().getSimulationTime() + timeToSendSomePacket;

                connections.connectMessageArrived(connectMessage, busyUntil);  

                // Programming TaskFinished event
                Simulator.simulator().registerEvent(new NodeTaskFinishedEvent(busyUntil, this));
                
                break;
                
            case ProtocolMessage.DISCONNECT:
                
                // Remote node demanded to close connection
                DisconnectMessage disconnectMessage = (DisconnectMessage) message;            
                
                // Updating busyUntil
                busyUntil = Simulator.simulator().getSimulationTime() + 1;
                
                connections.disconnectMessageArrived(disconnectMessage);

                // Programming TaskFinished event
                Simulator.simulator().registerEvent(new NodeTaskFinishedEvent(busyUntil, this));            
                
                break;
                
            case ProtocolMessage.ACCEPT:
                
                // Remote node accepted connection
                AcceptMessage acceptMessage = (AcceptMessage) message;

                // Updating busyUntil
                busyUntil = Simulator.simulator().getSimulationTime() + 1;

                // Programming TaskFinished event
                Simulator.simulator().registerEvent(new NodeTaskFinishedEvent(busyUntil, this));
                
                if(!connections.acceptMessageArrived(acceptMessage, busyUntil))
                    break;
                
                // Should we disconnect from any other node?                
                if(!(connections.outgoingConnections().size() > DanteConf.getPresentSimConf().outConns()))
                    break;
                
                // Choosing node to disconnect from
                DanteNode nodeToDiscFrom = (DanteNode)nodesToDisconnectFrom.remove(acceptMessage.getAcceptingNode());
                
                if((nodeToDiscFrom == null) || (!connections.outgoingConnections().contains(nodeToDiscFrom))){

                    nodeToDiscFrom = DanteConf.getPresentSimConf().disChooser().pickOneNodeToDisconnectFrom(connections.outgoingConnections());
                    
                    if(nodeToDiscFrom == null)
                        throw new Error("Could not choose any node to disconnect from?");
                
                }
                
                // Disconnecting from neighbor
                disconnectFrom(nodeToDiscFrom);
                
                break;
            
            case ProtocolMessage.REJECT:
                
                // Remote node rejected connection
                RejectMessage rejectMessage = (RejectMessage) message;
                
                // Updating busyUntil
                busyUntil = Simulator.simulator().getSimulationTime() + 1;
            
                connections.rejectMessageArrived(rejectMessage);

                // Programming TaskFinished event
                Simulator.simulator().registerEvent(new NodeTaskFinishedEvent(busyUntil, this));
                
                break;
        
            case ProtocolMessage.LOOK_FOR_RESOURCE:
            
                // New resource search arrived
                LookForResourceMessage lookForResourceMessage = (LookForResourceMessage) message;
               
                ResourceLookingUpResult resourceLookingUpResult = resourceIsKnown(lookForResourceMessage.getResource(), lookForResourceMessage.getSender());

                // Computing the total time it took to search the resource
                long timeInSearch = resourceLookingUpResult.getTimeInSearch();
                long processTime = Math.max(timeInSearch, timeToSendSomePacket);
                
                // Registering processed petition for stats. Param is the time the petition was started, second the time it took to process it.
                long insertedInQueueAt = lookForResourceMessage.timeWasInsertedInQueue();
                long timeInQueue = (insertedInQueueAt < 0 ? 0 : Simulator.simulator().getSimulationTime() - insertedInQueueAt);
                //petitionsCounter.petitionProcessed(Simulator.simulator().getSimulationTime(), processTime + timeInQueue);      
                petsCounter.petProcessed(Simulator.simulator().getSimulationTime(), processTime, timeInQueue);
                
                // Updating busyUntil, depending on the total resources checked
                busyUntil = Simulator.simulator().getSimulationTime() + processTime + DanteConf.getPresentSimConf().fixedTimeCost();    
                
                // Adding this node to the counter of traversed nodes
                lookForResourceMessage.increaseCounterOfTraversedNodes();
                    
                // Checking if resource is known locally
                if(resourceLookingUpResult.getNodeResourceIs() == null){           
                    
                    // Resource is unknown, downgrading ttl and checking if forwarding message or sending not found.
                    if(lookForResourceMessage.decreaseTTL() == 0){
                        
                        // Sending ResourceNotFound
                        ResourceNotFoundMessage resourceNotFoundMessage = new ResourceNotFoundMessage(this,
                                                                                                      lookForResourceMessage.getSearchID(),
                                                                                                      lookForResourceMessage.getNumberOfNodesTraversedInSearch(),
                                                                                                      lookForResourceMessage.getSearchOriginGeneration());
                        
                        sendMessageToNode(busyUntil, lookForResourceMessage.getSearchOrigin(), resourceNotFoundMessage); // -> inherited from Node class                        
                    
                    } else {
                        
                        // Search must be forwarded to some neighbor, which one?
                        DanteNode nodeToForwardTo = DanteConf.getPresentSimConf().fwdSearchChooser().nodeToForwardTo(neighbors, lookForResourceMessage.getSender());
                        
                        if(nodeToForwardTo == null){
                             
                            // Can not find any neighbor
                            
                            // Sending RESOURCE_NOT_FOUND to original sender
                            ResourceNotFoundMessage resourceNotFoundMessage = new ResourceNotFoundMessage(this,
                                                                                                          lookForResourceMessage.getSearchID(),
                                                                                                          lookForResourceMessage.getNumberOfNodesTraversedInSearch(),
                                                                                                          lookForResourceMessage.getSearchOriginGeneration());
                            
                            sendMessageToNode(busyUntil, lookForResourceMessage.getSearchOrigin(), resourceNotFoundMessage);
                            
                        } else {
                            
                            // Forwarding search
                            lookForResourceMessage.setSender(this);
                            sendMessageToNode(busyUntil, nodeToForwardTo, lookForResourceMessage);
                            
                        }   
                        
                    }
                    
                } else {            
                    
                    // Resource was found at time 'busyUntil', sending ResourceFound message
                    ResourceFoundMessage resourceFoundMessage = new ResourceFoundMessage(this,
                                                                                         lookForResourceMessage.getSearchID(),
                                                                                         resourceLookingUpResult.getNodeResourceIs(),
                                                                                         lookForResourceMessage.getResource(),
                                                                                         lookForResourceMessage.getNumberOfNodesTraversedInSearch(),
                                                                                         lookForResourceMessage.getSearchOriginGeneration());
                    
                    sendMessageToNode(busyUntil, lookForResourceMessage.getSearchOrigin(), resourceFoundMessage);
                }               

                // Programming TaskFinished event
                Simulator.simulator().registerEvent(new NodeTaskFinishedEvent(busyUntil, this));
                
                break;
                
            case ProtocolMessage.RESOURCE_FOUND:
                
                // Resource search was successful
                ResourceFoundMessage resourceFoundMessage = (ResourceFoundMessage) message;     

                // Updating busyUntil
                busyUntil = Simulator.simulator().getSimulationTime() + 1;   
                
                if(resourceFoundMessage.getSearchOriginGeneration() == nodeGeneration)                    
                    searchesStats.searchFinished(resourceFoundMessage.getSearchID(),
                                                Simulator.simulator().getSimulationTime(),
                                                resourceFoundMessage.getNumberOfNodesTraversedInSearch(),
                                                true);
                
                if(DanteConf.getPresentSimConf().onlineReplication()) {
                	
                	DanteNode whereResourseIs = resourceFoundMessage.getNodeResourceIsIn();
                	int resource = resourceFoundMessage.getResource();
                	int numberOfResourceToAdd = DanteConf.getPresentSimConf().orReplicationCount();
                	double extraResChance = DanteConf.getPresentSimConf().orExtraResRate();
                	
                	// First replicate the founded resource
                	this.resources.addExtraResource(resource);
                	numberOfResourceToAdd--;
                	
                	// Now request for some more resources to replicate 
                	if (numberOfResourceToAdd > 0) {
                		
                		int numberOfMainResToReplicate = 0;
                		int numberOfExtraResToReplicate = 0;
                		Random randomGenerator = new Random();

                		while (numberOfResourceToAdd-- != 0) {
                			// Chance to select a resource from extra resources
                			if (randomGenerator.nextInt(100)+1 <= extraResChance*100)
                				numberOfExtraResToReplicate++;
                			else
                				numberOfMainResToReplicate++;
                		}
                		
                		ReplicationMessage replicationMessage = new ReplicationMessage(0, // Request
                																this, // message origin
                																numberOfMainResToReplicate, numberOfExtraResToReplicate, 
                																new SortedArrayList());
                		
                		busyUntil += timeToSendSomePacket;
                		sendMessageToNode(busyUntil, whereResourseIs, replicationMessage);
                	}
                }
                
                // Programming TaskFinished event
                Simulator.simulator().registerEvent(new NodeTaskFinishedEvent(busyUntil, this));
                
                break;
            
            case ProtocolMessage.RESOURCE_NOT_FOUND:
                
                // Resource search was unsuccessful
                ResourceNotFoundMessage resourceNotFoundMessage = (ResourceNotFoundMessage) message;      
                
                // Updating busyUntil
                busyUntil = Simulator.simulator().getSimulationTime() + 1;                             

                if(resourceNotFoundMessage.getSearchOriginGeneration() == nodeGeneration)
                    searchesStats.searchFinished(resourceNotFoundMessage.getSearchID(),
                                                Simulator.simulator().getSimulationTime(),
                                                resourceNotFoundMessage.getNumberOfNodesTraversedInSearch(),
                                                false);

                // Programming TaskFinished event
                Simulator.simulator().registerEvent(new NodeTaskFinishedEvent(busyUntil, this));
                
                break;
                
            case ProtocolMessage.REPLICATION_MESSAGE:
            	
            	ReplicationMessage replicationMessage = (ReplicationMessage) message;

            	busyUntil = Simulator.simulator().getSimulationTime() + 1;

            	// Check if the message is a request
            	if (replicationMessage.getPacketType() == 0) {

            		SortedArrayList resForReplication = new SortedArrayList();
            		int numberOfMainResToReplicate = replicationMessage.getMainResourcesCount();
            		int numberOfExtraResToReplicate = replicationMessage.getExtraResourcesCount();
            		Random randomGenerator = new Random();
            		int res = 0;

            		while(numberOfMainResToReplicate-- != 0) {

            			if(DanteConf.getPresentSimConf().resReplIsUniform()) {

            				int max = this.resources.getMaxRes();
            				int min = this.resources.getMinRes();
            				long range = (long)max - (long)min + 1;
            				// compute a fraction of the range, 0 <= frac < range
            				long fraction = (long)(range * randomGenerator.nextDouble());
            				res = (int)(fraction + min);

            			} else {

            				res = this.resources.getResource(randomGenerator.nextInt(this.resources.resources.size()));
            			}

            			resForReplication.add(res);
            		}	
            		
            		if (this.resources.extraResources.size() != 0) {

            			while(numberOfExtraResToReplicate-- != 0) {

            				res = this.resources.getExtraResource(randomGenerator.nextInt(this.resources.extraResources.size()));
            				resForReplication.add(res);
            			}	
            		}


            		ReplicationMessage replicationMessageResponse = new ReplicationMessage(1, // Response
            																		this, // message origin
            																		replicationMessage.getMainResourcesCount(), 
            																		replicationMessage.getExtraResourcesCount(),
            																		resForReplication);

            		busyUntil += timeToSendSomePacket;
            		sendMessageToNode(busyUntil, replicationMessage.getMessageOrigin(), replicationMessageResponse);

            	} else { // if message type is a response

            		SortedArrayList allRes = replicationMessage.getResources();

            		Iterator iterator = allRes.iterator();
            		while (iterator.hasNext()){
            			this.resources.addExtraResource((Integer)iterator.next());
            		}

            	}
            	
            	// Programming TaskFinished event
                Simulator.simulator().registerEvent(new NodeTaskFinishedEvent(busyUntil, this));
                
            	break;
                
            case ProtocolMessage.LOOK_FOR_NODES:
                
                // A nodes search message has arrived
                LookForNodeMessage lookForNodesMessage = (LookForNodeMessage) message;

                // Updating busyUntil
                busyUntil = Simulator.simulator().getSimulationTime() + timeToSendSomePacket;
                
                lookForNodesMessage.addNodeToListOfTraversedNodes(this);
                
                if(lookForNodesMessage.decreaseTTL() == 0){
                    
                    // Must send NODES_FOUND message to origin
                    NodesFoundMessage nodesFoundMessage = new NodesFoundMessage(this,
                                                                                lookForNodesMessage.getSearchID(),
                                                                                lookForNodesMessage.getTraversedNodesList(),
                                                                                lookForNodesMessage.getSearchOriginGeneration());
                    
                    sendMessageToNode(busyUntil, lookForNodesMessage.getSearchOrigin(), nodesFoundMessage);
                    
                } else {
                    
                    // Search must be forwarded to some neighbor
                    DanteNode nodeToForwardTo = DanteConf.getPresentSimConf().fwdChooser().nodeToForwardTo(neighbors, lookForNodesMessage.getSender());
                    
                    if(nodeToForwardTo == null){
                        
                        // Must send NODES_FOUND message to origin
                        NodesFoundMessage nodesFoundMessage = new NodesFoundMessage(this,
                                                                                    lookForNodesMessage.getSearchID(),
                                                                                    lookForNodesMessage.getTraversedNodesList(),
                                                                                    lookForNodesMessage.getSearchOriginGeneration());
                        
                        sendMessageToNode(busyUntil, lookForNodesMessage.getSearchOrigin(), nodesFoundMessage);
                        
                    } else {
                        
                        // Forwarding to neighbor
                        
                        lookForNodesMessage.setSender(this);
                        sendMessageToNode(busyUntil, nodeToForwardTo, lookForNodesMessage);
                        
                    }
                
                }                
                    
                // Programming TaskFinished event
                Simulator.simulator().registerEvent(new NodeTaskFinishedEvent(busyUntil, this));
                
                break;
                
            case ProtocolMessage.NODES_FOUND:
                
                NodesFoundMessage nodesFoundMessage = (NodesFoundMessage)message;
                
                if(nodesFoundMessage.getSearchOriginGeneration() != nodeGeneration){
                    busyUntil = Simulator.simulator().getSimulationTime() + 1;          
                                
                    // Programming TaskFinished event
                    Simulator.simulator().registerEvent(new NodeTaskFinishedEvent(busyUntil, this));
                    break;
                }
                
                // Before computing to which nodes this node must connect to, must remove repeated nodes from
                // candidates list, and this node itself in case it appears
                // OrderedArrayList automatically discards duplicates.
                SortedArrayList candidates = new SortedArrayList(nodesFoundMessage.getTraversedNodesList());
                // Removing this node from list of candidates (just in case)
                candidates.remove(this);

                List nodesToConnectTo = DanteConf.getPresentSimConf().kernel().nodesToConnectTo(candidates, this);
                
                // Reconnecting
                redoOutgoingConnections(nodesToConnectTo);

                // NOT Updating busyUntil, its done by the redoOutgoingConnections() function
                /*busyUntil = Simulator.simulator().getSimulationTime() + 1;   
                Simulator.simulator().registerEvent(new TaskFinishedEvent(busyUntil, this));*/
            
                break;
                
            default:
                throw new Error("Unexpected message type???");
        }
    }    
    
    public void neighborDeactivated(DanteNode neighbor){
        
        boolean isOutgoingConn = connections.outgoingConnections().contains(neighbor);
        
        // Some node deactivated, if there was some outgoing connection pointing to that peer
        // a new reconnect process must be started UNLESS this node was going to disconnect
        // from it anyway.        
        connections.neighborDown(neighbor);     
        
        nodesToDisconnectFrom.remove(neighbor);  // Just in case, although it would be really weird...
        
        connectPendingPetitions.remove(neighbor); // In vase it was an incoming connection we were going to connect to
        disconnectPendingPetitions.remove(neighbor); // In vase it was an outgoing connection we were going to disconnect from 
        
        if(isOutgoingConn){
            
            // Choosing a new outgoing neighbor at random
            DanteNode newNeighbor = null;
            do {
                newNeighbor = (DanteNode)allActiveNodesInSystem.get(RandomGenerator.randomIntValue( allActiveNodesInSystem.size()) );
            } while(neighbors.contains(newNeighbor) || connectPendingPetitions.contains(newNeighbor));
            
            connectTo(newNeighbor);
            
            reconnecter.triggerReconnectionInmediately();            
        }
        
    }
    
    public void searchArrivedToDeactivatedNode(int searchID){
        searchesStats.searchArrivedToDeactivatedNode(searchID);
    }
    
    public void searchDiscardedBecauseFullQueue(int searchID){
        searchesStats.searchDiscardedBecauseFullQueue(searchID);
    }
    
    public void searchDiscardedFromQueueBecauseDeactivation(int searchID){
        searchesStats.searchDiscardedFromQueueBecauseDeactivation(searchID);
    }
    
    public void searchResultLostBecauseDeactivation(int searchID){
        searchesStats.searchResultLostBecauseDeactivation(searchID);
    }
    
    // The P2Pnode must process the TaskFinished events!!. So it overwrites 
    // the processEvent method inherited from the Node class.
    public void processEvent(Event event) {
        
        if(!(event instanceof NodeTaskFinishedEvent)){
            // Not a NodeTaskFinishedEvent, passing it to the parent Node class
            super.processEvent(event);
            return;
        }
        
        // Is node active?
        if(!nodeIsActive){            
            eventsIgnoredBecauseNodeNotActive++;
            return;
        }
        
        NodeTaskFinishedEvent taskFinishedEvent = (NodeTaskFinishedEvent)event;
        
        // Is an event of the present generation of node?
        if( taskFinishedEvent.getNodeGeneration() < nodeGeneration)
            // Event from a previous generation of node, ignoring
            return;
        
        if( taskFinishedEvent.getNodeGeneration() > nodeGeneration)
            throw new Error("WHAT? Event of a future generation of node!!");
        
        if(lastTaskFinishedEventTime > Simulator.simulator().getSimulationTime())
            throw new Error(id() + ": TaskFinishedEvent at " + Simulator.simulator().getSimulationTime() + " and previous was at " + lastTaskFinishedEventTime);
        else
            lastTaskFinishedEventTime = Simulator.simulator().getSimulationTime();
        
        // Task finished event!, node can process next connection process, local search, or incoming message
        
        // Checking for pending reconnection processes
        if(reconnectPendingPetitions > 0){
            reconnectPendingPetitions--;
            totalPendingReconnectionsInSystem--;
            startReconnectionProcess();
            return;
        }            
        
        // Checking for pending connection processes
        
        if(!connectPendingPetitions.isEmpty()){
            totalPendingConnectsPetsInSystem--;
            DanteNode node = (DanteNode)connectPendingPetitions.remove(0);

            startConnectProcess(node);
            return;
        }
        
        
        // Checking for pending disconection processes
        
        if(!disconnectPendingPetitions.isEmpty()){
            totalPendingDisconnectsPetsInSystem--;
            DanteNode node = (DanteNode)disconnectPendingPetitions.remove(0);
            startDisconnectProcess(node);
            return;
        }        
        
        // Checking for pending local searchs
        if(pendingLocalSearchsQueue.isEmpty()){
            PendingLocalSearch pendingLocalSearch = pendingLocalSearchsQueue.nextSearch();
            
            processLocalSearch(pendingLocalSearch.getSearchID(), pendingLocalSearch.getResource());
            return;            
        }
        
        // No local petition waiting, unenqueue message and process it (if any)
        ProtocolMessage message = null;
        if((message = messagesQueues.getMessage()) != null){
            processMessage(message); 
            return;
        }       
        
    }   
    
    public Resources getResources(){
        return resources;
    }
    
    public void setResources(Resources resources){
        this.resources = resources;
    }
    
    public NodeSearchesStats getSearchesStats(){
        return searchesStats;
    }
    
    public PetsCounter getPetitionsCounter(){
        return petsCounter;
    }
    
    public MessagesQueuesSet getMessagesQueuesSet(){
        return messagesQueues;
    }
    
    public double getCapacity(){
        return capacity;
    }
    
    public double getBandwidth(){
        return bandwidth;
    }

 
    // SimulationComponent methods
    public void beforeStart() {   
    }

    public void afterStop() {        
        searchesStats.simulationFinished();
    }
    
}
