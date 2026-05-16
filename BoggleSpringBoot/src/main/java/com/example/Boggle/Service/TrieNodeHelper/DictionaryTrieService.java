package com.example.Boggle.Service.TrieNodeHelper;

import com.example.Boggle.Model.Tables.Dictionary;
import com.example.Boggle.repository.DictionaryRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provides dictionary word lookup support for board word search.
 *
 * <p>This service manages dictionary lookup state used by higher-level
 * word-search functionality.
 *
 * <p>Side effects: may initialize in-memory dictionary lookup data during setup.
 *
 */
@Service
public class DictionaryTrieService {
    private final DictionaryRepository dictionaryRepository;
    private TrieNode root = new TrieNode();

    /**
     * Creates a dictionary lookup service
     *
     * @param dictionaryRepository repository used to load dictionary entries
     */
    public DictionaryTrieService(DictionaryRepository dictionaryRepository){
        this.dictionaryRepository = dictionaryRepository;
    }

    /**
     * Initializes in-memory dictionary lookup data from persisted dictionary entries
     *
     * <p> This will populate TrieNode upon service launch.
     */
    @PostConstruct
    public void init(){
        List<Dictionary> words = dictionaryRepository.findAll();
        for (Dictionary entry: words){
            addWordToTrie(entry.getWord(),entry.getPointValue());
        }

    }

    /**
     * Adds a dictionary word to in-memory lookup state.
     *
     * @param word dictionary word to add
     * @param pointValue point value associated with the word
     */
    private void addWordToTrie(String word, int pointValue){
        if(word==null){
            return;
        }
        String normalized = word.trim().toUpperCase();

        TrieNode current = root;

        for(char ch: normalized.toCharArray()){
            current = current.children.computeIfAbsent(ch, character -> new TrieNode());
        }
        current.isWord = true;
        current.pointValue = pointValue;
        current.word = normalized;
    }

    /**
     * Returns the root dictionary lookup state.
     *
     * @return root trie node
     */
    public TrieNode getRoot() {
        return root;
    }

}
