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

package es.ladyr.util.math;

import java.util.Random;


public class ZipfDistribution {
    
    protected static Random random = new Random();
    
    protected double[] distribution = null;
    protected double tau = 0.0;
    protected int minCutoff = 0;
    
    public ZipfDistribution(double tau, int cutoff){
        this(tau, 0, cutoff);
    }
    
    public ZipfDistribution(double tau, int minCutoff, int cutoff){
        
        if(minCutoff > cutoff)
            throw new Error("Min cutoff can not be greater than cutoff in Zipf distribution");
        
        distribution = new double[cutoff - minCutoff + 1];
        
        this.tau = tau;
        this.minCutoff = minCutoff;
        
        computeDistribution();
        
    }
    
    public double[] distribution(){
        
        return distribution;
        
    }
    
    public int nextInt(){
        
        if(distribution.length > 1){
            
            double rand = random.nextDouble();

            for(int index = 0; index < distribution.length; index++){
                if(rand <= distribution[index]){
                    return index + minCutoff;
                }
            }
            
        }
        
        return distribution.length - 1;        
    }
    
    protected void computeDistribution(){
        
        double zeta = 0.0;
    
        // Compute distribution (distribution[index] corresponds to P(index+1), where P(k) = 1 / (k^tau)  )
        //distribution[0] = 1.0;
        for(int index = 1; index < distribution.length; index++){
            distribution[index] = 1.0 / Math.pow((double)index + minCutoff, tau);  // 1 / (k^tau)
            zeta += distribution[index];
            //System.out.println(distribution[index] + " " + zeta);
        }
        
        //System.out.println(" Distribution: " + distributionAsString());
        
        // Normalize and converting to a comulative distribution function (a probability distribution)
        distribution[0] = distribution[0] / zeta;
        for(int index = 1; index < distribution.length; index++){
            distribution[index] = distribution[index] / zeta;
            distribution[index] += distribution[index-1];
            //System.out.println(distribution[index]);
        }
        
        if(distribution[distribution.length - 1] > 1.0)
            distribution[distribution.length - 1] = 1.0;
        
        //System.out.println(" Normalized distribution: " + distributionAsString());
        
    }
    
    protected String distributionAsString(){
        
        String distrAsString = "[ ";
        
        if(distribution != null)
            for(int index = 0; index < distribution.length; index++)
                distrAsString = distrAsString.concat(distribution[index] + " ");
        
        distrAsString.concat("]");
        
        return distrAsString;
        
    }
    
}
