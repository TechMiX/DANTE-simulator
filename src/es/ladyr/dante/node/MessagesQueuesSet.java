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
 *                 C/Tulipán s/n, 28933, Móstoles, Spain 
 *       
 */

package es.ladyr.dante.node;

import java.util.ArrayList;

import es.ladyr.dante.protocol.ProtocolMessage;
import es.ladyr.dante.protocol.LookForResourceMessage;
import es.ladyr.dante.protocol.ResourceFoundMessage;
import es.ladyr.dante.protocol.ResourceNotFoundMessage;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.simulator.Simulator;


// Each node keeps its messages in queues holds by an instance
// of this class
public class MessagesQueuesSet {
    
    protected static int totalMessagesInQueuesInSystem = 0;
    
    protected ArrayList resourcesSearchMessagesQueue = new ArrayList(); // Maybe linked lists would work faster??
    protected ArrayList nodesSearchMessagesQueue = new ArrayList();
    protected ArrayList connectionMessagesQueue = new ArrayList();
    protected ArrayList searchResultsMessagesQueue = new ArrayList();
    
    protected DanteNode thisNode = null;
    
    // // Counters for stats // //
    
    protected long initialTime = 0;
    
    protected long lastSearchMessageTime = 0;    
    protected long totalSearchQueueTimes = 0;
    //protected int maxSearchQueueSize = 0;

    protected long lastSearchResultMessageTime = 0;    
    protected long totalSearchResultsQueueTimes = 0;
    //protected int maxSearchResultsQueueSize = 0;
    
    protected long lastConnectMessageTime = 0;
    protected long totalConnectsQueueTimes = 0;
    //protected int maxConnectQueueSize = 0;
    
    /*protected long totalMessagesForSearchQueue = 0;
    protected long totalMessagesForSearchResultsQueue = 0;
    protected long totalMessagesForConnectQueue = 0;
    protected long totalMessagesServedFromSearchQueue = 0;
    protected long totalMessagesServedFromSearchResultsQueue = 0;
    protected long totalMessagesServedFromConnectQueue = 0;
    protected long totalTimeInSearchQueue = 0;
    protected long totalTimeInSearchResultsQueue = 0;
    protected long totalTimeInConnectQueue = 0;*/
    
    protected static int totalSearchMessagesRejectedBecauseQueuesFull = 0;
    protected static int totalSearchMessagesLostBecauseNodeDeactivated = 0;
    
    public static int totalSearchMessagesRejectedBecauseQueuesFull(){
        return totalSearchMessagesRejectedBecauseQueuesFull;
    }
    
    public static int totalSearchMessagesLostBecauseNodeDeactivated(){
        return totalSearchMessagesLostBecauseNodeDeactivated;
    }
    
    public static int totalMessagesInQueuesInSystem(){
        return totalMessagesInQueuesInSystem;
    }
    
    public static void resetCounters(){
        totalMessagesInQueuesInSystem = 0;
        totalSearchMessagesLostBecauseNodeDeactivated = 0;
        totalSearchMessagesRejectedBecauseQueuesFull = 0;
    }    
    
    public MessagesQueuesSet(DanteNode thisNode){
        this.thisNode = thisNode;
        initialTime = Simulator.simulator().getSimulationTime();
        lastSearchMessageTime = initialTime;
        lastSearchResultMessageTime = initialTime;
        lastConnectMessageTime = initialTime;
    }
    
    public void nodeWasDeactivated(){
        
        totalMessagesInQueuesInSystem -= (resourcesSearchMessagesQueue.size() + 
                                          nodesSearchMessagesQueue.size() +
                                          connectionMessagesQueue.size() + 
                                          searchResultsMessagesQueue.size());
        
        totalSearchMessagesLostBecauseNodeDeactivated += resourcesSearchMessagesQueue.size();
        
        // Warning nodes about discarded searchs (for statistics purposes)
        ProtocolMessage[] messages = (ProtocolMessage[])resourcesSearchMessagesQueue.toArray(new ProtocolMessage[0]); 
        for(int index = 0; index < messages.length; index++){
            if(messages[index].getMessageType() == ProtocolMessage.LOOK_FOR_RESOURCE){
                LookForResourceMessage lfrMessage = (LookForResourceMessage)messages[index];
                lfrMessage.getSearchOrigin().searchDiscardedFromQueueBecauseDeactivation(lfrMessage.getSearchID());
            }
        }
        
        // Warning this node stats module about searchs results discarded (for statistics purposes)
        messages = (ProtocolMessage[])searchResultsMessagesQueue.toArray(new ProtocolMessage[0]);
        for(int index = 0; index < messages.length; index++){
            
            int messageType = messages[index].getMessageType();

            if(messageType == ProtocolMessage.RESOURCE_FOUND)
                thisNode.searchResultLostBecauseDeactivation(((ResourceFoundMessage)messages[index]).getSearchID());
            else if (messageType == ProtocolMessage.RESOURCE_NOT_FOUND)
                thisNode.searchResultLostBecauseDeactivation(((ResourceNotFoundMessage)messages[index]).getSearchID());
            
        }
        
        resourcesSearchMessagesQueue.clear();
        nodesSearchMessagesQueue.clear();
        connectionMessagesQueue.clear();
        searchResultsMessagesQueue.clear();
        
        // PROBLEM what we do with queues statistics??
        
    }

    public void addMessage(ProtocolMessage message){
        
        switch(message.getMessageType()){
        
            // CONNECT, ACCEPT, REJECT, DISCONNECT or BYE message
            case ProtocolMessage.CONNECT:
            case ProtocolMessage.ACCEPT:
            case ProtocolMessage.REJECT:
            case ProtocolMessage.DISCONNECT:
                //totalMessagesForConnectQueue++;
                //updateConnectQueueCounters();
                connectionMessagesQueue.add(connectionMessagesQueue.size(),message);
                //if(connectionMessagesQueue.size() > maxConnectQueueSize)
                //    maxConnectQueueSize = connectionMessagesQueue.size();
                
                break;
        
            // LOOK_FOR_RESOURCE or LOOK_FOR_NODES message
            case ProtocolMessage.LOOK_FOR_RESOURCE:
                LookForResourceMessage lfrMessage = (LookForResourceMessage)message;
                if( (DanteConf.getPresentSimConf().queMaxSize() > 0) &&
                    (resourcesSearchMessagesQueue.size() >= DanteConf.getPresentSimConf().queMaxSize()) ){
                    totalSearchMessagesRejectedBecauseQueuesFull++;
                    // Warning origin node
                    lfrMessage.getSearchOrigin().searchDiscardedBecauseFullQueue(lfrMessage.getSearchID());                    
                } else {
                    resourcesSearchMessagesQueue.add(resourcesSearchMessagesQueue.size(), lfrMessage);                    
                }
                break;
                
            case ProtocolMessage.LOOK_FOR_NODES:
                //totalMessagesForSearchQueue++;                
                
                //updateSearchQueueCounters();
                nodesSearchMessagesQueue.add(nodesSearchMessagesQueue.size(),message);
                //if(searchMessagesQueue.size() > maxSearchQueueSize)
                    //maxSearchQueueSize = searchMessagesQueue.size();
                               
                break;
            
            // RESOURCE_FOUND, RESOURCE_NOT_FOUND, KNOWLEDGE_PACKAGE or NODES_FOUND message
            case ProtocolMessage.RESOURCE_FOUND:
            case ProtocolMessage.RESOURCE_NOT_FOUND:
            case ProtocolMessage.REPLICATION_MESSAGE:
            case ProtocolMessage.NODES_FOUND:
                //totalMessagesForSearchResultsQueue++;
                //updateSearchResultQueueCounters();
                searchResultsMessagesQueue.add(searchResultsMessagesQueue.size(),message);
                //if(searchResultsMessagesQueue.size() > maxSearchResultsQueueSize)
                //    maxSearchResultsQueueSize = searchResultsMessagesQueue.size();
                
                break;
            
            // What the hell is this??
            default:
                throw new Error(thisNode.id() +": Arrived message of unknown type: " + message.getMessageType());               
        }        
        
        totalMessagesInQueuesInSystem++;
    }
    
    public ProtocolMessage getMessage(){
        
        // CONNECT messages have max priority
        if(!connectionMessagesQueue.isEmpty()){ 
            //updateConnectQueueCounters();  
            ProtocolMessage message = (ProtocolMessage)connectionMessagesQueue.remove(0);
            //totalMessagesServedFromConnectQueue++;
            //totalTimeInConnectQueue += (Simulator.simulator().getSimulationTime() - message.timeWasInsertedInQueue());            

            totalMessagesInQueuesInSystem--;
            return message;
        }

        // RESOURCE_FOUND, RESOURCE_NOT_FOUND and NODES_FOUND are next in priority
        if(!searchResultsMessagesQueue.isEmpty()){
            //updateSearchResultQueueCounters();
            ProtocolMessage message = (ProtocolMessage)searchResultsMessagesQueue.remove(0);
            //totalMessagesServedFromSearchResultsQueue++;
            //totalTimeInSearchResultsQueue += (Simulator.simulator().getSimulationTime() - message.timeWasInsertedInQueue());          

            totalMessagesInQueuesInSystem--;
            return message;            
        }
        
        // LOOK_FOR_NODES messages are next in priority
        if(!nodesSearchMessagesQueue.isEmpty()){
            //updateSearchQueueCounters();
            ProtocolMessage message = (ProtocolMessage)nodesSearchMessagesQueue.remove(0);
            //totalMessagesServedFromSearchQueue++;
            //totalTimeInSearchQueue += (Simulator.simulator().getSimulationTime() - message.timeWasInsertedInQueue());          

            totalMessagesInQueuesInSystem--;
            return message;                
        }    

        // LOOK_FOR_RESOURCE messages have minimum priority
        if(!resourcesSearchMessagesQueue.isEmpty()){
            //updateSearchQueueCounters();
            ProtocolMessage message = (ProtocolMessage)resourcesSearchMessagesQueue.remove(0);
            //totalMessagesServedFromSearchQueue++;
            //totalTimeInSearchQueue += (Simulator.simulator().getSimulationTime() - message.timeWasInsertedInQueue());          

            totalMessagesInQueuesInSystem--;
            return message;                
        }    
        
        return null;
        
    }
    
    public int getNumberPendingMessages(){
        return resourcesSearchMessagesQueue.size() + connectionMessagesQueue.size() + searchResultsMessagesQueue.size();
    } 
    
    public int getNumberPendingSearchMessages(){
        return resourcesSearchMessagesQueue.size();
    }
    
    /*protected long updateSearchQueueCounters(){
        long now = Simulator.simulator().getSimulationTime();
        totalSearchQueueTimes += (now - lastSearchMessageTime) * searchMessagesQueue.size();
        lastSearchMessageTime = now;
        return now;
    }
    
    protected long updateSearchResultQueueCounters(){
        long now = Simulator.simulator().getSimulationTime();
        totalSearchResultsQueueTimes += (now - lastSearchResultMessageTime) * searchResultsMessagesQueue.size();
        lastSearchResultMessageTime = now;
        return now;
    }
    
    protected long updateConnectQueueCounters(){
        long now = Simulator.simulator().getSimulationTime();
        totalConnectsQueueTimes += (now - lastConnectMessageTime) * connectionMessagesQueue.size();
        lastConnectMessageTime = now;
        return now;
    }   
    
    public double averageSearchQueueSize(){
        long now = updateSearchQueueCounters();
        BigDecimal roundedMean = new BigDecimal(Double.toString(((double)totalSearchQueueTimes)/(now - initialTime)));
        roundedMean = roundedMean.setScale(1, BigDecimal.ROUND_HALF_EVEN);
        return roundedMean.doubleValue();
    }
    
    public double averageSearchResultQueueSize(){
        long now = updateSearchResultQueueCounters();
        BigDecimal roundedMean = new BigDecimal(Double.toString(((double)totalSearchResultsQueueTimes)/(now - initialTime)));
        roundedMean = roundedMean.setScale(1, BigDecimal.ROUND_HALF_EVEN);
        return roundedMean.doubleValue();
    }
    
    public double averageConnectQueueSize(){
        long now = updateConnectQueueCounters();
        BigDecimal roundedMean = new BigDecimal(Double.toString(((double)totalConnectsQueueTimes)/(now - initialTime)));
        roundedMean = roundedMean.setScale(1, BigDecimal.ROUND_HALF_EVEN);
        return roundedMean.doubleValue();
    }
    
    public double averageTimeInConnectQueue(){
        BigDecimal roundedMean = new BigDecimal(Double.toString( totalMessagesServedFromConnectQueue != 0 ?
                                                                ((double)totalTimeInConnectQueue)/totalMessagesServedFromConnectQueue :
                                                                 0));
        roundedMean = roundedMean.setScale(1, BigDecimal.ROUND_HALF_EVEN);
        return roundedMean.doubleValue();        
    }
    
    public double averageTimeInSearchsQueue(){
        BigDecimal roundedMean = new BigDecimal(Double.toString( totalMessagesServedFromSearchQueue != 0 ?
                                                                ((double)totalTimeInSearchQueue)/totalMessagesServedFromSearchQueue :
                                                                 0));
        roundedMean = roundedMean.setScale(1, BigDecimal.ROUND_HALF_EVEN);
        return roundedMean.doubleValue();               
    }
    
    public double averageTimeInSearchResultsQueue(){
        BigDecimal roundedMean = new BigDecimal(Double.toString( totalMessagesServedFromSearchResultsQueue != 0 ? 
                                                                 ((double)totalTimeInSearchResultsQueue)/totalMessagesServedFromSearchResultsQueue :
                                                                  0));
        roundedMean = roundedMean.setScale(1, BigDecimal.ROUND_HALF_EVEN);
        return roundedMean.doubleValue();         
    }
    
    public int maxConnectQueueSize(){
        return maxConnectQueueSize;
    }
    
    public int maxSearchQueueSize(){
        return maxSearchQueueSize;
    }
    
    public int maxSearchResultsQueueSize(){
        return maxSearchResultsQueueSize;
    }*/

}
