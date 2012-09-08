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

public class OldExponentialDistribution {
    
    protected static Random random = new Random();
    
    protected double lambda = 0.0; // 1/mean
    protected int cutoff = -1; // cutoff
    protected double[] distribution = null;
    
    public static void main(String args[]){
        
        OldExponentialDistribution exp1 = new OldExponentialDistribution(1000,10000);
        OldExponentialDistribution exp2 = new OldExponentialDistribution(1000,10000);
        
        double total1 = 0.0;
        double total2 = 0.0;
        
        int iters = 100000;
        
        for(int index = 0 ; index < iters; index++){
            int val1 = exp1.nextInt();
            int val2 = exp2.nextInt();
            total1 += val1;
            total2 += val2;
            System.out.println("\t" + val1 + " - " + val2);
        }
        System.out.println("\tMean 1: " + total1/iters + " - Mean 2: " + total2/iters);
    }
    
    public OldExponentialDistribution(long mean, int cutoff){
        this((double)mean, cutoff);
    }
    
    public OldExponentialDistribution(double mean, int cutoff){
        
        this.lambda = 1.0/mean;
        this.cutoff = cutoff;
        
        distribution = new double[cutoff];
        
        //computeDistribution();
        
    }
    
    
    public int nextInt(){
        
        double rand = random.nextDouble();
        
        while(rand == 0.0d)
            rand = random.nextDouble();
        
        long longValue = Math.round(-(Math.log(-rand + 1)/lambda));
        
        while(longValue > cutoff){
            rand = random.nextDouble();
            while(rand == 0.0d)
                rand = random.nextDouble();
            longValue = Math.round(-(Math.log(-rand + 1)/lambda));            
        }
        
        /*int intValue = 0; 
        for(int index = 0; index < distribution.length; index++){
            if(rand < distribution[index]){
                intValue = index + 1;
                break;
            }            
        }
        
        if(intValue == 0) intValue = distribution.length - 1;*/

        return (int)longValue;
    }
    
    protected void computeDistribution(){
        
        // Cumulative Distribution Function = 1 - exp (-x * lambda)
        for(int index = 0; index < distribution.length; index++){
            distribution[index] = 1.0 - Math.exp( - (index+1) * lambda);
            System.out.println("\tx = " + (index+1) + " CDF: " + distribution[index]);
            if(distribution[index] >= 1.0){
                for(int remainIndex = index; remainIndex < distribution.length; remainIndex++)
                    distribution[remainIndex] = 1.0;
                break;                
            }
        }
        
        System.out.println(distributionAsString());
    }
    
    protected String distributionAsString(){
        
        String distrAsString = "[ ";
        
        if(distribution != null)
            for(int index = 0; index < distribution.length; index++)
                distrAsString = distrAsString.concat(distribution[index] + "\n");
        
        distrAsString.concat("]");
        
        return distrAsString;
        
    }
    
    
}
