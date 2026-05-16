package com.example.Boggle.Service.TrieNodeHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a node used for dictionary word lookup.
 *
 * <p>This class stores lookup state for a word path.
 */
public class TrieNode {
    public Map<Character,TrieNode> children = new HashMap<>();
    public boolean isWord;
    public Integer pointValue;
    public String word;


}
