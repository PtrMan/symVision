package ptrman.misc;

import ptrman.math.MapTvUtils;
import ptrman.math.Tv;

import java.util.ArrayList;
import java.util.List;

// see https://www.foundalis.com/res/Unification_of_Clustering_Concept_Formation_Categorization_and_Analogy_Making.pdf
// TODO< implement categories with multiple exemplars >
public class TvClassifier {
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
    public long classify(Tv[] stimulus, boolean add) {
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


    ////////////////
    // "low level"

    // calculate distance of exemplars
    // assumes uniform weights for simplicity
    public float calcDist(Tv[] aArr, Tv[] bArr) {
        /* old distance computation for real values
        float d = 0.0f;
        for(int i=0;i<aArr.length;i++) {
            float w = 1.0f / aArr.length; // weight

            float diff = 0.0f;
            // old code for real values:
            //float a = (float)aArr.getDataRef()[i];
            //float b = (float)bArr.getDataRef()[i];
            //diff = a-b;

            d += (w*(float)Math.pow(Math.abs(diff), r));
        }
         */

        Tv[] resemblance = MapTvUtils.resemblance(aArr, bArr); // how similar are they for each TV?
        Tv merged = MapTvUtils.calcMergedTv(resemblance); // how similar are they as one TV?

        //return (float)Math.pow(d, 1.0f / r);
        return merged.freq;
    }

    // calculate similarity
    // /param d distance
    public float calcSim(float d) {
        return (float)Math.exp(-Math.pow(c*d, q));
    }

    // contemporary called the "class"
    public static class Category {
        public ArrayList<Tv[]> examplars;
        public long categoryId;

        public Category(ArrayList<Tv[]> examplars, long categoryId) {
            this.examplars = examplars;
            this.categoryId = categoryId;
        }

        public static Category makeSingleExemplar(Tv[] ex, long categoryId) {
            ArrayList<Tv[]> arr = new ArrayList<>();
            arr.add(ex);
            return new Category(arr, categoryId);
        }
    }
}
