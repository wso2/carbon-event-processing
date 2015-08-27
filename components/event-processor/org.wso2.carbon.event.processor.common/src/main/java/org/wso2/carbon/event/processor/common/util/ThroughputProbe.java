package org.wso2.carbon.event.processor.common.util;

import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Probe to calculate the throughput by sampling in a given rate. first the the startSampling() must be used to start the probe
 * and the call  update() for each message.
 */
public class ThroughputProbe extends TimerTask {
    private static Logger log = Logger.getLogger(ThroughputProbe.class);
    private long count = 0;
    private int sampleCount = 0;
    private long samplingRateInSeconds;
    private String name;
    private double maxThroughput = 0.0;
    private double minThroughput = Double.MAX_VALUE;
    private double accumulatedThroughput = 0.0;
    DecimalFormat formatter = new DecimalFormat("#.000");
    Timer timer;
    private long totalEventCount = 0;

    public ThroughputProbe(String name, int samplingRateInSeconds){
        this.name = name;
        this.samplingRateInSeconds = samplingRateInSeconds;
    }

    /**
        Starting the probe, this will start calculating the throughout
     */
    public void startSampling(){
        count = 0l;
        timer = new Timer();
        timer.schedule(this, samplingRateInSeconds * 1000, samplingRateInSeconds * 1000);
    }

    /**
     * This must be called when a message received.
     */
    public void update(){
        count++;
        totalEventCount++;
    }

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {
        if (log.isDebugEnabled()){
            if (totalEventCount > 0){
                double throughput = count / samplingRateInSeconds;

                if (maxThroughput < throughput){
                    maxThroughput = throughput;
                }
                if (minThroughput > throughput && throughput != 0.0){
                    minThroughput = throughput;
                }

                accumulatedThroughput += throughput;
                sampleCount++;

                log.debug("[ThroughputProbe:" + name + "] " + count + " events in " + samplingRateInSeconds
                        + " seconds. Throughput=" + formatter.format(throughput)
                        + " events/s.(Avg=" + formatter.format(accumulatedThroughput / sampleCount)
                        + " ,Max=" + formatter.format(maxThroughput)
                        + " ,Min=" + ((minThroughput == Double.MAX_VALUE) ? "0.0" : formatter.format(minThroughput))
                        + " ) TotalEvents=" + totalEventCount);

                count = 0l;
            }
        }
    }
}
