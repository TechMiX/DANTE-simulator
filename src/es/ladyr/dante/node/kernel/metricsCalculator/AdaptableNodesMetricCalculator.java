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

import es.ladyr.dante.externalEvents.EventForwarder;
import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.simulator.Simulator;

// k ^ ( 2 - (load/processingRate) )

//OBSERVATIONS FROM EXPERIMENTS.
// This kernel makes the network to easily form an star-like, yet it is very likely that
// the best connected nodes are not the mos capable.
public class AdaptableNodesMetricCalculator implements NodesMetricCalculator {
    
    public HashMap allNodesMetrics = new HashMap();
    public HashMap metricLastComputationTime = new HashMap();
    
    public long[] computeNodesMetrics(DanteNode nodes[]){
        
        long[] nodesMetrics = new long[nodes.length];
        
        for(int nodeIndex = 0; nodeIndex < nodesMetrics.length; nodeIndex++)
            nodesMetrics[nodeIndex] = computeNodeMetric(nodes[nodeIndex]);
        
        return nodesMetrics;
    }
    
    protected long computeNodeMetric(DanteNode node){
        
        long lastTimeMetricWasComputer = -1;
        long previousValue = -1;

        boolean metricComputedBefore = metricLastComputationTime.containsKey(node);
        if(metricComputedBefore){
         
            lastTimeMetricWasComputer =  ((Long)metricLastComputationTime.get(node)).longValue();
            previousValue = ((Long)allNodesMetrics.get(node)).longValue();
            
            if(lastTimeMetricWasComputer == Simulator.simulator().getSimulationTime())                
                return previousValue;
        }
        
        long metric = 0;
        double congestion = node.getNodeStats().nodeCongestion();
    
        if(congestion < 1.0){
            
            // Node not congested
            // Metric = degree ^ [ maxExponent * ( 1 - congestion^Alpha ) ]
            
            double congestionPowToAlpha = 0.0;

            // If Alpha is 0.0, then the classic formula applies (degree ^ maxExponent), so congestionPowToAlpha is let to 0
            
            if(DanteConf.getPresentSimConf().alpha() != 0.0){     
                
                // Alpha == 1 or Alpha = 2 is used many times. It's worth to take this case into account.
                if(DanteConf.getPresentSimConf().alpha() == 1.0)
                    congestionPowToAlpha = congestion;                  
                else if(DanteConf.getPresentSimConf().alpha() == 2.0)
                    congestionPowToAlpha = congestion * congestion;
                else
                    congestionPowToAlpha = Math.pow(congestion, DanteConf.getPresentSimConf().alpha());      
                
                
            }   
            
            // node degree
            int nodeDegree = node.degree();
            nodeDegree = (nodeDegree == 0) ? 1 : nodeDegree;
            
            double exponent = DanteConf.getPresentSimConf().maxKernelExp() * (1.0 - congestionPowToAlpha);
            
            // In many configurations, exponent == 2 or exponent == 3. So it is worth to take this case into account. 
            if(exponent == 2.0)
                metric = nodeDegree * nodeDegree;
            else if(exponent == 3.0)
                metric = nodeDegree * nodeDegree * nodeDegree;
            else            
                metric = Math.round(Math.pow(nodeDegree, exponent));
                    
        } else {

            // Node congested
            // Metric = degree^0 = 1
        
            metric = 1;
        }
        
        boolean metricChanged = (!metricComputedBefore || previousValue != metric);
        
        allNodesMetrics.put(node, new Long(metric));
        metricLastComputationTime.put(node, new Long(Simulator.simulator().getSimulationTime()));
        
        if( DanteConf.getPresentSimConf().generateEvents() && metricChanged && node.nodeIsActive() )
            EventForwarder.getInstance().nodeMetricChanged(node, metric);        
        
        return metric;
    }

}
