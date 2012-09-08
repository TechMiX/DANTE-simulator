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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class Params {

    protected static int numberOfExps = 0;
    
    public static int getNumberOfExps(){
        return numberOfExps;
    }
    
    public static void setNumberOfExps(int numberOfExps){
        Params.numberOfExps = numberOfExps;
    }
    
    
    private Properties expParams = null;
    private int expIndex = 0;
    
    public Params(int experimentIndex, Properties expParams){
        this.expIndex = experimentIndex;
        this.expParams = expParams;        
    }
    
    public int getExpIndex(){
        return expIndex;
    }
    
    public String expIndexAsString(){
        
        String maxIndexAsString = numberOfExps + "";
        String indexAsString = expIndex + "";
        
        while(indexAsString.length() < maxIndexAsString.length())
            indexAsString = "0" + indexAsString;
        
        return indexAsString;
    }
    
    public String getParam(String paramName){

        String value = expParams.getProperty(paramName);
        
        if(value == null)
            throw new Error("Could not find param with name " + paramName);
        
        return value;
    }
    
    public Class getClassParam(String paramName) throws ClassNotFoundException {
        
        String value = getParam(paramName);
        
        return Class.forName(value);
    }
    
    public boolean getBooleanParam(String paramName){
        
        String value = getParam(paramName);
        
        return Boolean.valueOf(value).booleanValue();
        
    }
    
    public int getIntParam(String paramName){
        
        String value = getParam(paramName);
        
        int intValue = 0;
        
        try {
            intValue = Integer.parseInt(value);            
        } catch(NumberFormatException exception){
            throw new Error("Param " + paramName + " value (" + value + ") can not be parsed to int");
        }
        
        return intValue;
            
    }
    
    public long getLongParam(String paramName){
        
        String value = getParam(paramName);
        
        long longValue = 0;
        
        try {
            longValue = Long.parseLong(value);            
        } catch(NumberFormatException exception){
            throw new Error("Param " + paramName + " value (" + value + ") can not be parsed to long");
        }
        
        return longValue;
        
    }
    
    public float getFloatParam(String paramName){
        
        String value = getParam(paramName);
        
        float floatValue = 0;
        
        try {
            floatValue = Float.parseFloat(value);            
        } catch(NumberFormatException exception){
            throw new Error("Param " + paramName + " value (" + value + ") can not be parsed to float");
        }
        
        return floatValue;
        
    }
    
    public double getDoubleParam(String paramName){
        
        String value = getParam(paramName);
        
        double doubleValue = 0.0;
        
        try {
            doubleValue = Double.parseDouble(value);            
        } catch(NumberFormatException exception){
            throw new Error("Param " + paramName + " value (" + value + ") can not be parsed to double");
        }
        
        return doubleValue;
                
    }
    
    public void storeParamsInFile(File paramsFile){
        PrintWriter paramsWriter;
        try {
            paramsWriter = new PrintWriter(paramsFile);
        } catch (FileNotFoundException exception) {
            System.out.println("Could not open file " + paramsFile.getAbsolutePath() + ", FileNotFoundException caught with message: " + exception.getMessage());
            return;
        }
        storeParamsInFile(paramsWriter);
        paramsWriter.close();
    }
    
    public void storeParamsInFile(PrintWriter paramsFile) {
        
        paramsFile.println("## PARAMETERS OF EXPERIMENT " + expIndex);
        
        String[] paramsNames = (String[])(new ArrayList(expParams.keySet()).toArray(new String[0]));
        Arrays.sort(paramsNames);
        
        for(int resultIndex = 0; resultIndex < paramsNames.length; resultIndex++)
            paramsFile.println(paramsNames[resultIndex] + " = " + expParams.getProperty(paramsNames[resultIndex]));
        
    }

}
