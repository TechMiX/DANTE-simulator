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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;

public class ExponentialDistribution {
    
    private static Random staticRandom = new Random(RandomGenerator.randomLongValue(100000 * (System.currentTimeMillis()+1)));
    
    private Random random = new Random(RandomGenerator.randomLongValue(100000 * (System.currentTimeMillis()+1)));
    
    //private double cutoff = -1; // cutoff
    private double mean = 0.0;
    
    public ExponentialDistribution(long mean){
        this((double)mean);
    }
    
    public ExponentialDistribution(double mean){
        this.mean = mean;        
    }   
    
    public double mean(){
        return mean;
    }
    
    public static void main(String args[]) throws FileNotFoundException{
        
        long mean = 10000;
        long iters = 5000000;
        
        Hashtable counters = new Hashtable();
        
        ExponentialDistribution expDistribution = new ExponentialDistribution(mean);
        
        System.out.print(" Computing values... ");
        double valuesSum = 0.0;
        for(long i = 0; i < iters; i++){
            Long value = new Long(expDistribution.nextLong());
            valuesSum += value.longValue();
            Long counter = (Long)counters.get(value);
            if(counter == null)
                counter = new Long(1);
            else
                counter = new Long(counter.longValue() + 1);
            counters.put(value, counter);
        }
        System.out.println("done, average value is : " + (valuesSum/iters));

        File countersFile = new File("ExpDistributionFreqDistr.txt");
        System.out.print(" Writing frequencies to file " + countersFile.getPath() + "... ");
        PrintWriter writer = new PrintWriter(countersFile);
        
        writer.println("# Frequency distribution of a exponential distribution with mean " +  mean);
        
        Long[] values = (Long[])counters.keySet().toArray(new Long[0]);
        Arrays.sort(values);
        
        for(int i = 0; i < values.length; i++){
            writer.println(values[i] + "\t" + counters.get(values[i]));
        }
        
        writer.close();
        
        System.out.println("done");
    }
    
    /*public ExponentialDistribution(long mean, long cutoff){
        this((double)mean, (double)cutoff);
    }*/
    
    /*public ExponentialDistribution(double mean, double cutoff){        
        this.cutoff = cutoff;
        this.mean = mean;        
    } */   
    
    public long nextLong(){        
        return Math.round( Math.log(nextUniformValue()) * -mean );       
    }
    
    private double nextUniformValue() {
        double rand = random.nextDouble();
        while(rand == 0.0)
            rand = random.nextDouble();
        return rand;        
    }
    
    public static long nextLongForMean(double mean){
        return Math.round( Math.log(nextUniformValueStatic()) * -mean );
    }
    
    private static double nextUniformValueStatic(){
        double rand = staticRandom.nextDouble();
        while(rand == 0.0)
            rand = staticRandom.nextDouble();
        return rand;                
    }
    
}
