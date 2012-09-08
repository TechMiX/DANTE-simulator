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

package es.ladyr.dante.node.ttl;

import java.util.HashMap;

import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.run.DanteConf;

public class AdaptableTTLStimator implements ResourceSearchsTTLStimator {
    
    protected final static int INITIAL_DEVIATION = 100;
    protected final static float ALFA = ((float)7)/8;
    
    HashMap nodesDistanceStimators = new HashMap();
    HashMap nodesDeviations = new HashMap();

    public int ttl(DanteNode node) {
        
        // Using a stimation similar to the one used in TCP for the round-trip time and time outs. In this case, we stimate hops to
        // find some resource, instead of time.
        // See 'Computer Networks' from Andrew S. Tanembaum, page 551, for more info.
        
        // Basically, in TCP the RTT is constantly updated using RTT = alfa * RTT + (1 - alfa) * M .
        // M is the time it took to get the last acknowledgement, and alfa is a smoothing factor (tipically 7/8)        
        // In our case, the RTT is the number of hops, and M is the mean number of hops that took to find the last resource found.
        // But we need to get the stimator of the TTL, as TCP needs to stimate the timeout. Initially, 
        // Timeout = beta * RTT
        // By Jacobson algorithm, that beta is (roughly) made proportional to the standard deviation of the last M values. This is,
        // a large variance means a large beta. Jacobson also suggested to use the mean deviation as a stimator of the standar deviation.
        // This deviation D value is computed as:
        // D = alfa * D + (1 * alfa) |RTT - M|
        // Where alfa caa have the same value than before, 7/8. Finally, the timeout is then computed as:
        // Timeout = RTT + 4 * D
        
        // In our case, the Timeout is the TTL stimator. The RTT is an stimation to the distance, using
        // a random walk, to the resource looked for. We call it DistanceStimator. alfa is set to 7/8. M is taken from
        // each node 
        
        // Getting previous distance stimator
        Integer distanceStimatorValue = (Integer)nodesDistanceStimators.get(node);
        int distanceStimator = (distanceStimatorValue != null) ? distanceStimatorValue.intValue() : DanteConf.getPresentSimConf().searchesTTL();
        
        // Computing and setting new value of the distance stimator
        int m = node.getSearchesStats().lastSearchTTL();
        if(m >= 0)
            distanceStimator = Math.round( (ALFA * distanceStimator) + (1 - ALFA) * m );
        nodesDistanceStimators.put(node, new Integer(distanceStimator));
        
        // Now, getting the deviation value
        Integer deviationValue = (Integer)nodesDeviations.get(node);
        int deviation = (deviationValue != null) ? deviationValue.intValue() : INITIAL_DEVIATION;
        
        // Computing and setting new value for the deviation
        if(m >= 0)
            deviation = Math.round( (ALFA * deviation) + (1 - ALFA) * Math.abs(distanceStimator - m));
        nodesDeviations.put(node, new Integer(deviation));
        
        // Finally, computing and returning TTL
        return distanceStimator + 4 * deviation;
    }

}
