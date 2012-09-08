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

import java.util.HashMap;


import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.util.dataStructs.SortedArrayList;
import es.ladyr.util.math.RandomGenerator;

public class ExpProportionalTopologyGenerator implements TopologyGenerator {
    
    protected int normalizationFactor = 1;
    
    protected double exponent = 1.2;

    public void generateTopology() {
        
        // Topology must be generated so higher capacity nodes have a greater degree
        
        HashMap metrics = new HashMap();

        SortedArrayList nodesCopy = new SortedArrayList(DanteNode.allActiveNodesInSystem());
        
        // Must normalize capacity??
        double minCapacity = Double.MAX_VALUE;
        DanteNode[] nodesArray = (DanteNode[])nodesCopy.toArray(new DanteNode[0]);
        for(int nodeIndex = 0; nodeIndex < nodesArray.length; nodeIndex++){
            double capacity = nodesArray[nodeIndex].getCapacity();
            if((capacity < 1) && (capacity < minCapacity))
                minCapacity = capacity;
        }
        if(minCapacity < 1){
            if(1 % minCapacity != 0)
                throw new Error("Can not normalize capacities with a min capacity of " + minCapacity);
            
            normalizationFactor = (int)(1 / minCapacity);
        }
        
        // Check
        for(int nodeIndex = 0; nodeIndex < nodesArray.length; nodeIndex++)
            if((long)nodesArray[nodeIndex].getCapacity()*normalizationFactor != nodesArray[nodeIndex].getCapacity()*normalizationFactor)
                throw new Error("Can not normalize capacity " + nodesArray[nodeIndex].getCapacity() + " with normalization factor " + normalizationFactor + " = 1 / " + minCapacity);

        // Check
        for(int nodeIndex = 0; nodeIndex < nodesArray.length; nodeIndex++){
            if((long)Math.pow(nodesArray[nodeIndex].getCapacity()*normalizationFactor, exponent) != Math.pow(nodesArray[nodeIndex].getCapacity()*normalizationFactor, exponent))
                throw new Error("Can not normalize capacity " + nodesArray[nodeIndex].getCapacity() + " with normalization factor " + normalizationFactor + " = 1 / " + minCapacity);
            metrics.put(nodesArray[nodeIndex], new Long((long)Math.pow(nodesArray[nodeIndex].getCapacity()*normalizationFactor, exponent)));
        }
        
        
        // Computing sum of all capacities
        long allNodesCapacitiesSum = -1;
        for(int nodeIndex = 0; nodeIndex < nodesArray.length; nodeIndex++)
            allNodesCapacitiesSum += ((Long)metrics.get(nodesArray[nodeIndex])).longValue();

        // For each node, compute its native (outgoing) connections
        int numberOutgoingConns = DanteConf.getPresentSimConf().outConns();
        for(int nodeIndex = 0; nodeIndex < nodesArray.length; nodeIndex++){
            
            DanteNode node = nodesArray[nodeIndex];
            
            nodesCopy.remove(node);
            
            // Choosing new neighbors
            DanteNode[] newNeighbors = new DanteNode[numberOutgoingConns];
            long partialCapsSum = (long)(allNodesCapacitiesSum - ((Long)metrics.get(nodesArray[nodeIndex])).longValue());
            for(int connCounter = 0; connCounter < newNeighbors.length; connCounter++){                
                DanteNode candidate = chooseNode(partialCapsSum, nodesCopy, metrics);                
                partialCapsSum -= ((Long)metrics.get(nodesArray[nodeIndex])).longValue();
                nodesCopy.remove(candidate);
                newNeighbors[connCounter] = candidate;
            }
            
            // Connecting to them, and restoring nodesCopy
            for(int connCounter = 0; connCounter < newNeighbors.length; connCounter++){
                node.connectTo(newNeighbors[connCounter]);
                nodesCopy.add(newNeighbors[connCounter]);
            }
            
            nodesCopy.add(node);
            
        }

    }
    
    protected DanteNode chooseNode(long allCapacitiesSum, SortedArrayList nodes, HashMap metrics){
        
        if(allCapacitiesSum <= 0)
            throw new Error("Sum of capacities can not be lesser than or equal to cero");
        
        long randomValue = RandomGenerator.randomLongValue(allCapacitiesSum) + 1;
        long sumOfCaps = 0;
        DanteNode[] nodesArray = (DanteNode[])nodes.toArray(new DanteNode[0]);
        for(int nodeIndex = 0; nodeIndex < nodesArray.length; nodeIndex++){
            DanteNode node = nodesArray[nodeIndex];
            sumOfCaps += ((Long)metrics.get(nodesArray[nodeIndex])).longValue();
            if(sumOfCaps >= randomValue)
                return node;
        }
        
        throw new Error("Error trying to choose node at random");
    }

}
