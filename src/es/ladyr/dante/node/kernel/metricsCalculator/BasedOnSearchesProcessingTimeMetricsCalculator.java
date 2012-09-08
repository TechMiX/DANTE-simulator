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

//k ^ ( MAX_EXP / servTime ), where servTime is normalized, MAX_EXP should be 2.0

// OBSERVATIONS FROM EXPERIMENTS.
// It seems this kernel distributes well the connections, making the most capable nodes to have more
// connections. Yet, it seems also it does not adapts its topology at high loads (topology is almost the
// same for 1 query every 5 secs and 1 query every 2.5 secs).
public class BasedOnSearchesProcessingTimeMetricsCalculator implements NodesMetricCalculator {
    
    protected static final double MAX_EXPONENT = 2.0;

    public long[] computeNodesMetrics(DanteNode[] nodes) {        
        
        double[] serviceTimeMeans = new double[nodes.length];
        double minimumMean = Double.MAX_VALUE;
        for(int nodeIndex = 0; nodeIndex < nodes.length; nodeIndex++){
            serviceTimeMeans[nodeIndex] = nodes[nodeIndex].getNodeStats().meanServiceTimeInLastTimeUnit();
            if(serviceTimeMeans[nodeIndex] < minimumMean)
                minimumMean = serviceTimeMeans[nodeIndex];
        }
        
        // Computing metrics, using normalized means metric = k ^ ( MAX_EXP / servTime ), where servTime is normalized, MAX_EXP should be 2.0
        long[] metrics = new long[nodes.length];
        for(int nodeIndex = 0; nodeIndex < nodes.length; nodeIndex++)
            metrics[nodeIndex] = (long)Math.pow(nodes[nodeIndex].degree(), MAX_EXPONENT / (serviceTimeMeans[nodeIndex] / minimumMean));         
        
        return metrics;
    }
}
