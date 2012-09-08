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

package es.ladyr.netSims.net;

import es.ladyr.simulator.Event;
import es.ladyr.simulator.EventHandler;
import es.ladyr.simulator.SimulationComponent;
import es.ladyr.simulator.Simulator;
import es.ladyr.util.dataStructs.SortedArrayList;


public class Node implements EventHandler, SimulationComponent, Comparable {
    
    private static int messagesInNet = 0;
    
    public static int messagesInNet(){
        return messagesInNet;
    }
    
    public static void resetMessagesCounter(){
        messagesInNet = 0;
    }
    
    private int nodeID = -1;
    private int hashCode = -1;
    protected SortedArrayList neighbors = null;
    
    public Node(int nodeID){
        this.nodeID = nodeID;
        hashCode = new Integer(nodeID).hashCode();
        neighbors = new SortedArrayList();
    }
    
    public int id(){
        return nodeID;
    }
    
    public void sendMessageToNode(long time, Node destinationNode, Object data){
        
        // Creating link from this node to destination node
        /*Link link = new Link(this, destinationNode);
        
        Simulator.simulator().registerEvent(new MessageSendingEvent(time, link, data));*/
        
        Simulator.simulator().registerEvent(new MessageReceivingEvent(time + NetConfiguration.getPresentSimConf().linkDelay(), 
                                                                    destinationNode, 
                                                                    data, 
                                                                    this));
        
        messagesInNet++;
    }
    
    public void processEvent(Event event) {

        if(NetConfiguration.getPresentSimConf().beParanoid()){
            if(!(event instanceof MessageReceivingEvent))
                throw new Error("No MessageReceivingEvent in node???");
        }

        messagesInNet--;
        
        MessageReceivingEvent messageReception = (MessageReceivingEvent) event;
        
        newMessage(messageReception.getData());

    }    
    
    public boolean equals(Object object){
        
        if(object == null)
            return false;

        if(!(object instanceof Node))
            return false;
            
        return ((Node)object).id() == nodeID;    
        
    }

    public void beforeStart(){
    }
    
    public void afterStop(){        
    }
    
    protected void newMessage(Object data){        
    }
    
    public SortedArrayList neighbors(){
        return neighbors;
    }
    
    public int degree() {
        return neighbors.size();
    }
    
    public int hashCode(){
        return hashCode;
    }
    
    public String toString(){
        return nodeID+"";
    }

    public int compareTo(Object o) {
        return ( ((Node)o).id() - nodeID );
    }

}
