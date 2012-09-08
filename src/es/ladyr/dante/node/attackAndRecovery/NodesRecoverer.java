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

package es.ladyr.dante.node.attackAndRecovery;

import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.node.deactivationAndReactivation.NodesDeactivator;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.simulator.Event;
import es.ladyr.simulator.EventHandler;
import es.ladyr.simulator.Simulator;
import es.ladyr.util.dataStructs.SortedArrayList;

public class NodesRecoverer implements EventHandler {
    
    protected static NodesRecoverer _instance = new NodesRecoverer();
    
    protected static NodesRecoverer getInstance(){
        return _instance;
    }
    
    public void scheduleNodeRecovery(DanteNode node){
        
        long timeBefRec = DanteConf.getPresentSimConf().timeBefNodeRec();
        
        Simulator.simulator().registerEvent(new RecoverNodeEvent(Simulator.simulator().getSimulationTime() + timeBefRec, this, node));
    }

    public void processEvent(Event event) {
        
        if(!(event instanceof RecoverNodeEvent))
            throw new Error("Not RecoverNodeEvent event at Nodes Recoverer!");
        
        // Choosing random neighbors
        SortedArrayList activeNodes = DanteNode.allActiveNodesInSystem();

        DanteNode nodeToRecover = ((RecoverNodeEvent)event).getNode();
        
        if(activeNodes.contains(nodeToRecover))
            throw new Error("Trying to recover an active node");
        
        nodeToRecover.recoverFromAttack();
        
        // Warning nodes activator and deactivator
        NodesDeactivator.getInstance().nodeWasRecovered(nodeToRecover);
        
    }    

}

class RecoverNodeEvent extends Event {
    
    public final static int RECOVER_NODE_EVENT_PRIORITY = Event.MINIMUM_EVENT_PRIORITY;

    protected DanteNode node = null;

    public RecoverNodeEvent(long time, EventHandler eventHandler, DanteNode node) {
        super(time, eventHandler, RECOVER_NODE_EVENT_PRIORITY);
        this.node = node;
    }
    
    public DanteNode getNode(){
        return node;
    }
    
}
