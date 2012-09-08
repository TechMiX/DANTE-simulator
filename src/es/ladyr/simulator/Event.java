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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;



public class Event {
    
    public static final int MINIMUM_EVENT_PRIORITY = Integer.MIN_VALUE;
    public static final int MAXIMUM_EVENT_PRIORITY = Integer.MAX_VALUE;
    
    // Event firing time
    private long firingTime = 0;   
    
    // Event priority level. If two events are to be fired at the same time
    // the first to be fired is the one with bigger priority.
    private int priority = MINIMUM_EVENT_PRIORITY;
    
    // Events can be fired in two ways: by calling the handleEvent() method of the EventHandler instance,
    // or calling the methodToCall method of the methodTarget object.
    
    // Who will handle the event. If null, then the method in 'methodToInvoke' is called.
    private EventHandler eventHandler = null;
    
    // Method to invoke when the event is fired. Only used if eventHandler == null.
    private Method methodToInvoke = null;
    private Object objectToCall = null;
    
    public Event(long time, EventHandler eventHandler, int priority){
        if(time < 0)
            throw new Error("Trying to create an event to be triggered at a negative time");
        if(eventHandler == null)
            throw new Error("Trying to create an event with a null Event Handler");
        this.firingTime = time;
        this.eventHandler = eventHandler;
        this.priority = priority;
    }
    
    public Event(long time, Method methodToInvoke, Object objectToCall, int priority){
        if(time < 0)
            throw new Error("Trying to create an event to be triggered at a negative time");
        if(methodToInvoke == null)
            throw new Error("Trying to create an event with a null Method to invoke");
        if(objectToCall == null)
            throw new Error("Trying to create an event with a null Object to call");
        this.firingTime = time;
        this.methodToInvoke = methodToInvoke;
        this.objectToCall = objectToCall;
        this.priority = priority;
    }
    
    public void recicleEvent(long time, EventHandler eventHandler, int priority){
        if(time < 0)
            throw new Error("Trying to recicle an event to be triggered at a negative time");
        if(eventHandler == null)
            throw new Error("Trying to recicle an event with a null Event Handler");
        this.firingTime = time;
        this.eventHandler = eventHandler;
        this.priority = priority;
    }
    
    public long getFiringTime(){
        return firingTime;
    }
    
    public int getPriority(){
        return priority;
    }
    
    public void fireEvent(){
        if(eventHandler != null)
            eventHandler.processEvent(this);
        else
            try {
                methodToInvoke.invoke(objectToCall, new Object[]{this});
            } catch (IllegalArgumentException exception) {
                throw new Error("IllegalArgumentException caught when invoking method " + methodToInvoke.getName() + ", the method must accept SimulationEvent as the first (and only) argument", exception);
            } catch (IllegalAccessException exception) {
                throw new Error("IllegalAccessException caught when invoking method " + methodToInvoke.getName() + ", the method can not be accessed by the simulator", exception);
            } catch (InvocationTargetException exception) {
                throw new Error("InvocationTargetException caught when invoking method " + methodToInvoke.getName() + ", the method thrown some exception", exception);
            }
    }
}
