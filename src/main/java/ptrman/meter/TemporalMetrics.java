/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ptrman.meter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author me
 */
public class TemporalMetrics<O> extends Metrics<Double,O> {

    public TemporalMetrics(final int historySize) {
        super(historySize);
    }

    /** adds all meters which exist as fields of a given object (via reflection) */
    public void addMeters(final Object obj) {
        final Class c = obj.getClass();
        final Class meter = Meter.class;
        //System.out.println("field: " + f.getType() + " " + f.isAccessible() + " " + Meter.class.isAssignableFrom( f.getType() ));
        for (final var f : c.getFields())
            if (meter.isAssignableFrom(f.getType())) {
                Meter m = null;
                try {
                    m = (Meter) f.get(obj);
                } catch (final IllegalAccessException e) {
                    //TODO ignore or handle errors?
                }
                addMeter(m);
            }
    }

    public List<SignalData> getSignalDatas() {
        final var l = getSignals().stream().map(sv -> newSignalData(sv.id)).collect(Collectors.toList());

        return l;
    }

    /** allows updating with an integer/long time, because it will be converted
     * to double internally
     */
    public void update(final long integerTime) {
        update((double)integerTime);
    }    
    
}
