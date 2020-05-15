package ptrman.misc;

import ptrman.Datastructures.IMap2d;

import java.util.HashMap;
import java.util.Map;

import ptrman.math.MapTvUtils;
import ptrman.math.Tv;

import java.util.ArrayList;
import java.util.List;

// see https://www.foundalis.com/res/Unification_of_Clustering_Concept_Formation_Categorization_and_Analogy_Making.pdf
// TODO< implement revision with a flag, only with found best category above threshold! >
// TODO< implement categories with multiple exemplars >
public class MultilayerClassifier {
    public float r = 2.0f; // used to compute distance

    public float q = 1.5f; // used for computing similarity
    public float c = 1.0f; // how many categories should it build?

    public float minSimilarity = 0.0f; // minimal similarity to create new category

    // categories/classes
    public List<Category> categories = new ArrayList<>();

    public long categoryIdCounter = 1;

    public boolean verbose = false;

    ////////////////
    // "high level" classifier

    public float bestCategorySimilarity = 0;

    public String lastClassfnMsg = ""; // "message" of last classification
    // "NOCLASS" : was no class found?
    // "BELOWT" : was classification below thresold?
    // "OK" : all fine
    public long classify(Map<Long, List<Tv>> stimulus, boolean add) {
        bestCategorySimilarity = 0;
        lastClassfnMsg = "NOCLASS";
        long bestCategoryId = -1; // -1 : invalid

        {
            // for testing
            //float d = calcDist(stimulus, stimulus);
        }

        for(Category iCat : categories) {
            float d = calcDist(stimulus, iCat.examplars.get(0));
            float sim = calcSim(d);
            if (sim > bestCategorySimilarity) {
                lastClassfnMsg = "BELOWT";
                bestCategorySimilarity = sim;
                bestCategoryId = iCat.categoryId;
            }
        }

        if(verbose) System.out.println("CLASSIFIER: class="+bestCategoryId+"   best similarity="+bestCategorySimilarity);// DEBUG

        if(lastClassfnMsg.equals("BELOWT") && bestCategorySimilarity > minSimilarity) {
            lastClassfnMsg = "OK";
        }

        if (add && bestCategorySimilarity == Float.POSITIVE_INFINITY) { // was no class found?
            // add new one
            bestCategoryId = categoryIdCounter++;
            categories.add(Category.makeSingleExemplar(stimulus, bestCategoryId));
        }
        else if (add && bestCategorySimilarity < minSimilarity) { // was no close enough category found?
            // add new one
            bestCategoryId = categoryIdCounter++;
            categories.add(Category.makeSingleExemplar(stimulus, bestCategoryId));
        }

        return bestCategoryId;
    }

    // computes distance between exemplars, in this case maps of TV's
    public static float calcDist(Map<Long, List<Tv>> a, Map<Long, List<Tv>> b) {

        // we can only compute overlaps when there are same classes

        Map<Long, Integer> commonCounter = new HashMap<>();
        for(long ia : a.keySet()) {
            if(!commonCounter.containsKey(ia)) {
                commonCounter.put(ia, 0);
            }
            commonCounter.put(ia, commonCounter.get(ia)+1); // count up
        }
        for(long ib : b.keySet()) {
            if(!commonCounter.containsKey(ib)) {
                commonCounter.put(ib, 0);
            }
            commonCounter.put(ib, commonCounter.get(ib)+1); // count up
        }

        List<Tv> alist = new ArrayList<>();
        List<Tv> blist = new ArrayList<>();

        // compose by common keys
        for(Map.Entry<Long, Integer> iKeyVal : commonCounter.entrySet()) {
            long key = iKeyVal.getKey();
            int cnt = iKeyVal.getValue();
            if(cnt == 2) { // is common?
                List<Tv> amap = a.get(key);
                List<Tv> bmap = b.get(key);
                alist.addAll(amap);
                blist.addAll(bmap);
            }
        }
        // compose by uncommon keys
        for(Map.Entry<Long, Integer> iKeyVal : commonCounter.entrySet()) {
            long key = iKeyVal.getKey();
            int cnt = iKeyVal.getValue();
            if(cnt == 1) { // is not common?
                alist.add(new Tv(0.0f, 0.02f));
                alist.add(new Tv(0.0f, 0.02f));
                blist.add(new Tv(0.0f, 0.02f));
                blist.add(new Tv(0.0f, 0.02f));
            }
        }

        // fold
        Tv[] resemblance = MapTvUtils.resemblance(alist, blist); // how similar are they for each TV?
        Tv merged = MapTvUtils.calcMergedTv(resemblance); // how similar are they as one TV?

        return merged.freq;
    }

    // calculate similarity
    // /param d distance
    public float calcSim(float d) {
        return (float)Math.exp(-Math.pow(c*d, q));
    }

    // contemporary called the "class"
    public static class Category {
        public ArrayList<Map<Long, List<Tv>>> examplars;
        public long categoryId;

        public Category(ArrayList<Map<Long, List<Tv>>> examplars, long categoryId) {
            this.examplars = examplars;
            this.categoryId = categoryId;
        }

        public static Category makeSingleExemplar(Map<Long, List<Tv>> ex, long categoryId) {
            ArrayList<Map<Long, List<Tv>>> arr = new ArrayList<>();
            arr.add(ex);
            return new Category(arr, categoryId);
        }
    }
}
