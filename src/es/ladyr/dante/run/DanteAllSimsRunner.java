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

package es.ladyr.dante.run;

import java.io.File;
import java.io.IOException;

import es.ladyr.util.commandLine.CommandLineParser;
import es.ladyr.util.exps.ExpParamsBuilder;
import es.ladyr.util.exps.Params;
import es.ladyr.util.exps.PropertyNotInSet;



public class DanteAllSimsRunner {
    
    private final static String DEFAULT_CONFIG_FILE_NAME = "dante_all_experiments_configuration";

    private final static String HELP_FLAG = CommandLineParser.DEFAULT_ARGUMENT_NAMES_HEADER + "help";
    private final static String FROM_EXP_ARG_NAME = CommandLineParser.DEFAULT_ARGUMENT_NAMES_HEADER + "fromExp";
    private final static String TO_EXP_ARG_NAME = CommandLineParser.DEFAULT_ARGUMENT_NAMES_HEADER + "toExp";
    private final static String CONF_FILE_ARG_NAME = CommandLineParser.DEFAULT_ARGUMENT_NAMES_HEADER + "config";
    private final static String GROUP_BY_PARAMS_ARG_NAME = CommandLineParser.DEFAULT_ARGUMENT_NAMES_HEADER + "groupExpsBy";
    private final static String[] validArgs = new String[] {HELP_FLAG, FROM_EXP_ARG_NAME, TO_EXP_ARG_NAME, CONF_FILE_ARG_NAME, GROUP_BY_PARAMS_ARG_NAME};
    
    private boolean helpFlagSet = false; 
    private int firstExpIndex = 1;
    private int lastExpIndex = Integer.MAX_VALUE;
    private String[] groupByParams = null;
    private String confFileName = DEFAULT_CONFIG_FILE_NAME;
    
    public static void main(String args[]){     
        new DanteAllSimsRunner().run(args);
    }
    
    public void run(String args[]){
        
        try {
            readCommandLineArgs(args);
        } catch (Exception exception) {
            System.out.println("Error when reading command line arguments: " + exception.getMessage());
            printUsage();
            return;
        }
        
        if(helpFlagSet){
            printHelp();
            return;
        }
        
        runExps();
            
    }
    
    private void readCommandLineArgs(String args[]) throws Exception {
        
        CommandLineParser commandLineParser = new CommandLineParser();
        
        String unknownArg = commandLineParser.lookForUnknownArguments(args, validArgs);
        if(unknownArg != null)
            throw new Exception("Unknown argument " + unknownArg);  
        
        confFileName = commandLineParser.getArgVal(args, CONF_FILE_ARG_NAME);
        if(confFileName == null)
            confFileName = DEFAULT_CONFIG_FILE_NAME;
        confFileName.trim();
        
        String firstExpIndexString = commandLineParser.getArgVal(args, CONF_FILE_ARG_NAME);
        if(firstExpIndexString == null)
            firstExpIndex = 1;
        else
            firstExpIndex = Integer.parseInt(firstExpIndexString);
        
        String lastExpIndexString = commandLineParser.getArgVal(args, TO_EXP_ARG_NAME);
        if(lastExpIndexString == null)
            lastExpIndex =  Integer.MAX_VALUE;
        else
            lastExpIndex = Integer.parseInt(lastExpIndexString);
        
        String groupByArg = commandLineParser.getArgVal(args, GROUP_BY_PARAMS_ARG_NAME);
        if(groupByArg != null){
            groupByParams = groupByArg.split(";");
            for(int paramIndex = 0;paramIndex < groupByParams.length; paramIndex++)
                groupByParams[paramIndex] = groupByParams[paramIndex].trim();
        }
        
        helpFlagSet = commandLineParser.isArgPresent(args, HELP_FLAG);
        
    }
    
    private void runExps(){
        
        if(firstExpIndex <= 0){
            System.out.println("Error, first experiment index (" + firstExpIndex + ") must be greater than 0");
            printUsage();
            return;
        }
        
        if(lastExpIndex <= 0){
            System.out.println("Error, last experiment index (" + lastExpIndex + ") must be greater than 0");
            printUsage();
            return;            
        }
        
        if(firstExpIndex > lastExpIndex){
            System.out.println("Error, first experiment index (" + firstExpIndex + ") can not be greater than last experiment index (" + lastExpIndex + ")");
            printUsage();
            return;
        }
        
        File confFile = new File(confFileName);
        
        System.out.println("Creating configurations from file " + confFile.getAbsolutePath());

        Params[] simConfs = null;
        try {
            simConfs = ExpParamsBuilder.buildExperiments(confFile, null, groupByParams);
        } catch (IOException exception) {
            System.out.println("Error when trying to build experiments configs: " + exception.getMessage());
            return;
        } catch (PropertyNotInSet exception) {
            System.out.println("Error when trying to build experiments configs: " + exception.getMessage());
            return;
        }
        
        int configsNumber = simConfs.length;
        lastExpIndex = (lastExpIndex > configsNumber) ? configsNumber : lastExpIndex;

        // Now, executing one by one
        System.out.println(configsNumber + " configurations generated, will be run from " + firstExpIndex + " to " + lastExpIndex);        
        if(firstExpIndex > configsNumber){
            System.out.println("Error, first experiment index set to " + firstExpIndex + " but only " + configsNumber + " different configurations were created");
            printUsage();
            return;
        }            
            
        for(int expIndex = 1; expIndex <= configsNumber; expIndex++){
            if((expIndex < firstExpIndex) || (expIndex > lastExpIndex)){
                System.out.println(" Skipping experiment " + expIndex + " of " + simConfs.length);     
                continue;
            }
            System.out.println(" Calling to gc() before experiment");
            System.gc();
            Params conf = simConfs[expIndex-1];
            System.out.println(" Running experiment " + expIndex + " of " + configsNumber);
            new DanteSimRunner().runSimulation(conf);   
            System.out.println(" Experiment " + expIndex + " of " + configsNumber + " finished");         
        }        
        
        System.out.println("bye...");
        
    }
    
    private static void printUsage(){
        System.out.println("Usage: java DNTAllSimsRunner " + "[" + HELP_FLAG + "]" +
                           " [" + FROM_EXP_ARG_NAME + " <firstExpIndex>]" +
                           " [" + TO_EXP_ARG_NAME + " <lastExpIndex>]" +
                           " [" + CONF_FILE_ARG_NAME + " <configFilePath>]" +
                           " [" + GROUP_BY_PARAMS_ARG_NAME + " <paramsToGroupExpsBy>]");
    }
    
    private static void printHelp(){
        printUsage();
        System.out.println("Optional Arguments:");
        System.out.println( "\t [" + HELP_FLAG + "]\n" +
        		            "\t\t Print this message and exit.");
        System.out.println( "\t [" + FROM_EXP_ARG_NAME + " <firstExpIndex>]\n" +
                            "\t\t For each configuration created an experiment should be run. This arg sets which experiment\n" +
                            "\t\t must be the first to be run, all previous are skipped. Thus it can  be avoided repeating\n" +
                            "\t\t experiments already run. Defaults to 1.");
        System.out.println( "\t [" + TO_EXP_ARG_NAME + " <lastExpIndex>]\n" +
                            "\t\t Last experiment to run of all set by configuration. In general, only experiments from\n" +
                            "\t\t <firstExpIndex> to <lastExpIndex> are run. By default <lastExpIndex> is the total number\n" +
                            "\t\t of experiments. If set to a value greater than the total number of configurations, then this\n" +
        		            "\t\t argument is ignored.");
        System.out.println( "\t [" + CONF_FILE_ARG_NAME + " <configFilePath>]\n" +
                            "\t\t All experiments configuration file. Default is " + DEFAULT_CONFIG_FILE_NAME);
        System.out.println( "\t [" + GROUP_BY_PARAMS_ARG_NAME + " <paramsToGroupExpsBy>]\n" +
                            "\t\t Experiments can be grouped by different config params values. For example, if the replication\n" +
                            "\t\t rates are set to two values (UniformReplicationRate=0.05;0.1) and we set UniformReplicationRate\n" +
                            "\t\t as the param to group by, then first all exps with rate 0.05 will be run, and then the exps\n" +
                            "\t\t with rate 0.1. It is possible to specify different params to group exps by, separating them\n" +
                            "\t\t by ';'. For example '" + GROUP_BY_PARAMS_ARG_NAME + " UniformReplicationRate;TimeBetweenSearches'.");
    }

}
