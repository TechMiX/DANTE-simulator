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

package es.ladyr.dante.searchesTriggerer;

import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.node.Resources;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.simulator.Event;
import es.ladyr.simulator.EventHandler;
import es.ladyr.simulator.SimulationComponent;
import es.ladyr.simulator.Simulator;

import es.ladyr.simulator.timer.SimulatorTimer;
import es.ladyr.simulator.timer.TimeEventsWaiter;
import es.ladyr.util.math.RandomGenerator;
import es.ladyr.util.math.ZipfDistribution;


public class PeriodicSearchesTriggerer implements EventHandler, SimulationComponent, TimeEventsWaiter {
    
    protected boolean keepRunning = true;
    protected SimulatorTimer simulatorTimer = new SimulatorTimer();
    protected ZipfDistribution zipfResourceIDGenerator = null;
    
    public PeriodicSearchesTriggerer(){
        if(!DanteConf.getPresentSimConf().resPopulIsUniform())
            zipfResourceIDGenerator = new ZipfDistribution(DanteConf.getPresentSimConf().resPopulTau(), DanteConf.getPresentSimConf().resPopulCutoff());
    }

    public void processEvent(Event event) {
        
        if(!keepRunning)
            return;
        
        if(!(event instanceof TriggerSearchEvent))
            throw new Error("Unknown event in SearchsTriggerer???"); 

        ((TriggerSearchEvent)event).getNode().lookForResource(nextResourceToSearch());
        
        return;   
    }
    
    protected int nextResourceToSearch(){
        if(!DanteConf.getPresentSimConf().resPopulIsUniform())
            return zipfResourceIDGenerator.nextInt();
        else
            return RandomGenerator.randomIntValue(Resources.difResInSystem()) + 1;
    }

    public void beforeStart() {
        // Programming first search
        long firstSearchAt = DanteConf.getPresentSimConf().firstSearchTime();
        long timeBetSearches = DanteConf.getPresentSimConf().timeBetSearches(); 
        if(firstSearchAt >= 0)
            simulatorTimer.schedulePeriodicalTimeEvent(this, 
                                                       firstSearchAt, 
                                                       timeBetSearches);
    }

    public void afterStop() {
        keepRunning = false;
        simulatorTimer.suspendTimeEvent();
    }

    public void timeExpired(long time) {
        
        if(!keepRunning)
            return;

        long timeBetSearches = DanteConf.getPresentSimConf().timeBetSearches(); 
        
        // Calling to all nodes lookForResource() function
        DanteNode[] activeNodes = (DanteNode[])DanteNode.allActiveNodesInSystem().toArray(new DanteNode[0]);
        for(int nodeIndex = 0; nodeIndex < activeNodes.length; nodeIndex++){

            // Random deviation in search
            long randomDeviation = RandomGenerator.randomLongValue(timeBetSearches);
            
            // New search
            Simulator.simulator().registerEvent(new TriggerSearchEvent(Simulator.simulator().getSimulationTime() + randomDeviation,
                                                                       this,
                                                                       activeNodes[nodeIndex]));  
            
        }
        
        // Checking if the time between searches param has changed
        if(simulatorTimer.period() != timeBetSearches)
            // Period must be updated (remember that setting period to 0 is like suspending the timer)
            simulatorTimer.updatePeriod(timeBetSearches);
        
    }

}

//Events used to signal that some search must be started in some node
class TriggerSearchEvent extends Event {
 
    public final static int TRIGGER_SEARCH_EVENT_PRIORITY = Event.MINIMUM_EVENT_PRIORITY; 
     
    protected DanteNode node = null;
    
    public TriggerSearchEvent(long time, EventHandler eventHandler, DanteNode node) {
        super(time, eventHandler, TRIGGER_SEARCH_EVENT_PRIORITY);
        this.node = node;
    }
     
    public DanteNode getNode(){
        return node;
    }
}
