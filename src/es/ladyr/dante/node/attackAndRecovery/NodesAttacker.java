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

package es.ladyr.dante.node.attackAndRecovery;

import java.util.ArrayList;

import es.ladyr.dante.monitoring.netState.TopologyCapturer;
import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.node.deactivationAndReactivation.NodesDeactivator;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.dante.run.DanteSimRunner;
import es.ladyr.simulator.SimulationComponent;
import es.ladyr.simulator.Simulator;

import es.ladyr.simulator.timer.SimulatorTimer;
import es.ladyr.simulator.timer.TimeEventsWaiter;

public class NodesAttacker implements TimeEventsWaiter, SimulationComponent {
    
    protected boolean simulationOver = false;
    protected int attackCounter = 0;
    protected SimulatorTimer simulatorTimer = new SimulatorTimer();
    
    protected static NodesAttacker _instance = new NodesAttacker();
    
    public static NodesAttacker getInstance(){
        return _instance;
    }

    public void beforeStart() {
        
        // Programming first attack time (if time between attacks < 0, then only one attack will be run)
        long firstAttackTime = DanteConf.getPresentSimConf().firstAttTime();
        if(firstAttackTime >= 0){
            long timeBetAttacks = DanteConf.getPresentSimConf().timeBetAtts();
            simulatorTimer.schedulePeriodicalTimeEvent(this, firstAttackTime, timeBetAttacks);
        }
        
        simulationOver = false;
    }

    public void afterStop() {
        simulationOver = true;
        simulatorTimer.suspendTimeEvent();
    }
    
    
    public void timeExpired(long time) {
        attackNodes(time);
    }
    
    private void attackNodes(long time) {
        
        if(simulationOver)
            return;        
        
        // Which are the best connected nodes?
        ArrayList bestConnectedNodes = new ArrayList();
        int lowestDeg = Integer.MAX_VALUE;
        DanteNode[] activeNodes = (DanteNode[])DanteNode.allActiveNodesInSystem().toArray(new DanteNode[0]);
        int nodesToAttack = DanteConf.getPresentSimConf().nodesToAtt();
        for(int nodeIndex = 0; nodeIndex < activeNodes.length; nodeIndex++){
            DanteNode node = activeNodes[nodeIndex];
            if(bestConnectedNodes.size() < nodesToAttack){
                bestConnectedNodes.add(node);
                if (node.degree() < lowestDeg)
                    lowestDeg = node.degree();
            } else {
                if (lowestDeg < node.degree()){
                    removeNodeWithDegree(lowestDeg, bestConnectedNodes);
                    bestConnectedNodes.add(node);
                    lowestDeg = lowestDegree(bestConnectedNodes);
                }                
            }
        }
        
        // Capturing topology before attack
        captureBeforeAttack(++attackCounter);
        
        // let's do very bad things...
        DanteNode[] nodesToDeactivate = (DanteNode[])bestConnectedNodes.toArray(new DanteNode[0]);
        for(int nodeIndex = 0; nodeIndex < nodesToDeactivate.length; nodeIndex++){
            DanteNode node = nodesToDeactivate[nodeIndex];
            node.nodeUnderAttack();
            // Warning the node recoverer
            NodesRecoverer.getInstance().scheduleNodeRecovery(node);
            // Warning the nodes activator and de-activator
            NodesDeactivator.getInstance().nodeWasAttacked(node);
        }
        
        // After the attack, topology must be captured again 
        if(DanteConf.getPresentSimConf().timeNetCapAftAtt() >= 0)
            captureAfterAttack(attackCounter, DanteConf.getPresentSimConf().timeNetCapAftAtt());
        
    }
    
    private int lowestDegree(ArrayList nodesList){
        
        if(nodesList.size() == 0)
            throw new Error("Nodes list is empty?");
        
        int lowestDegree = Integer.MAX_VALUE;
        
        DanteNode[] nodes = (DanteNode[])nodesList.toArray(new DanteNode[0]);
        for(int nodeIndex = 0; nodeIndex < nodes.length; nodeIndex++)
            if(nodes[nodeIndex].degree() < lowestDegree)
                lowestDegree = nodes[nodeIndex].degree();
        
        return lowestDegree;
        
    }
    
    private void removeNodeWithDegree(int degree, ArrayList nodesList){
        
        if(nodesList.size() == 0)
            throw new Error("Nodes list is empty?");
        
        DanteNode[] nodes = (DanteNode[])nodesList.toArray(new DanteNode[0]);
        for(int nodeIndex = 0; nodeIndex < nodes.length; nodeIndex++)
            if(nodes[nodeIndex].degree() == degree){
                nodesList.remove(nodes[nodeIndex]);
                return;
            }
        
        throw new Error("Not node with degree " + degree + " was found in list");
        
    }
    
    private void captureBeforeAttack(int attackCounter){
        String attackCounterString = "";
        if (attackCounter < 10)
            attackCounterString = "000" + attackCounter;
        else if (attackCounter < 100)
            attackCounterString = "00" + attackCounter;
        else if (attackCounter < 1000)
            attackCounterString = "0" + attackCounter;
        else 
            attackCounterString = "" + attackCounter;        
        String pajekDir = DanteConf.getPresentSimConf().pajekDir() + "_" + DanteSimRunner.getExpIndexAsString();
        String fileName = "network_before_attack_" + attackCounterString + "_" + Simulator.simulator().getSimulationTime()+".NET";
        TopologyCapturer.getInstance().captureTopologyNow(fileName, pajekDir);
        fileName = "network_before_attack_" + attackCounterString + "_" + Simulator.simulator().getSimulationTime()+"_degree_distribution.txt";
        TopologyCapturer.getInstance().captureDegreeDistributionNow(fileName, pajekDir);
    }
    
    private void captureAfterAttack(int attackCounter, long waitingTime){
        String attackCounterString = "";
        if (attackCounter < 10)
            attackCounterString = "000" + attackCounter;
        else if (attackCounter < 100)
            attackCounterString = "00" + attackCounter;
        else if (attackCounter < 1000)
            attackCounterString = "0" + attackCounter;
        else 
            attackCounterString = "" + attackCounter;
        String pajekDir = DanteConf.getPresentSimConf().pajekDir() + "_" + DanteSimRunner.getExpIndexAsString();
        String fileName = "network_after_attack_" + attackCounterString + "_" + Simulator.simulator().getSimulationTime()+".NET";
        TopologyCapturer.getInstance().captureTopologyAt(fileName, pajekDir, Simulator.simulator().getSimulationTime() + waitingTime);
        fileName = "network_after_attack_" + attackCounterString + "_" + Simulator.simulator().getSimulationTime()+"_degree_distribution.txt";
        TopologyCapturer.getInstance().captureDegreeDistributionAt(fileName, pajekDir, Simulator.simulator().getSimulationTime() + waitingTime);
        
    }

}
