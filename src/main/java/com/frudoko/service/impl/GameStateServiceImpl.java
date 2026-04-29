package com.frudoko.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frudoko.DAO.GameStateDAO;
import com.frudoko.DAO.ScoreDAO;
import com.frudoko.DAO.UserDAO;
import com.frudoko.model.GameState;
import com.frudoko.model.User;
import com.frudoko.service.GameStateService;
import com.frudoko.service.SudokuGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Service
@Transactional
public class GameStateServiceImpl implements GameStateService {
    @Autowired
    private GameStateDAO gameStateDAO;  // تاع العمليات على game_states table

    @Autowired
    private ScoreDAO scoreDAO;          // تاع حفظ النقاط بعد الفوز
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private SudokuGenerator generator;  // تاع توليد الألغاز
    /**
     * ObjectMapper — مكتبة Jackson
     * تحول بين JSON string ومصفوفات Java
     * مثال: "[[1,2],[3,4]]" ↔ int[][]
     */
    private final ObjectMapper mapper = new ObjectMapper();

    // ── النقاط الأساسية لكل مستوى ────────────────────────────────
    // HARD يعطي نقاط أكثر لأن اللعبة أصعب
    private static final int BASE_EASY   = 100;
    private static final int BASE_MEDIUM = 200;
    private static final int BASE_HARD   = 350;
    // ══════════════════════════════════════════════════════════════
    //  1. getOrCreateGame — تلقى لعبة موجودة أو تخلق جديدة
    // ══════════════════════════════════════════════════════════════

    @Override
    public GameState getOrCreateGame(int userId, String level) {

        // أولاً: نشوف إذا عند اليوزر لعبة IN_PROGRESS
        GameState existing = gameStateDAO.findByUserIdAndStatus(
                userId,
                GameState.GameStatus.IN_PROGRESS
        );

        if (existing != null) {
            // لقينا لعبة → نرجعها مباشرة (resume)
            return existing;
        }

        // ما لقيناش → نخلق لعبة جديدة بالمستوى المطلوب
        return createNewGame(userId, level);
    }

    /**
     * createNewGame — تخلق لعبة جديدة من الصفر
     *
     * الخطوات:
     *  1. نجيب الـ User من قاعدة البيانات
     *  2. نولد حل كامل للـ Sudoku
     *  3. نزيل خلايا حسب المستوى (puzzle)
     *  4. نحفظ كل شيء في GameState جديد
     */

    private GameState createNewGame(int userId, String level) {
        try {
            // 1. نجيب اليوزر — em.find يرجع null إذا ما وجدوش
            User user = userDAO.findById( userId);
            if (user == null) {
                throw new RuntimeException("User not found: " + userId);
            }

            // 2. نولد حل كامل (4×4 مصفوفة فيها 1-4 بشكل صحيح)
            int[][] solution = generator.generateSolution();

            // 3. نزيل خلايا حسب المستوى → هذا هو الـ puzzle اللي يشوفه اليوزر
            //    EASY: يزيل 4 خلايا، MEDIUM: 8، HARD: 11
            int[][] puzzle = generator.generatePuzzle(solution, level);

            // 4. نبني GameState جديد ونملأ كل حقولو
            GameState gs = new GameState();

            gs.setUser(user);                                           // ربط مع اليوزر
            gs.setLevel(GameState.Level.valueOf(level.toUpperCase()));  // EASY/MEDIUM/HARD
            gs.setSolution(mapper.writeValueAsString(solution));         // الحل الكامل (سري)
            gs.setInitialPuzzle(mapper.writeValueAsString(puzzle));      // الـ puzzle الأصلي
            gs.setGridState(mapper.writeValueAsString(puzzle));          // الشبكة الحالية (تتغير مع كل تحديث)
            gs.setTimeElapsed(0L);                                       // الوقت يبدأ من الصفر
            gs.setStatus(GameState.GameStatus.IN_PROGRESS);             // اللعبة بدأت
            gs.setCreatedAt(LocalDateTime.now());
            gs.setUpdatedAt(LocalDateTime.now());

            // 5. نحفظ في قاعدة البيانات — id == 0 → persist (جديد)
            gameStateDAO.save(gs);

            return gs;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create new game: " + e.getMessage(), e);
        }
    }


    // ══════════════════════════════════════════════════════════════
    //  2. saveProgress — تحفظ التقدم (auto-save)
    // ══════════════════════════════════════════════════════════════

    @Override
    public void saveProgress(int gameId, String gridJson, long elapsed) {

        // نجيب اللعبة من قاعدة البيانات
        GameState gs = gameStateDAO.findById(gameId);

        // تحقق مزدوج: اللعبة موجودة ولا زالت IN_PROGRESS
        if (gs != null && gs.getStatus() == GameState.GameStatus.IN_PROGRESS) {
            gs.setGridState(gridJson);          // نحدث الشبكة
            gs.setTimeElapsed(elapsed);         // نحدث الوقت المنقضي
            gs.setUpdatedAt(LocalDateTime.now()); // وقت آخر تحديث

            // id != 0 → merge (تحديث لعبة موجودة)
            gameStateDAO.save(gs);
        }
        // إذا gs == null أو status != IN_PROGRESS → نتجاهل الطلب بهدوء
    }
    // ══════════════════════════════════════════════════════════════
    //  3. checkWin — تتحقق من الفوز
    // ══════════════════════════════════════════════════════════════
    @Override
    public GameState checkWin(int gameId, String gridJson) {
        try {
            GameState gs = gameStateDAO.findById(gameId);
            if (gs == null) {
                throw new RuntimeException("Game not found: " + gameId);
            }

            // نحول JSON → مصفوفات باش نقارن
            int[][] submitted = mapper.readValue(gridJson,         int[][].class); // اللي بعثه اليوزر
            int[][] solution  = mapper.readValue(gs.getSolution(), int[][].class); // الحل الصحيح المحفوظ

            if (generator.isValidSolution(submitted)) {
                // اليوزر ربح!
                gs.setStatus(GameState.GameStatus.WON);
                gs.setGridState(gridJson);            // نحفظ الشبكة النهائية
                gs.setUpdatedAt(LocalDateTime.now());
                gameStateDAO.save(gs);
            }

            // نرجع GameState المحدث — الـ Controller يشوف status منو
            return gs;

        } catch (Exception e) {
            throw new RuntimeException("Win check failed: " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  4. abandonGame — تخلي اللعبة الحالية
    // ══════════════════════════════════════════════════════════════
    /**
     * تخلي اللعبة الحالية (تغير status لـ LOST)
     * تُستدعى كي يختار اليوزر مستوى جديد
     */

    @Override
    public void abandonGame(int userId) {
        // نلقى اللعبة IN_PROGRESS للمستخدم
        GameState gs = gameStateDAO.findByUserIdAndStatus(
                userId,
                GameState.GameStatus.IN_PROGRESS
        );

        if (gs != null) {
            // نغير status لـ LOST باش ما تجيش تتحمل تاني
            gs.setStatus(GameState.GameStatus.LOST);
            gs.setUpdatedAt(LocalDateTime.now());
            gameStateDAO.save(gs);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  5. parseGrid — JSON → int[][]
    // ══════════════════════════════════════════════════════════════

    @Override
    public int[][] parseGrid(String json) {
        try {
            // Jackson تحول "[[1,2,3,4],[...]]" لـ int[][]
            return mapper.readValue(json, int[][].class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid grid JSON: " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  6. buildFixedMask — تحدد الخلايا الثابتة
    // ══════════════════════════════════════════════════════════════

    @Override
    public boolean[][] buildFixedMask(int[][] puzzle) {
        // puzzle هو الـ initialPuzzle — الخلايا اللي كانت غير صفر هي ثابتة
        boolean[][] fixed = new boolean[4][4];

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                // puzzle[r][c] != 0 → هذه الخلية كانت معطاة في البداية → ثابتة
                fixed[r][c] = (puzzle[r][c] != 0);
            }
        }

        return fixed;
    }

   
}


