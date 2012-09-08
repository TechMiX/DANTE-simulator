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
import java.math.BigDecimal;
import java.util.Iterator;

import es.ladyr.dante.node.DanteNode;
import es.ladyr.dante.run.DanteConf;
import es.ladyr.dante.run.DanteSimRunner;

public class AllNodesStatsFileGenerator {
    
    public void createAllNodesStatsFile(){
        
        File statsFile = new File(DanteConf.getPresentSimConf().statsFileName() + "_" + DanteSimRunner.getExpIndexAsString());
        File statsByNodeFile = new File(DanteConf.getPresentSimConf().statsByNodeFileName() + "_" + DanteSimRunner.getExpIndexAsString());
        
        FileOutputStream fileOutputStream = null;
        
        try {
            fileOutputStream = new FileOutputStream(statsByNodeFile);
        } catch (FileNotFoundException exception) {
            throw new Error("Could not open file " + statsByNodeFile.getAbsolutePath());
        }

        int numberOfNodes = 0;
        int totalSearchesStarted = 0;
        int totalSearchesFinished = 0;
        int totalSuccessfulSearches = 0;
        int totalFailedSearches = 0;
        int totalPendingSearches = 0;
        int totalIgnoredSearches = 0;
        int totalDiscardedSearches = 0;
        long totalSuccessSearchTTLs = 0;
        long totalTTLs = 0;
        long totalSuccessSearchTimes = 0;
        long totalSearchTimes = 0;
        try {
            // Writing file header
            fileOutputStream.write(("NODE\tSEARCHES\tSUCCESS\tFAIL\tPENDING\tIGNORED\tDISCARD\tA_TTL\tA_TIME\tS_A_TTL\tS_A_TIM" +
                    "\tCONNECT\tACCEPT\tREJECT\tDISCON\tLFRES\tRESFOU\tRESNOT" +
                    //"\tMx_CONQ\tMx_SEAQ\tMx_RESQ\tA_CONQ\tA_SEAQ\tA_RESQ\tA_ICONQ\tA_ISEAQ\tA_IRESQ\n").getBytes());
                    "\n").getBytes());
        
            // Writing nodes data
            Iterator iter = DanteNode.allNodesInSystem().iterator();
            while(iter.hasNext()){
                DanteNode node = (DanteNode)iter.next();
                NodeSearchesStats nodeStats = node.getSearchesStats();
                //MessagesQueuesSet messagesQueuesSet = node.getMessagesQueuesSet();
                fileOutputStream.write((""+node.id()+
                                        "\t"+nodeStats.startedSearches()+
                                        "\t"+nodeStats.successfulSearches()+
                                        "\t"+nodeStats.failedSearches()+
                                        "\t"+nodeStats.pendingSearches()+
                                        "\t"+nodeStats.ignoredSearches()+
                                        "\t"+nodeStats.discardedSearchesBecauseDeactivation()+
                                        "\t"+nodeStats.finishedSearchesAverTTL()+
                                        "\t"+nodeStats.finishedSearchesAverTime()+
                                        "\t"+nodeStats.successfulSearchesAverTTL()+
                                        "\t"+nodeStats.successfulSearchesAverTime()+
                                        "\t"+nodeStats.counterOfCONNECTsReceived()+
                                        "\t"+nodeStats.counterOfACCEPTsReceived()+
                                        "\t"+nodeStats.counterOfREJECTsReceived()+
                                        "\t"+nodeStats.counterOfDISCONNECTsReceived()+
                                        "\t"+nodeStats.counterOfLOOK_FOR_RESOURCEsReceived()+
                                        "\t"+nodeStats.counterOfRESOURCE_FOUNDReceived()+
                                        "\t"+nodeStats.counterOfRESOURCE_NOT_FOUNDsReceived()+
                                        /*"\t"+messagesQueuesSet.maxConnectQueueSize()+
                                        "\t"+messagesQueuesSet.maxSearchQueueSize()+
                                        "\t"+messagesQueuesSet.maxSearchResultsQueueSize()+
                                        "\t"+messagesQueuesSet.averageConnectQueueSize()+
                                        "\t"+messagesQueuesSet.averageSearchQueueSize()+
                                        "\t"+messagesQueuesSet.averageSearchResultQueueSize()+
                                        "\t"+messagesQueuesSet.averageTimeInConnectQueue()+
                                        "\t"+messagesQueuesSet.averageTimeInSearchsQueue()+
                                        "\t"+messagesQueuesSet.averageTimeInSearchResultsQueue()+*/
                                        "\n").getBytes());       
                // Computing totals
                numberOfNodes++;
                totalSearchesStarted += nodeStats.startedSearches();
                totalSuccessfulSearches += nodeStats.successfulSearches();
                totalFailedSearches += nodeStats.failedSearches();
                totalSearchesFinished += nodeStats.finishedSearches();
                totalPendingSearches += nodeStats.pendingSearches();
                totalIgnoredSearches += nodeStats.ignoredSearches();
                totalDiscardedSearches += nodeStats.discardedSearchesBecauseDeactivation();
                totalSuccessSearchTTLs += nodeStats.successfulSearchesTTLsSum();
                totalTTLs += nodeStats.ttlsSum();
                totalSuccessSearchTimes += nodeStats.successfulSearchesTimesSum();
                totalSearchTimes += nodeStats.searchTimesSum();
                
            }
            
            fileOutputStream.close();            
        } catch (IOException exception) {
            throw new Error("Error writing in file " + statsByNodeFile.getAbsolutePath());
        }
        
        // Computing total ttls and times means
        BigDecimal roundedMean = new BigDecimal(Double.toString(totalSearchesFinished != 0 ? 
                                                                (double)totalTTLs/totalSearchesFinished : 
                                                                0)); 
        roundedMean = roundedMean.setScale(1, BigDecimal.ROUND_HALF_EVEN);
        double ttlsMean = roundedMean.doubleValue();
        //System.out.println("totalSearchTimes/totalSearchs:" + totalSearchTimes + "/" + totalSearchs + " = " + ((double)totalSearchTimes/totalSearchs));
        roundedMean = new BigDecimal(Double.toString(totalSearchesFinished != 0 ? 
                                                     (double)totalSearchTimes/totalSearchesFinished:
                                                     0)); 
        roundedMean = roundedMean.setScale(1, BigDecimal.ROUND_HALF_EVEN);
        double timesMean = roundedMean.doubleValue();
        
        roundedMean = new BigDecimal(Double.toString(totalSuccessfulSearches != 0 ?
                                                     (double)totalSuccessSearchTTLs/totalSuccessfulSearches : 
                                                     0));
        roundedMean = roundedMean.setScale(1, BigDecimal.ROUND_HALF_EVEN);
        double successTTLsMean = roundedMean.doubleValue();
        
        roundedMean = new BigDecimal(Double.toString(totalSuccessfulSearches != 0 ? 
                                                     (double)totalSuccessSearchTimes/totalSuccessfulSearches:
                                                     0)); 
        roundedMean = roundedMean.setScale(1, BigDecimal.ROUND_HALF_EVEN);
        double successTimesMean = roundedMean.doubleValue();

        try {
            fileOutputStream = new FileOutputStream(statsFile);
            
            // Writing file header
            fileOutputStream.write(("NODES\tSEARCHES\tSUCCESS\tFAIL\tPENDING\tIGNORED\tDISCARD\tA_TTL\tA_TIME\tS_A_TTL\tS_A_TIM\n").getBytes());
            
            // Writing data
            fileOutputStream.write((""+numberOfNodes+
                                    "\t"+totalSearchesStarted+
                                    "\t"+totalSuccessfulSearches+
                                    "\t"+totalFailedSearches+
                                    "\t"+totalPendingSearches+
                                    "\t"+totalIgnoredSearches+                   
                                    "\t"+totalDiscardedSearches+
                                    "\t"+ttlsMean+
                                    "\t"+timesMean+
                                    "\t"+successTTLsMean+
                                    "\t"+successTimesMean).getBytes());
            
            // Closing file
            fileOutputStream.close();
            
        } catch (FileNotFoundException exception) {
            throw new Error("Could not open file " + statsFile.getAbsolutePath());
        }  catch (IOException exception) {
            throw new Error("Error writing in file " + statsFile.getAbsolutePath());
        }
        
    }

}
