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

import es.ladyr.dante.externalEvents.EventForwarder;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.simulator.Simulator;

public class NodeState {
    
    protected DanteNode node = null;
    
    protected long lastTimeCongestionWasComputed = -1;
    protected long lastTimeMeanProcessingRateWasComputed = -1;
    protected long lastTimeMeanServiceRateWasComputed = -1;
    protected double congestion = 0.0;
    protected double nodeMeanProcessingRate = 0.0;
    protected double nodeMeanServiceRate = 0.0;
    
    public NodeState(DanteNode node){
        this.node = node;
    }
    
    public double meanProcessingTimeInLastTimeUnit(){
        
        if(lastTimeMeanProcessingRateWasComputed == Simulator.simulator().getSimulationTime())
            return nodeMeanProcessingRate;
        
        nodeMeanProcessingRate = node.getPetitionsCounter().averProcTime(Simulator.simulator().getSimulationTime());
        
        if(nodeMeanProcessingRate == 0)
            // Making estimation if mean could not be computed 
            nodeMeanProcessingRate = stimatedTimeToProcessOneSearch();
        
        lastTimeMeanProcessingRateWasComputed = Simulator.simulator().getSimulationTime();
        
        return nodeMeanProcessingRate;
    }
    
    public double meanServiceTimeInLastTimeUnit(){
        
        if(lastTimeMeanServiceRateWasComputed == Simulator.simulator().getSimulationTime())
            return nodeMeanServiceRate;
        
        nodeMeanServiceRate = node.getPetitionsCounter().averServTime(Simulator.simulator().getSimulationTime());
        if(nodeMeanServiceRate == 0)        
            return stimatedTimeToProcessOneSearch();
        
        lastTimeMeanServiceRateWasComputed = Simulator.simulator().getSimulationTime();
        
        return nodeMeanServiceRate;        
    }
    
    public int nodeArrivalRate(){
        int nodeLoad = node.getPetitionsCounter().petsCountInLastTimeUnit(Simulator.simulator().getSimulationTime());

        if( DanteConf.getPresentSimConf().generateEvents() && node.nodeIsActive() )
            EventForwarder.getInstance().nodeLoadChanged(node, nodeLoad);
        
        return nodeLoad;
    }
    
    public double nodeProcessingRate(){        
        return ((double)DanteConf.getPresentSimConf().timeStepsForCong())/meanProcessingTimeInLastTimeUnit();        
    }
    
    public double nodeServiceRate(){
        return ((double)DanteConf.getPresentSimConf().timeStepsForCong())/meanServiceTimeInLastTimeUnit();
    }
    
    public double nodeCongestion(){
        
        if(lastTimeCongestionWasComputed == Simulator.simulator().getSimulationTime())
            return congestion;
        
        // Congestion = arrivalRate / (processingRate * congestionAcceptanceRate)
        
        int arrivalRate = nodeArrivalRate(); 
        
        double processingRate = nodeProcessingRate();

        // if processingRate == -1 => Infinite capacity, then node is never congested at all!
        if(processingRate == -1.0)
            congestion = 0.0;
        else
            congestion = ((double)arrivalRate)/ processingRate;
        
        lastTimeCongestionWasComputed = Simulator.simulator().getSimulationTime();
        
        return congestion;
        
    }
    
    public boolean nodeIsCongested(){
        return (nodeCongestion() >= 1.0);
    }
    
    public int nodeDegree(){
        return node.degree();
    }
    
    protected double stimatedTimeToProcessOneSearch(){       
        
        long packetSendingTime = 0;
        if(node.getBandwidth() > 0)
            packetSendingTime = (long)Math.ceil(((double)DanteConf.getPresentSimConf().maxPacketsSize()) / node.getBandwidth());
        
        long searchProcessingTime = 0;
        if(node.getCapacity() > 0)
            searchProcessingTime = (long)Math.ceil(((double)DanteConf.getPresentSimConf().resByNode() * node.degree()) / node.getCapacity());
        
        long timeToProcessOneSearch = Math.max(packetSendingTime, searchProcessingTime); 
        
        return timeToProcessOneSearch;
    }

}
