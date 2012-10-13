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

import es.ladyr.dante.node.connectionsAccepter.ConnectionsAccepter;
import es.ladyr.dante.node.forwardNodeChooser.ForwardNodeChooser;
import es.ladyr.dante.node.kernel.DanteKernel;
import es.ladyr.dante.node.kernel.disconnectionNodeChooser.NodesToDiscFromChooser;
import es.ladyr.dante.node.kernel.metricsCalculator.NodesMetricCalculator;
import es.ladyr.dante.node.kernel.newConnectionsFilter.NewConnectionsFilter;
import es.ladyr.dante.node.ttl.ResourceSearchsTTLStimator;
import es.ladyr.dante.topologyGenerator.TopologyGenerator;
import es.ladyr.util.exps.Params;


public class DanteConf {  
    
    private static DanteConf presentSimConf = null;
    
    public static DanteConf getPresentSimConf(){
        return presentSimConf;
    }
    
    public static void setPresentSimConf(DanteConf presentSimConf){
        DanteConf.presentSimConf = presentSimConf;
    }

    private final static String MAX_PACKETS_SIZE = "DantePacketsMaxSize";
    private final static String SEARCHES_TTL = "SearchesTTL";
    private final static String NODE_SEARCHES_TTL = "NodeSearchesTTL";
    private final static String RES_BY_NODE = "ResourcesByNode";
    private final static String RES_REPL_IS_UNIFORM = "ResourcesReplicationIsUniform";
    private final static String RATE_FOR_UNIFORM_RES_REPL = "UniformReplicationRate";
    private final static String TAU_FOR_ZIPF_RES_REPL = "ResourcesReplicationTau";
    private final static String CUTOFF_FOR_ZIPF_RES_REPL = "ResourcesReplicationCutoff";
    private final static String RES_POPUL_IS_UNIFORM = "ResourcesPopularityIsUniform";
    private final static String TAU_FOR_ZIPF_RES_POPUL = "ResourcesPopularityTau";
    private final static String CUTOFF_FOR_ZIPF_RES_POPUL = "ResourcesPopularityCutoff";
    private final static String PROC_TIME_IS_EXP = "ProcTimeByExpDistr";
    private final static String QUEUES_MAX_SIZE = "QueuesMaxSize";
    private final static String TIME_STEPS_FOR_CONG = "TimeUnitsForCongestion";
    private final static String CONG_THRESHOLD = "CongestionThreshold";
    private final static String PETS_COUNT_SLOT_LENGTH = "PetsCounterSlotLength";
    private final static String CONG_ACCEPT_RATE = "CongestionAcceptanceRate";
    private final static String MAX_EXPONENT = "MaxExponent";
    private final static String ALPHA = "Alpha";
    private final static String OUTGOING_CONNS = "NumberOutgoingConnections";
    private final static String CONNS_CHANGED_AT_REC = "NumberConsChangedAtReconn";
    private final static String FIXED_TIME_COST = "FixedTimeCost";
    private final static String IGNORE_SEARCHES_BEFORE = "IgnoreBefore";
    private final static String IGNORE_SEARCHES_AFTER = "IgnoreBeyond";
    private final static String SEARCHES_TO_RUN = "SearchesToRun";
    private final static String STATS_FILE_NAME = "StatsFileName";
    private final static String STATS_BY_NODE_FILE_NAME = "StatsByNodeFileName";
    private final static String ALL_SEARCHES_LOG_FILE = "AllSearchesLog";
    private final static String ALL_SEARCHES_DIG_LOG_FILE = "AllSearchesDigestedLog";
    private final static String DIG_LOG_INT_LENGTH = "DigestedLogIntervalLength";
    private final static String GENERATE_EVENTS = "GenerateEvents";
    private final static String NODES_CONFIG = "NodesNumbersCapacitiesAndBandwidths";
    private final static String TIME_BET_TOP_CAP = "TimeBetweenTopologyCaptures";
    private final static String FIRST_TOP_CAP_AT = "FirstTopologyCaptureTime";
    private final static String CAP_DEG_DISTR = "CaptureDegreeDistribution";
    private final static String TIME_BET_SEARCHES = "TimeBetweenSearches";
    private final static String FIRST_SEARCH_AT = "FirstSearchTime";
    private final static String LOAD_MOD_INTERV = "LoadModifierIntervals";
    private final static String FIRST_REC_AT = "FirstReconnectionTime";
    private final static String TIME_BET_REC = "TimeBetweenReconnections";
    private final static String STOP_REC_AFTER = "StopReconnectionsAfter";
    private final static String PROB_NODE_ACT_START = "ProbabilityNodeActiveAtStartTime";
    private final static String ACT_MEAN_TIME = "ActivationMean";
    private final static String DEACT_MEAN_TIME = "DeactivationMean";
    private final static String DEACT_FIX_TIME = "DeactivationFixedTime";
    private final static String FIRST_ATTACK_TIME = "FirstAttackTime";
    private final static String TIME_BET_ATT = "TimeBetweenAttacks";
    private final static String NODES_TO_ATT = "NumberOfNodesToAttack";
    private final static String TIME_BEF_REC = "TimeBeforeNodesRecover";
    private final static String TIME_NET_CAP_AFT_ATT = "TimeNetworkCaptureAfterAttack";
    private final static String BE_PARANOID= "DanteBeParanoid";
    private final static String PAJEK_DIR = "PajekFilesDir";
    private final static String TOP_GEN = "InitialTopologyGenerator";
    private final static String CONNS_ACC = "ConnectionsAccepter";
    private final static String FWD_CHOOSER = "ForwardNodeChooser"; 
    private final static String FWD_SEARCH_CHOOSER = "ForwardSearchNodeChooser"; 
    private final static String DANTE_KERNEL = "DanteKernel";
    private final static String METRICS_CALC = "NodesMetricCalculator";
    private final static String DISC_CHOOSER = "NodeToDiscFromChooser";
    private final static String NEW_CONNS_FILTER = "NewConnectionsFilter";
    private final static String SEARCHES_TTL_EST = "ResourceSearchesTTLEstimator";
    private final static String NODE_SEARCHES_BY_WALK = "NodeSearchByWalkers";
    private final static String ONLINE_REPLICATION = "OnlineReplication";
    private final static String ONLINE_REPLICATION_PATH = "PathReplication";
    private final static String ONLINE_REPLICATION_REPLICATION_COUNT = "ReplicationCount";
    private final static String ONLINE_REPLICATION_EXTRA_RATE = "ExtraResourceReplicationRate";
    private final static String CONFIG_STORE_FILE_NAME = "ConfigStoreFileName";
    
    public DanteConf(Params params){
        maxPacketsSize = params.getIntParam(MAX_PACKETS_SIZE); // 1
        searchesTTL = params.getIntParam(SEARCHES_TTL); // 2
        nodeSearchesTTL = params.getIntParam(NODE_SEARCHES_TTL); // 3
        resByNode = params.getIntParam(RES_BY_NODE); // 4
        resReplIsUniform = params.getBooleanParam(RES_REPL_IS_UNIFORM); // 5
        unifReplRate = params.getDoubleParam(RATE_FOR_UNIFORM_RES_REPL); // 6
        resReplTau = params.getDoubleParam(TAU_FOR_ZIPF_RES_REPL); // 7
        resReplCutoff = params.getIntParam(CUTOFF_FOR_ZIPF_RES_REPL); // 8
        resPopulIsUniform = params.getBooleanParam(RES_POPUL_IS_UNIFORM); // 9
        resPopulTau = params.getDoubleParam(TAU_FOR_ZIPF_RES_POPUL); // 10
        resPopulCutoff = params.getIntParam(CUTOFF_FOR_ZIPF_RES_POPUL); // 11
        procTimesByExpDis = params.getBooleanParam(PROC_TIME_IS_EXP); // 12
        queMaxSize = params.getIntParam(QUEUES_MAX_SIZE); // 13
        timeStepsForCong = params.getLongParam(TIME_STEPS_FOR_CONG); // 15
        congThreshold = params.getIntParam(CONG_THRESHOLD); // 16
        petsCounterSlotLength = params.getLongParam(PETS_COUNT_SLOT_LENGTH);
        congAcceptRate = params.getDoubleParam(CONG_ACCEPT_RATE); // 17
        maxKernelExp = params.getDoubleParam(MAX_EXPONENT); // 18
        alpha = params.getDoubleParam(ALPHA); // 19
        outConns = params.getIntParam(OUTGOING_CONNS); // 20
        connsChangedAtRec = params.getIntParam(CONNS_CHANGED_AT_REC); // 21        
        if(outConns < connsChangedAtRec)
            throw new Error("Number of conns to change at reconn greater than number of outgoing conns!");
        
        fixedTimeCost = params.getIntParam(FIXED_TIME_COST); // 22
        ignSearchesBef = params.getLongParam(IGNORE_SEARCHES_BEFORE); // 23
        ignSearchesAft = params.getLongParam(IGNORE_SEARCHES_AFTER); // 24
        searchesToRun = params.getLongParam(SEARCHES_TO_RUN); // 25
        statsFileName = params.getParam(STATS_FILE_NAME);
        statsByNodeFileName = params.getParam(STATS_BY_NODE_FILE_NAME);
        allSearLogFileName = params.getParam(ALL_SEARCHES_LOG_FILE); // 26
        allSearDigLogFileName = params.getParam(ALL_SEARCHES_DIG_LOG_FILE); // 27
        digLogIntLength = params.getLongParam(DIG_LOG_INT_LENGTH); // 28
        generateEvents = params.getBooleanParam(GENERATE_EVENTS); // 29        
        readNodesCapacitiesAndBandwidths(params); // 30
        timeBetTopCap = params.getLongParam(TIME_BET_TOP_CAP); // 31
        firstCapTime = params.getLongParam(FIRST_TOP_CAP_AT); // 32
        capDegDistr = params.getBooleanParam(CAP_DEG_DISTR); // 33
        timeBetSearches = params.getLongParam(TIME_BET_SEARCHES); // 34
        firstSearchTime = params.getLongParam(FIRST_SEARCH_AT); // 35
        readLoadModifierIntervals(params); // 36
        firstRecTime = params.getLongParam(FIRST_REC_AT); // 37
        timeBetRec = params.getLongParam(TIME_BET_REC); // 38
        stopRecAfter = params.getLongParam(STOP_REC_AFTER);
        probNodeActStart = params.getDoubleParam(PROB_NODE_ACT_START); // 39
        actMeanTime = params.getDoubleParam(ACT_MEAN_TIME); // 40
        deactMeanTime = params.getDoubleParam(DEACT_MEAN_TIME); // 42
        deactFixTime = params.getLongParam(DEACT_FIX_TIME); // 44
        firstAttTime = params.getLongParam(FIRST_ATTACK_TIME); // 45
        timeBetAtts = params.getLongParam(TIME_BET_ATT); // 46
        nodesToAtt = params.getIntParam(NODES_TO_ATT); // 47
        timeBefNodeRec = params.getLongParam(TIME_BEF_REC); // 48
        timeNetCapAftAtt = params.getLongParam(TIME_NET_CAP_AFT_ATT);
        beParanoid = params.getBooleanParam(BE_PARANOID); // 49
        pajekDir = params.getParam(PAJEK_DIR); // 50        
        nodeSeaByWalk = params.getBooleanParam(NODE_SEARCHES_BY_WALK); // 51
        onlineReplication = params.getBooleanParam(ONLINE_REPLICATION);
        orReplicationCount = params.getIntParam(ONLINE_REPLICATION_REPLICATION_COUNT);
        if (orReplicationCount < 1)
        	throw new Error("Resource replication count can't be less than one");
        orExtraResRate = params.getDoubleParam(ONLINE_REPLICATION_EXTRA_RATE);
        orPathReplication = params.getBooleanParam(ONLINE_REPLICATION_PATH);
        configStoreFileName = params.getParam(CONFIG_STORE_FILE_NAME);
        
        // Components
        try {
            topGen = (TopologyGenerator)params.getClassParam(TOP_GEN).newInstance(); // 52
            connsAcc = (ConnectionsAccepter)params.getClassParam(CONNS_ACC).newInstance(); // 53
            fwdChooser = (ForwardNodeChooser)params.getClassParam(FWD_CHOOSER).newInstance(); // 54
            fwdSearchChooser = (ForwardNodeChooser)params.getClassParam(FWD_SEARCH_CHOOSER).newInstance();
            kernel = (DanteKernel)params.getClassParam(DANTE_KERNEL).newInstance(); // 55
            metricCal = (NodesMetricCalculator)params.getClassParam(METRICS_CALC).newInstance(); // 56
            discChooser = (NodesToDiscFromChooser)params.getClassParam(DISC_CHOOSER).newInstance(); // 57
            newConnsFilter = (NewConnectionsFilter)params.getClassParam(NEW_CONNS_FILTER).newInstance(); // 58
            ttlEstimator = (ResourceSearchsTTLStimator)params.getClassParam(SEARCHES_TTL_EST).newInstance(); // 59
        } catch (InstantiationException exc) {
            throw new Error("Instantiation exception caught: " + exc.getMessage());
        } catch (IllegalAccessException exc) {
            throw new Error("Illegal access exception caught: " + exc.getMessage());
        } catch (ClassNotFoundException exc) {
            throw new Error("Class not found exception caught: " + exc.getMessage());
        }
    }
    

    // 1 - Max packet size
    private int maxPacketsSize = -1;
    public int maxPacketsSize(){
        return maxPacketsSize;
    }
    
    // 2 - Resource searches TTL 
    // (only used when FixedTTLStimator is used to estimate TTL)
    private int searchesTTL = -1;
    public int searchesTTL(){
        return searchesTTL;
    }

    // 3 - Node searches TTL
    private int nodeSearchesTTL = -1;
    public int nodeSearchesTTL(){
        return nodeSearchesTTL;
    }

    // 4 - Resources by node
    private int resByNode = -1;
    public int resByNode(){
        return resByNode;
    }
   
    // 5 - Uniform res. replication? (if not, is Zipf)
    private boolean resReplIsUniform = true;
    public boolean resReplIsUniform(){
        return resReplIsUniform;
    }
   
    // 6 - Resource replication rate (used if replication is uniform)
    private double unifReplRate = -1;
    public double unifReplRate(){
        return unifReplRate;
    }
   
    // 7 - Resource replication Tau (used if replication by Zipf)
    private double resReplTau = -1;
    public double resReplTau(){
        return resReplTau;
    }
   
    // 8 - Resource replication Cutoff (used if replication by Zipf)
    private int resReplCutoff = -1;
    public int resReplCutoff(){
        return resReplCutoff;
    }
   
    // 9 -  Uniform resources popularity? (if not, is Zipf)
    private boolean resPopulIsUniform = true;
    public boolean resPopulIsUniform(){
        return resPopulIsUniform;
    }
    
    // 10 - Resource popularity Tau (used if popularity by Zipf)
    private double resPopulTau = -1;
    public double resPopulTau(){
        return resPopulTau;
    }
    
    // 11 - Resource popularity Cutoff (used if popularity by Zipf)
    private int resPopulCutoff = -1;
    public int resPopulCutoff(){
        return resPopulCutoff;
    }
    
    // 12 - Processing times by exponential distribution
    private boolean procTimesByExpDis = false;
    public boolean procTimesByExpDis(){
        return procTimesByExpDis;
    }
    
    // 13 - Queues max size
    private int queMaxSize = -1;
    public int queMaxSize(){
        return queMaxSize;
    }
    
    private long petsCounterSlotLength = -1;
    public long petsCounterSlotLength(){
        return petsCounterSlotLength;
    }
    
    // 15 - Time steps to compute node congestion
    private long timeStepsForCong = -1;
    public long timeStepsForCong(){
        return timeStepsForCong;
    }
    
    // 16 - Congestion threshold (only used if metrics are computed by
    //      es.ladyr.dante.node.kernel.metricsCalculator.FixedNodesMetricCalculator)
    private int congThreshold = -1;
    public int congThreshold(){
        return congThreshold;
    }
    
    // 17 - Congestion acceptance rate (normally only used if metrics are computed by
    //      es.ladyr.dante.node.kernel.metricsCalculator.AdaptableNodesMetricCalculator)
    private double congAcceptRate = -1;
    public double congAcceptRate(){
        return congAcceptRate;
    }
    
    // 18 - Max exponent (for gamma in kernels)
    private double maxKernelExp = -1;
    public double maxKernelExp(){
        return maxKernelExp;
    }
    
    // 19 - Alpha, for node metric computation (normally only used if metrics are computed by
    //      es.ladyr.dante.node.kernel.metricsCalculator.AdaptableNodesMetricCalculator)
    private double alpha = -1;
    public double alpha(){
        return alpha;
    }    
    
    // 20 - Outgoing connections at each node
    private int outConns = -1;
    public int outConns(){
        return outConns;
    }
    
    // 21 - Connections changed at each reconnection
    private int connsChangedAtRec = -1;
    public int connsChangedAtRec(){
        return connsChangedAtRec;
    }
    
    // 22 - Fixed time cost
    private int fixedTimeCost = -1;
    public int fixedTimeCost(){
        return fixedTimeCost;
    }
    
    // 23 - Ignore searches started before this moment
    //      (if <0, then parameter is not taken into account)
    private long ignSearchesBef = -1;
    public long ignSearchesBef(){
        return ignSearchesBef;
    }
    
    // 24 - Ignore searches started after this moment
    //      (if <0, then parameter is not taken into account)
    private long ignSearchesAft = -1;
    public long ignSearchesAft(){
        return ignSearchesAft;
    }
    
    // 25 - Searches to run. If >0, then the simulation is stopped
    //      when this amount of searches have finished.
    private long searchesToRun = -1;
    public long searchesToRun(){
        return searchesToRun;
    }
    
    // Statistics file name
    private String statsFileName = null;
    public String statsFileName(){
        return statsFileName;
    }
    
    // Statistics by node file name
    private String statsByNodeFileName = null;
    public String statsByNodeFileName(){
        return statsByNodeFileName;
    }

    // 26 - Searches log file name
    private String allSearLogFileName = null;
    public String allSearLogFileName(){
        return allSearLogFileName;
    }
    
    // 27 - Searches 'digested' log file name
    private String allSearDigLogFileName = null;
    public String allSearDigLogFileName(){
        return allSearDigLogFileName;
    }
    
    // 28 - Intervals length in digested log
    private long digLogIntLength = -1;
    public long digLogIntLength(){
        return digLogIntLength;
    }
    
    // 29 - Events generation on/off
    private boolean generateEvents = false;
    public boolean generateEvents(){
        return generateEvents;
    }
    
    // 30 - Nodes configurations (capacities & bandwidths, amount of nodes
    // with each configuration)
    private double[] allCapacities = null;
    public double[] capacities(){
        return allCapacities;
    }
    private double[] allBandwidths = null;
    public double[] bandwidths(){
        return allBandwidths;
    }
    private int[] nodesPerCapAndBand = null;
    public int[] nodesPerCapAndBand(){
        return nodesPerCapAndBand;
    }
    
    // 31 - Time between topology captures
    private long timeBetTopCap = -1;
    public long timeBetTopCap(){
        return timeBetTopCap;
    }
    
    // 32 - Time first capture is done at
    private long firstCapTime = -1;
    public long firstCapTime(){
        return firstCapTime;
    }
    
    // 33 - Degree distribution capture on/off
    private boolean capDegDistr = false;
    public boolean capDegDistr(){
        return capDegDistr;
    }
    
    // 34 - Time between searches
    private long timeBetSearches = -1;
    public long timeBetSearches(){
        return timeBetSearches;
    }
    public void updateTimeBetSearches(long timeBetSearches){
        this.timeBetSearches = timeBetSearches;
    }
    
    // 35 - First search time (time searches start at)
    private long firstSearchTime = -1;
    public long firstSearchTime(){
        return firstSearchTime;
    }
    
    // 36 - Load modifications parameters
    // (when, and proportion)
    private long[] loadModifTimes = null;
    public long[] loadModifTimes(){
        return loadModifTimes;
    }
    private double[] loadModifProp = null;
    public double[] loadModifProp(){
        return loadModifProp;
    }
    
    // 37 - Time re-connections start at
    private long firstRecTime = -1;
    public long firstRecTime(){
        return firstRecTime;
    }
    
    // 38 - Time between re-connections
    private long timeBetRec = -1;
    public long timeBetRec(){
        return timeBetRec;
    }
    
    private long stopRecAfter = -1;
    public long stopRecAfter(){
        return stopRecAfter;
    }
    
    // 39 - Probability node active at simulation start
    private double probNodeActStart = -1;
    public double probNodeActStart(){
        return probNodeActStart;
    }
    
    // The times nodes are active is computed by an exponential
    // distribution. If mean <= 0 or cutoff <= 0, no
    // de-activations are performed.
    // 40 - Mean time nodes are active
    private double actMeanTime = -1;
    public double actMeanTime(){
        return actMeanTime;
    }
    
    // The times nodes are not active is computed by an exponential
    // distribution if mean > 0 and cutoff > 0, or is fixed if fixed > 0. 
    // Otherwise, no re-activations are performed.
    // 42 - Mean time nodes are not active
    private double deactMeanTime = -1;
    public double deactMeanTime(){
        return deactMeanTime;
    }
    
    // 44 - Fixed time nodes are not active
    private long deactFixTime = -1;
    public long deactFixTime(){
        return deactFixTime;
    }
    
    // 45 - Time of the first attack
    private long firstAttTime = -1;
    public long firstAttTime(){
        return firstAttTime;
    }
    
    // 46 - Time between attacks
    private long timeBetAtts = -1;
    public long timeBetAtts(){
        return timeBetAtts;
    }
    
    // 47 - Number of nodes to attack
    private int nodesToAtt = -1;
    public int nodesToAtt(){
        return nodesToAtt;
    }
    
    // 48 - Time to wait before node recovers from attack
    private long timeBefNodeRec = -1;
    public long timeBefNodeRec(){
        return timeBefNodeRec;
    }
    
    private long timeNetCapAftAtt = -1;
    public long timeNetCapAftAtt(){
        return timeNetCapAftAtt;
    }
    
    // 49 - Paranoid checking on/off
    private boolean beParanoid = false;
    public boolean beParanoid(){
        return beParanoid;
    }
    
    // 50 - Directory for 'pajek' files
    private String pajekDir = null;
    public String pajekDir(){
        return pajekDir;
    }    
    
    // 51 - Nodes searches by walkers?
    private boolean nodeSeaByWalk = false;
    public boolean nodeSeaByWalk(){
        return nodeSeaByWalk;
    }
    
    // Nodes perform online replication?
    private boolean onlineReplication = false;
    public boolean onlineReplication(){
        return onlineReplication;
    }
    
    // Number of resources to be replicated
    private int orReplicationCount = -1;
    public int orReplicationCount(){
    	
        return orReplicationCount;
    }
    
    // Extra resources replication rate
    private double orExtraResRate = -1;
    public double orExtraResRate(){
        return orExtraResRate;
    }

    private boolean orPathReplication = false;
    public boolean orPathReplication(){
    	return orPathReplication;
    }

    // File to store the experiment simulation (it helps later analysis)
    private String configStoreFileName = null;
    public String configStoreFileName(){
        return configStoreFileName;
    }
    
    ////////////////
    // Components //
    ////////////////
    
    // 52 - Initial topology generator
    private TopologyGenerator topGen = null;
    public TopologyGenerator topGen(){
        return topGen;
    }
    
    // 53 - Accepter of incoming connections
    private ConnectionsAccepter connsAcc = null;
    public ConnectionsAccepter connsAcc(){
        return connsAcc;
    }    
    
    // 54 - Chooser of node to forward messages to
    private ForwardNodeChooser fwdChooser = null;
    public ForwardNodeChooser fwdChooser(){
        return fwdChooser;
    }
    
    // 54 - Chooser of node to forward messages to
    private ForwardNodeChooser fwdSearchChooser = null;
    public ForwardNodeChooser fwdSearchChooser(){
        return fwdSearchChooser;
    }
    
    // 55 - Node kernel
    private DanteKernel kernel = null;
    public DanteKernel kernel(){
        return kernel;
    }
    
    // 56 - Metric calculator
    private NodesMetricCalculator metricCal = null;
    public NodesMetricCalculator metricCal(){
        return metricCal;
    }
    
    // 57 - Chooser of neighbors to disconnect from
    private NodesToDiscFromChooser discChooser = null;
    public NodesToDiscFromChooser disChooser(){
        return discChooser;
    }
    
    // 58 - Decides if new connections must be or performed or not
    private NewConnectionsFilter newConnsFilter = null;
    public NewConnectionsFilter newConnsFilter(){
        return newConnsFilter;
    }
    
    // 59 - Compute the resource searches TTL
    private ResourceSearchsTTLStimator ttlEstimator = null;
    public ResourceSearchsTTLStimator ttlEstimator(){
        return ttlEstimator;
    }

    
    private void readNodesCapacitiesAndBandwidths(Params params){
        
        String[] capacitiesAndBandwidthsAsString = params.getParam(NODES_CONFIG).trim().split("[,]");
        if(capacitiesAndBandwidthsAsString.length == 0)
            throw new Error("Error when reading capacities and bandwidths, could not find any set of values from " + capacitiesAndBandwidthsAsString);
        
        for(int index = 0; index < capacitiesAndBandwidthsAsString.length; index++)
            if(capacitiesAndBandwidthsAsString[index].trim().length() == 0)
                throw new Error("Empty data when reading capacities and bandwidths, set " + (index + 1));
        
        allCapacities = new double[capacitiesAndBandwidthsAsString.length];
        allBandwidths = new double[capacitiesAndBandwidthsAsString.length];
        nodesPerCapAndBand = new int[capacitiesAndBandwidthsAsString.length];
        
        for(int index = 0; index < capacitiesAndBandwidthsAsString.length; index++){
            
            String values = capacitiesAndBandwidthsAsString[index].trim();
                        
            if( (!values.startsWith("[")) || (!values.endsWith("]")) )
                throw new Error("Error when reading capacities and bandwiths, could not extract a valid set of values from " + capacitiesAndBandwidthsAsString[index] +
                                ", it does not start with [ and/or does not end with ]");
            
            values = values.substring(1,values.length()-1);
            
            String[] valuesSplitted = values.split("[|]");
            if(valuesSplitted.length != 3)
                throw new Error("Error when reading capacities, could not extract a valid set of values from " + values);

            nodesPerCapAndBand[index] = Integer.parseInt(valuesSplitted[0].trim());
            allCapacities[index] = Double.parseDouble(valuesSplitted[1].trim());
            allBandwidths[index] = Double.parseDouble(valuesSplitted[2].trim());
                        
        }
        
    }
    
    private void readLoadModifierIntervals(Params params){
        
        String[] loadModifierIntervals = params.getParam(LOAD_MOD_INTERV).trim().split("[,]");
        
        if(loadModifierIntervals.length == 0)
            return;
        
        if((loadModifierIntervals.length == 1) && (loadModifierIntervals[0].trim().length() == 0))
            return;
        
        for(int intervalIndex = 0; intervalIndex < loadModifierIntervals.length; intervalIndex++)
            if(loadModifierIntervals[intervalIndex].trim().length() == 0)
                throw new Error("Load modifier data empty in interval " + (intervalIndex + 1));
        
        loadModifTimes = new long[loadModifierIntervals.length];
        loadModifProp = new double[loadModifierIntervals.length];
        
        for(int intervalIndex = 0; intervalIndex < loadModifierIntervals.length; intervalIndex++){
            
            String intervalDataString = loadModifierIntervals[intervalIndex].trim();
            
            if((!intervalDataString.startsWith("[")) && (!intervalDataString.endsWith("]")))
                throw new Error("Error when reading load modifiers intervals, could not extract valid values from " + intervalDataString +
                                ", it does not start with [ and/or does not end with ]");
            
            intervalDataString = intervalDataString.substring(1,intervalDataString.length() -1 );
            
            String[] valuesSplitted = intervalDataString.split("[|]");
            if(valuesSplitted.length != 2)
                throw new Error("Error in load modifiers interval " + (intervalIndex + 1) + ", could not extract two values from " + intervalDataString);
            
            loadModifTimes[intervalIndex] = Long.parseLong(valuesSplitted[0].trim());
            loadModifProp[intervalIndex] = Double.parseDouble(valuesSplitted[1].trim());
        }
    }
    
}
