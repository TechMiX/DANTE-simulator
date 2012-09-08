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

package es.ladyr.util.exps;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

public class Results {
    
    private Properties expResults = new Properties();
    private int expIndex = -1;
    
    public Results(int expIndex){
        this.expIndex = expIndex;
    }
    
    public Set getResultsNames(){
        return expResults.keySet();
    }
    
    public void setResult(String resultName, String value){
        expResults.setProperty(resultName, value);
    }
    
    public void setResult(String resultName, int value){
        expResults.setProperty(resultName, (new Integer(value)).toString());
    }
    
    public void setResult(String resultName, boolean value){
        expResults.setProperty(resultName, Boolean.toString(value));
    }
    
    public void setResult(String resultName, long value){
        expResults.setProperty(resultName, (new Long(value)).toString());        
    }
    
    public void setResult(String resultName, float value){
        expResults.setProperty(resultName, (new Float(value)).toString());              
    }
    
    public void setResult(String resultName, double value){
        expResults.setProperty(resultName, (new Double(value)).toString());              
    }
    
    public String getResult(String resultName){

        String value = expResults.getProperty(resultName);
        
        if(value == null)
            throw new Error("Could not find result with name " + resultName);
        
        return value;
    }
    
    public int getIntResult(String resultName){
        
        String value = getResult(resultName);
        
        int intValue = 0;
        
        try {
            intValue = Integer.parseInt(value);            
        } catch(NumberFormatException exception){
            throw new Error("Result " + resultName + " value (" + value + ") can not be parsed to int");
        }
        
        return intValue;
            
    }
    
    public long getLongResult(String resultName){
        
        String value = getResult(resultName);
        
        long longValue = 0;
        
        try {
            longValue = Long.parseLong(value);            
        } catch(NumberFormatException exception){
            throw new Error("Result " + resultName + " value (" + value + ") can not be parsed to long");
        }
        
        return longValue;
        
    }
    
    public float getFloatResult(String resultName){
        
        String value = getResult(resultName);
        
        float floatValue = 0;
        
        try {
            floatValue = Float.parseFloat(value);            
        } catch(NumberFormatException exception){
            throw new Error("Result " + resultName + " value (" + value + ") can not be parsed to float");
        }
        
        return floatValue;
        
    }
    
    public double getDoubleResult(String resultName){
        
        String value = getResult(resultName);
        
        double doubleValue = 0;
        
        try {
            doubleValue = Double.parseDouble(value);            
        } catch(NumberFormatException exception){
            throw new Error("Result " + resultName + " value (" + value + ") can not be parsed to double");
        }
        
        return doubleValue;                
    }
    
    public void storeResultsInFile(PrintWriter resultsFile) {
        
        resultsFile.println("## RESULTS OF EXPERIMENT " + expIndex);
        
        String[] resultsNames = (String[])(new ArrayList(expResults.keySet()).toArray(new String[0]));
        Arrays.sort(resultsNames);
        
        for(int resultIndex = 0; resultIndex < resultsNames.length; resultIndex++)
            resultsFile.println(resultsNames[resultIndex] + " = " + expResults.getProperty(resultsNames[resultIndex]));
        
    }

}
