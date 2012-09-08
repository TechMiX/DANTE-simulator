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

package es.ladyr.dante.externalEvents;

import java.util.Observable;

import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.simulator.timer.SimulatorTimer;
import es.ladyr.simulator.timer.TimeEventsWaiter;

import es.ladyr.simulator.SimulationComponent;
import es.ladyr.simulator.Simulator;

public class EventForwarder extends Observable implements TimeEventsWaiter, SimulationComponent {
    
    private static EventForwarder _instance = new EventForwarder();
    private SimulatorTimer timer = null;
    private long timeUnitsPerSecond = -1;
    
    public static EventForwarder getInstance(){
        return _instance;
    }
    
    private EventForwarder(){
        timer = new SimulatorTimer();
    }
    
    public void setTimeUnitsPerSecond(long timeUnitsPerSecond){
        if(timeUnitsPerSecond <= 0)
            throw new Error("A second must last at least one unit of virtual time");
        this.timeUnitsPerSecond = timeUnitsPerSecond;        
        scheduleVirtualSecond();
    }
    
    public void nodeAdded(DanteNode node){
        
        if(!DanteConf.getPresentSimConf().generateEvents())
            throw new Error("Events must not be generated");
        
        setChanged();
        notifyObservers(new NodeAddedEvent(node));
    }
    
    public void nodeDeleted(DanteNode node){
        
        if(!DanteConf.getPresentSimConf().generateEvents())
            throw new Error("Events must not be generated");
        
        setChanged();
        notifyObservers(new NodeDeletedEvent(node));
    }
    
    public void connectionAdded(DanteNode from, DanteNode to){
        
        if(!DanteConf.getPresentSimConf().generateEvents())
            throw new Error("Events must not be generated");
        
        setChanged();
        notifyObservers(new ConnectionAddedEvent(from, to));
    }
    
    public void connectionDeleted(DanteNode from, DanteNode to){
        
        if(!DanteConf.getPresentSimConf().generateEvents())
            throw new Error("Events must not be generated");
        
        setChanged();
        notifyObservers(new ConnectionDeletedEvent(from, to));
    }
    
    public void nodeMetricChanged(DanteNode node, long nodeMetricValue){
        
        if(!DanteConf.getPresentSimConf().generateEvents())
            throw new Error("Events must not be generated");
        
        setChanged();
        notifyObservers(new NodeMetricChangedEvent(node, nodeMetricValue));
        
    }
    
    public void nodeLoadChanged(DanteNode node, long nodeLoadValue){
        
        if(!DanteConf.getPresentSimConf().generateEvents())
            throw new Error("Events must not be generated");

        setChanged();
        notifyObservers(new NodeLoadChangedEvent(node, nodeLoadValue));        
        
    }


    //// TimeEventsWaiter interface method
    // Second passed
    public void timeExpired(long time){
        // Notifying observers
        
        if(!DanteConf.getPresentSimConf().generateEvents())
            throw new Error("Events must not be generated");
            
        setChanged();
        notifyObservers(new VirtualSecondEvent(time/timeUnitsPerSecond));
    }


    //// Simulator component methods
    public void beforeStart() {
        scheduleVirtualSecond();
    }

    public void afterStop() {
        timer.suspendTimeEvent();
    }
    
    private void scheduleVirtualSecond(){
        timer.suspendTimeEvent();
        if(timeUnitsPerSecond > 0)
            timer.schedulePeriodicalTimeEvent(this,Simulator.simulator().getSimulationTime(), timeUnitsPerSecond);        
    }

}
