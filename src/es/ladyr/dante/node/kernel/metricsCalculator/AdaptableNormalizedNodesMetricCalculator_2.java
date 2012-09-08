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

import es.ladyr.dante.node.DanteNode;


public class AdaptableNormalizedNodesMetricCalculator_2 implements NodesMetricCalculator {
    
    // Here we compute each node attractiveness ALMOST (read next paragraph) as it is described in:
    // "A Topology Self-Adaptation Mechanism for Efficient Resource Location"
    // LNCS, ISBN: 3-540-68067-5. Volume 4330, pp. 660-671.
    
    // We apply the following change: we modify the way that the compute the normalized service time, t_i_{\text{norm}},
    // so it is computed as
    //                           t_i
    // t_i_{\text{norm}} = ----------------
    //                      t_{\text{max}}
    //
    // Originally, it was computed as:
    //                           t_{\text{max}} - t_i
    // t_i_{\text{norm}} = ---------------------------------
    //                      t_{\text{max}} - t_{\text{min}}
    
    public long[] computeNodesMetrics(DanteNode nodes[]){
        
        // Another possibility is to use the processing rate instead the capacity
        
        long[] nodesMetrics = new long[nodes.length];
        
        double maxTime = Double.MIN_VALUE;
        double[] nodesMeanServiceTimes = new double[nodes.length];
        double[] nodesCapacities = new double[nodes.length];
        double maxCapacity = Double.MIN_VALUE;
        
        for(int nodeIndex = 0; nodeIndex < nodesMetrics.length; nodeIndex++){
        
            nodesMeanServiceTimes[nodeIndex] = nodes[nodeIndex].getNodeStats().meanServiceTimeInLastTimeUnit();
            if(nodesMeanServiceTimes[nodeIndex] > maxTime)
                maxTime = nodesMeanServiceTimes[nodeIndex];
            
            nodesCapacities[nodeIndex] = nodes[nodeIndex].getCapacity();
            if(nodesCapacities[nodeIndex] > maxCapacity)
                maxCapacity = nodesCapacities[nodeIndex];
        }
        
        for(int nodeIndex = 0; nodeIndex < nodesMetrics.length; nodeIndex++){
            double normServiceTime = nodesMeanServiceTimes[nodeIndex] / maxTime;
            double normCapacity = nodesCapacities[nodeIndex] / maxCapacity;
            nodesMetrics[nodeIndex] = Math.round(Math.pow(nodes[nodeIndex].getNodeStats().nodeDegree(), 2.0 * normCapacity * (1.0-normServiceTime)));
            if(nodesMetrics[nodeIndex] < 1)
                nodesMetrics[nodeIndex] = 1;
        }
        
        return nodesMetrics;
    }

}
