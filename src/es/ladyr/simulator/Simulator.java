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

package es.ladyr.simulator;

import java.util.ArrayList;
import java.util.Iterator;


public class Simulator {
    
    // Singleton pattern
    private static Simulator _simulator = new Simulator();
    
    // To hold simulation future events
    private EventHolder eventHolder = new EventHolder();
    
    // Logical simulation time
    private long simulationTime = 0;
    
    // Registered components list. Only used to call them
    // before starting and after finishing simulation.
    private ArrayList components = new ArrayList();
    
    // Time the simulation will last, in logical units. The value
    // is set by the SimulationWatcher instance, if present, if not 
    // it defaults to -1. When -1, the simulation keeps running for ever
    // until the stop() method is called.
    private long simulationDuration = -1;
    
    private long nextMark = SimulatorConfiguration.getSimConf().progressMark(); // >= just to show on terminal where the simulation is
    
    private long maxRealTimeBetweenMarks = 5000;
    private long lastMarkRealTime = -1;
    
    // System state reporter (in case we want to report something periodically, like queues size)
    private ArrayList systemStateReporters = new ArrayList();
    
    private boolean stopped = false;
    private boolean paused = false;
    
    private Thread simulationThread = null;
    
    // Get singleton instance
    public static Simulator simulator(){
        return _simulator;
    }
    
    // Get logical simulation time
    public long getSimulationTime(){
        return simulationTime;
    }
    
    // Set system state reporter
    public void addSystemStateReporter(Reporter systemStateReporter){
        systemStateReporters.add(systemStateReporter);
    }
    
    // New event
    public void registerEvent(Event event){
        
        if(event == null)
            throw new Error("Trying to register a null event???");             
        
        if(event.getFiringTime() < simulationTime)
            throw new Error("Event to be fired before present simulation time???");       
        
        eventHolder.addEvent(event);
        
    }
    
    // Suspend event
    public void suspendEvent(Event event){
        
        if(event == null)
            throw new Error("Trying to remove a null event???");            
        
        if(event.getFiringTime() < simulationTime)
            return;
        
        eventHolder.removeEvent(event);
    }
    
    // Add simulation component
    public void registerSimulationComponent(SimulationComponent component){
        components.add(component);
    }
    
    public void run(long simulationDuration){
        this.simulationDuration = simulationDuration;
        run();
    }
    
    // Run simulation
    public void run() {
        
        if(stopped)
            throw new Error("Can not run a stopped simulation (must call to clearSimulator() first)!!");
        
        simulationThread = Thread.currentThread();
        
        // Starting registered components        
        startComponents();
        
        // Run while there are events in queue, simulation has not been stopped and logical time is 
        // smaller than simulationDuration.
        Event event = null;
        while((event = eventHolder.nextEvent()) != null && !stopped){
            
            if(paused)
                synchronized(simulationThread){
                    System.out.println("SIMULATION PAUSED");
                    try {
                        simulationThread.wait();
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                        throw new Error("Error, simulator thread was interrupted");
                    }
                    if(paused)
                        throw new Error("Error, to restart simulator call resumeSimulation()...");
                    if(stopped)
                        break;
                    System.out.println("SIMULATION RESTARTED");                    
                }
            
            if((simulationDuration >= 0) && (simulationDuration < event.getFiringTime())){
                simulationTime = simulationDuration;
                break;                
            }
            
            simulationTime = event.getFiringTime();         
            
            printTimeMark();
                
            // Event has a handler associated to it.
            //event.getEventHandler().processEvent(event);
            event.fireEvent();
            
        }
        
        // Stopping registered components
        stopComponents();        
    }
    
    // Start simulation components
    protected void startComponents(){
        Iterator iter = components.iterator();
        while(iter.hasNext())
            ((SimulationComponent)iter.next()).beforeStart();            
    }
    
    // Stop simulation components
    protected void stopComponents(){                
        Iterator iter = components.iterator();
        while(iter.hasNext())
            ((SimulationComponent)iter.next()).afterStop();
    }
    
    // Print time mark
    protected void printTimeMark(){
        
        long progressMark = SimulatorConfiguration.getSimConf().progressMark();
        
        // It is demanded to print progression marks, and is there any object to report them?
        if((progressMark > 0) && 
           ( (simulationTime >= nextMark)||(System.currentTimeMillis() - lastMarkRealTime >= maxRealTimeBetweenMarks) ) ){
            
            String memInfo = "Free mem: " + Runtime.getRuntime().freeMemory()/1024 + " KBs (" +
                              (Runtime.getRuntime().freeMemory() * 100)/ Runtime.getRuntime().maxMemory() + "%)";
            
            String perCentOfSimRun = "";
            if(simulationDuration > 0)
                perCentOfSimRun = " (" + ((simulationTime*100)/simulationDuration) + "%)";
            
            if (simulationTime >= nextMark){
                
                if(simulationTime - nextMark <  progressMark){
                    System.out.println("> " + nextMark + perCentOfSimRun + " - " + memInfo);
                    nextMark += progressMark;                        
                } else {
                    nextMark += progressMark * ((simulationTime - nextMark) / progressMark + 1);
                    System.out.println("> " + (nextMark - progressMark) + perCentOfSimRun + " - " + memInfo);                        
                }
                
            } else if (System.currentTimeMillis() - lastMarkRealTime >= maxRealTimeBetweenMarks){
                
                System.out.println("> " + simulationTime + perCentOfSimRun + " - " + memInfo);              
                
            }

            Iterator iter = systemStateReporters.iterator();
            while(iter.hasNext())
                System.out.println(((Reporter)iter.next()).getReport());
            System.out.println();
            
            lastMarkRealTime = System.currentTimeMillis();      
        }
        
    }
    
    // Stop simulation although there are still events on queue
    public void stopSimulation(){
        stopped = true;
    }
    
    public void pauseSimulation(){
        paused = true;
    }
    
    public void resumeSimulation(){
        if(stopped)
            throw new Error("Can not resume a stopped simulation (must call to clearSimulator() first)!!");
        paused = false;
        synchronized(simulationThread){
            simulationThread.notify();
        }
    }
    
    // Return number of events in queue
    public int eventsInQueue(){
        return eventHolder.totalEvents();
    }
    
    // Clearing simulator before it starts working
    public void clearSimulator(){
        eventHolder.clearEvents();
        components.clear();
        simulationTime = 0;
        simulationDuration = -1;
        stopped = false;
        systemStateReporters.clear();
        nextMark = SimulatorConfiguration.getSimConf().progressMark();
    }
}
