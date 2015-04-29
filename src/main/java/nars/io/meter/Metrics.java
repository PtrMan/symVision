/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.nars.io.meter;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.gson.*;
import main.java.nars.io.meter.event.DoubleMeter;
import main.java.nars.io.meter.event.HitMeter;

import java.io.PrintStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

/**
 * A tabular data store where each (# indexed) column represents a different
 * type of value, and each row is a that value sampled/recorded at a different
 * time point (first column).
 * 
 */
public class Metrics<RowKey,Cell> implements Iterable<Object[]> {

    final static int PRECISION = 4;
    public final static Gson json = new GsonBuilder()
             .registerTypeAdapter(Double.class, new JsonSerializer<Double>()  { 
                        @Override
                        public JsonElement serialize(Double value, Type theType,
JsonSerializationContext context) { 
                                if (value.isNaN()) { 
                                        return new JsonPrimitive("NaN");
                                } else if (value.isInfinite()) { 
                                        return new JsonPrimitive(value);
                                } else { 
                                        return new JsonPrimitive(
                                                new BigDecimal(value).
                                                    setScale(PRECISION,
                                                    BigDecimal.ROUND_HALF_UP).stripTrailingZeros()); 
                                } 
                        } 
                }) 
                .create(); 

    
    public static void printJSONArray(PrintStream out, Object[] row, boolean includeBrackets) {
        String r = json.toJson(row);
        if (!includeBrackets) {
            r = r.substring(1, r.length()-1);
        }
        out.println(r);
        
    }

    
    /**
    *  Calculates a 2-tuple with the following data:
    *   0: minimum value among all columns in given signals
    *   1: maximum value among all columns in given signals
    * 
    * @param data
    * @return 
    */
    public double[] getBounds(Iterable<SignalData> data) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
    
        for (SignalData ss : data) {   
            double a = ss.getMin();
            double b = ss.getMax();
            if (a < min) min = a;
            if (b > max) max = b;
        }
        return new double[] { min, max };        
    }
    
    protected void setMin(int signal, double n) {
        getSignal(signal).setMin(n);
    }
    protected void setMax(int signal, double n) {
        getSignal(signal).setMax(n);
    }
    
    public double getMin(int signal) {
        Signal s = getSignal(signal);
        if (s == null) return Double.NaN;        
        return s.getMin();
    }
    public double getMax(int signal) {
        Signal s = getSignal(signal);
        if (s == null) return Double.NaN;        
        return s.getMax();
    }
    
    //TODO make a batch version of this
    public void updateBounds(int signal) {
        
        Signal s = getSignal(signal);
        s.resetBounds();
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;


        Iterator<Object[]> ii = iterator(); //signal);
        while (ii.hasNext()) {
            Object e = ii.next()[signal];
            if (e instanceof Number) {
                double d = ((Number)e).doubleValue();
                if (d < min) min = d;
                if (d > max) max = d;                
            }
        }
        s.setMin(min);
        s.setMax(max);
    }

    public SignalData newSignalData(String n) {
        Signal s = getSignal(n);
        if (s == null) return null;
        return new SignalData(this, s);
    }

    public Metrics addMeters(Meter... c) {
        for (Meter x : c)
            addMeter(x);
        return this;
    }

    public <M extends Meter<?>> M getMeter(String id) {
        int i = indexOf(id);
        if (i == -1) return null;
        return (M) meters.get(i);
    }

    private static class firstColumnIterator implements Function<Object[], Object[]> {
        final Object[] next;
        final int thecolumn;
        private final int[] columns;

        public firstColumnIterator(int... columns) {
            this.columns = columns;
            next = new Object[1];
            thecolumn = columns[0];
        }

        @Override public Object[] apply(Object[] f) {
            next[0] = f[thecolumn];
            return next;
        }
    }

    private static class nColumnIterator implements Function<Object[], Object[]> {

        final Object[] next;
        private final int[] columns;

        public nColumnIterator(int... columns) {
            this.columns = columns;
            next = new Object[columns.length];
        }

        @Override
        public Object[] apply(Object[] f) {

            int j = 0;
            for (int c : columns) {
                next[j++] = f[c];
            }
            return next;
        }

    }


    /** generates the value of the first entry in each row */
    class RowKeyMeter extends FunctionMeter {

        public RowKeyMeter() {
            super("key");
        }

        @Override
        protected RowKey getValue(Object key, int index) {
            return nextRowKey;
        }
        
    }
    
    private RowKey nextRowKey = null;
    
    /** the columns of the table */
    private final List<Meter<?>> meters = new ArrayList<>();
    private final ArrayDeque<Object[]> rows = new ArrayDeque<>();
    
    transient private List<Signal> signalList = new ArrayList<>();
    transient private Map<String, Integer> signalIndex = new HashMap();
    
    int numColumns;
    
    /** capacity */
    int history;

    /** unlimited size */
    public Metrics() {
        this(-1);
    }

    /** fixed size */
    public Metrics(int historySize) {
        super();
        this.history = historySize;
        
        addMeter(new RowKeyMeter());
    }
    

    public void clear() {
        clearData();
        clearSignals();
    }
    
    public void clearSignals() {
        numColumns = 0;
        signalList = null;
        signalIndex = null;
    }
    
    public void clearData() {
        rows.clear();
    }
    
    public <M extends Meter<C>, C extends Cell> M addMeter(M m) {
        for (Signal s : m.getSignals()) {
            if (getSignal(s.id)!=null)
                throw new RuntimeException("Signal " + s.id + " already exists in "+ this);
        }
        
        meters.add(m);
        numColumns+= m.numSignals();
        
        signalList = null;
        signalIndex = null;
        return m;
    }
    
    public void removeMeter(Meter<? extends Cell> m) {
        throw new RuntimeException("Removal not supported yet");
    }
    
    /** generate the next row.  key can be a time number, or some other unique-like identifying value */
    public synchronized <R extends RowKey> void update(R key) {
        nextRowKey = key;        
        
        boolean[] extremaToInvalidate = new boolean[ numColumns ];
        
        Object[] nextRow = new Object[ numColumns ];
        append(nextRow, extremaToInvalidate); //append it so that any derivative columns further on can work with the most current data (in lower array indices) while the array is being formed

        int c = 0;
        for (Meter m : meters) {
            Cell[] v = ((Meter<? extends Cell>)m).sample(key);
            if (v == null) continue;
            int vl = v.length;

            if (c + vl > nextRow.length) 
                throw new RuntimeException("column overflow: " + m + ' ' + c + '+' + vl + '>' + nextRow.length);

            if (vl == 1) {
                nextRow[c++] = v[0];
            }
            else if (vl == 2) {
                nextRow[c++] = v[0];
                nextRow[c++] = v[1];
            }
            else {
                System.arraycopy(v, 0, nextRow, c, vl);
                c += vl;
            }

        }
        
        invalidateExtrema(true, nextRow, extremaToInvalidate);
   
        
        for (int i = 0; i < getSignals().size(); i++) {
            if (i == 0) extremaToInvalidate[i] = true;
            if (extremaToInvalidate[i]) {        
                updateBounds(i);
            }
            //if (i == 0) System.out.println(get extremaToInvalidate[0] + " "  + history);
        }
        
    }
    
    private void invalidateExtrema(boolean added, Object[] row, boolean[] extremaToInvalidate) {
        for (int i = 0; i < row.length; i++) {
            Object ri = row[i];
            if (ri == null || !(ri instanceof Number)) continue;
            
            double n = ((Number)row[i]).doubleValue();
            if (Double.isNaN(n)) continue;
            
            double min = getMin(i);
            double max = getMax(i);
            
            boolean minNAN = Double.isNaN(min);
            boolean maxNAN = Double.isNaN(max);
            
            if (added) {
                //for rows which have been added
                if ((minNAN) || (n < min))  {                     
                    setMin(i, n);
                }
                if ((maxNAN) || (n > max))  { 
                    setMax(i, n);
                }
            }
            else {
                //for rows which have been removed
                if (minNAN || (n == min))  { extremaToInvalidate[i] = true; continue; }
                if (maxNAN || (n == max))  { extremaToInvalidate[i] = true; continue; }
            }
                
        }
    }
    
    
    protected void append(Object[] next, boolean[] invalidatedExtrema) {
        if (next==null) return;        

        if (history > 0) {
            while (rows.size() >= history) {
                Object[] prev = rows.removeFirst();
                invalidateExtrema(false, prev, invalidatedExtrema);
            }
        }
        
        rows.addLast(next);     

    }
    
    public List<Signal> getSignals() {
        if (signalList == null) {
            signalList = new ArrayList(numColumns);
            for (Meter<?> m : meters)
                signalList.addAll(m.getSignals());
        }
        return signalList;        
    }
    
    public Map<String,Integer> getSignalIndex() {
        if (signalIndex == null) {
            signalIndex = new HashMap(numColumns);
            int i = 0;
            for (Signal s : getSignals()) {
                signalIndex.put(s.id, i++);
            }
        }
        return signalIndex;
    }
    
    public int indexOf(Signal s) {
        return indexOf(s.id);
    }
    
    public int indexOf(String signalID) {
        Integer i = getSignalIndex().get(signalID);
        if (i == null) return -1;
        return i;
    }
    
    public Signal getSignal(int index) {
       return getSignals().get(index); 
    }
    public Signal getSignal(String s) {
       if (s == null) return null;
       int ii = indexOf(s);
       if (ii == -1) return null;
       return getSignals().get(ii); 
    }    
    
    public Object[] rowFirst() { return rows.getFirst(); }
    public Object[] rowLast() { return rows.getLast(); }
    public int numRows() { return rows.size(); }
    
    @Override
    public Iterator<Object[]> iterator() {        
        return rows.iterator();
    }
    
    public Iterator<Object[]> reverseIterator() {        
        return rows.descendingIterator();
    }
    
    
    public Object[] getData(int signal, Object[] c) {        
        if ((c == null) || (c.length != numRows() )) 
            c = new Object[ numRows() ];
        
        int r = 0;
        for (Object[] row : this) {
            c[r++] = row[signal];
        }
        
        return c;
    }
    
    public Object[] getData(int signal) {        
        return getData(signal, null);
    }
    
    //Table<Signal,Object (row),Object (value)> getSignalTable(int columns...)
    
    /*    public Signal getSignal(int column) {
        int p = 0;
        for (Meter<?> m : meters) {
            int s = m.numSignals();            
            if (column < p + s) {
                return m.getSignals().get(column - p);
            }
            p += s;
        }
        return null;
    }*/
    
    public Iterator<Object> iterateSignal(int column, boolean reverse) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Iterator<Object[]> iterator(final int... columns) {
        if (columns.length == 1) {
            //fast 1-argument
            return Iterators.transform(iterator(), new firstColumnIterator(columns));
        }
                
        return Iterators.transform(iterator(), new nColumnIterator(columns));
    }

    public static List<Double> doubles(Iterable<Object> l) {
        List<Double> r = new ArrayList();
        for (Object o : l)
            if (o instanceof Number) r.add(((Number)o).doubleValue());
        return r;
    }


    public List<Object> getNewSignalValues(int column, int num) {
        List<Object> l = new ArrayList(num);
        Iterator<Object[]> r = reverseIterator();        
        while (r.hasNext() && num > 0) {
            l.add(r.next()[column]);
            num--;
        }
        return l;
    }
    
    public String[] getSignalIDs() {
        String[] r = new String[getSignals().size()];
        int i = 0;
        for (Signal s : getSignals()) {
            r[i++] = s.id;
        }
        return r;
    }

    public void printCSVHeader(PrintStream out) {
        printJSONArray(out, getSignalIDs(),false );
    }
    public void printCSVLastLine(PrintStream out) {
        if (rows.size() > 0)
            printJSONArray(out, rowLast(), false);
    }

    public void printCSV(PrintStream out) {
        printCSVHeader(out);
        for (Object[] row : this) {
            printJSONArray(out, row, false);
        }
    }

    public String name() {
        return getClass().getSimpleName();
    }


    public void printARFF(PrintStream out) {
        printARFF(out, null);
    }

    public void printARFF(PrintStream out, Predicate<Object[]> p) {
        //http://www.cs.waikato.ac.nz/ml/weka/arff.html
        out.println("@RELATION " + name());


        int n = 0;
        for (Meter<?> m : meters) {
            for (Signal s : m.getSignals()) {
                if (n == 0) {
                    //key, for now we'll use string
                    out.println("@ATTRIBUTE " + s.id + " STRING");
                }
                else if ((m instanceof DoubleMeter) || (m instanceof HitMeter)) {
                    out.println("@ATTRIBUTE " + s.id + " NUMERIC");
                }
                else {
                    out.println("@ATTRIBUTE " + s.id + " STRING");
                    //TODO use nominal by finding the unique values
                }
                n++;
            }
        }

        out.print('%'); //ARFF comment character
        printCSVHeader(out);

        out.println("@DATA");
        for (Object[] x : this) {
            if (p!=null)
                if (!p.apply(x)) continue;
            for (int i = 0; i < numColumns; i++) {
                if (i < x.length) {
                    Object y = x[i];
                    if (y == null)
                        out.print('?');
                    else if (y instanceof Number)
                        out.print(y);
                    else
                        out.print('\"' + y.toString() + '\"');
                }
                else {
                    //pad extra values with '?'
                    out.print('?');
                }
                if (i!=numColumns-1)
                    out.print(',');
                else
                    out.println();
            }

        }
    }

}
