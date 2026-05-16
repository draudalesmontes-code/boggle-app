package com.example.Boggle.Service;

import com.example.Boggle.Service.TrieNodeHelper.DictionaryTrieService;
import com.example.Boggle.Service.TrieNodeHelper.TrieNode;
import com.example.Boggle.Service.TrieNodeHelper.WordCandidate;
import com.example.Boggle.util.unflatten;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides word-search functionality for a Boggle board.
 *
 * <p>This service returns valid word candidates that can be formed from a board.
 *
 *
 * <p>For additional details about dictionary lookup behavior, see
 * {@link DictionaryTrieService},
 * {@link TrieNode}, and
 * {@link WordCandidate}.
 */
@Service
public class FindWordsService {

    /**
     * possible Direction of traversal
     */
    private static final int [][] DIRECTION = {
            {-1,-1},{-1,0},{-1,1},
            {0,-1},        {0, 1},
            {1,-1}, {1,0}, {1, 1}
    };

    private final DictionaryTrieService dictionaryTrieService;

    /**
     * Creates a word-search service.
     *
     * @param dictionaryTrieService service that provides dictionary lookup support
     */
    public FindWordsService(DictionaryTrieService dictionaryTrieService) {
        this.dictionaryTrieService = dictionaryTrieService;
    }

    /**
     * Returns valid word candidates that can be formed on a board.
     *
     * @param flattenedBoard The flattened String representation of the Board
     * @return An ArrayList of all words on the board.
     */
    public List<WordCandidate> findValidWords(String flattenedBoard) {
        String[][] board = unflatten.parseStringTo4x4(flattenedBoard);
        TrieNode root = dictionaryTrieService.getRoot();

        Map<String, WordCandidate> foundWords = new HashMap<>();
        boolean[][] visited = new boolean[board.length][board[0].length];

        for(int row = 0; row < 4; row++){
            for(int col = 0; col < 4; col++){
                dfs(board,row,col,root,visited,foundWords);
            }
        }

        return new ArrayList<>(foundWords.values());
    }

    /**
     * Explores word candidates starting from a board position.
     *
     * <p>Updates the provided collection of discovered words during traversal.
     *
     * @param board The board being analyzed for all its words.
     * @param row The row being explored.
     * @param col  The column being explored.
     * @param currentNode The TrieNode node currently being explored to see if we reached end of word.
     * @param visited Array that keeps track of tile visited.
     * @param foundWords  Stores the words found on the board.
     */
    public void dfs (String[][]board, int row, int col, TrieNode currentNode, boolean[][] visited,
                     Map<String,WordCandidate> foundWords){

        if (row < 0 || row >= board.length || col < 0 || col >= board[0].length) {
            return;
        }

        if(visited[row][col]){
            return;
        }

        char letter = Character.toUpperCase(board[row][col].charAt(0));
        TrieNode nextNode = currentNode.children.get(letter);

        if(nextNode == null){
            return;
        }

        visited[row][col] = true;

        if(nextNode.isWord && nextNode.word !=null && nextNode.word.length()>=3){
            foundWords.putIfAbsent(nextNode.word, new WordCandidate(nextNode.word,nextNode.pointValue));
        }

        for(int[]dir: DIRECTION){
            dfs(board,row + dir[0],col + dir[1], nextNode, visited, foundWords);
        }

        visited[row][col] = false;


    }

}
