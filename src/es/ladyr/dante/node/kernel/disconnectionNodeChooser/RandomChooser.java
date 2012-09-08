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

package es.ladyr.dante.node.kernel.disconnectionNodeChooser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import es.ladyr.dante.node.DanteNode;
import es.ladyr.util.math.RandomGenerator;

// This class chooses a random neighbor to disconnect from it at each
// reconnection
public class RandomChooser implements NodesToDiscFromChooser {

    public HashMap nodesToDisconnectFrom(List listNodesToConnectTo, List listOutgoingConnections) {
        
        HashMap connectionsMap = new HashMap();
        
        List copyListOutgoingConnections = new ArrayList(listOutgoingConnections);

        DanteNode[] newConns =  (DanteNode[])listNodesToConnectTo.toArray(new DanteNode[0]);
        for(int connIndex = 0; connIndex < newConns.length; connIndex++){
            
            DanteNode newConn = newConns[connIndex];
            
            if(!copyListOutgoingConnections.isEmpty()){

                // Chosing randomly the node to disconnect from.
                DanteNode connToClose = (DanteNode)copyListOutgoingConnections.remove(RandomGenerator.randomIntValue(copyListOutgoingConnections.size()));
            
                connectionsMap.put(newConn,connToClose);
                
            }
        
        }
        
        return connectionsMap;
    }

    public DanteNode pickOneNodeToDisconnectFrom(List listOutgoingConnections) {
        
        DanteNode node = (DanteNode)listOutgoingConnections.get(RandomGenerator.randomIntValue(listOutgoingConnections.size()));
        
        return node;
    }

}
