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

package es.ladyr.dante.topologyGenerator;

import java.util.ArrayList;


import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.util.dataStructs.SortedArrayList;
import es.ladyr.util.math.RandomGenerator;

public class RandomTopologyGenerator implements TopologyGenerator {

    public void generateTopology() {
        
        DanteNode[] nodes = (DanteNode[])DanteNode.allActiveNodesInSystem().toArray(new DanteNode[0]);
        SortedArrayList candidates = new SortedArrayList(DanteNode.allActiveNodesInSystem());
        
        for(int nodeIndex = 0; nodeIndex < nodes.length; nodeIndex++){
            
            DanteNode node = nodes[nodeIndex];
            
            candidates.remove(node);
            ArrayList newNeighbors  = new ArrayList();
            for(int candidateCounter = 0; 
                ((candidateCounter < DanteConf.getPresentSimConf().outConns()) && (candidates.size() > 0));
                candidateCounter++){
                
                int randomNodeIndex = RandomGenerator.randomIntValue(candidates.size());
                DanteNode candidate = (DanteNode)candidates.remove(randomNodeIndex);
                
                node.connectTo(candidate);
                newNeighbors.add(candidate);
            }
            
            // Restoring candidates list
            candidates.add(node);
            candidates.addAll(newNeighbors);
        }
    }

}
