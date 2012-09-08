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

public class EventHolder {
    
    protected static int operationsBeforeTrim = 10000;
    // Events as a list, ordered from closest to farthest in time: first event in list is the next one to be executed. 
    protected ArrayList eventHeap = new ArrayList();
    
    // Events array is trimmed every 'operationsBeforeTrim'. This var counts the operations so far.
    protected int operations = 0;
    
    public void addEvent(Event eventToInsert){  
        
        checkTrim();
        
        // Old way, forget it
        // We search from last position, using time and priority
        /*int position = events.size();
        int eventPriority = eventToInsert.getPriority();
        long eventFiringTime = eventToInsert.getFiringTime();
        while(position != 0){
            Event tempEvent = (Event)events.get(position-1);
            if((eventFiringTime > tempEvent.getFiringTime()) ||
               ((eventFiringTime == tempEvent.getFiringTime()) &&  (eventPriority <= tempEvent.getPriority())) )
                // By timing and priority, this is the position.
                break;
            position--;
        }
        
        events.add(position,eventToInsert);*/
        
        // Empty?
        if(eventHeap.isEmpty()){
            eventHeap.add(eventToInsert);
            return;
        }

        // Where should we insert this event?
        
        int left = 0;
        int right = eventHeap.size() - 1;
        int middlePoint = 0;
        
        do{
            middlePoint = (left + right) / 2;
            
            Event middleEvent = (Event)eventHeap.get(middlePoint);
            
            if(beforeOrAtThan(middleEvent, eventToInsert)){
                
                // Ok, after that 'middle' event
                
                // Any element after?
                if(middlePoint < eventHeap.size() - 1) {
                    
                    // Some elements after 'middle' event, must check
                    Event followingMiddleEvent = (Event)eventHeap.get(middlePoint + 1);
                    
                    // That following event, goes after eventToInsert?
                    if(!beforeOrAtThan(followingMiddleEvent, eventToInsert)){
                        // Here it is
                        eventHeap.add(middlePoint + 1, eventToInsert);
                        return;
                    }
                    
                    // Keep trying
                    left = middlePoint + 1;
                    
                } else {
                    // Inserting at the end (is the last event!)
                    eventHeap.add(eventToInsert);
                    return;
                }
                
                
            } else {
                
                // Ok, before that 'middle' event
                
                // Any element before?
                if(middlePoint > 0){
                    
                    // Some elements before 'middle' event, must check
                    
                    Event previousMiddleEvent = (Event)eventHeap.get(middlePoint - 1);
                    
                    // That previous element, goes before eventToInsert?
                    if(!beforeOrAtThan(eventToInsert, previousMiddleEvent)){
                        // Inserting here
                        eventHeap.add(middlePoint, eventToInsert);
                        return;
                    }
                    
                    // Keep trying
                    right = middlePoint - 1;
                    
                } else {                    
                    // Inserting at the beginning (is the first event!)
                    eventHeap.add(0,eventToInsert);            
                    return;
                }
                
            }
            
        } while(left <= right);        
        
        // Adding at the end
        eventHeap.add(eventToInsert);
    }
    
    public void removeEvent(Event event){
        
        // Look for events with same time and priority position
        int index = eventIndex(event);
        
        if(index < 0)     
            return;
        
        eventHeap.remove(index);
    }
    
    protected int eventIndex(Event eventToSearch){
        
        // First, trivial check
        if(eventHeap.isEmpty())
            return -1;
        
        // Looking for event position. First, let's look for some
        // event to be fired at the same time and with the same priority
        
        int left = 0;
        int right = eventHeap.size() - 1;
        int middlePoint = 0;
        Event middleEvent = null;
        boolean found = false;
        
        do{
            middlePoint = (left + right) / 2;
            
            middleEvent = (Event)eventHeap.get(middlePoint);
            
            if((middleEvent.getFiringTime() == eventToSearch.getFiringTime()) && (middleEvent.getPriority() == eventToSearch.getPriority())){
                found = true;
                break;
            }
            
            if(beforeOrAtThan(middleEvent, eventToSearch)){
                
                // Ok, after that 'middle' event
                
                // Any element after?
                if(middlePoint < eventHeap.size() - 1) {
                    
                    // Keep trying
                    left = middlePoint + 1;                    
                    
                } else {
                    // Could not find element!!
                    break;
                }
                
            } else {
                
                // Ok, before that 'middle' event
                
                // Any element before?
                if(middlePoint > 0){
                    
                    // Keep trying
                    right = middlePoint - 1;
                    
                } else {
                    // Could not find element!!
                    break;
                }
            }
            
            
        } while(left <= right);
        
        // Could not find any event with that time and priority
        if(!found)
            return -1;
        
        // So, we have found some event with the same firing time and priority. But maybe it is not the
        // event we are looking for!!.
        if(middleEvent == eventToSearch)
            // It is!!
            return middlePoint;
        
        // We must compare with previous and later events...
        
        // Comparing with previous events...
        int indexToCheck = middlePoint;
        Event eventToCheck = null;
        do{
            if(--indexToCheck < 0)
                break;
            
            eventToCheck = (Event)eventHeap.get(indexToCheck);
            
            if(eventToCheck == eventToSearch)
                return indexToCheck;
            
        } while ((eventToCheck.getFiringTime() == eventToSearch.getFiringTime()) &&
                 (eventToCheck.getPriority() == eventToSearch.getPriority()));
        
        // Comparing with later events....
        indexToCheck = middlePoint;
        eventToCheck = null;
        do{            
            if(++indexToCheck > eventHeap.size() -1)
                break;
            
            eventToCheck = (Event)eventHeap.get(indexToCheck);
            
            if(eventToCheck == eventToSearch)
                return indexToCheck;
            
        } while ((eventToCheck.getFiringTime() == eventToSearch.getFiringTime()) &&
                 (eventToCheck.getPriority() == eventToSearch.getPriority()));
        
        // Finally, not found :/
        return -1;
    }
    
    
    // event1 must be fired before or at event2?
    // maybe we should implement Comparable on the Event class. But I prefer this way (it's clearer).
    protected boolean beforeOrAtThan(Event event1, Event event2){
        
        if(event1.getFiringTime() < event2.getFiringTime())
            return true;        

        if(event1.getFiringTime() > event2.getFiringTime())
            return false;
        
        if(event1.getPriority() < event2.getPriority())
            return false;
        
        return true;
    }
    
    public Event nextEvent(){
        
        if(eventHeap.isEmpty())
            return null;
        
        checkTrim();
        
        return (Event)eventHeap.remove(0);
    }
    
    public int totalEvents(){
        return eventHeap.size();
    }
    
    public void clearEvents(){
        eventHeap.clear();
    }
    
    protected void checkTrim(){
        
        if(operationsBeforeTrim <= 0)
            return;
        
        operations++;
        
        if(operations >= operationsBeforeTrim){
            operations = 0;
            eventHeap.trimToSize();
        }
    }
    
}
