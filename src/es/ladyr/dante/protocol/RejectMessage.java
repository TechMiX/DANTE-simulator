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

package es.ladyr.dante.protocol;

import es.ladyr.dante.node.DanteNode;


public class RejectMessage extends ProtocolMessage {
    
    protected int askingNodeGeneration = -1;
    protected int rejectingNodeGeneration = -1;
    
    public RejectMessage(DanteNode rejectingNode, int rejectingNodeGeneration, int askingNodeGeneration){
        this.sender = rejectingNode;
        this.rejectingNodeGeneration = rejectingNodeGeneration;
        this.askingNodeGeneration = askingNodeGeneration;
        messageType = REJECT;
    }
    
    public DanteNode getRejectingNode(){
        return sender;
    }
    
    public int getAskingNodeGeneration(){
        return askingNodeGeneration;
    }
    
    public int getRejectingNodeGeneration(){
        return rejectingNodeGeneration;
    }

}
