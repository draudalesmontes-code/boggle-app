package com.example.Boggle.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class that builds and exposes a weighted Boggle letter bag.
 *
 * <p>Each letter appears in the bag a number of times based on its configured
 * frequency in {@code WEIGHTED_BAG}. The resulting {@code BAG} can be used
 * for randomized board generation where more common letters should appear
 * more often than rare letters.
 */
public class BoggleBag {

    /**
     * Mapping of each uppercase letter to the number of times it should appear
     * in the weighted bag.
     */
    private static final Map<String, Integer> WEIGHTED_BAG =Map.ofEntries(
            Map.entry("A", 9), Map.entry("B", 2), Map.entry("C", 2), Map.entry("D", 4),
            Map.entry("E", 12), Map.entry("F", 2), Map.entry("G", 3), Map.entry("H", 2),
            Map.entry("I", 9), Map.entry("J", 1), Map.entry("K", 1), Map.entry("L", 4),
            Map.entry("M", 2), Map.entry("N", 6), Map.entry("O", 8), Map.entry("P", 2),
            Map.entry("Q", 1), Map.entry("R", 6), Map.entry("S", 4), Map.entry("T", 6),
            Map.entry("U", 4), Map.entry("V", 2), Map.entry("W", 2), Map.entry("X", 1),
            Map.entry("Y", 2), Map.entry("Z", 1)
    );
    /**
     * Precomputed weighted bag of letters generated from {@code WEIGHTED_BAG}.
     */
    private static final List<String> BAG = bagMaker();


    /**
     * Builds the weighted letter bag by repeating each letter according to
     * its configured frequency.
     *
     * @return a list containing repeated letter entries based on the weights
     */
    private static List<String> bagMaker(){
        List<String> bag =  new ArrayList<>();

        for(String letter: WEIGHTED_BAG.keySet()){
            int count = WEIGHTED_BAG.get(letter);
            for(int i =0; i <count; i++){
                bag.add(letter);
            }
        }
        return bag;
    }

    /**
     * Returns the prebuilt weighted Boggle letter bag.
     *
     * @return the weighted letter bag
     */
    public static List<String> getBag() {
        return BAG;
    }

}
