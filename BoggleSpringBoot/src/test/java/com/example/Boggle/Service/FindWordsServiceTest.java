package com.example.Boggle.Service;


import com.example.Boggle.Service.TrieNodeHelper.DictionaryTrieService;
import com.example.Boggle.Service.TrieNodeHelper.TrieNode;
import com.example.Boggle.Service.TrieNodeHelper.WordCandidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FindWordsService}.
 *
 * <p>These tests focus on confirming correct
 * behavior from DFS traversal and the correct word being returned.
 */
public class FindWordsServiceTest {

    private DictionaryTrieService dictionaryTrieService;
    private FindWordsService findWordsService;

    /**
     * Initializes mocked dependencies and the service under test before each test.
     */
    @BeforeEach
    void setUp(){
        dictionaryTrieService = mock(DictionaryTrieService.class);
        findWordsService = new FindWordsService(dictionaryTrieService);
    }


    /**
     * Build simple trie to avoid depending on other classes.
     *
     * @param word the word to add
     * @param points the point value of the word
     * @return root trie node
     */
    private TrieNode buildTrieForSingleWord(String word, int points){
        TrieNode root = new TrieNode();
        TrieNode current = root;

        for(char ch: word.toUpperCase().toCharArray()){
            current.children.putIfAbsent(ch, new TrieNode());
            current = current.children.get(ch);
        }

        current.isWord = true;
        current.pointValue = points;
        current.word = word.toUpperCase();

        return root;
    }

    /**
     * Tests that dfs finds a word that exists in the trie and can be formed on the board.
     */
    @Test
    void testDfsFindsExistingWord(){
        TrieNode root = buildTrieForSingleWord("CAT", 2);

        String[][] board = {
                {"C", "A", "T", "X"},
                {"X", "X", "X", "X"},
                {"X", "X", "X", "X"},
                {"X", "X", "X", "X"}
        };

        boolean[][] visited = new boolean[4][4];
        Map<String, WordCandidate> foundWords = new HashMap<>();

        findWordsService.dfs(board, 0, 0, root, visited, foundWords);

        assertEquals(1, foundWords.size());
        assertTrue(foundWords.containsKey("CAT"));
        assertEquals("CAT", foundWords.get("CAT").getWord());
        assertEquals(2, foundWords.get("CAT").getPoints());
    }

    /**
     * Tests that findValidWords returns words found on the full flattened board string.
     */
    @Test
    void testFindValidWords(){
        TrieNode root = buildTrieForSingleWord("CAT", 2);
        when(dictionaryTrieService.getRoot()).thenReturn(root);

        List<WordCandidate> result = findWordsService.findValidWords("CATS\nXXXX\nXXXX\nXXXX");

        assertEquals(1, result.size());
        assertEquals("CAT", result.get(0).getWord());
    }

}
