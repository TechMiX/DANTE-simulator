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

package es.ladyr.dante.monitoring.searchesStats;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.node.Resources;
import es.ladyr.dante.protocol.ProtocolMessage;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.simulator.Simulator;

public class NodeSearchesStats {
    
    private static int allActiveSearchesInSystemCounter = 0;  // When this value reaches 0, the simulation can be stopped.
    
    private static int totalSearchesFinished = 0;
    
    private static void updateAllActiveSearchesInSystemCounter(int value){
        allActiveSearchesInSystemCounter += value;
       
        if(allActiveSearchesInSystemCounter < 0)
            throw new Error("Counter of all active searches can not be negative");
        
        if( (allActiveSearchesInSystemCounter == 0) &&
            (DanteConf.getPresentSimConf().ignSearchesAft() > 0)  && 
            (Simulator.simulator().getSimulationTime() > DanteConf.getPresentSimConf().ignSearchesAft()) ){
            Simulator.simulator().stopSimulation();                
        }
    }
    
    private static void checkTotalSearchesFinished(){
        
        long searchesToRun = DanteConf.getPresentSimConf().searchesToRun();
        
        if(searchesToRun <= 0)
            return;
        
        totalSearchesFinished++;
        
        if(totalSearchesFinished == searchesToRun)
            Simulator.simulator().stopSimulation();
    }
    
    public static int allActiveSearchesInSystem(){
        return allActiveSearchesInSystemCounter;
    }
    
    public static void resetGlobalCounters(){
        allActiveSearchesInSystemCounter = 0;
        totalSearchesFinished = 0;        
    }    
    
    private long ttlsSum = 0;
    private long successfulSearchesTTLsSum = 0;
    private long searchTimesSum = 0;
    private long successfulSearchesTimesSum = 0;
    private int startedSearches = 0;
    private int finishedSearches = 0;
    private int successfulSearches = 0;
    private int failedSearches = 0;
    private int ignoredSearches = 0;
    private int discardedSearches = 0; // <= pending searches 'forgotten' because node was de-activated
    
    private int receivedCONNECTs = 0;
    private int receivedACCEPTs = 0;
    private int receivedREJECTs = 0;
    private int receivedDISCONNECTs = 0;
    private int receivedLOOK_FOR_RESOURCEs = 0;
    private int receivedRESOUCE_FOUNDs = 0;
    private int receivedRESOURCE_NOT_FOUNDs = 0;
    private int receivedLOOK_FOR_NODEs = 0;
    private int receivedNODES_FOUNDs = 0;
    
    private int lastSearchTTL = -1; // Needed by the adaptable searches TTL estimator. 
    
    private int searchID = 0;
        
    private Map activeSearches = new HashMap();   
    
    private DanteNode thisNode = null;
    
    public NodeSearchesStats(DanteNode thisNode){
        this.thisNode = thisNode;
    }
    
    public void nodeWasDeactivated(){
        updateAllActiveSearchesInSystemCounter(-activeSearches.size());        
        discardedSearches += activeSearches.size();
        activeSearches.clear();
    }
    
    public void simulationFinished(){
        
        Integer[] searchesID = (Integer[])activeSearches.keySet().toArray(new Integer[0]);
        for(int searchIndex = 0; searchIndex < searchesID.length; searchIndex++)
            searchDiscardedBecauseEndOfSimulation(searchesID[searchIndex].intValue());
        
    }
    
    protected int nextSearchID(){
        return ++searchID;
    }
    
    public void newMessageArrived(ProtocolMessage message){
        
        switch(message.getMessageType()){
            case ProtocolMessage.CONNECT:
                receivedCONNECTs++;
                break;
            case ProtocolMessage.ACCEPT:
                receivedACCEPTs++;
                break;
            case ProtocolMessage.REJECT:
                receivedREJECTs++;
                break;
            case ProtocolMessage.DISCONNECT:
                receivedDISCONNECTs++;
                break;
            case ProtocolMessage.LOOK_FOR_RESOURCE:
                receivedLOOK_FOR_RESOURCEs++;
                break;
            case ProtocolMessage.RESOURCE_FOUND:
                receivedRESOUCE_FOUNDs++;
                break;
            case ProtocolMessage.RESOURCE_NOT_FOUND:
                receivedRESOURCE_NOT_FOUNDs++;
                break;
            case ProtocolMessage.LOOK_FOR_NODES:
                receivedLOOK_FOR_NODEs++;
                break;
            case ProtocolMessage.NODES_FOUND:
                receivedNODES_FOUNDs++;
                break;
            default:
                throw new Error(thisNode.id() + ": Unknown message type???");
        }
        
    }
    
    public int registerNewSearch(int resource, long searchStartedAt){  
        
        int newSearchID = nextSearchID();      

        long ignSearchesBef = DanteConf.getPresentSimConf().ignSearchesBef();
        
        if((ignSearchesBef >= 0) && (searchStartedAt < ignSearchesBef)){
            ignoredSearches++;
            return newSearchID;            
        }
        
        long ignSearchesAft = DanteConf.getPresentSimConf().ignSearchesAft();
        
        if((ignSearchesAft >= 0) && (searchStartedAt > ignSearchesAft)){
            ignoredSearches++;
            return newSearchID;            
        }
        
        updateAllActiveSearchesInSystemCounter(1);
        
        activeSearches.put(new Integer(newSearchID), new ActiveSearchData(searchStartedAt, resource));
        startedSearches++;
        return newSearchID;
    }
    
    public void searchFinished(int searchID, long searchFinishedAt, int searchTTL, boolean success){
        
        // Computing search time
        ActiveSearchData activeSearchData = (ActiveSearchData)activeSearches.remove(new Integer(searchID));
        
        if(activeSearchData == null)
            // Not registered search
            return;
        
        if(searchFinishedAt < activeSearchData.startingTime())
            throw new Error(thisNode.id() + ": Search finished before it was started???");
        
        updateAllActiveSearchesInSystemCounter(-1);
        
        // Writing log
        AllSearchesLog.allSearchesLog().writeSearchLog(thisNode.id(), activeSearchData.resource(), searchTTL, Resources.resourceInSystem(activeSearchData.resource()),
                       success, !success, false, false, false, false, false, false,
                       activeSearchData.startingTime(), searchFinishedAt);
        
        // Updating statistics
        finishedSearches++;
        
        checkTotalSearchesFinished();
        
        if(success){
            successfulSearches++;   
            successfulSearchesTimesSum += (searchFinishedAt - activeSearchData.startingTime());
            successfulSearchesTTLsSum += searchTTL;  
        } else
            failedSearches++;   
        
        ttlsSum += searchTTL;  
        lastSearchTTL = searchTTL;
        
        searchTimesSum += (searchFinishedAt - activeSearchData.startingTime());
    }
    
    public void searchArrivedToDeactivatedNode(int searchID){
        
        ActiveSearchData activeSearchData = (ActiveSearchData)activeSearches.remove(new Integer(searchID));
        
        if(activeSearchData != null){
            AllSearchesLog.allSearchesLog().writeSearchLog(thisNode.id(), activeSearchData.resource(), -1, Resources.resourceInSystem(activeSearchData.resource()),
                           false, false, false, true, false, false, false, false,
                           activeSearchData.startingTime(), Simulator.simulator().getSimulationTime());
            updateAllActiveSearchesInSystemCounter(-1);
            discardedSearches++;
        }
        
    }
    
    public void searchDiscardedBecauseFullQueue(int searchID){
        
        ActiveSearchData activeSearchData = (ActiveSearchData)activeSearches.remove(new Integer(searchID));
        
        if(activeSearchData != null){
            AllSearchesLog.allSearchesLog().writeSearchLog(thisNode.id(), activeSearchData.resource(), -1, Resources.resourceInSystem(activeSearchData.resource()),
                           false, false, false, false, true, false, false, false,
                           activeSearchData.startingTime(), Simulator.simulator().getSimulationTime());
            updateAllActiveSearchesInSystemCounter(-1);
            discardedSearches++;
        }
        
    }
    
    public void searchDiscardedFromQueueBecauseDeactivation(int searchID){
        
        ActiveSearchData activeSearchData = (ActiveSearchData)activeSearches.remove(new Integer(searchID));
        
        if(activeSearchData != null){
            AllSearchesLog.allSearchesLog().writeSearchLog(thisNode.id(), activeSearchData.resource(), -1, Resources.resourceInSystem(activeSearchData.resource()),
                           false, false, false, false, false, true, false, false,
                           activeSearchData.startingTime(), Simulator.simulator().getSimulationTime());
            updateAllActiveSearchesInSystemCounter(-1);
            discardedSearches++;
        }
        
    }
    
    public void searchResultLostBecauseDeactivation(int searchID){
        
        ActiveSearchData activeSearchData = (ActiveSearchData)activeSearches.remove(new Integer(searchID));
        
        if(activeSearchData != null){
            AllSearchesLog.allSearchesLog().writeSearchLog(thisNode.id(), activeSearchData.resource(), -1, Resources.resourceInSystem(activeSearchData.resource()), 
                           false, false, false, false, false, false, true, false,
                           activeSearchData.startingTime(), Simulator.simulator().getSimulationTime());
            updateAllActiveSearchesInSystemCounter(-1);
            discardedSearches++;
        }
        
    }
    
    public void searchDiscardedBecauseEndOfSimulation(int searchID){
        
        ActiveSearchData activeSearchData = (ActiveSearchData)activeSearches.remove(new Integer(searchID));
        
        if(activeSearchData != null){
            AllSearchesLog.allSearchesLog().writeSearchLog(thisNode.id(), activeSearchData.resource(), -1, Resources.resourceInSystem(activeSearchData.resource()),
                           false, false, false, false, false, false, false, true,
                           activeSearchData.startingTime(), Simulator.simulator().getSimulationTime());
            updateAllActiveSearchesInSystemCounter(-1);
        }
        
    }
    
    public void searchOfPreviousGeneration(int searchID){
        //Nothing to do
        //deadInActionSearchs++;        
    }
    
    public double successfulSearchesAverTTL(){
        if(successfulSearches == 0)
            return 0;
        
        // Computing and rounding to 1 decimal        
        // it is recommended to set BigDecimal with an string. Not optimal, but performance is not a big deal
        // here as this method is supposed to be called only once per node when the simulation has finished (to collect
        // the node statistics)
        BigDecimal roundedMean = new BigDecimal(Double.toString((double)successfulSearchesTTLsSum/successfulSearches)); 
        roundedMean = roundedMean.setScale(1, BigDecimal.ROUND_HALF_EVEN);
        return roundedMean.doubleValue();
    }
    
    public long successfulSearchesAverTime(){
        return (successfulSearches !=0 ? successfulSearchesTimesSum/successfulSearches : 0);
    }
    
    public long finishedSearchesAverTime(){
        return (finishedSearches != 0 ? searchTimesSum/finishedSearches : 0);
    }
    
    public double finishedSearchesAverTTL(){
        if(finishedSearches == 0)
            return 0;
        
        // Computing and rounding to 1 decimal        
        // it is recommended to init BigDecimal with an string. Not optimal, but performance is not a big deal
        // here as this method is supposed to be called only once per node when the simulation has finished (to collect
        // the node statistics)
        BigDecimal roundedMean = new BigDecimal(Double.toString((double)ttlsSum/finishedSearches)); 
        roundedMean = roundedMean.setScale(1, BigDecimal.ROUND_HALF_EVEN);
        return roundedMean.doubleValue();
        
    }
    
    public int lastSearchTTL(){
        return lastSearchTTL;
    }
    
    public long ttlsSum(){
        return ttlsSum;
    }
    
    public long searchTimesSum(){
        return searchTimesSum;
    }
    
    public long successfulSearchesTTLsSum(){
        return successfulSearchesTTLsSum;
    }
    
    public long successfulSearchesTimesSum(){
        return successfulSearchesTimesSum;
    }
    
    public int startedSearches(){
        return startedSearches;
    }
    
    public int finishedSearches(){
        return finishedSearches;
    }
    
    public int successfulSearches(){
        return successfulSearches;
    }
    
    public int failedSearches(){
        return failedSearches;
    }
    
    public int pendingSearches(){
        return activeSearches.size();
    }
    
    public int ignoredSearches(){
        return ignoredSearches;
    }
    
    public int discardedSearchesBecauseDeactivation(){
        return discardedSearches;
    }

    /*public List finishedSearchs(){
        return finishedSearchs;
    }*/
    
    
    // Messages counters
    public int counterOfCONNECTsReceived(){
        return receivedCONNECTs;
    }
    public int counterOfACCEPTsReceived(){
        return receivedACCEPTs;
    }
    public int counterOfREJECTsReceived(){
        return receivedREJECTs;
    }
    public int counterOfDISCONNECTsReceived(){
        return receivedDISCONNECTs;
    }
    public int counterOfLOOK_FOR_RESOURCEsReceived(){
        return receivedLOOK_FOR_RESOURCEs;
    }
    public int counterOfRESOURCE_FOUNDReceived(){
        return receivedRESOUCE_FOUNDs;
    }
    public int counterOfRESOURCE_NOT_FOUNDsReceived(){
        return receivedRESOURCE_NOT_FOUNDs;
    }
    public int counterOfLOOK_FOR_NODESsReceived(){
        return receivedLOOK_FOR_NODEs;
    }
    public int counterOfNODES_FOUNDsReceived(){
        return receivedNODES_FOUNDs;
    }

    class ActiveSearchData {
        
        private long startingTime = 0;
        private int resource = 0;
        
        public ActiveSearchData(long startingTime, int resource){
            this.startingTime = startingTime;
            this.resource = resource;
        }
        
        public long startingTime(){
            return startingTime;
        }
        
        public int resource(){
            return resource;
        }

    }
    
}
