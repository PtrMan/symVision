/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ptrman.meter;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import java.util.*;

/**
 * A tabular data store where each (# indexed) column represents a different
 * type of value, and each row is a that value sampled/recorded at a different
 * time point (first column).
 * 
 */
public class Metrics<RowKey,Cell> implements Iterable<Object[]> {

    final static int PRECISION = 4;
//    public final static Gson json = new GsonBuilder()
//             .registerTypeAdapter(Double.class, new JsonSerializer<Double>()  {
//                        @Override
//                        public JsonElement serialize(Double value, Type theType,
//JsonSerializationContext context) {
//                                if (value.isNaN()) {
//                                        return new JsonPrimitive("NaN");
//                                } else if (value.isInfinite()) {
//                                        return new JsonPrimitive(value);
//                                } else {
//                                        return new JsonPrimitive(
//                                                new BigDecimal(value).
//                                                    setScale(PRECISION,
//                                                    BigDecimal.ROUND_HALF_UP).stripTrailingZeros());
//                                }
//                        }
//                })
//                .create();
//
//
//    public static void printJSONArray(PrintStream out, Object[] row, boolean includeBrackets) {
//        String r = json.toJson(row);
//        if (!includeBrackets) {
//            r = r.substring(1, r.length()-1);
//        }
//        out.println(r);
//
//    }

    
    /**
    *  Calculates a 2-tuple with the following data:
    *   0: minimum value among all columns in given signals
    *   1: maximum value among all columns in given signals
    * 
    * @param data
    * @return 
    */
    public static double[] getBounds(final Iterable<SignalData> data) {
        var min = Double.POSITIVE_INFINITY;
        var max = Double.NEGATIVE_INFINITY;
    
        for (final var ss : data) {
            final var a = ss.getMin();
            final var b = ss.getMax();
            if (a < min) min = a;
            if (b > max) max = b;
        }
        return new double[] { min, max };        
    }
    
    protected void setMin(final int signal, final double n) {
        getSignal(signal).setMin(n);
    }
    protected void setMax(final int signal, final double n) {
        getSignal(signal).setMax(n);
    }
    
    public double getMin(final int signal) {
        final var s = getSignal(signal);
        if (s == null) return Double.NaN;        
        return s.getMin();
    }
    public double getMax(final int signal) {
        final var s = getSignal(signal);
        if (s == null) return Double.NaN;        
        return s.getMax();
    }
    
    //TODO make a batch version of this
    public void updateBounds(final int signal) {
        
        final var s = getSignal(signal);
        s.resetBounds();
        var min = Double.POSITIVE_INFINITY;
        var max = Double.NEGATIVE_INFINITY;


		//signal);
		for (final var objects : this) {
			final var e = objects[signal];
			if (e instanceof Number) {
				final var d = ((Number) e).doubleValue();
				if (d < min) min = d;
				if (d > max) max = d;
			}
		}
        s.setMin(min);
        s.setMax(max);
    }

    public SignalData newSignalData(final String n) {
        final var s = getSignal(n);
        if (s == null) return null;
        return new SignalData(this, s);
    }

    public Metrics addMeters(final Meter... c) {
        for (final var x : c)
            addMeter(x);
        return this;
    }

    public <M extends Meter<?>> M getMeter(final String id) {
        final var i = indexOf(id);
        if (i == -1) return null;
        return (M) meters.get(i);
    }

    private static class firstColumnIterator implements Function<Object[], Object[]> {
        final Object[] next;
        final int thecolumn;
        private final int[] columns;

        public firstColumnIterator(final int... columns) {
            this.columns = columns;
            next = new Object[1];
            thecolumn = columns[0];
        }

        @Override public Object[] apply(final Object[] f) {
            next[0] = f[thecolumn];
            return next;
        }
    }

    private static class nColumnIterator implements Function<Object[], Object[]> {

        final Object[] next;
        private final int[] columns;

        public nColumnIterator(final int... columns) {
            this.columns = columns;
            next = new Object[columns.length];
        }

        @Override
        public Object[] apply(final Object[] f) {

            var j = 0;
            for (final var c : columns) next[j++] = f[c];
            return next;
        }

    }


    /** generates the value of the first entry in each row */
    class RowKeyMeter extends FunctionMeter {

        public RowKeyMeter() {
            super("key");
        }

        @Override
        protected RowKey getValue(final Object key, final int index) {
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
	final int history;

    /** unlimited size */
    public Metrics() {
        this(-1);
    }

    /** fixed size */
    public Metrics(final int historySize) {
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
    
    public <M extends Meter<C>, C extends Cell> M addMeter(final M m) {
        for (final var s : m.getSignals())
            assert getSignal(s.id) == null : "Signal " + s.id + " already exists in " + this;
        
        meters.add(m);
        numColumns+= m.numSignals();
        
        signalList = null;
        signalIndex = null;
        return m;
    }
    
    public void removeMeter(final Meter<? extends Cell> m) {
        throw new RuntimeException("Removal not supported yet");
    }
    
    /** generate the next row.  key can be a time number, or some other unique-like identifying value */
    public synchronized <R extends RowKey> void update(final R key) {
        nextRowKey = key;        
        
        final var extremaToInvalidate = new boolean[ numColumns ];
        
        final var nextRow = new Object[ numColumns ];
        append(nextRow, extremaToInvalidate); //append it so that any derivative columns further on can work with the most current data (in lower array indices) while the array is being formed

        var c = 0;
        for (final Meter m : meters) {
            final var v = ((Meter<? extends Cell>)m).sample(key);
            if (v == null) continue;
            final var vl = v.length;

            assert c + vl <= nextRow.length : "column overflow: " + m + ' ' + c + '+' + vl + '>' + nextRow.length;

            if (vl == 1) nextRow[c++] = v[0];
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
   
        
        for (var i = 0; i < getSignals().size(); i++) {
            if (i == 0) extremaToInvalidate[i] = true;
            if (extremaToInvalidate[i]) updateBounds(i);
            //if (i == 0) System.out.println(get extremaToInvalidate[0] + " "  + history);
        }
        
    }
    
    private void invalidateExtrema(final boolean added, final Object[] row, final boolean[] extremaToInvalidate) {
        for (var i = 0; i < row.length; i++) {
            final var ri = row[i];
            if (!(ri instanceof Number)) continue;
            
            final var n = ((Number)row[i]).doubleValue();
            if (Double.isNaN(n)) continue;
            
            final var min = getMin(i);
            final var max = getMax(i);
            
            final var minNAN = Double.isNaN(min);
            final var maxNAN = Double.isNaN(max);
            
            if (added) {
                //for rows which have been added
                if ((minNAN) || (n < min)) setMin(i, n);
                if ((maxNAN) || (n > max)) setMax(i, n);
            }
            else {
                //for rows which have been removed
                if (minNAN || (n == min))  { extremaToInvalidate[i] = true; continue; }
                if (maxNAN || (n == max))  { extremaToInvalidate[i] = true; continue; }
            }
                
        }
    }
    
    
    protected void append(final Object[] next, final boolean[] invalidatedExtrema) {
        if (next==null) return;        

        if (history > 0) while (rows.size() >= history) {
            final var prev = rows.removeFirst();
            invalidateExtrema(false, prev, invalidatedExtrema);
        }
        
        rows.addLast(next);     

    }
    
    public List<Signal> getSignals() {
        if (signalList == null) {
            signalList = new ArrayList(numColumns);
            for (final var m : meters)
                signalList.addAll(m.getSignals());
        }
        return signalList;        
    }
    
    public Map<String,Integer> getSignalIndex() {
        if (signalIndex == null) {
            signalIndex = new HashMap(numColumns);
            var i = 0;
            for (final var s : getSignals()) signalIndex.put(s.id, i++);
        }
        return signalIndex;
    }
    
    public int indexOf(final Signal s) {
        return indexOf(s.id);
    }
    
    public int indexOf(final String signalID) {
        final var i = getSignalIndex().get(signalID);
        if (i == null) return -1;
        return i;
    }
    
    public Signal getSignal(final int index) {
       return getSignals().get(index); 
    }
    public Signal getSignal(final String s) {
       if (s == null) return null;
       final var ii = indexOf(s);
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
    
    
    public Object[] getData(final int signal, Object[] c) {
        if ((c == null) || (c.length != numRows() )) 
            c = new Object[ numRows() ];

        var r = 0;
        for (final var row : this) c[r++] = row[signal];
        
        return c;
    }
    
    public Object[] getData(final int signal) {
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
    
    public static Iterator<Object> iterateSignal(final int column, final boolean reverse) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Iterator<Object[]> iterator(final int... columns) {
        //fast 1-argument
        if (columns.length == 1) return Iterators.transform(iterator(), new firstColumnIterator(columns));
                
        return Iterators.transform(iterator(), new nColumnIterator(columns));
    }

    public static List<Double> doubles(final Iterable<Object> l) {
        final List<Double> r = new ArrayList();
        for (final var o : l)
            if (o instanceof Number) r.add(((Number)o).doubleValue());
        return r;
    }


    public List<Object> getNewSignalValues(final int column, int num) {
        final List<Object> l = new ArrayList(num);
        final var r = reverseIterator();
        while (r.hasNext() && num > 0) {
            l.add(r.next()[column]);
            num--;
        }
        return l;
    }
    
    public String[] getSignalIDs() {
        final var r = new String[getSignals().size()];
        var i = 0;
        for (final var s : getSignals()) r[i++] = s.id;
        return r;
    }

}
