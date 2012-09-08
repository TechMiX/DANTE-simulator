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

package es.ladyr.util.commandLine;

// Parser of arguments passed by command line. 
// Arguments are assumed to be of the form "--argName argValue".
public class CommandLineParser {
    
    public final static String DEFAULT_ARGUMENT_NAMES_HEADER = "--";
    private String argNamesHeader = DEFAULT_ARGUMENT_NAMES_HEADER;
    
    public CommandLineParser(){};
    public CommandLineParser(String argNamesHeader){
        this.argNamesHeader = argNamesHeader;
    }
    
    public boolean isArgPresent(String args[], String argName){
        checkValidArgName(argName);
        return (posInArray(args, argName) >= 0);
    }
    
    public String getArgVal(String args[], String argName){
        checkValidArgName(argName);
        int position = posInArray(args, argName);
        if(position < 0)
            return null;
        if(position == args.length-1)
            return null;
        return args[position+1];
    }
    
    public String[] removeArg(String args[], String argName){
        checkValidArgName(argName);
        int position = posInArray(args, argName);
        if(position < 0)
            return args;
        
        String[] newArgs = new String[args.length-1];
        for(int index = 0; index < position; index++)
            newArgs[index] = args[index];
        for(int index = position + 1; index < args.length; index++)
            newArgs[index-1] = args[index];
        
        return newArgs;
    }
    
    public String[] removeArgAndVal(String args[], String argName){
        checkValidArgName(argName);
        int position = posInArray(args, argName);
        if(position < 0)
            return args;
        
        String[] newArgs = null;
        if(position == args.length-1){
            newArgs = new String[args.length-1];
            for(int index = 0; index < args.length - 1; index++)
                newArgs[index] = args[index];
        } else {
            newArgs = new String[args.length-2];
            for(int index = 0; index < position; index++)
                newArgs[index] = args[index];
            for(int index = position + 2; index < args.length; index++)
                newArgs[index-2] = args[index];            
        }
                     
        return newArgs;
    }
    
    private int posInArray(String args[], String argName){
        for(int position = 0; position < args.length; position++)
            if(args[position].equals(argName))
                return position;
        return -1;
    }
    
    public String lookForUnknownArguments(String[] args, String[] knownArgs){
        for(int argIndex = 0; argIndex < args.length; argIndex++){
            if(isValidArgName(args[argIndex]))
                if(!isArgPresent(knownArgs, args[argIndex]))
                    return args[argIndex];
        }
        return null;
    }
    
    public boolean isValidArgName(String argName) {
        return argName.startsWith(argNamesHeader);            
    }

    private void checkValidArgName(String argName) throws IllegalArgumentException {
        if(!isValidArgName(argName))
            throw new IllegalArgumentException(argName + " is not a valid command line argument name, it does not start by " + argNamesHeader);
    }

}
