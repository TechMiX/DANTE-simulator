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

import es.ladyr.dante.monitoring.searchesStats.NodeSearchesStats;
import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.node.MessagesQueuesSet;
import es.ladyr.simulator.Reporter;

public class DanteReporter implements Reporter {
    
    // Singleton
    protected static DanteReporter _instance = new DanteReporter();
    
    public static DanteReporter getInstance(){
        return _instance;
    }    
    
    public String getReport() {
        
        // Report is total queue sizes , total pending searches, total pending connects and disconnects.
        StringBuffer buffer = new StringBuffer();
        buffer.append("\t(DNT LEVEL)\tMESSAGES IN QUEUES: " + MessagesQueuesSet.totalMessagesInQueuesInSystem());
        buffer.append("\tACTIVE NODES IN SYSTEM: " + DanteNode.allActiveNodesInSystem().size());
        buffer.append(" (" + (DanteNode.allActiveNodesInSystem().size()*100/DanteNode.allNodesInSystem().size()) + "%)");
        buffer.append("\tRUNNING SEARCHES IN SYSTEM: " + NodeSearchesStats.allActiveSearchesInSystem());
        //String report = "\t(DNT LEVEL)\tMESSAGES IN QUEUES: " + MessagesQueuesSet.totalMessagesInQueuesInSystem() +
        //                "\tACTIVE NODES IN SYSTEM: " + DNTNode.allActiveNodesInSystem().size() + 
        //                    " (" + (DNTNode.allActiveNodesInSystem().size()*100/DNTNode.allNodesInSystem().size()) + "%)" +
        //                "\tRUNNING SEARCHS IN SYSTEM: " + AllSearchsLog.allActiveSearchsInSystem();
                        /*"\tPENDING LOCAL SEARCHS: " + PendingLocalSearchsQueue.totalPendingPetitionsInSystem() +
                        "\tPENDING LOCAL RECON: " + DNTNode.totalPendingReconnectionsInSystem() +
                        "\tPENDING LOCAL CONN: " + DNTNode.totalPendingConnectsPetsInSystem() +
                        "\tPENDING LOCAL DISC: " + DNTNode.totalPendingDisconnectsPetsInSystem();*/
        
        return buffer.toString();
    }
    
    public void resetReporter(){
    }

}
