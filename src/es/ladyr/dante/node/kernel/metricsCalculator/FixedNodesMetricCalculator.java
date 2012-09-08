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

package es.ladyr.dante.node.kernel.metricsCalculator;

import java.util.HashMap;
import java.util.Map;

import es.ladyr.dante.externalEvents.EventForwarder;
import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.simulator.Simulator;

public class FixedNodesMetricCalculator implements NodesMetricCalculator {
    
    public HashMap allNodesMetrics = new HashMap();
    public HashMap metricLastComputationTime = new HashMap();
    
    public long[] computeNodesMetrics(DanteNode[] nodes){
        
        long[] nodesMetrics = new long[nodes.length];
        
        for(int nodeIndex = 0; nodeIndex < nodesMetrics.length; nodeIndex++)
            nodesMetrics[nodeIndex] = computeNodeMetric(nodes[nodeIndex]);
        
        return nodesMetrics;
    }
    
    protected long computeNodeMetric(DanteNode node){
        
        long lastTimeMetricWasComputer = -1;
        if(metricLastComputationTime.containsKey(node))
            lastTimeMetricWasComputer =  ((Long)metricLastComputationTime.get(node)).longValue();
        
        if(lastTimeMetricWasComputer == Simulator.simulator().getSimulationTime())
            return ((Long)allNodesMetrics.get(node)).longValue();
        
        long metric = -1;
        
        if(!node.getNodeStats().nodeIsCongested()){

            // Node not congested
            // Metric = degree ^ maxExponent
            
            int nodeDegree = (node.degree() == 0 ? 1 : node.degree());

            if(DanteConf.getPresentSimConf().maxKernelExp() != 1){
            
                if(DanteConf.getPresentSimConf().maxKernelExp() == 2)
                    metric = nodeDegree * nodeDegree;
                else if (DanteConf.getPresentSimConf().maxKernelExp() == 3)
                    metric = nodeDegree * nodeDegree * nodeDegree;
                else
                    metric = (long)Math.pow(nodeDegree, DanteConf.getPresentSimConf().maxKernelExp());                
            
            }
                
        } else {
            
            // Node congested
            // Metric = degree^0 = 1
            metric = 1; 
            
        }   
        
        allNodesMetrics.put(node, new Long(metric));
        metricLastComputationTime.put(node, new Long(Simulator.simulator().getSimulationTime()));
        
        if(DanteConf.getPresentSimConf().generateEvents())
            EventForwarder.getInstance().nodeMetricChanged(node, metric);
        
        return metric;
        
    }
    
    public Map allNodesMetrics(){
        return allNodesMetrics;
    }

}
