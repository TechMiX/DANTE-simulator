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

package es.ladyr.dante.monitoring.netState;

import es.ladyr.dante.run.DanteConf;
import es.ladyr.dante.run.DanteSimRunner;
import es.ladyr.simulator.timer.SimulatorTimer;
import es.ladyr.simulator.timer.TimeEventsWaiter;
import es.ladyr.simulator.SimulationComponent;
import es.ladyr.simulator.Simulator;

// Captures periodically the topology of the network
public class NetStateCaptureTriggerer implements SimulationComponent, TimeEventsWaiter {
    
    protected int captureCounter = 0;
    
    protected boolean keepRunning = true;
    
    protected SimulatorTimer simulatorTimer = new SimulatorTimer();
    
    public void beforeStart() {
        // Programming first capture (only if it is in a time >= 0)
        long firstCaptureAt = DanteConf.getPresentSimConf().firstCapTime();
        if(firstCaptureAt >= 0) {
            long timeBetCap = DanteConf.getPresentSimConf().timeBetTopCap();
            simulatorTimer.schedulePeriodicalTimeEvent(this,
                                                       firstCaptureAt,
                                                       timeBetCap);
        }
    }

    public void afterStop() {
        keepRunning = false;
        simulatorTimer.suspendTimeEvent();      
    }

    public void timeExpired(long time) {
        
        if(!keepRunning)
            return;
        
        captureCounter++;
        String captureCounterString = "";
        if (captureCounter < 10)
            captureCounterString = "000" + captureCounter;
        else if (captureCounter < 100)
            captureCounterString = "00" + captureCounter;
        else if (captureCounter < 1000)
            captureCounterString = "0" + captureCounter;
        else 
            captureCounterString = "" + captureCounter;
        String fileName = "network_" + captureCounterString + "_" + Simulator.simulator().getSimulationTime()+".NET";
        
        String pajekDir = DanteConf.getPresentSimConf().pajekDir() + "_" + DanteSimRunner.getExpIndexAsString();
        // Creating pajek file with topology
        TopologyCapturer.getInstance().captureTopologyNow(fileName, pajekDir);
        
        // Creating degree distribution file
        if(DanteConf.getPresentSimConf().capDegDistr()){
            fileName = "network_" + captureCounterString + "_" + Simulator.simulator().getSimulationTime()+"_degree_distribution.txt";
            TopologyCapturer.getInstance().captureDegreeDistributionNow(fileName, pajekDir);          
        }
        
        // Creating file to save nodes state
        fileName = "network_" + captureCounterString + "_nodes_state_" + Simulator.simulator().getSimulationTime()+".txt";
        NodesStateCapturer.getInstance().captureActiveNodesStateNow(fileName, pajekDir);
        
    }

}

