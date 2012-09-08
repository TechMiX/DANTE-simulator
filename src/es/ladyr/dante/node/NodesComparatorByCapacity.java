/*
 * Copyright 2008 Luis Rodero Merino.  All Rights Reserved.
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

import java.util.Comparator;

public class NodesComparatorByCapacity implements Comparator {

    public int compare(Object arg0, Object arg1) {
        
        DanteNode node1 = (DanteNode)arg0;
        DanteNode node2 = (DanteNode)arg1;
        
        if(node1 == node2)  // Yes, I compare by reference. No, it's not a mistake.
            return 0;
        
        // Checking first by processing capacity
        if(node1.getCapacity() > node2.getCapacity())
            return -1;

        if(node1.getCapacity() < node2.getCapacity())
            return 1;
        
        // Checking now by bandwidth
        if(node1.getBandwidth() > node2.getBandwidth())
            return -1;
        
        if(node1.getBandwidth() < node2.getBandwidth())
            return 1;
        
        /* // Checking now by degree
        if(node1.getDegree() > node2.getDegree())
            return -1; 
        
        if(node1.getDegree() < node2.getDegree())
            return 1;*/
        
        // Finally, comparing by id
        if(node1.id() < node2.id())
            return -1;
        
        if(node1.id() > node2.id())
            return 1;
        
        return 0;
    }

}
