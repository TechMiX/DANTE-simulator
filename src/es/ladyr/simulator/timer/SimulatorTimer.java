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

package es.ladyr.simulator.timer;

import es.ladyr.simulator.Event;
import es.ladyr.simulator.EventHandler;
import es.ladyr.simulator.Simulator;

public class SimulatorTimer implements EventHandler {
    
    protected TimerEvent timerEvent = null;
    protected TimeEventsWaiter eventsWaiter = null;
    protected long period = 0;
    protected boolean waitingForTimeEvent = false;
    
    
    public void scheduleTimeEvent(TimeEventsWaiter eventsWaiter, long time){        
        scheduleTimeEvent(eventsWaiter, time, TimerEvent.DEFAULT_TIMER_EVENT_PRIORITY);        
    }
    
    public void scheduleTimeEvent(TimeEventsWaiter eventsWaiter, long time, int eventPriority){
        
        if(time < Simulator.simulator().getSimulationTime())
            throw new Error("Can not program a time event before present simulation time");

        // Registering who waits for time events 
        this.eventsWaiter = eventsWaiter;
        
        // Suspending any already scheduled time event
        suspendTimeEvent();
        
        // Now, programming new Timer event         
        if(timerEvent == null)
            timerEvent = new TimerEvent(time, this, eventPriority);
        else
            timerEvent.recicleTimerEvent(time, this, eventPriority);
        
        Simulator.simulator().registerEvent(timerEvent);
        
        waitingForTimeEvent = true;
        
    }
    
    public void schedulePeriodicalTimeEvent(TimeEventsWaiter eventsWaiter, long startingTime, long period){
        schedulePeriodicalTimeEvent(eventsWaiter, startingTime, period, TimerEvent.DEFAULT_TIMER_EVENT_PRIORITY);
    }
    
    public void schedulePeriodicalTimeEvent(TimeEventsWaiter eventsWaiter, long startingTime, long period, int eventPriority){
        
        scheduleTimeEvent(eventsWaiter, startingTime, eventPriority);
        
        this.period = period;
        
    }
    
    public boolean eventScheduled(){
        //return (timerEvent != null);
        return waitingForTimeEvent;
    }
    
    public long eventSchedulingTime(){
        return (!waitingForTimeEvent ? -1 : timerEvent.getFiringTime());
    }
    
    public long timeToEvent(){
        return (!waitingForTimeEvent ? -1 : timerEvent.getFiringTime() - Simulator.simulator().getSimulationTime());
    }
    
    public long period(){
        return period;
    }
    
    public void updatePeriod(long newPeriod){
        
        if(period <= 0)
            throw new Error("Not periodical events programmed");
        
        period = newPeriod;
    }
    
    public void suspendTimeEvent(){
        
        // Suspending any already scheduled time event
        if(waitingForTimeEvent)
            Simulator.simulator().suspendEvent(timerEvent);
        waitingForTimeEvent = false;
        
        // Suspending periodical timing of events too. 
        period = 0;
    }

    public void processEvent(Event event) {
        
        if(event != timerEvent)
            throw new Error("Unexpected event");
        
        if(!waitingForTimeEvent)
            throw new Error("Timer was not expecting a time event");
        
        // If event is periodical, must be programmed again.
        if(period > 0){
            timerEvent.recicleTimerEvent(Simulator.simulator().getSimulationTime() + period, this, timerEvent.getPriority());
            Simulator.simulator().registerEvent(timerEvent);
        } else         
            waitingForTimeEvent = false;
        
        eventsWaiter.timeExpired(Simulator.simulator().getSimulationTime());
        
    }

}

class TimerEvent extends Event {
    
    public static final int DEFAULT_TIMER_EVENT_PRIORITY = MINIMUM_EVENT_PRIORITY;
    
    public TimerEvent(long time, EventHandler eventHandler, int priority){
        super(time, eventHandler, priority);
    }
    
    public void recicleTimerEvent(long time, EventHandler eventHandler, int priority){
        recicleEvent(time, eventHandler, priority);
    }

}