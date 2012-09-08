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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;


public class ExpParamsBuilder {
    
    private final static String DEFAULT_VALUES_SEPARATOR ="[;\\s]+";
    
    public static Params[] buildExperiments(File allExpsConfigFile, String valuesSeparator) throws IOException {
        Params[] expParams = null;
        try {
            expParams = buildExperiments(allExpsConfigFile, valuesSeparator, null);
        } catch (PropertyNotInSet exception) {
            throw new Error("This should never happen!", exception);
        }        
        
        return expParams;
    }
    
    public static Params[] buildExperiments(File allExpsConfigFile, String valuesSeparator, String[] propsToGroupBy) throws IOException, PropertyNotInSet {
        
        Properties[] experimentParams = ExpParamsBuilder.buildAllConfigurations(allExpsConfigFile, valuesSeparator);
        
        if(propsToGroupBy != null)
            experimentParams = groupByProperties(experimentParams, propsToGroupBy);
        
        Params.setNumberOfExps(experimentParams.length);
        
        Params[] expParams = new Params[experimentParams.length];
        
        for(int index = 0; index < experimentParams.length; index++)
            expParams[index] = new Params(index + 1, experimentParams[index]);
        
        return expParams;
        
    }
    
    public static void main(String args[]){
        
        Properties props1 = new Properties(); Properties props2 = new Properties();
        Properties props3 = new Properties(); Properties props4 = new Properties();
        Properties props5 = new Properties(); Properties props6 = new Properties();
        Properties props7 = new Properties(); Properties props8 = new Properties();
        Properties props9 = new Properties(); Properties props10 = new Properties();
        Properties props11 = new Properties(); Properties props12 = new Properties();
        
        Properties[] propsSets = new Properties[] {props1,props2,props3,props4,props5,props6,props7,props8,props9,props10,props11,props12};
        
        props1.setProperty("PropA", "VPA1");
        props1.setProperty("PropB", "VPB1");
        props1.setProperty("PropC", "VPC1");
        
        props2.setProperty("PropA", "VPA2");
        props2.setProperty("PropB", "VPB1");
        props2.setProperty("PropC", "VPC1");
        
        props3.setProperty("PropA", "VPA3");
        props3.setProperty("PropB", "VPB1");
        props3.setProperty("PropC", "VPC1");
        
        props4.setProperty("PropA", "VPA1");
        props4.setProperty("PropB", "VPB2");
        props4.setProperty("PropC", "VPC1");
        
        props5.setProperty("PropA", "VPA2");
        props5.setProperty("PropB", "VPB2");
        props5.setProperty("PropC", "VPC1");
        
        props6.setProperty("PropA", "VPA3");
        props6.setProperty("PropB", "VPB2");
        props6.setProperty("PropC", "VPC1");
        
        props7.setProperty("PropA", "VPA1");
        props7.setProperty("PropB", "VPB1");
        props7.setProperty("PropC", "VPC2");
        
        props8.setProperty("PropA", "VPA2");
        props8.setProperty("PropB", "VPB1");
        props8.setProperty("PropC", "VPC2");
        
        props9.setProperty("PropA", "VPA3");
        props9.setProperty("PropB", "VPB1");
        props9.setProperty("PropC", "VPC2");
        
        props10.setProperty("PropA", "VPA1");
        props10.setProperty("PropB", "VPB2");
        props10.setProperty("PropC", "VPC2");
        
        props11.setProperty("PropA", "VPA2");
        props11.setProperty("PropB", "VPB2");
        props11.setProperty("PropC", "VPC2");
        
        props12.setProperty("PropA", "VPA3");
        props12.setProperty("PropB", "VPB2");
        props12.setProperty("PropC", "VPC2");
        
        Properties[] groupedPropsSets;
        try {
            groupedPropsSets = groupByProperties(propsSets, new String[] {"PropB", "PropA"});
        } catch (PropertyNotInSet exception) {
            System.out.println(exception.getMessage());
            return;
        }
        
        if(groupedPropsSets.length != propsSets.length)
            throw new Error("Grouped properties sets array has a different length (" + groupedPropsSets.length + ") than original (" + propsSets + ")");
        
        for(int propsSetIndex = 0; propsSetIndex < groupedPropsSets.length; propsSetIndex++){
            Properties propsSet = groupedPropsSets[propsSetIndex];
            System.out.println("Properties set " + (propsSetIndex + 1));
            propsSet.list(System.out);
        }
        
    }
    
    private static Properties[] groupByProperties(Properties[] propsSetsArray, String[] propsToGroupBy) throws PropertyNotInSet{
        
        if(propsSetsArray == null)
            throw new NullPointerException("The properties set to group can not be null");
        
        if(propsToGroupBy == null)
            throw new NullPointerException("The array of properties names to group properties set by can not be null");
        
        if(propsToGroupBy.length == 0)
            return propsSetsArray;

        Properties[][] groupedPropsSets = splitByProperty(propsSetsArray, propsToGroupBy[0]);

        String[] remainingPropsNames = new String[propsToGroupBy.length - 1];
        System.arraycopy(propsToGroupBy, 1, remainingPropsNames, 0, remainingPropsNames.length);
        
        for(int propsSetsGroupIndex = 0; propsSetsGroupIndex < groupedPropsSets.length; propsSetsGroupIndex++)
            groupedPropsSets[propsSetsGroupIndex] = groupByProperties(groupedPropsSets[propsSetsGroupIndex], remainingPropsNames);
        
        return joinPropsSetsArrays(groupedPropsSets);
    }
    
    private static Properties[][] splitByProperty(Properties[] propsSets, String propName) throws PropertyNotInSet{
        
        if(propsSets.length <= 1)
            return new Properties[][] {propsSets};
        
        // Gathering all different values of the property.
        // Checking also that all sets of properties contain that property.
        ArrayList propValues = new ArrayList();
        for(int propsSetIndex = 0; propsSetIndex < propsSets.length; propsSetIndex++){
            Object value = propsSets[propsSetIndex].getProperty(propName);
            if(value == null)
                throw new PropertyNotInSet("Property '" + propName + "' can not be found in properties set");
            if(!propValues.contains(value))
                propValues.add(value);
        }
        
        if(propValues.size() == 1)
            return new Properties[][] {propsSets};
        
        // Arranging in groups of property sets, by property values. For now, each group in a list.
        ArrayList[] propsSetsGroups = new ArrayList[propValues.size()];
        for(int valueIndex = 0; valueIndex < propValues.size(); valueIndex++){
            Object value = propValues.get(valueIndex);
            propsSetsGroups[valueIndex] = new ArrayList();
            // Looking for all properties sets with that value for the property to group by.
            for(int propsSetIndex = 0; propsSetIndex < propsSets.length; propsSetIndex++){
                if(propsSets[propsSetIndex].getProperty(propName) == value){
                    propsSetsGroups[valueIndex].add(propsSets[propsSetIndex]);
                }
            }
        }
        
        // Passing to arrays.
        Properties[][] groupedPropsSets = new Properties[propValues.size()][];
        for(int valueIndex = 0; valueIndex < propValues.size(); valueIndex++)
            groupedPropsSets[valueIndex] = (Properties[])propsSetsGroups[valueIndex].toArray(new Properties[0]);
        
        // Just checking...
        int totalProps = 0;
        for(int valueIndex = 0; valueIndex < propValues.size(); valueIndex++)
            totalProps += groupedPropsSets[valueIndex].length;        
        if(totalProps != propsSets.length)
            throw new Error("What??");
        
        return groupedPropsSets;
        
    }
    
    private static Properties[] joinPropsSetsArrays(Properties[][] propSetsArrays){
        
        int totalPropsSets = 0;
        for(int propsSetIndex = 0; propsSetIndex < propSetsArrays.length; propsSetIndex++)
            totalPropsSets += propSetsArrays[propsSetIndex].length;
        
        Properties[] joinedPropsSetsArray = new Properties[totalPropsSets];        

        int nextPropsSetPos = 0;
        for(int propsSetIndex = 0; propsSetIndex < propSetsArrays.length; propsSetIndex++){
            System.arraycopy(propSetsArrays[propsSetIndex], 0, joinedPropsSetsArray, nextPropsSetPos, propSetsArrays[propsSetIndex].length);
            nextPropsSetPos +=  propSetsArrays[propsSetIndex].length;
        }
        
        return joinedPropsSetsArray;
        
    }
    
    private static Properties[] buildAllConfigurations(File allExpsConfigFile, String valuesSeparator) throws IOException {
        
        String separator = (valuesSeparator != null) ? valuesSeparator : DEFAULT_VALUES_SEPARATOR;            
        
        System.out.println("Parsing values of config file");
        
        Properties params = new Properties();
        
        FileInputStream inputStream = new FileInputStream(allExpsConfigFile);
        
        params.load(inputStream);
        
        inputStream.close();
        
        String[] paramsKeys = (String[])(params.keySet().toArray(new String[0]));
        String[][] paramsValues = new String[paramsKeys.length][];
        
        // Parsing values...
        int numberOfExps = 1;
        for(int paramIndex = 0; paramIndex < paramsKeys.length; paramIndex++){
            String rawValue = params.getProperty((String)paramsKeys[paramIndex]);
            /*System.out.println("Property: " + (String)paramsKeys[paramIndex]);
            System.out.println("Value: " + rawValue);*/
            String[] parsedValues = rawValue.split(separator);
            /*System.out.print("After Split:");
            for(int kk = 0; kk<parsedValues.length; kk++)
                System.out.print(" " + parsedValues[kk]);
            System.out.println();
            System.out.println();*/
            paramsValues[paramIndex] = parsedValues;
            numberOfExps *= parsedValues.length;            
        }
        
        // Build experiments configurations
        
        // Building all values permutations (it could be done recursively, but I prefer this way...)
        Properties[] expsConfigurations = new Properties[numberOfExps];
        for(int expIndex = 0; expIndex < numberOfExps; expIndex++)
            expsConfigurations[expIndex] = new Properties();
        
        int repeatParamTimes = numberOfExps;
        for(int paramIndex = 0; paramIndex < paramsKeys.length; paramIndex++){
            
            // Key to store
            String paramKey = paramsKeys[paramIndex];
            // Values to store
            String[] paramValues = paramsValues[paramIndex];
            // How many repetitions of the parameter must be printed at each iteration?
            repeatParamTimes = repeatParamTimes / paramValues.length;
            // How many iterations?
            int iterations = numberOfExps / (repeatParamTimes * paramValues.length); 
            
            for(int iterIndex = 0; iterIndex < iterations; iterIndex++)
                for(int valueIndex = 0; valueIndex < paramValues.length; valueIndex++)  {
                    
                    String paramValue = paramValues[valueIndex];
                    
                    for(int repeatIndex = 0; repeatIndex < repeatParamTimes; repeatIndex++){                    
                        int experimentIndex = ( iterIndex * (repeatParamTimes * paramValues.length) ) + valueIndex * repeatParamTimes + repeatIndex;
                        expsConfigurations[experimentIndex].setProperty(paramKey, paramValue);                    
                    }
                    
                }
            
        }
        
        return expsConfigurations;
        
    }

}
