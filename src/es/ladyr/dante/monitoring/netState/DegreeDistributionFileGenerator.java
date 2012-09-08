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

package es.ladyr.dante.monitoring.netState;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import es.ladyr.dante.node.DanteNode;
import es.ladyr.util.dataStructs.SortedArrayList;

public class DegreeDistributionFileGenerator {
    
    public static void generateDegreeDistributionFile(String fileName, String dirName){
        
        // Reading all nodes degrees
        HashMap degreesMap = new HashMap();
        
        DanteNode[] nodesArray = (DanteNode[])DanteNode.allActiveNodesInSystem().toArray(new DanteNode[0]);
        for(int nodeIndex = 0; nodeIndex < nodesArray.length; nodeIndex++){
            Integer nodeDegree = new Integer(nodesArray[nodeIndex].degree());
            Integer degreeCounter = (Integer)degreesMap.get(nodeDegree);
            if(degreeCounter == null)
                degreesMap.put(nodeDegree, new Integer(1));
            else
                degreesMap.put(nodeDegree, new Integer(degreeCounter.intValue() + 1));
        } 

        // Creating directory, just in case it does not exist yet
        File dir = new File(dirName);
        dir.mkdirs();
        
        File degreeDistrFile = new File(dirName, fileName);      
        
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(degreeDistrFile);
        } catch (FileNotFoundException exception) {
            throw new Error("Could not open file: " + degreeDistrFile.getAbsolutePath(), exception);
        }
        
        // Writing file
        Integer[] degreesArray = (Integer[])(new SortedArrayList(degreesMap.keySet())).toArray(new Integer[0]);
        for(int degreeIndex = degreesArray.length - 1; degreeIndex >= 0; degreeIndex--){
            String degreeLine = degreesArray[degreeIndex].intValue() + "\t" + ((Integer)degreesMap.get(degreesArray[degreeIndex])).intValue() + "\n";
            try {
                fileOutputStream.write(degreeLine.getBytes());
            } catch (IOException exception) {
                throw new Error("Error when writing in file " + degreeDistrFile.getAbsolutePath(), exception);
            }
            
        }
        
        try {
            fileOutputStream.close();
        } catch (IOException exception) {
            throw new Error("Error when closing file " + degreeDistrFile.getAbsolutePath(), exception);
        }
        
        
    }

}
