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

import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.simulator.Event;
import es.ladyr.simulator.EventHandler;
import es.ladyr.simulator.SimulationComponent;
import es.ladyr.simulator.Simulator;
import es.ladyr.util.math.ExponentialDistribution;

public class NodesReactivator implements EventHandler, SimulationComponent {

    private ExponentialDistribution deactiveTimeDistrb = null;
    
    private boolean simulationOver = false;
    private boolean mustWork = true;
    
    private static NodesReactivator _instance = new NodesReactivator();
    
    public static NodesReactivator getInstance(){
        return _instance;
    }

    public void processEvent(Event event) {
        
        if(!mustWork)
            throw new Error("Should not receive events in Nodes Reactivator, no distributions for times were set");
        
        if(simulationOver)
            return;
        
        if(!(event instanceof ActivateNodeEvent))
            // Unknown event type
            throw new Error("Unknown event in NodesActivationAndDeactivationTriggerer???");        

        DanteNode nodeToActivate = ((ActivateNodeEvent)event).getNode();
        
        if(nodeToActivate.nodeIsActive())
            throw new Error("Trying to reactivate an active node");
        
        nodeToActivate.reactivateNode();

        NodesDeactivator.getInstance().scheduleNodeDeactivation(nodeToActivate);
        
    }
    
    public void scheduleNodeReactivation(DanteNode node){
        
        if(node.nodeIsActive())
            throw new Error("Node is already active");
        
        if(!mustWork)
            return;
        
        if(simulationOver)
            return;
        
        long timeToNextEvent = 0;
        if(deactiveTimeDistrb != null) {            
            long deactiveTime = deactiveTimeDistrb.nextLong();
            timeToNextEvent = Simulator.simulator().getSimulationTime() + deactiveTime;         
        } else {
            long deactFixedTime = DanteConf.getPresentSimConf().deactFixTime();
            if(deactFixedTime < 0)
                throw new Error("Can not set a negative time for node activation");
            timeToNextEvent = Simulator.simulator().getSimulationTime() + deactFixedTime;
        }
        
        ActivateNodeEvent activateNodeEvent = new ActivateNodeEvent(timeToNextEvent, this, node);
        
        Simulator.simulator().registerEvent(activateNodeEvent);     
    }

    public void beforeStart() {
        
        simulationOver = false;
        
        double deactMeanTime = DanteConf.getPresentSimConf().deactMeanTime();
        
        deactiveTimeDistrb = (deactMeanTime > 0) ? new ExponentialDistribution(deactMeanTime) : null;
            
        long deactFixtime = DanteConf.getPresentSimConf().deactFixTime();
        
        mustWork = (deactiveTimeDistrb != null) || (deactFixtime > 0); 
        
        if(!mustWork)
            return;
        
        // Programming activation time for all nodes
        DanteNode[] allNodes = (DanteNode[])DanteNode.allNodesInSystem().toArray(new DanteNode[0]);
        for(int nodeIndex = 0; nodeIndex < allNodes.length; nodeIndex++){
            DanteNode node = allNodes[nodeIndex];
            
            if(!node.nodeIsActive())
                scheduleNodeReactivation(node);            
        }        
        
    }

    public void afterStop() {
        simulationOver = true;        
    }
    
}

class ActivateNodeEvent extends Event {
    
    public final static int ACTIVATION_EVENT_PRIORITY = Event.MINIMUM_EVENT_PRIORITY; 

    protected DanteNode node = null;

    public ActivateNodeEvent(long time, EventHandler eventHandler,DanteNode node) {
        super(time, eventHandler, ACTIVATION_EVENT_PRIORITY);
        this.node = node;
    }
    
    public DanteNode getNode(){
        return node;
    }
    
}
