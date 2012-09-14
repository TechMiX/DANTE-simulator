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

public abstract class ProtocolMessage {

    public final static short CONNECT = 0;
    public final static short ACCEPT = 1;
    public final static short REJECT = 2;
    public final static short DISCONNECT = 3;
    public final static short LOOK_FOR_NODES = 4;
    public final static short NODES_FOUND = 5;
    public final static short LOOK_FOR_RESOURCE = 6;
    public final static short RESOURCE_FOUND = 7;
    public final static short RESOURCE_NOT_FOUND = 8;
    public final static short REPLICATION_MESSAGE = 9;    
    
    protected short messageType = 0;
    
    protected DanteNode sender = null;
    
    protected long timeInsertedInQueue = -1;
    
    public short getMessageType(){
        return messageType;
    }
    
    public DanteNode getSender(){
        return sender;
    }
    
    public void setSender(DanteNode sender){
        this.sender = sender;
    }
    
    public void setTimeWasInsertedInQueue(long time){
        timeInsertedInQueue = time;
    }
    
    public long timeWasInsertedInQueue(){
        return timeInsertedInQueue;
    }

}
