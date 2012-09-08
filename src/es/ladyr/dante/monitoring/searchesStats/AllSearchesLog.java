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

package es.ladyr.dante.monitoring.searchesStats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import es.ladyr.dante.run.DanteConf;
import es.ladyr.dante.run.DanteSimRunner;

public class AllSearchesLog {
    
    private static AllSearchesLog _instance = null;
    
    private FileOutputStream allSearchesLogFileStream = null;
    private boolean isClosed = false;
    
    public static void prepareNewSimulationSearchesLog(){
        if(_instance != null)
            _instance.closeSimulationSearchesLog();
        _instance = new AllSearchesLog();
    }
    
    public static AllSearchesLog allSearchesLog(){
        return _instance;
    }
    
    private AllSearchesLog(){
        
        File allSearchsLogFile = new File(DanteConf.getPresentSimConf().allSearLogFileName() + "_" + DanteSimRunner.getExpIndexAsString());
        try {
            allSearchesLogFileStream = new FileOutputStream(allSearchsLogFile);
        } catch (FileNotFoundException exception) {
            throw new Error("Could not open stream for file " + allSearchsLogFile.getAbsolutePath(), exception);
        }
        
        // Writing file header
        try {
            allSearchesLogFileStream.write(("# NODE\tOrigin node\n").getBytes());
            allSearchesLogFileStream.write(("# RES\tResource\n").getBytes());
            allSearchesLogFileStream.write(("# TTL\tSearch TTL (-1 for searches that did not finished normally)\n").getBytes());
            allSearchesLogFileStream.write(("# IN_SYS\tResource in system flag\n").getBytes());
            allSearchesLogFileStream.write(("# FO\t\tSearch was found flag\n").getBytes());
            allSearchesLogFileStream.write(("# N_F\t\tSearch was not found flag\n").getBytes());
            allSearchesLogFileStream.write(("# L_PEND\tPending search lost because node was deactivated\n").getBytes());
            allSearchesLogFileStream.write(("# L_ARRI\tSearch lost because arrived to deactivated node\n").getBytes());
            allSearchesLogFileStream.write(("# L_FU_Q\tSearch lost because arriving node queue was full\n").getBytes());
            allSearchesLogFileStream.write(("# L_DE_Q\tSearch lost because the node where it was waiting to be processed was deactivated\n").getBytes());
            allSearchesLogFileStream.write(("# L_RES_Q\tSearch result in queue lost because node was deactivated\n").getBytes());
            allSearchesLogFileStream.write(("# END_S\t\tSearch lost because end of simulation reached\n").getBytes());
            allSearchesLogFileStream.write(("# START_AT\tTime search was started at\n").getBytes());
            allSearchesLogFileStream.write(("# FINISH_AT\tTime search was finished at\n").getBytes());
            allSearchesLogFileStream.write(("# NODE\tRES\tTTL\tIN_SYS\tFO\tN_F\tL_PEND\tL_ARRI\tL_FU_Q\tL_DE_Q\tL_RES_Q\tEND_S\tSTART_AT\tFINISH_AT\n").getBytes());
        } catch (IOException exception) {
            throw new Error("Could not write in file " + allSearchsLogFile.getAbsolutePath(), exception);
        }
        
    }
    
    public void closeSimulationSearchesLog(){
        
        if(isClosed)
            return;
        
        try {
            allSearchesLogFileStream.close();
        } catch (IOException exception) {
            File allSearchsLogFile = new File(DanteConf.getPresentSimConf().allSearLogFileName() + "_" + DanteSimRunner.getExpIndexAsString());
            throw new Error("Could not close file " + allSearchsLogFile.getAbsolutePath(), exception);
        }
        
        isClosed = true;
    }
    
    public void writeSearchLog(int nodeID, int resource, int TTL, boolean res_in_sys,
                                      boolean found, boolean n_f, boolean l_pend, boolean l_arri, boolean l_full_q, boolean l_de_q, boolean l_res_q, boolean end_s,
                                      long start_at, long finish_at){
        
        StringBuffer buffer = new StringBuffer();
        
        buffer.append(nodeID + "\t").append(resource + "\t" ).append(TTL + "\t").append((res_in_sys ? "1" : "0") + "\t").append((found ? "1" : "0") + "\t");
        buffer.append((n_f ? "1" : "0") + "\t").append((l_pend ? "1" : "0") + "\t").append((l_arri ? "1" : "0") + "\t").append((l_full_q ? "1" : "0") + "\t");
        buffer.append((l_de_q ? "1" : "0") + "\t").append((l_res_q ? "1" : "0") + "\t").append((end_s ? "1" : "0") + "\t").append(start_at + "\t").append(finish_at + "\n");
        
        //String buffer = nodeID + "\t" + resource + "\t" + TTL + "\t" + (res_in_sys ? "1" : "0") + "\t" + 
        //                (found ? "1" : "0") + "\t" + (n_f ? "1" : "0") + "\t" + (l_pend ? "1" : "0") + "\t" + (l_arri ? "1" : "0") + "\t" + (l_full_q ? "1" : "0") + "\t" +
        //                (l_de_q ? "1" : "0") + "\t" + (l_res_q ? "1" : "0") + "\t" + (end_s ? "1" : "0") + "\t" + 
        //                start_at + "\t" + finish_at + "\n"; 
        
        try {
            allSearchesLogFileStream.write(buffer.toString().getBytes());
        } catch (IOException exception) {
            throw new Error("IOException caught when trying to write in searchs log file", exception);
        }
    }  

}
