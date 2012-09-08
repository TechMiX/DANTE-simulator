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

import java.util.ArrayList;



public class PendingLocalSearchesQueue {
    
    protected ArrayList penLocSearches = new ArrayList();
    
    protected DanteNode thisNode = null;
    
    protected static int totalPendingLocalSearchesInSystem = 0;
    
    public static int totalPendingLocalPetsInSystem(){
        return totalPendingLocalSearchesInSystem;
    }
    
    public static void resetPendLocSearches(){
        totalPendingLocalSearchesInSystem = 0;
    }
    
    public PendingLocalSearchesQueue(DanteNode thisNode){
        this.thisNode = thisNode;
    }
    
    public void nodeWasDeactivated(){
        totalPendingLocalSearchesInSystem -= penLocSearches.size();
        penLocSearches.clear();
    }
    
    public void addSearch(int searchID, int resource){
        totalPendingLocalSearchesInSystem++;
        penLocSearches.add(penLocSearches.size(),new PendingLocalSearch(searchID, resource));
    }
    
    public boolean isEmpty(){
        return !penLocSearches.isEmpty();
    }
    
    public PendingLocalSearch nextSearch(){
        totalPendingLocalSearchesInSystem--;
        return (PendingLocalSearch)penLocSearches.remove(0);
    }
    
    public int size(){
        return penLocSearches.size();
    }

}

class PendingLocalSearch{
    
    protected int searchID = 0;
    protected int resource = 0;         // Resource looked for
    
    public PendingLocalSearch(int searchID, int resource){
        this.searchID = searchID;
        this.resource = resource;
    }
    
    public int getSearchID(){
        return searchID;
    }
    
    public int getResource(){
        return resource;
    }
    
}
