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
import es.ladyr.simulator.Simulator;
import es.ladyr.simulator.timer.SimulatorTimer;
import es.ladyr.simulator.timer.TimeEventsWaiter;

public class Reconnecter implements TimeEventsWaiter {

    private SimulatorTimer simulatorTimer = new SimulatorTimer();

    private DanteNode thisNode = null;

    public Reconnecter(DanteNode thisNode) {
        this.thisNode = thisNode;
    }

    private void startReconnectionsScheduling(long timeToWaitUntilFirstReconnection) {

        long firstRecTime = DanteConf.getPresentSimConf().firstRecTime();

        // If negative, no reconnections are performed
        if (firstRecTime < 0)
            return;

        if ((DanteConf.getPresentSimConf().stopRecAfter() > 0)
            && (Simulator.simulator().getSimulationTime() >= DanteConf.getPresentSimConf().stopRecAfter()))
            return;

        if (Simulator.simulator().getSimulationTime() + timeToWaitUntilFirstReconnection >= firstRecTime)
            simulatorTimer.schedulePeriodicalTimeEvent(this, Simulator.simulator().getSimulationTime()
                                                             + timeToWaitUntilFirstReconnection,
                                                       DanteConf.getPresentSimConf().timeBetRec());
        else
            simulatorTimer.schedulePeriodicalTimeEvent(this, firstRecTime, DanteConf.getPresentSimConf().timeBetRec());

    }

    public void triggerReconnectionInmediately() {
        simulatorTimer.suspendTimeEvent();
        startReconnectionsScheduling(1);
    }

    public void nodeDeactivated() {
        simulatorTimer.suspendTimeEvent();
    }

    public void nodeActivated() {
        if (simulatorTimer.eventScheduled())
            throw new Error("Node activated, but reconnecter was already waiting for events...");
        startReconnectionsScheduling(DanteConf.getPresentSimConf().timeBetRec());
    }

    public void timeExpired(long time) {
        if ((DanteConf.getPresentSimConf().stopRecAfter() > 0)
            && (Simulator.simulator().getSimulationTime() >= DanteConf.getPresentSimConf().stopRecAfter())) {
            simulatorTimer.suspendTimeEvent();
            return;
        }
        reconnectNow();
    }

    private void reconnectNow() {
        if (DanteConf.getPresentSimConf().nodeSeaByWalk())
            thisNode.reconnect();
        else
            thisNode.redoOutgoingConnections(DanteConf.getPresentSimConf().kernel().nodesToConnectTo(thisNode));
    }

}
