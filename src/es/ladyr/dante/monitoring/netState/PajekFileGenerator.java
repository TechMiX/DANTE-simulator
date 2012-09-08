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
import java.util.Map;

import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.node.NodesComparatorByCapacity;
import es.ladyr.util.dataStructs.SortedArrayList;


public class PajekFileGenerator {
    
    protected static String DEFAULT_PATH = "./";
    protected static String DEFAULT_FILE = "dnt_net.NET";
    
    public static void generatePajekFile(SortedArrayList nodes, String fileNameParam, String dirNameParam){        
        
        // Setting dir and file names
        String fileName = (fileNameParam == null ? DEFAULT_FILE : fileNameParam);
        String dirName = (dirNameParam == null ? DEFAULT_PATH : dirNameParam);        

        // Creating dir, just in case it does not exist yet
        File dir = new File(dirName);
        dir.mkdirs();
        
        File graphFile = new File(dirName, fileName);        
        
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(graphFile);
        } catch (FileNotFoundException exception) {
            throw new Error("Could not open file: " + graphFile.getAbsolutePath(), exception);
        }
        
        // Writing file
        
        try{
            
            // Giving 'vertex id' to each node. We do not use the own node id as
            // the set of id's could not be complete (gaps could appear)
            SortedArrayList nodesCopy = new SortedArrayList(new NodesComparatorByCapacity(), nodes);
            DanteNode[] nodesArray = (DanteNode[])nodesCopy.toArray(new DanteNode[0]);
            Map vertexIdsInFile = new HashMap();
            for(int nodeIndex = 0; nodeIndex < nodesArray.length; nodeIndex++)
                vertexIdsInFile.put(nodesArray[nodeIndex], new Integer(nodeIndex + 1));

            
            // Writing nodes capacities and bandwidths. Must be saved as pajek file comments
            // as there is no other way to store this data using pajek format. One line per node.
            fileOutputStream.write(("/** *CapacitiesBandwidths    " + nodes.size() + " **/\r\n").getBytes());
            for(int nodeIndex = 0; nodeIndex < nodesArray.length; nodeIndex++){
                DanteNode node = nodesArray[nodeIndex];
                Integer vertexId = (Integer)vertexIdsInFile.get(node);
                String nodeInfo = "/** " + vertexId + "    " + ((double)node.getCapacity()) + "    " + ((double)node.getBandwidth()) + " **/\r\n";
                fileOutputStream.write(nodeInfo.getBytes());                
            }
            
            // Vertices
            String verticesNumber = "*Vertices     " + nodes.size() + "\r\n";
            fileOutputStream.write(verticesNumber.getBytes());
            for(int nodeIndex = 0; nodeIndex < nodesArray.length; nodeIndex++){
                DanteNode node = nodesArray[nodeIndex];
                Integer vertexId = (Integer)vertexIdsInFile.get(node);
                String nodeLabel = node.id() + "";
                String vertex = "    " + vertexId + " \"" + nodeLabel + "\"\r\n";
                fileOutputStream.write(vertex.getBytes());
            }   
            
            // Arrows
            String arrowsHeader = "*Arcs\r\n";
            fileOutputStream.write(arrowsHeader.getBytes());
            
            for(int nodeIndex = 0; nodeIndex < nodesArray.length; nodeIndex++){
                DanteNode node = nodesArray[nodeIndex];
                Integer vertexId = (Integer)vertexIdsInFile.get(node);
                DanteNode[] outgoingConnections = (DanteNode[])((SortedArrayList)node.outgoingConnections()).toArray(new DanteNode[0]);
                for(int neighborIndex = 0; neighborIndex < outgoingConnections.length; neighborIndex++){
                    DanteNode neighbor = outgoingConnections[neighborIndex];
                    Integer neighborId = (Integer)vertexIdsInFile.get(neighbor); 
                    String arrow = "    " + vertexId + "    " + neighborId + "    1\r\n";
                    fileOutputStream.write(arrow.getBytes());        
                }
            }

            // Edges
            String edgesHeader = "*Edges\r\n";
            fileOutputStream.write(edgesHeader.getBytes());
            
        } catch(IOException exception) {
            throw new Error("Error when writing in file " + graphFile.getAbsolutePath(), exception);
        }
        
        try {
            fileOutputStream.close();
        } catch (IOException exception) {
            throw new Error("Error when closing file " + graphFile.getAbsolutePath(), exception);
        }
        
    }

}

