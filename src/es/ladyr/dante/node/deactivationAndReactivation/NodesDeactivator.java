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

package es.ladyr.dante.node.deactivationAndReactivation;

import java.util.HashMap;

import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.simulator.Event;
import es.ladyr.simulator.EventHandler;
import es.ladyr.simulator.SimulationComponent;
import es.ladyr.simulator.Simulator;
import es.ladyr.util.math.ExponentialDistribution;

public class NodesDeactivator  implements EventHandler, SimulationComponent {
    
    private ExponentialDistribution activeTimeDistrb = null;
    
    private boolean simulationOver = false;
    private boolean mustWork = true;
    
    private HashMap nodesEvents = new HashMap();
    
    private static NodesDeactivator _instance = new NodesDeactivator();
    
    public static NodesDeactivator getInstance(){        
        return _instance;
    }

    public void processEvent(Event event) {
        
        if(!mustWork)
            throw new Error("Should not receive events in Nodes Deactivator, no distributions for times were set");
        
        if(simulationOver)
            return;
        
        if(!(event instanceof DeactivateNodeEvent))            
            // Unknown event type
            throw new Error("Unknown event in NodesDeactivator???");
        
        DanteNode nodeToDeactivate = ((DeactivateNodeEvent)event).getNode();
        
        nodesEvents.remove(nodeToDeactivate);
            
        if(!nodeToDeactivate.nodeIsActive())
            throw new Error("Trying to deactivate a not active node");      
            
        nodeToDeactivate.deactiveNode();            
            
        NodesReactivator.getInstance().scheduleNodeReactivation(nodeToDeactivate);
            
        return;
        
    }

    public void beforeStart() {
        
        simulationOver = false;
        
        nodesEvents.clear();
        
        double actMeanTime = DanteConf.getPresentSimConf().actMeanTime();
        
        activeTimeDistrb = (actMeanTime > 0) ? new ExponentialDistribution(actMeanTime) : null;
        
        mustWork = (activeTimeDistrb != null);
        
        if(!mustWork)
            return;
        
        // Programming deactivation or activation time for all nodes
        DanteNode[] allNodes = (DanteNode[])DanteNode.allNodesInSystem().toArray(new DanteNode[0]);
        for(int nodeIndex = 0; nodeIndex < allNodes.length; nodeIndex++){

            DanteNode node = allNodes[nodeIndex];
            
            if(node.nodeIsActive())     
                scheduleNodeDeactivation(node);
        }
    }
    
    public void scheduleNodeDeactivation(DanteNode node){
        
        if(!node.nodeIsActive())
            throw new Error("Node is not active");
        
        if(simulationOver)
            return;
        
        if(!mustWork)
            return;
            
        long activeTime = activeTimeDistrb.nextLong();
        long timeToNextEvent = Simulator.simulator().getSimulationTime() + activeTime;
        
        DeactivateNodeEvent deactivateNodeEvent = new DeactivateNodeEvent(timeToNextEvent, this, node);
        nodesEvents.put(node, deactivateNodeEvent);
        
        Simulator.simulator().registerEvent(deactivateNodeEvent);     
        
    }

    public void afterStop() {
        simulationOver = true;                
    }

    // If some node has been attacked, its corresponding deactivation event (if any) must be suspended
    public void nodeWasAttacked(DanteNode node){
        
        if(!mustWork)
            return;
        
        if(simulationOver)
            return;
                
        Event event = (Event)nodesEvents.remove(node);
        
        if(event == null)
            return;
        
        Simulator.simulator().suspendEvent((DeactivateNodeEvent)event);
        
    }
    
    // After some node recovers from an attack, it must be planned again its deactivation
    public void nodeWasRecovered(DanteNode node){
        
        if(!mustWork)
            return;
        
        if(!simulationOver)
            return;
                
        Event event = (Event)nodesEvents.remove(node);
        
        if(event != null)
            throw new Error("There was a pending event of a recovered node");
        
        scheduleNodeDeactivation(node);
        
    }

}

class DeactivateNodeEvent extends Event {
    
    public final static int DEACTIVATION_EVENT_PRIORITY = Event.MINIMUM_EVENT_PRIORITY; 

    protected DanteNode node = null;

    public DeactivateNodeEvent(long time, EventHandler eventHandler,DanteNode node) {
        super(time, eventHandler, DEACTIVATION_EVENT_PRIORITY);
        this.node = node;
    }
    
    public DanteNode getNode(){
        return node;
    }
    
}
