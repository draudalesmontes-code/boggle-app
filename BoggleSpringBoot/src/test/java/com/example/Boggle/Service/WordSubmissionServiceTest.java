package com.example.Boggle.Service;

import com.example.Boggle.Model.Tables.*;
import com.example.Boggle.repository.DictionaryRepository;
import com.example.Boggle.repository.FoundWordRepository;
import com.example.Boggle.repository.GameRepository;
import com.example.Boggle.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.DisplayName;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WordSubmissionService.
 *
 * These tests verify the full word-submission workflow for a Boggle game.
 * The service is responsible for validating whether a submitted word:
 *
 *  - belongs to an existing game
 *  - is submitted by a valid player in that game
 *  - exists in the dictionary
 *  - appears on the current board
 *  - has not already been submitted by the same player in the same game
 *  - can be safely saved without violating database uniqueness constraints
 *
 * Test coverage includes both successful submissions and several rejection paths.
 * Mock repositories are used so that service logic can be tested in isolation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Word Submission Service Tests")
class WordSubmissionServiceTest {

    /**
     * Repository used to load game state for submissions.
     */
    @Mock
    private GameRepository gameRepository;

    /**
     * Repository used to load and validate the submitting user.
     */
    @Mock
    private UserRepository userRepository;

    /**
     * Repository used to confirm whether a word exists in the dictionary.
     */
    @Mock
    private DictionaryRepository dictionaryRepository;

    /**
     * Repository used to detect duplicates and persist accepted words.
     */
    @Mock
    private FoundWordRepository foundWordRepository;

    /**
     * Service under test.
     * Mockito injects the mocked dependencies above into this service.
     */
    @InjectMocks
    private WordSubmissionService wordSubmissionService;

    /**
     * Test player one, used as a valid participant in the game.
     */
    private User player1;

    /**
     * Test player two, used as the second participant in the game.
     */
    private User player2;

    /**
     * Shared game object used by most tests.
     */
    private Game game;

    /**
     * Shared board used by most tests.
     * The board layout is:
     *
     * C A T S
     * D O G E
     * B I R D
     * F I S H
     *
     * This allows valid words such as CAT and DOG,
     * while rejecting words like COW as not being on the board.
     */
    private Board board;

    /**
     * Creates a consistent test environment before each test.
     *
     * This setup:
     *  - creates two users
     *  - assigns them IDs using reflection
     *  - creates a board string
     *  - creates a game containing both players and the board
     *
     * Using @BeforeEach keeps the tests short and avoids repeated setup code.
     */
    @BeforeEach
    void setUp() {
        player1 = new User("p1", "p1@test.com", "hash");
        player2 = new User("p2", "p2@test.com", "hash");
        setPrivateId(player1, 1);
        setPrivateId(player2, 2);

        board = new Board();
        board.setBoardId("board-1");
        board.setBoardString("CATS\nDOGE\nBIRD\nFISH");

        game = new Game();
        game.setId(100);
        game.setPlayer1(player1);
        game.setPlayer2(player2);
        game.setBoard(board);
        game.setStatus(GameStatus.IN_PROGRESS);

    }

    /**
     * Verifies that a submitted word is rejected when it does not exist
     * in the dictionary, even before board validation or saving occurs.
     *
     * Expected result:
     *  - accepted = false
     *  - reason = NOT_IN_DICTIONARY
     *  - normalized word is uppercase
     *  - no FoundWord entity is saved
     */
    @Test
    void submitWordRejectsWordNotInDictionary() {
        when(gameRepository.findById(100)).thenReturn(Optional.of(game));
        when(userRepository.findById(1)).thenReturn(Optional.of(player1));
        when(dictionaryRepository.findByWordIgnoreCase("COW")).thenReturn(Optional.empty());

        WordSubmissionService.Result result = wordSubmissionService.submitWord(100, 1, "cow");

        assertFalse(result.accepted);
        assertEquals(WordSubmissionService.SubmissionReason.NOT_IN_DICTIONARY, result.reason);
        assertEquals("COW", result.normalizedWord);
        verify(foundWordRepository, never()).save(any());
    }

    /**
     * Verifies that a word already submitted by the same player
     * in the same game is rejected as a duplicate.
     *
     * This test confirms that duplicate detection happens before saving.
     *
     * Expected result:
     *  - accepted = false
     *  - reason = DUPLICATE
     *  - no save operation occurs
     */
    @Test
    void submitWordRejectsDuplicateWordForSamePlayerInSameGame() {
        Dictionary dictionary = mock(Dictionary.class);
        when(dictionary.getId()).thenReturn(77);

        when(gameRepository.findById(100)).thenReturn(Optional.of(game));
        when(userRepository.findById(1)).thenReturn(Optional.of(player1));
        when(dictionaryRepository.findByWordIgnoreCase("CAT")).thenReturn(Optional.of(dictionary));
        when(foundWordRepository.existsByGame_IdAndPlayer_IdAndDictionaryWord_Id(100, 1, 77)).thenReturn(true);

        WordSubmissionService.Result result = wordSubmissionService.submitWord(100, 1, "cat");

        assertFalse(result.accepted);
        assertEquals(WordSubmissionService.SubmissionReason.DUPLICATE, result.reason);
        verify(foundWordRepository, never()).save(any());
    }

    /**
     * Verifies that a valid dictionary word appearing on the board
     * is accepted and saved correctly.
     *
     * This test also captures the saved FoundWord entity to verify that
     * the service stored the correct game, player, and dictionary entry.
     *
     * Expected result:
     *  - accepted = true
     *  - reason = OK
     *  - normalized word = CAT
     *  - points reflect dictionary metadata
     *  - FoundWord is persisted with correct relationships
     */
    @Test
    void submitWordAcceptsValidWordAndSavesFoundWord() {
        Dictionary dictionary = mock(Dictionary.class);
        when(dictionary.getId()).thenReturn(88);
        when(dictionary.getPointValue()).thenReturn(2);

        when(gameRepository.findById(100)).thenReturn(Optional.of(game));
        when(userRepository.findById(1)).thenReturn(Optional.of(player1));
        when(userRepository.getReferenceById(1)).thenReturn(player1);
        when(dictionaryRepository.findByWordIgnoreCase("CAT")).thenReturn(Optional.of(dictionary));
        when(foundWordRepository.existsByGame_IdAndPlayer_IdAndDictionaryWord_Id(100, 1, 88)).thenReturn(false);

        WordSubmissionService.Result result = wordSubmissionService.submitWord(100, 1, "cat");

        assertTrue(result.accepted);
        assertEquals(WordSubmissionService.SubmissionReason.OK, result.reason);
        assertEquals("CAT", result.normalizedWord);
        assertEquals(2, result.points);

        ArgumentCaptor<FoundWord> captor = ArgumentCaptor.forClass(FoundWord.class);
        verify(foundWordRepository).save(captor.capture());
        FoundWord saved = captor.getValue();
        assertEquals(game, saved.getGame());
        assertEquals(player1, saved.getPlayer());
        assertEquals(dictionary, saved.getDictionaryWord());
    }

    /**
     * Verifies that even if the duplicate pre-check says the word is not yet present,
     * the service still handles a database-level duplicate safely.
     *
     * Why this matters:
     * Two requests could arrive at almost the same time. Both might pass the
     * "exists" check, but one of them could fail during save because of a
     * unique constraint in the database. This is a race condition.
     *
     * In that case, the service should not crash or return a generic failure.
     * Instead, it should translate that database exception into the same
     * DUPLICATE response the user would expect.
     *
     * Expected result:
     *  - accepted = false
     *  - reason = DUPLICATE
     */
    @Test
    void submitWordReturnsDuplicateWhenSaveHitsUniqueConstraintRaceCondition() {
        Dictionary dictionary = mock(Dictionary.class);
        when(dictionary.getId()).thenReturn(99);

        when(gameRepository.findById(100)).thenReturn(Optional.of(game));
        when(userRepository.findById(1)).thenReturn(Optional.of(player1));
        when(userRepository.getReferenceById(1)).thenReturn(player1);
        when(dictionaryRepository.findByWordIgnoreCase("DOG")).thenReturn(Optional.of(dictionary));
        when(foundWordRepository.existsByGame_IdAndPlayer_IdAndDictionaryWord_Id(100, 1, 99)).thenReturn(false);
        doThrow(new DataIntegrityViolationException("duplicate")).when(foundWordRepository).save(any());

        WordSubmissionService.Result result = wordSubmissionService.submitWord(100, 1, "dog");

        assertFalse(result.accepted);
        assertEquals(WordSubmissionService.SubmissionReason.DUPLICATE, result.reason);
    }

    /**
     * Verifies that even dictionary-valid words are rejected if they do not
     * appear on the current board layout.
     *
     * Expected result:
     *  - accepted = false
     *  - reason = NOT_ON_BOARD
     */
    @Test
    void submitWordRejectsWordThatIsNotOnBoard() {
        Dictionary dictionary = mock(Dictionary.class);

        when(gameRepository.findById(100)).thenReturn(Optional.of(game));
        when(userRepository.findById(1)).thenReturn(Optional.of(player1));
        when(dictionaryRepository.findByWordIgnoreCase("COW")).thenReturn(Optional.of(dictionary));

        WordSubmissionService.Result result = wordSubmissionService.submitWord(100, 1, "cow");

        assertFalse(result.accepted);
        assertEquals(WordSubmissionService.SubmissionReason.NOT_ON_BOARD, result.reason);
    }

    /**
     * Verifies that a user who is not one of the game's participants
     * cannot submit words for that game.
     *
     * Expected result:
     *  - accepted = false
     *  - reason = PLAYER_NOT_IN_GAME
     */
    @Test
    void submitWordRejectsPlayerNotInGame() {
        User outsider = new User("outsider", "out@test.com", "hash");
        setPrivateId(outsider, 99);

        when(gameRepository.findById(100)).thenReturn(Optional.of(game));
        when(userRepository.findById(99)).thenReturn(Optional.of(outsider));

        WordSubmissionService.Result result = wordSubmissionService.submitWord(100, 99, "cat");

        assertFalse(result.accepted);
        assertEquals(WordSubmissionService.SubmissionReason.PLAYER_NOT_IN_GAME, result.reason);
    }

    /**
     * Helper method used to assign IDs to User entities in tests.
     *
     * Since IDs are normally generated by JPA and may not have public setters,
     * reflection is used here to simulate persisted entities with known IDs.
     *
     * This makes repository and service tests easier to control and verify.
     */
    private static void setPrivateId(User user, Integer id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}