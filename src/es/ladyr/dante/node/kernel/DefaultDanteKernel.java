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

package es.ladyr.dante.node.kernel;

import java.util.ArrayList;
import java.util.List;

import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.util.dataStructs.SortedArrayList;
import es.ladyr.util.math.RandomGenerator;


public class DefaultDanteKernel implements DanteKernel {

    public List nodesToConnectTo(SortedArrayList candidates, DanteNode node) {
        
        SortedArrayList candidatesCopy = new SortedArrayList(candidates);
        
        candidatesCopy.remove(node);
        
        if(candidatesCopy.isEmpty())
            return candidatesCopy;
        
        // Computing candidates metrics
        DanteNode[] candidatesArray = (DanteNode[])candidatesCopy.toArray(new DanteNode[0]);
        long[] nodesMetrics = DanteConf.getPresentSimConf().metricCal().computeNodesMetrics(candidatesArray);

        // Computing the sum of all candidates metrics        
        long allCandidatesMetricsSum = 0;
        for(int nodeIndex = 0; nodeIndex < nodesMetrics.length; nodeIndex++)            
            allCandidatesMetricsSum += nodesMetrics[nodeIndex];
        
        if(allCandidatesMetricsSum < 0)
            throw new Error("All metrics sum is negative!! " + allCandidatesMetricsSum);
        
        ArrayList newNeighbors = new ArrayList();
        while(newNeighbors.size() < candidatesArray.length  && newNeighbors.size() < DanteConf.getPresentSimConf().connsChangedAtRec()){

            int candidateIndex = chooseRandomNode(allCandidatesMetricsSum, nodesMetrics);
            newNeighbors.add(candidatesArray[candidateIndex]);
            allCandidatesMetricsSum -= nodesMetrics[candidateIndex];
            nodesMetrics[candidateIndex] = 0;  // This candidate will not be chosen again
        }
        
        return newNeighbors;
    
    }

    public List nodesToConnectTo(DanteNode node) {        
        return nodesToConnectTo(DanteNode.allActiveNodesInSystem(), node);   
    }
    
    private int chooseRandomNode(long allMetricsSum, long[] nodesMetrics){
        
        long randomValue = RandomGenerator.randomLongValue(allMetricsSum) + 1;
        
        // Getting node in list corresponding to that random value
        int candidateIndex = 0;
        long sumOfAllValuesChecked = nodesMetrics[0];
        while(sumOfAllValuesChecked < randomValue){
            candidateIndex++;
            sumOfAllValuesChecked += nodesMetrics[candidateIndex];  
        }
        
        return candidateIndex;
        
    }

}
