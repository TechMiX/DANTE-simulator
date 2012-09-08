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



import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.util.dataStructs.SortedArrayList;
import es.ladyr.util.math.RandomGenerator;

public class StarlikeTopologyGenerator implements TopologyGenerator {

    public void generateTopology() {
        
        SortedArrayList nodesCopy = new SortedArrayList(DanteNode.allActiveNodesInSystem());

        int numberCentralNodes = DanteConf.getPresentSimConf().outConns();
        
        // Central nodes will be between those with higher capacity
        // Not the most optimal solution for all cases. But for the average case we handle, it suits well. 
        SortedArrayList centralNodes = new SortedArrayList();
        while((centralNodes.size() < numberCentralNodes) && (!nodesCopy.isEmpty())){
            
            // Looking for the highest capacity
            double higherCapacity = -1;
            DanteNode[] nodesArray = (DanteNode[])nodesCopy.toArray(new DanteNode[0]);
            for(int nodeIndex = 0; nodeIndex < nodesArray.length; nodeIndex++)
                if(nodesArray[nodeIndex].getCapacity() > higherCapacity)
                    higherCapacity = nodesArray[nodeIndex].getCapacity();

            // Pickung up nodes with higher capacity
            for(int nodeIndex = 0; nodeIndex < nodesArray.length; nodeIndex++){
                if(nodesArray[nodeIndex].getCapacity() == higherCapacity){
                    centralNodes.add(nodesArray[nodeIndex]);
                    nodesCopy.remove(nodesArray[nodeIndex]);
                    if((centralNodes.size() == numberCentralNodes) || (nodesCopy.isEmpty()))
                        break;
                }
            }
        }
        
        // Connecting all non central nodes to central nodes
        DanteNode[] nodesArray = (DanteNode[])nodesCopy.toArray(new DanteNode[0]);
        DanteNode[] centralNodesArray = (DanteNode[])centralNodes.toArray(new DanteNode[0]);
        for(int nodeIndex = 0; nodeIndex < nodesCopy.size(); nodeIndex++){
            DanteNode node = nodesArray[nodeIndex];
            for(int centralNodeIndex = 0; centralNodeIndex < centralNodesArray.length; centralNodeIndex++){
                DanteNode centralNode = centralNodesArray[centralNodeIndex];
                node.connectTo(centralNode);
            }
        }
        
        // Finally, all central nodes must connect to the rest of central nodes and
        // to other randomly chosen node
        for(int centralNodeIndex = 0; centralNodeIndex < centralNodesArray.length; centralNodeIndex++){
            DanteNode centralNode = centralNodesArray[centralNodeIndex];
            
            // Connecting to rest of central nodes
            for(int centralNodeIndex2 = 0; centralNodeIndex2 < centralNodesArray.length; centralNodeIndex2++){
                DanteNode otherCentralNode = centralNodesArray[centralNodeIndex2];
                if(centralNode != otherCentralNode)
                    centralNode.connectTo(otherCentralNode);
            }
            
            // Connecting to some other node, randomly chosen
            int nodeIndex = RandomGenerator.randomIntValue(nodesArray.length);
            DanteNode randomNode = nodesArray[nodeIndex];
            centralNode.connectTo(randomNode);            
        }                    

    }

}
