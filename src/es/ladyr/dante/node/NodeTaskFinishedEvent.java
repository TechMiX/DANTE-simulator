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

import es.ladyr.netSims.net.MessageReceivingEvent;
import es.ladyr.simulator.Event;

// Event that signals that some P2PNode (the event source itself)
// has finished.
// When some node has to do a blocking task for some time t, it
// programs a TaskFinishedEvent to be sent to itself at t,
// and queues all tasks that arrive to it before or at t.
// A TaskFinishedEvent means that the node can keep working so it takes
// some task from the queue and process it. Of course, if that new task
// is blocking (say, for t'), the node must program a new TaskFinishedEvent
// at t + t'.
public class NodeTaskFinishedEvent extends Event {
    
    public static final int TAK_FINISHED_EVENT_PRIORITY = MessageReceivingEvent.MESSAGE_RECEIVING_EVENT_PRIORITY + 1;
    
    protected int nodeGeneration = -1;

    public NodeTaskFinishedEvent(long time, DanteNode node) {
        super(time, node, TAK_FINISHED_EVENT_PRIORITY);
        this.nodeGeneration = node.nodeGeneration();
    }
    
    public int getNodeGeneration(){
        return nodeGeneration;
    }

}
