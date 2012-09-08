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

public class MathUtils {
    
    protected final static double sqrRootTwicePi = Math.sqrt(2 * Math.PI);
    
    public static double mean(int[] values){
        
        // Computing mean
        double sum = 0.0;
        for(int index = 0; index < values.length; index++)
            sum += values[index];
        
        return sum / values.length;        
    }
    
    public static double stdDeviation(int[] values, double mean){

        // Computing standar deviation
        double sum = 0.0;
        for(int index = 0; index < values.length; index++)
            sum += (mean - values[index]) * (mean - values[index]);
        
        return Math.sqrt(sum / values.length);
    }
    
    public static double stdDeviation(int[] values){
        
        double mean = mean(values);

        // Computing standar deviation
        double sum = 0.0;
        for(int index = 0; index < values.length; index++)
            sum += (mean - values[index]) * (mean - values[index]);
        
        return Math.sqrt(sum / values.length);
    }
    
    public static int min(int[] values){        
        int min = Integer.MAX_VALUE;
        for(int index = 0; index < values.length; index++)
            min = (values[index] < min) ? values[index] : min;
            
        return min;
    }
    
    public static int max(int[] values){
        int max = Integer.MIN_VALUE;
        for(int index = 0; index < values.length; index++)
            max = (values[index] > max) ? values[index] : max;
            
        return max;
    }
    
    public static double[] secondDegreeEqSols(double a, double b, double c){
        
        if(b*b - 4*a*c < 0)
            throw new Error("Second degree equation has no solution");
        
        double sqr = Math.sqrt(b*b - 4*a*c);
        
        double firstSol = (-b + sqr) / (2*a);
        double secondSol = (-b - sqr) / (2*a);
        
        return new double[]{firstSol, secondSol};
    }
    
    private static double[] FACTORIALS = {1.0, 2.0, 6.0, 24.0}; 
    public static double factorial(double n){
        
        if(n < 0.0)
            throw new Error("Can not compute the factorial of a negative number");
        
        if(n <= 1.0)
            return 1.0;
            
        if(n <= 4.0){
            int floor = (int)Math.floor(n);
            int ceil = (int)Math.ceil(n);
            if(floor == ceil)
                return FACTORIALS[floor-1];
            return ( (n - floor)*(FACTORIALS[ceil-1] - FACTORIALS[floor-1])/(ceil - floor) ) + floor;
        }
        
        // Stirling 
        double fact = Math.sqrt(2*Math.PI*n) * Math.pow(n/Math.E,n);
        
        return fact;
    }
    
    public static double binomialCoefficient(double n, double k){
        return factorial(n)/(factorial(k)*factorial(n-k));
    }
    
    public static double hyperGeometric(double N1, double N2, double n, double r){
        
        /*System.out.println("\nN1: " + N1);
        System.out.println("N2: " + N2);
        System.out.println("n: " + n);
        System.out.println("r: " + r);
        System.out.println("N1+N2-n: " + (N1+N2-n));
        System.out.println("N2-n: " + (N2-n));
        System.out.println();*/

        if(r == 0.0){            

            if(N2 == 0.0)
                throw new Error("What the hell?");
            
            double H = N1 + N2 - n;
            double S = N1 + N2;
            double U = N2 - n;
            
            double sumOfLogs = 0.0;
            
            double value = 0.0;
            
            value = Math.log(Math.sqrt(2.0*Math.PI*N2));
            //System.out.println(value);
            sumOfLogs += value;
            //System.out.println("Sum:" + sumOfLogs);
            value = N2*Math.log(N2) - N2;
            //System.out.println(value);
            sumOfLogs += value;
            //System.out.println("Sum:" + sumOfLogs);
            
            value = Math.log(Math.sqrt(2.0*Math.PI*H));
            //System.out.println(value);
            sumOfLogs += value;
            //System.out.println("Sum:" + sumOfLogs);
            value = H*Math.log(H) - H;
            //System.out.println(value);
            sumOfLogs += value;
            //System.out.println("Sum:" + sumOfLogs);
            
            value = -Math.log(Math.sqrt(2.0*Math.PI*S));
            //System.out.println(value);
            sumOfLogs += value;
            //System.out.println("Sum:" + sumOfLogs);
            value = -S*Math.log(S) + S;
            //System.out.println(value);
            sumOfLogs += value;
            //System.out.println("Sum:" + sumOfLogs);
            
            value = -Math.log(Math.sqrt(2.0*Math.PI*U));
            //System.out.println(value);
            sumOfLogs += value;
            //System.out.println("Sum:" + sumOfLogs);
            value = -U*Math.log(U) + U;
            //System.out.println(value);
            sumOfLogs += value;
            //System.out.println("Sum:" + sumOfLogs);
            
            return Math.exp(sumOfLogs);
            
            //return (factorial(N2) * factorial(N1 + N2 - n)) / (factorial(N1 + N2) * factorial(N2 - n));
        }
        
        return binomialCoefficient(N1, r) * binomialCoefficient(N2, n - r) / binomialCoefficient(N1 + N2, n);
    }

}
