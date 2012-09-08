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

import java.util.HashMap;

import es.ladyr.dante.externalEvents.EventForwarder;
import es.ladyr.dante.protocol.AcceptMessage;
import es.ladyr.dante.protocol.ConnectMessage;
import es.ladyr.dante.protocol.DisconnectMessage;
import es.ladyr.dante.protocol.RejectMessage;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.util.dataStructs.SortedArrayList;


public class ConnectedNodesSet {
    
    protected final static long MIN_TIME_UNITS_BETWEEN_CONNECTS = 100000000; 
    
    protected SortedArrayList incomingConnections = new SortedArrayList();
    
    protected SortedArrayList outgoingConnections = new SortedArrayList();
    
    protected SortedArrayList connectedNodes = null; // <- No strictly necessary, as we have 'neighborsGenerations'. But is more efficient.
    
    protected HashMap connectsWaitingForReplyTimes = new HashMap();
    
    protected HashMap neighborsGenerations = new HashMap();
    
    protected DanteNode thisNode = null;
    
    public ConnectedNodesSet(DanteNode thisNode){
        this.thisNode = thisNode;
        connectedNodes = thisNode.neighbors();
    }
    
    public void nodeWasDeactivated(){
        

        DanteNode[] connNodes = (DanteNode[])connectedNodes.toArray(new DanteNode[0]);
        for(int connIndex = 0; connIndex < connNodes.length; connIndex++)
            connNodes[connIndex].neighborDeactivated(thisNode);
        
        // Sending events that outgoing connections are down
        if(DanteConf.getPresentSimConf().generateEvents()){
            DanteNode[] outNeighbors = (DanteNode[])outgoingConnections.toArray(new DanteNode[0]);
            for(int outIndex = 0; outIndex < outNeighbors.length; outIndex++)
                EventForwarder.getInstance().connectionDeleted(thisNode, outNeighbors[outIndex]);
        }
        
        // Empty all lists
        incomingConnections.clear();
        outgoingConnections.clear();
        connectsWaitingForReplyTimes.clear();
        neighborsGenerations.clear();
        connectedNodes.clear();
        
    }
    
    // returns true if a connect message was sent
    public boolean connectToNode(DanteNode node, long connectStartsAt, long timeToSendPacket){
        
        if(outgoingConnections.contains(node))
            return false;
        
        if(connectsWaitingForReplyTimes.containsKey(node)){
            
            long connectTime = ((Long)connectsWaitingForReplyTimes.get(node)).longValue();
            
            if(connectStartsAt - connectTime < MIN_TIME_UNITS_BETWEEN_CONNECTS){
                // It has been too little time since last connect to node was sent. So ignoring that petition.
                return false;
            }
        }
        
        // Creating CONNECT message
        ConnectMessage connectMessage = new ConnectMessage(thisNode, thisNode.nodeGeneration());
        
        // Storing connect
        connectsWaitingForReplyTimes.put(node, new Long(connectStartsAt));
        
        // Sending message to node
        thisNode.sendMessageToNode(connectStartsAt + timeToSendPacket, node, connectMessage);
        
        return true;
    }
    
    public void disconnectFromNode(DanteNode neighbor, long disconnectStartsAt, long timeToSendPacket){
        
        if(!outgoingConnections.contains(neighbor))
            return;            
        
        if(!neighbor.nodeIsActive()){
            neighborDown(neighbor);            
        } else {
            
            // Creating DISCONNECT message
            DisconnectMessage disconnectMessage = new DisconnectMessage(thisNode, thisNode.nodeGeneration());
                    
            // Sending message to node
            thisNode.sendMessageToNode(disconnectStartsAt + timeToSendPacket, neighbor, disconnectMessage);
            
            // Removing node from list of outgoing connections
            removeOutgoingConnection(neighbor);
            
        }
        
        if(DanteConf.getPresentSimConf().generateEvents())
            EventForwarder.getInstance().connectionDeleted(thisNode, neighbor);
    }
    
    public void connectMessageArrived(ConnectMessage connectMessage, long processingMessageAt){
        
        DanteNode askingNode = connectMessage.getAskingNode();
        
        // Already connected? (this should not happen, unless the two CONNECTS were sent in little time)
        if(incomingConnections.contains(askingNode)){            
            
            int neighborGeneration = ((Integer)neighborsGenerations.get(askingNode)).intValue();
            
            if (neighborGeneration < connectMessage.getAskingNodeGeneration()){
                // Trickier, must ignore old generation of node, and see if we accept new connection
                removeIncomingConnection(askingNode);
            } else if (neighborGeneration == connectMessage.getAskingNodeGeneration()){
                // A bit strange...
                // Sending ACCEPT, just in case.
                if(askingNode.nodeIsActive()){
                    AcceptMessage acceptMessage = new AcceptMessage(thisNode, thisNode.nodeGeneration(), connectMessage.getAskingNodeGeneration());
                    thisNode.sendMessageToNode(processingMessageAt, askingNode, acceptMessage);                     
                } else {
                    neighborDown(askingNode);
                }                
                
                return;      
            } // else {} // Do nothing, ignore CONNECT from old generation node
            
        }
        
        if(!askingNode.nodeIsActive()){
            // Do nothing anyway, ignore message from not active node
            return;
        }
        
        // Accept the new incoming connection? 
        if(DanteConf.getPresentSimConf().connsAcc().acceptNewConnection(askingNode)) {   
            
            addIncomingConnection(askingNode, connectMessage.getAskingNodeGeneration());
            
            // Sending ACCEPT
            AcceptMessage acceptMessage = new AcceptMessage(thisNode, thisNode.nodeGeneration(), connectMessage.getAskingNodeGeneration());
            thisNode.sendMessageToNode(processingMessageAt, askingNode, acceptMessage);
            
        } else {
            
            // Sending REJECT
            RejectMessage rejectMessage = new RejectMessage(thisNode, thisNode.nodeGeneration(), connectMessage.getAskingNodeGeneration());
            thisNode.sendMessageToNode(processingMessageAt, askingNode, rejectMessage);
        }
        
    }
    
    public void disconnectMessageArrived(DisconnectMessage disconnectMessage){
        
        Integer neighborGenerationInt = (Integer)neighborsGenerations.get(disconnectMessage.getAskingNode());
        
        if(neighborGenerationInt != null){
            if(neighborGenerationInt.intValue() > disconnectMessage.getAskingNodeGeneration())
                // disconnect message from and old generation of neighbor, ignoring
                return;
            else 
                removeIncomingConnection(disconnectMessage.getAskingNode());
        }
        
    }
    
    public boolean acceptMessageArrived(AcceptMessage acceptMessage, long processingMessageAt){
        
        // Maybe is an accept from and old generation of this node
        if(acceptMessage.getAskingNodeGeneration() < thisNode.nodeGeneration()){
            // Ignoring, but before we should send a disconnect if node is not a neighbor
            // and no connect was sent to it
            if( !connectsWaitingForReplyTimes.containsKey(acceptMessage.getAcceptingNode()) && !(connectedNodes.contains(acceptMessage.getAcceptingNode())) ){                
                // Creating DISCONNECT message
                DisconnectMessage disconnectMessage = new DisconnectMessage(thisNode, thisNode.nodeGeneration());                        
                // Sending message to node
                thisNode.sendMessageToNode(processingMessageAt, acceptMessage.getAcceptingNode(), disconnectMessage);
            }
            return false;
        }
        
        // Already a connection to that node?
        if( outgoingConnections.contains(acceptMessage.getAcceptingNode()) ){
            
            int neighborGeneration = ((Integer)neighborsGenerations.get(acceptMessage.getAcceptingNode())).intValue();
            
            // Well, let's check generation of nodes
            if(neighborGeneration > acceptMessage.getAcceptingNodeGeneration()) {
                // Ignoring totally that accept
            } else if (neighborGeneration == acceptMessage.getAcceptingNodeGeneration()) {
                connectsWaitingForReplyTimes.remove(acceptMessage.getAcceptingNode());
            } else {
                // Must update connection generation data                
                neighborsGenerations.put(acceptMessage.getAcceptingNode(), new Integer(acceptMessage.getAcceptingNodeGeneration()));                
                connectsWaitingForReplyTimes.remove(acceptMessage.getAcceptingNode());
            }
            
           return false;                 
        }
        
        if(!acceptMessage.getAcceptingNode().nodeIsActive()){
            connectsWaitingForReplyTimes.remove(acceptMessage);
            neighborDown(acceptMessage.getAcceptingNode());
            return false;
        }
        
        if(!connectsWaitingForReplyTimes.containsKey(acceptMessage.getAcceptingNode()))            
            return false;
        
        connectsWaitingForReplyTimes.remove(acceptMessage.getAcceptingNode());
            
        addOutgoingConnection(acceptMessage.getAcceptingNode(), acceptMessage.getAcceptingNodeGeneration());
            
        if(DanteConf.getPresentSimConf().generateEvents())
            EventForwarder.getInstance().connectionAdded(thisNode, acceptMessage.getAcceptingNode());
                
        return true;
    }
    
    public void rejectMessageArrived(RejectMessage rejectMessage){
        if(rejectMessage.getAskingNodeGeneration() < thisNode.nodeGeneration())
            //discard
            return;
        connectsWaitingForReplyTimes.remove(rejectMessage.getRejectingNode());        
    }
    
    public void neighborDown(DanteNode neighbor){
        
        incomingConnections.remove(neighbor);
        outgoingConnections.remove(neighbor);
        connectedNodes.remove(neighbor);
        neighborsGenerations.remove(neighbor);
        
        // Just in case
        connectsWaitingForReplyTimes.remove(neighbor);           
    }
    
    protected void addIncomingConnection(DanteNode newNeighbor, int newNeighborGeneration){            
        incomingConnections.add(newNeighbor);       
        connectedNodes.add(newNeighbor);
        if(!neighborsGenerations.containsKey(newNeighbor))
            neighborsGenerations.put(newNeighbor, new Integer(newNeighborGeneration));
        else {
            int neighborGeneration = ((Integer)neighborsGenerations.get(newNeighbor)).intValue();
            if(neighborGeneration > newNeighborGeneration)
                throw new Error("GENERATION ERROR");
            neighborsGenerations.put(newNeighbor, new Integer(newNeighborGeneration));            
        }
    }
    
    protected void addOutgoingConnection(DanteNode newNeighbor, int newNeighborGeneration){
        outgoingConnections.add(newNeighbor);   
        connectedNodes.add(newNeighbor);
        if(!neighborsGenerations.containsKey(newNeighbor))
            neighborsGenerations.put(newNeighbor, new Integer(newNeighborGeneration));
        else {
            int neighborGeneration = ((Integer)neighborsGenerations.get(newNeighbor)).intValue();
            if(neighborGeneration > newNeighborGeneration)
                throw new Error("GENERATION ERROR");
            neighborsGenerations.put(newNeighbor, new Integer(newNeighborGeneration));            
        }
    }
    
    protected void removeIncomingConnection(DanteNode neighbor){        
        incomingConnections.remove(neighbor);
        if(!outgoingConnections.contains(neighbor)){
            connectedNodes.remove(neighbor);
            neighborsGenerations.remove(neighbor);
        }
    }
    
    protected void removeOutgoingConnection(DanteNode neighbor){
        outgoingConnections.remove(neighbor);
        if(!incomingConnections.contains(neighbor)){
            connectedNodes.remove(neighbor);
            neighborsGenerations.remove(neighbor);
        }
    }
    
    public SortedArrayList outgoingConnections(){
        return outgoingConnections;
    }
    
    public SortedArrayList incomingConnections(){
        return incomingConnections;
    }

}
