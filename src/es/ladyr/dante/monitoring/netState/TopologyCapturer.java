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

import es.ladyr.dante.node.DanteNode;
import es.ladyr.simulator.Event;
import es.ladyr.simulator.EventHandler;
import es.ladyr.simulator.Simulator;

public class TopologyCapturer implements EventHandler {
    
    protected static TopologyCapturer _instance = new TopologyCapturer();
    
    public static TopologyCapturer getInstance(){
        return _instance;
    }
    
    public void captureTopologyNow(String fileName, String dirName){        
        // Calling to PajekFileGenerator
        PajekFileGenerator.generatePajekFile(DanteNode.allActiveNodesInSystem(), fileName, dirName);    
    }
    
    public void captureDegreeDistributionNow(String fileName, String dirName){
        // Calling to DegreeDistributionFileGenerator
        DegreeDistributionFileGenerator.generateDegreeDistributionFile(fileName, dirName);
    }
    
    public void captureTopologyAt(String fileName, String dirName, long time){
        Simulator.simulator().registerEvent(new CaptureTopologyEvent(time, this, fileName, dirName));
    }
    
    public void captureDegreeDistributionAt(String fileName, String dirName, long time){
        Simulator.simulator().registerEvent(new DegreeDistributionCaptureEvent(time, this, fileName, dirName));        
    }

    public void processEvent(Event event) {
        
        if(event instanceof CaptureTopologyEvent){
            CaptureTopologyEvent captureTopologyEvent = (CaptureTopologyEvent)event;
            PajekFileGenerator.generatePajekFile(DanteNode.allActiveNodesInSystem(), captureTopologyEvent.getFileName(), captureTopologyEvent.getDirName());
            return;
        }    
        
        if(event instanceof DegreeDistributionCaptureEvent){
            DegreeDistributionCaptureEvent degreeDistributionEvent = (DegreeDistributionCaptureEvent)event;
            DegreeDistributionFileGenerator.generateDegreeDistributionFile(degreeDistributionEvent.getFileName(), degreeDistributionEvent.getDirName());
            return;
        }

        throw new Error("Event is neither a CaptureTopologyEvent nor a DegreeDistributionCaptureEvent event!");
    }
}

class CaptureTopologyEvent extends Event {
    
    protected String fileName = null;
    protected String dirName = null;    

    public CaptureTopologyEvent(long time, TopologyCapturer topologyCapturer, String fileName, String dirName) {
        super(time, topologyCapturer, Event.MINIMUM_EVENT_PRIORITY);
        this.fileName = fileName;
        this.dirName = dirName;
    }
    
    public String getFileName(){
        return fileName;
    }
    
    public String getDirName(){
        return dirName;
    }
    
}

class DegreeDistributionCaptureEvent extends Event {
    
    protected String fileName = null;
    protected String dirName = null;    

    public DegreeDistributionCaptureEvent(long time, TopologyCapturer topologyCapturer, String fileName, String dirName) {
        super(time, topologyCapturer, Event.MINIMUM_EVENT_PRIORITY);
        this.fileName = fileName;
        this.dirName = dirName;
    }
    
    public String getFileName(){
        return fileName;
    }
    
    public String getDirName(){
        return dirName;
    }
    
}
