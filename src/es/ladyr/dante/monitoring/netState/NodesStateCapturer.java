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

public class NodesStateCapturer implements EventHandler {
    
    protected static NodesStateCapturer _instance = new NodesStateCapturer();
    
    public static NodesStateCapturer getInstance(){
        return _instance;
    }
    
    public void captureActiveNodesStateNow(String fileName, String dirName){
        NodesStateFileGenerator.saveNodesState(DanteNode.allActiveNodesInSystem(), fileName, dirName);
    }
    
    public void captureActiveNodesStateAt(String fileName, String dirName, long time){
        Simulator.simulator().registerEvent(new NodesStateCaptureEvent(time, this, fileName, dirName));
    }

    public void processEvent(Event event) {
        
        if(event instanceof NodesStateCaptureEvent)
            throw new Error("Event is not an instance of NodesStateCaptureEvent");
        
        NodesStateCaptureEvent nodesStateCaptureEvent = (NodesStateCaptureEvent)event;
        
        NodesStateFileGenerator.saveNodesState(DanteNode.allActiveNodesInSystem(), nodesStateCaptureEvent.getFileName(), nodesStateCaptureEvent.getDirName());
        
    }
    
}

class NodesStateCaptureEvent extends Event {
    
    protected String fileName = null;
    protected String dirName = null;    

    public NodesStateCaptureEvent(long time, NodesStateCapturer nodesStateCapturer, String fileName, String dirName) {
        super(time, nodesStateCapturer, Event.MINIMUM_EVENT_PRIORITY);
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