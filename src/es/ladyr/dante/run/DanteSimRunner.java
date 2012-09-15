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

package es.ladyr.dante.run;

import java.io.File;
import java.util.Random;

import es.ladyr.dante.externalEvents.EventForwarder;
import es.ladyr.dante.monitoring.netState.NetStateCaptureTriggerer;
import es.ladyr.dante.monitoring.searchesStats.AllNodesStatsFileGenerator;
import es.ladyr.dante.monitoring.searchesStats.AllSearchesLog;
import es.ladyr.dante.monitoring.searchesStats.DigestedSearchesFileGenerator;
import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.node.Resources;
import es.ladyr.dante.node.attackAndRecovery.NodesAttacker;
import es.ladyr.dante.node.deactivationAndReactivation.NodesDeactivator;
import es.ladyr.dante.node.deactivationAndReactivation.NodesReactivator;
import es.ladyr.dante.searchesTriggerer.PeriodicSearchesTriggerer;
import es.ladyr.dante.searchesTriggerer.SystemLoadModifier;
import es.ladyr.netSims.net.NetConfiguration;
import es.ladyr.netSims.net.NetSystemStateReporter;
import es.ladyr.simulator.Simulator;
import es.ladyr.simulator.SimulatorConfiguration;
import es.ladyr.simulator.SimulatorReporter;
import es.ladyr.util.dataStructs.SortedArrayList;
import es.ladyr.util.exps.Params;
import es.ladyr.util.math.RandomGenerator;
import es.ladyr.util.math.ZipfDistribution;

public class DanteSimRunner {

    private static String expIndex = null;
    public static String getExpIndexAsString(){
        return expIndex;
    }
    
    // Simulation parameters
    private long simulationDuration = 0;    
    
    public void runSimulation(Params simConf){

        // Reading configuration //
        readConfiguration(simConf);
        
        // Saving configuration in file //
        File expConfFile = new File(DanteConf.getPresentSimConf().configStoreFileName()+"_"+simConf.expIndexAsString());
        simConf.storeParamsInFile(expConfFile);
        
        // Resetting simulator //
        Simulator.simulator().clearSimulator();
        
        // Searches log file must be open //        
        AllSearchesLog.prepareNewSimulationSearchesLog();
        
        // Creating DNT nodes, and feeding with resources //
        prepareDNTNodes();
        
        
        // Adding simulation components //        
        // Searches trigger
        Simulator.simulator().registerSimulationComponent(new PeriodicSearchesTriggerer());
        
        // Load modifier
        Simulator.simulator().registerSimulationComponent(new SystemLoadModifier());
        
        // Topology capturer
        Simulator.simulator().registerSimulationComponent(new NetStateCaptureTriggerer());
        
        // Activation and de-activation of nodes trigger
        Simulator.simulator().registerSimulationComponent(NodesDeactivator.getInstance());
        Simulator.simulator().registerSimulationComponent(NodesReactivator.getInstance());
                
        // Nodes attacker
        Simulator.simulator().registerSimulationComponent(NodesAttacker.getInstance());
        
        // Network events forwarder
        if(DanteConf.getPresentSimConf().generateEvents())
            Simulator.simulator().registerSimulationComponent(EventForwarder.getInstance());        
        
        // Adding reporters //
        DanteReporter.getInstance().resetReporter();
        Simulator.simulator().addSystemStateReporter(DanteReporter.getInstance());
        NetSystemStateReporter.getInstance().resetReporter();
        Simulator.simulator().addSystemStateReporter(NetSystemStateReporter.getInstance());
        SimulatorReporter.getInstance().resetReporter();
        Simulator.simulator().addSystemStateReporter(SimulatorReporter.getInstance());
        
        // Running simulation ... //
        Simulator.simulator().run(simulationDuration);
        
        // Simulation is over //        
        // Closing searches log file     
        AllSearchesLog.allSearchesLog().closeSimulationSearchesLog();
        
        // Writing digested searches data file  
        new DigestedSearchesFileGenerator().createDigestedSearchesLog();
        
        // Writing all searches statistics, global and grouped by node
        new AllNodesStatsFileGenerator().createAllNodesStatsFile();
        
    }
    
    private void readConfiguration(Params simConf){
                 
        // Net configuration // 
        NetConfiguration.setPresentSimConf(new NetConfiguration(simConf));
            
        // Simulator configuration //
        SimulatorConfiguration.setSimConf(new SimulatorConfiguration(simConf.getBooleanParam(SimulatorConfiguration.BE_PARANOID), simConf.getLongParam(SimulatorConfiguration.PROGRESS_MARK)));
        
        // Simulation configuration //         
        DanteSimRunner.expIndex = simConf.expIndexAsString();
        simulationDuration = simConf.getLongParam("SimulationDuration");
        
        // DNT configuration //
        DanteConf.setPresentSimConf(new DanteConf(simConf));
        
    }
    
    private void prepareDNTNodes() {

        // Nodes and triggers are registered as simulation components. This means that their methods 'beforeStart' and
        // 'afterStop' will be called when proper.

        DanteNode.allNodesInSystem().clear();
        DanteNode.allActiveNodesInSystem().clear();
        DanteNode.resetGlobalCounters();
        
        // Creating and registering nodes
        double[] allCapacities = DanteConf.getPresentSimConf().capacities();
        double[] allBandwidths = DanteConf.getPresentSimConf().bandwidths();
        int[] nodesPerCapAndBand = DanteConf.getPresentSimConf().nodesPerCapAndBand();
        int nodeCounter = 0;
        int totalNodes = 0;
        for(int index = 0; index < allCapacities.length ; index++)
            totalNodes += nodesPerCapAndBand[index];
        
        for(int index = 0; index < allCapacities.length ; index++){
            double capacity = allCapacities[index];
            double bandwidth = allBandwidths[index];
            int numberOfNodes = nodesPerCapAndBand[index];
            
            Random randomProbabilityGen = new Random();
            
            double probNodeActiveAtStartTime = DanteConf.getPresentSimConf().probNodeActStart();
            
            for(int nodeIndex = 1; nodeIndex <= numberOfNodes; nodeIndex++){
                
                nodeCounter++;
                
                boolean nodeActive = ( probNodeActiveAtStartTime >= 1 ? true : randomProbabilityGen.nextDouble() <= probNodeActiveAtStartTime);
                
                DanteNode node = new DanteNode(nodeCounter, capacity, bandwidth, nodeActive, null);
                
                Simulator.simulator().registerSimulationComponent(node);
            }       
            
        }
        
        if(nodeCounter != totalNodes)
            throw new Error("What happened??. " + totalNodes + " should have been created, instead of " + nodeCounter);
        if(nodeCounter != DanteNode.allNodesInSystem().size())
            throw new Error("I'm getting crazy...");
        
        
        // Nodes created, now feeding resources
        nodeCounter = 0;
        if(!DanteConf.getPresentSimConf().resReplIsUniform()) {
            
            // Resources 'creator'
            ZipfDistribution resourcesZipf = new ZipfDistribution(DanteConf.getPresentSimConf().resReplTau(), DanteConf.getPresentSimConf().resReplCutoff());
             
            DanteNode[] allNodes = (DanteNode[])(DanteNode.allNodesInSystem().toArray(new DanteNode[0]));
            for(int nodeIndex = 0; nodeIndex < allNodes.length; nodeIndex++){
                
                Resources resources = new Resources();
                while(resources.numberOfRes() < DanteConf.getPresentSimConf().resByNode())
                    resources.addResource(resourcesZipf.nextInt());
                
                allNodes[nodeIndex].setResources(resources);
                nodeCounter++;
            }
            
        } else {
            
            SortedArrayList allNodesCopy = new SortedArrayList(DanteNode.allNodesInSystem());
            
            int nodesThatHoldEachResource = (int)(allNodesCopy.size() * DanteConf.getPresentSimConf().unifReplRate());
            
            if(nodesThatHoldEachResource == 0)
                throw new Error("Review replication rate of resources, maybe is too low (Number of nodes to hold each resource results 0");
            if(allNodesCopy.size() % nodesThatHoldEachResource != 0)
                throw new Error("Number of nodes that hold each resource (" +nodesThatHoldEachResource + ") is not a divisor" +
                        " of the total number of nodes (" + allNodesCopy.size() + ")");
            
            int iters = allNodesCopy.size() / nodesThatHoldEachResource;
            for(int iterIndex = 0; iterIndex < iters; iterIndex++){

                int resourceMin = iterIndex*DanteConf.getPresentSimConf().resByNode() + 1;
                int resourceMax = resourceMin + DanteConf.getPresentSimConf().resByNode() - 1;
                
                for(int nodeIndex = 0; nodeIndex < nodesThatHoldEachResource; nodeIndex++){                    
                    // Choose a random node from list
                    DanteNode node = (DanteNode)allNodesCopy.remove(RandomGenerator.randomIntValue(allNodesCopy.size()));
                    node.setResources(new Resources(resourceMin, resourceMax));
                    nodeCounter++;
                }
                
            }
            
            if(!allNodesCopy.isEmpty())
                throw new Error("There are nodes left whose resources have not been set");
            
        }

        if(nodeCounter != totalNodes)
            throw new Error("Filled with resources less nodes than total");       
        
        // Setting total number of resources in system
        Resources.computeDifResInSystem();
        
        // Creating initial topology for nodes
        DanteConf.getPresentSimConf().topGen().generateTopology();  
        //StarlikeTopologyGenerator.generateTopology(nodes);       
        
    }

}
