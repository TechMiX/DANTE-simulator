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

package es.ladyr.util.dataStructs;

public class CountersArray {
    
    protected long[] slots = null;
    
    protected long timePerSlot = 0;
    
    protected int lastElementIndex = -1;
    protected long lastElementTime = -1;
    
    protected boolean sumUpdated = false;
    protected long lastSumTime = 0;
    protected long lastSumValue = 0;
    
    public CountersArray(int totalSlots, long timePerSlot){
        this.timePerSlot = timePerSlot;
        slots = new long[totalSlots];
        setCountersToCero(0, slots.length - 1);
    }
    
    public void reset(){
       lastElementIndex = -1;
       lastElementTime = -1;
       sumUpdated = false;
       lastSumTime = 0;
       lastSumValue = 0;
       setCountersToCero(0, slots.length -1);
    }
    
    public void sumToCounter(long value, long insertedAt){
        
        if(insertedAt < lastElementTime)
            throw new Error("Trying to insert petition before previous one?");
        
        sumUpdated = false;

        //System.out.println("newPetition() - Adding element at: " + insertedAt);
        
        // First element?
        if(lastElementIndex < 0){
            lastElementIndex = 0;
            slots[0] = value;
            lastElementTime = insertedAt;
            return;
        }
        
        // Must move time slots?
        if(insertedAt - lastElementTime >= timePerSlot){            
            removeOld(insertedAt);
            slots[0] = 0;
            lastElementTime = insertedAt;
        }

        lastElementIndex = 0;
        slots[0] += value;
        return;            
    }
    
    public long sumOfCounters(long now){
        
        if(now < lastElementTime)
            throw new Error("Trying to compute sum of counters before previous petition?");
        
        if(lastElementIndex < 0)
            return 0;
        
        // It is value updated?
        if(!(sumUpdated && (now == lastSumTime))){
            // Removing old counters
            removeOld(now);
            
            lastSumValue = 0;
            for(int index = 0; index < slots.length; index++)
                lastSumValue += slots[index];
            
            lastSumTime = now;
            sumUpdated = true;          
        }
        
        return lastSumValue;
        
    }
    
    protected void removeOld(long now){
        
        if(now == lastElementTime)
            return;
        
        // How many slots should be moved?
        int positionsToMove = (int)Math.floor((now - lastElementTime) / timePerSlot) - lastElementIndex;
        
        //System.out.println("removeOld() - Positions to move: " + positionsToMove + "(" + ((now - lastElementTime) / timePerCounter) + ")");
        
        if(positionsToMove <= 0)
            return;
        
        //System.out.println("removeOld() - Last element index: " + lastElementIndex);
        
        if(positionsToMove + lastElementIndex >= slots.length){
            // Setting all counters to cero
            //System.out.println("removeOld() - Setting all counters to cero");
            setCountersToCero(0, slots.length-1);
            lastElementIndex = -1;
            return;
        }
        

        //System.out.println("removeOld() - Elements to move: " + (counters.length - lastElementIndex - positionsToMove));
        // Moving data
        System.arraycopy(slots, lastElementIndex, slots, lastElementIndex + positionsToMove, slots.length - lastElementIndex - positionsToMove);
        lastElementIndex += positionsToMove;
        
        setCountersToCero(0, lastElementIndex - 1);
    }
    
    protected void setCountersToCero(int index1, int index2){
        
        int initPos = (index1 < 0 ? 0 : index1);
        int endPos = (index2 >= slots.length ? slots.length -1 : index2);
        
        for(int index=initPos; index <= endPos; index++)
            slots[index] = 0;
        
    }
    
    public void printData(){
        
        System.out.print("[ ");
        for(int index = slots.length -1 ; index >= 0; index--){
            System.out.print(slots[index]);
            if(index != 0)
                System.out.print(" - ");
        }
            
        System.out.println(" ] - Last element index: " + lastElementIndex + " - Last element time: " + lastElementTime);
        
    }
    
    


}
