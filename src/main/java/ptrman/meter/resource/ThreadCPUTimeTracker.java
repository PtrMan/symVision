package ptrman.meter.resource;


import ptrman.meter.event.DoubleMeter;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * CPU Time, in milliseconds
 *
 * @author The Stajistics Project
 */
public class ThreadCPUTimeTracker extends DoubleMeter {

    //private long startCPUTime; // nanos

    public ThreadCPUTimeTracker(final String id) {
        super(id);

        //ensureCPUTimeMonitoringEnabled();
    }

    public static double getCPUTime() {
        return getThreadMXBean().getCurrentThreadCpuTime();
    }

    @Override
    protected Double getValue(Object key, int index) {
        if (isCPUTimeMonitoringEnabled()) {
            return getCPUTime();
        }
        return null;
    }

 

    
   private static volatile boolean hasSetContentionMonitoringEnabled = false;
    private static volatile boolean hasSetCPUTimeMonitoringEnabled = false;

    private static boolean contentionMonitoringEnabled = false;
    private static boolean cpuTimeMonitoringEnabled = true;

    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();    

    protected static ThreadMXBean getThreadMXBean() {
        return threadMXBean;
    }

    protected static void ensureContentionMonitoringEnabled() {
        if (!hasSetContentionMonitoringEnabled) {
            hasSetContentionMonitoringEnabled = true;

            if (threadMXBean.isThreadContentionMonitoringSupported()) {
                threadMXBean.setThreadContentionMonitoringEnabled(true);
                contentionMonitoringEnabled = true;

                //logger.info("Enabling thread contention monitoring");

            } else {
                System.err.println("Thread contention monitoring is not supported in this JVM; "
                        + "Thread contention related trackers will be silent");
            }
        }
    }

    protected static boolean isContentionMonitoringEnabled() {
        return contentionMonitoringEnabled;
    }

    protected static void ensureCPUTimeMonitoringEnabled() {
        if (!hasSetCPUTimeMonitoringEnabled) {
            hasSetCPUTimeMonitoringEnabled = true;

            if (threadMXBean.isCurrentThreadCpuTimeSupported()) {
                threadMXBean.setThreadCpuTimeEnabled(true);
                cpuTimeMonitoringEnabled = true;

                //logger.info("Enabling thread CPU time monitoring");
            } else {
                System.err.println("Thread CPU time monitoring is not supported in this JVM; "
                        + "Thread CPU time related trackers will be silent");
            }
        }
    }

    protected static boolean isCPUTimeMonitoringEnabled() {
        return cpuTimeMonitoringEnabled;
    }

    protected static ThreadInfo getCurrentThreadInfo() {
        if (contentionMonitoringEnabled) {
            return threadMXBean.getThreadInfo(Thread.currentThread().getId(), 0);
        }

        return null;
    }
}
