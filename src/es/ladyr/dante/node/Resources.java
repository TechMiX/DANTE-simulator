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

package es.ladyr.dante.node;

import es.ladyr.dante.run.DanteConf;
import es.ladyr.util.dataStructs.SortedArrayList;

public class Resources {
    
    protected static SortedArrayList resInSystem = new SortedArrayList();
    
    public static boolean resourceInSystem(int resource){
        
        if(difResInSystem < 0)
            throw new Error("Total number of different resources in system has not been computed yet");
        
        if(DanteConf.getPresentSimConf().resReplIsUniform())
            return ((resource >= 1) && (resource <= difResInSystem()));
        
        return resInSystem.contains(new Integer(resource));
    }
    
    // Number of -different- resources in system
    public static int difResInSystem = -1;
    
    public static void computeDifResInSystem(){
        
        if(DanteConf.getPresentSimConf().resReplIsUniform())
            difResInSystem = (int)(DanteConf.getPresentSimConf().resByNode() * 100 / DanteConf.getPresentSimConf().unifReplRate());
        else 
            difResInSystem = resInSystem.size();
    }
    
    public static int difResInSystem(){   
        
        if(difResInSystem < 0)
            throw new Error("Total number of different resources in system has not been computed yet");
        
        return difResInSystem;
    }
    
    protected int minRes = -1;
    protected int maxRes = -1;
    protected SortedArrayList resources = null;
    
    public Resources(int minRes, int maxRes){
        
        if(!DanteConf.getPresentSimConf().resReplIsUniform())
            throw new Error("If resource replication is not uniform, resources must be kept as a set");
        
        if(minRes > maxRes)
            throw new Error("Min resource " + minRes + " is greater than max resource");
        
        if((maxRes - minRes + 1) != DanteConf.getPresentSimConf().resByNode())
            throw new Error("The number of resources " + (maxRes - minRes + 1) + " differs of the one configured " + DanteConf.getPresentSimConf().resByNode());
        
        this.minRes = minRes;
        this.maxRes = maxRes;
        
    }
    
    public Resources(){

        if(DanteConf.getPresentSimConf().resReplIsUniform())
            throw new Error("When resource replication is uniform, resources min and max must be passed as param");
        
        resources = new SortedArrayList();
        
    }
    
    public boolean addResource(int res){

        if(DanteConf.getPresentSimConf().resReplIsUniform())
            throw new Error("When resource replication is uniform, resources are kept as a range of numbers");
        
        resInSystem.add(new Integer(res));
        
        return resources.add(new Integer(res));
        
    }
    
    public int numberOfRes(){        
        return(resources != null ? resources.size() : (maxRes - minRes + 1));        
    }
    
    public boolean containsResource(int res){
        return(resources != null ? resources.contains(new Integer(res)) : ((res >= minRes)&&(res <= maxRes)) );
    }

}
