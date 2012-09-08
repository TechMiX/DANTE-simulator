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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import es.ladyr.dante.run.DanteConf;
import es.ladyr.dante.run.DanteSimRunner;

public class DigestedSearchesFileGenerator {
    
    public void createDigestedSearchesLog(){
        
        File allSearchesLogFile = new File(DanteConf.getPresentSimConf().allSearLogFileName() + "_" + DanteSimRunner.getExpIndexAsString());
        File digestedAllSearchesLogFile = new File(DanteConf.getPresentSimConf().allSearDigLogFileName() + "_" + DanteSimRunner.getExpIndexAsString());
        long intervalLength = DanteConf.getPresentSimConf().digLogIntLength();
        
        BufferedReader allSearchsLogFileReader = null;
        try {
            allSearchsLogFileReader = new BufferedReader(new FileReader(allSearchesLogFile));
        } catch (FileNotFoundException exception) {
            throw new Error("Could not create BufferedReader", exception);
        }
        
        // Searches are not ordered in file, so we must do this in a quite inefficient manner...
        HashMap intervalsDataMap = new HashMap();
        
        String line = getNextLineData(allSearchsLogFileReader);
        
        while(line != null){       
            
            // Parsing line data
            String[] searchData = line.split("\t");
            if(searchData.length != 14)
                throw new Error("Error in search data line " + line);
            
            long ttl = Long.parseLong(searchData[2]);
            if(ttl == -1){
                // Ignoring 'strange' searches
                line = getNextLineData(allSearchsLogFileReader);                
                continue;                
            }
            
            long initTime = Long.parseLong(searchData[12]);
            
            long finishTime = Long.parseLong(searchData[13]);
            
            Long interval = new Long((initTime/intervalLength) + 1);
            
            IntervalData intervalData = (IntervalData)intervalsDataMap.get(interval);
            if(intervalData == null)
                intervalData = new IntervalData();
            intervalData.newSearch(ttl, finishTime - initTime);
            intervalsDataMap.put(interval, intervalData);
            
            // Next line
            line = getNextLineData(allSearchsLogFileReader);
        }
        
        // Closing searches data file
        try {
            allSearchsLogFileReader.close();
        } catch (IOException exception) {
            throw new Error("IOException caught when closing BufferedReader", exception);
        }
        
        
        // Creating digested searches data file        
        FileOutputStream digestedAllSearchsLogStream = null;
        try {
            digestedAllSearchsLogStream = new FileOutputStream(digestedAllSearchesLogFile);
        } catch (FileNotFoundException exception) {
            throw new Error("Could not create FileOutputStream", exception);
        }
        
        // Writing digested log file header
        try {
            digestedAllSearchsLogStream.write(new String("# Digested searches log. Interval duration is " + intervalLength + "\n").getBytes());
            digestedAllSearchsLogStream.write(new String("# Interval\tSearches\tMean Time\tMean TTL\n").getBytes());
        } catch (IOException exception) {
            throw new Error("IOException caught when writing to FileOutputStream", exception);
        }
        
        ArrayList intervalsList = new ArrayList(intervalsDataMap.keySet());
        Collections.sort(intervalsList);
        Long[] intervals = (Long[])intervalsList.toArray(new Long[0]);
        long previousInterval = 1;
        for(int intervalIndex = 0; intervalIndex < intervals.length; intervalIndex++){
            
            long interval = intervals[intervalIndex].longValue();
            
            // Writing previous empty intervals, if any
            for(long prevIntervalsIndex = previousInterval; prevIntervalsIndex < (interval - 1); prevIntervalsIndex++){
                try {
                    digestedAllSearchsLogStream.write(new String(prevIntervalsIndex + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\n").getBytes());
                } catch (IOException exception) {
                    throw new Error("IOException caught when writing to FileOutputStream", exception);
                }                
            }
            
            IntervalData intervalData = (IntervalData)intervalsDataMap.get(intervals[intervalIndex]);
            try {
                digestedAllSearchsLogStream.write(new String(interval + "\t" + intervalData.totalSearches() + "\t" + intervalData.meanTime() + "\t" + intervalData.meanTTL() + "\n").getBytes());
            } catch (IOException exception) {
                throw new Error("IOException caught when writing to FileOutputStream", exception);
            }
            previousInterval = interval;
        }
        
        // Closing digested searches data file
        try {
            digestedAllSearchsLogStream.close();
        } catch (IOException exception) {
            throw new Error("IOException caught when closing Stream", exception);
        }
        
    }
    
    private String getNextLineData(BufferedReader allSearchesLogFileReader){
        
        String line = null;
        do{
            try {
                line = allSearchesLogFileReader.readLine();
            } catch (IOException exception) {
                throw new Error("IOException caught when reading line from BufferedReader", exception);
            }            
            if(line == null)
                return null;           
            line = line.trim();
        } while((line.startsWith("#")) || (line == "")); // Avoiding empty and commented lines in file
        
        return line;
        
    }

}

class IntervalData {
    
    long ttlsSum = 0;
    long searchesTimesSum = 0;
    long totalSearches = 0;
    
    public void newSearch(long ttl, long searchTime){
        ttlsSum += ttl;
        searchesTimesSum += searchTime;
        totalSearches++;
    }
    
    public double meanTTL(){
        BigDecimal roundedMean = new BigDecimal((double)ttlsSum/totalSearches);
        return roundedMean.setScale(1, BigDecimal.ROUND_HALF_EVEN).doubleValue();
    }
    
    public double meanTime(){
        BigDecimal roundedMean = new BigDecimal((double)searchesTimesSum/totalSearches);
        return roundedMean.setScale(1, BigDecimal.ROUND_HALF_EVEN).doubleValue();
    }
    
    public long totalSearches(){
        return totalSearches;
    }
    
}
