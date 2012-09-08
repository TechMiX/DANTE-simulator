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

package es.ladyr.dante.node;

import es.ladyr.dante.run.DanteConf;
import es.ladyr.util.dataStructs.CountersArray;


public class PetsCounter {
    
    protected CountersArray petsArrivedCount = null;
    
    protected CountersArray petsFinishedCount = null;
    
    protected CountersArray procTimesCount = null;
    
    protected CountersArray procAndQueueTimesCount = null;
    
    protected long averProcTimeComputedAt = -1;
    protected double averProcTime = 0;
    protected long averServTimeComputedAt = -1;
    protected double averServTime = 0;
    
    public PetsCounter(){
        
        long slotsLength = DanteConf.getPresentSimConf().petsCounterSlotLength();
        int totalSlots = (int)(DanteConf.getPresentSimConf().timeStepsForCong() / slotsLength);
        
        petsArrivedCount = new CountersArray(totalSlots, slotsLength);

        petsFinishedCount = new CountersArray(totalSlots, slotsLength);

        procTimesCount = new CountersArray(totalSlots, slotsLength);  

        procAndQueueTimesCount = new CountersArray(totalSlots, slotsLength);
    }
    
    public void nodeWasDeactivated(){
        petsArrivedCount.reset();
        petsFinishedCount.reset();
        procTimesCount.reset();
        procAndQueueTimesCount.reset();
    }
    
    public void registerPet(long arrivalTime){        
        // Adding the new petition arrival            
        petsArrivedCount.sumToCounter(1, arrivalTime); 
    }
    
    public void petProcessed(long processStartedAt, long processingTime, long timeInQueue){        
        // Adding the new petition arrival              
        petsFinishedCount.sumToCounter(1, processStartedAt);
        procTimesCount.sumToCounter(processingTime, processStartedAt);
        procAndQueueTimesCount.sumToCounter(processingTime + timeInQueue, processStartedAt);
    }
    
    public int petsCountInLastTimeUnit(long now) {
        return (int)petsArrivedCount.sumOfCounters(now);
    }
    
    public double averProcTime(long now){ 
        
        if(averProcTimeComputedAt == now)
            return averProcTime;
        
        long sumOfPetitions = petsFinishedCount.sumOfCounters(now);
        
        if(sumOfPetitions == 0)
            return 0.0;
        
        averProcTime = (double)procTimesCount.sumOfCounters(now) / sumOfPetitions;
        averProcTimeComputedAt = now;
        
        return averProcTime;        
    }

    public double averServTime(long now) {
        
        if(averServTimeComputedAt == now)
            return averServTime;
        
        long sumOfPetitions = petsFinishedCount.sumOfCounters(now);
        
        if(sumOfPetitions == 0)
            return 0.0;
        
        averServTime = (double)procAndQueueTimesCount.sumOfCounters(now) / sumOfPetitions;
        averServTimeComputedAt = now;
        
        return averServTime;        
    }

}
