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

import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.node.NodesComparatorByCapacity;
import es.ladyr.util.dataStructs.SortedArrayList;


public class NodesStateFileGenerator {
    
    public static void saveNodesState(SortedArrayList nodes, String fileName, String dirName){
        
        // Creating directory, just in case it does not exist yet
        File dir = new File(dirName);
        dir.mkdirs();

        File statsFile = new File(dirName, fileName);

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(statsFile);
        } catch (FileNotFoundException exception) {
            throw new Error("Could not open file: " + statsFile.getAbsolutePath(), exception);
        }
        
        // Writing file header
        try {
            fileOutputStream.write(new String("# Nodes state file\n").getBytes());
            fileOutputStream.write(new String("# Node\tCapa\tDegree\tLoad\t\tServRate\t\tQueueS\n").getBytes());
        } catch (IOException exception) {
            throw new Error("IOException caught when writing on file " + statsFile.getAbsolutePath(), exception);
        }
        
        // Writing nodes state
        // Ordering by nodes capacities
        SortedArrayList nodesCopy = new SortedArrayList(new NodesComparatorByCapacity(), nodes);
        
        DanteNode[] nodesArray = (DanteNode[])nodesCopy.toArray(new DanteNode[0]);
        for(int nodeIndex = 0; nodeIndex < nodesArray.length; nodeIndex++){
            DanteNode node = nodesArray[nodeIndex];
            try {
                fileOutputStream.write(new String(node.id() + "\t" + node.getCapacity() + "\t" + node.degree() + "\t" + node.getNodeStats().nodeArrivalRate() +
                                                  "\t" + node.getNodeStats().nodeServiceRate() +
                                                  "\t" + node.getMessagesQueuesSet().getNumberPendingSearchMessages() + "\n").getBytes());
            } catch (IOException exception) {
                throw new Error("IOException caught when writing on file " + statsFile.getAbsolutePath(), exception);
            }
        }
        
        // Closing file
        try {
            fileOutputStream.close();
        } catch (IOException exception) {
            throw new Error("IOException caught when trying to close file " + statsFile.getAbsolutePath(), exception);
        }
    }

}