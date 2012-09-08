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

import es.ladyr.dante.run.DanteConf;
import es.ladyr.simulator.SimulationComponent;
import es.ladyr.simulator.Simulator;
import es.ladyr.simulator.timer.SimulatorTimer;
import es.ladyr.simulator.timer.TimeEventsWaiter;

public class SystemLoadModifier implements SimulationComponent, TimeEventsWaiter {
    
    protected long[] timeIntervals = null;
    protected double[] loadFactors = null;
    protected int nextIntervalIndex = -1;
    
    protected long baseTimeBetweenSearches = 0;
    protected boolean keepRunning = true;
    
    protected SimulatorTimer simulatorTimer = new SimulatorTimer();
    
    public SystemLoadModifier(){
        
        timeIntervals = DanteConf.getPresentSimConf().loadModifTimes();
        loadFactors = DanteConf.getPresentSimConf().loadModifProp();
        
        if((timeIntervals == null) && (loadFactors == null)){
            keepRunning = false;
            return;
        }
        
        if(timeIntervals == null)
            throw new Error("Null time intervals list");
        
        if(loadFactors == null)
            throw new Error("Null load factors list");
        
        if((timeIntervals.length == 0) && (loadFactors.length == 0)){
            keepRunning = false;
            return;
        }
        
        if(timeIntervals.length != loadFactors.length)
            throw new Error("Time intervals and load factors lists must have the same length");
        
        for(int intervalIndex = 0; intervalIndex < loadFactors.length; intervalIndex++)
            if(loadFactors[intervalIndex] < 0)
                throw new Error("Load factor at interval " + (intervalIndex + 1) + " is negative");
        
        for(int intervalIndex = 0; intervalIndex < loadFactors.length; intervalIndex++)
            if(timeIntervals[intervalIndex] < 0)
                throw new Error("Initial time at interval " + (intervalIndex + 1) + " is negative");
        
        if(timeIntervals.length > 1)
            for(int intervalIndex = 0; intervalIndex < timeIntervals.length - 1; intervalIndex++)
                if(timeIntervals[intervalIndex] >= timeIntervals[intervalIndex+1])
                    throw new Error("Error at intervals, time at interval " + (intervalIndex + 1) + " (" + timeIntervals[intervalIndex] + 
                                    ") must be lesser than time at interval " + (intervalIndex + 2) + " (" + timeIntervals[intervalIndex] + ")");
        
    }
    
    public void beforeStart() {
        
        if((timeIntervals == null) || (timeIntervals.length == 0))
            // Nothing to do
            return;
        
        baseTimeBetweenSearches = DanteConf.getPresentSimConf().timeBetSearches();
        
        // Programming timer for first interval
        nextIntervalIndex = 0; 
        
        //System.out.println("\n >>> Programming first load modification at " + timeIntervals[nextIntervalIndex] + " <<<\n");
        simulatorTimer.scheduleTimeEvent(this, timeIntervals[nextIntervalIndex]);
    }
    
    public void afterStop() {
        keepRunning = false;
        simulatorTimer.suspendTimeEvent();
    }    
    
    public void timeExpired(long time) {
        
        if(!keepRunning)
            return;
        
        long now = Simulator.simulator().getSimulationTime();
        
        if(now != timeIntervals[nextIntervalIndex])
            throw new Error("Present time does not correspond with initial time for load modification");
        
        // Setting new system load
        int newTimeBetweenSearches = (int)(baseTimeBetweenSearches * loadFactors[nextIntervalIndex]);
        DanteConf.getPresentSimConf().updateTimeBetSearches(newTimeBetweenSearches);
        
        //System.out.println("\n >>> Setting load to " + newTimeBetweenSearches + " at " + now + " <<<\n");
        
        // Preparing next load change
        nextIntervalIndex++;
        if(nextIntervalIndex < timeIntervals.length)
            simulatorTimer.scheduleTimeEvent(this, timeIntervals[nextIntervalIndex]);
    }

}
