//package com.frudoko.controllers;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.frudoko.DAO.ScoreDAO;
//import com.frudoko.model.GameState;
//import com.frudoko.model.Score;
//import com.frudoko.model.User;
//import com.frudoko.service.GameService;
//import com.frudoko.service.GameStateService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpSession;
//import javax.transaction.Transactional;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@Controller
//@Transactional
//public class GameController {
//
//    // ── Dependency Injection ──────────────────────────────────────
//    @Autowired
//    private GameStateService gameStateSvc;  // منطق اللعبة
//    @Autowired
//    private GameService gameSvc;
//    @Autowired
//    private ScoreDAO scoreDAO;              // حفظ النقاط بعد الفوز
//
//    /**test
//     * الـ fruits array — index 1=🍎, 2=🍌, 3=🍇, 4=🍊
//     * index 0 فارغ باش نتجنب -1 في كل مكان
//     * نبعثها لـ Thymeleaf باش يعرض الـ emoji في الشبكة
//     */
//    private static final String[] FRUITS = {"", "🍎", "🍌", "🍇", "🍊"};
//
//    // ══════════════════════════════════════════════════════════════
//    //  صفحة اللعبة — GET /game?level=EASY
//    // ══════════════════════════════════════════════════════════════
//
//    /**
//     * يحمل اللعبة أو يستأنفها
//     *
//     * @param level   المستوى من الـ URL: ?level=EASY|MEDIUM|HARD  (افتراضي: EASY)
//     * @param newGame إذا true → يتجاهل اللعبة القديمة ويبدأ جديدة
//     */
//    @GetMapping("/game")
//    public String gamePage(
//            @RequestParam(defaultValue = "EASY") String level,
//            @RequestParam(defaultValue = "false") boolean newGame,
//            HttpSession session,
//            Model model) {
//
//        User user = (User) session.getAttribute("user");
//        if (user == null) return "redirect:/login";
//
//        // إذا اليوزر اختار "لعبة جديدة" → نخلي اللعبة الحالية
//        if (newGame) {
//            gameStateSvc.abandonGame(user.getId());
//        }
//
//        // تلقى لعبة موجودة أو تخلق جديدة
//        GameState gs = gameStateSvc.getOrCreateGame(user.getId(), level);
//
//        // ─── تحضير البيانات للـ template ───────────────────────────
//
//        // نحول JSON → مصفوفات باش Thymeleaf يقدر يعمل loop عليها
//        int[][] grid        = gameStateSvc.parseGrid(gs.getGridState());
//        int[][] initialPuzzle = gameStateSvc.parseGrid(gs.getInitialPuzzle());
//
//        // نبني mask للخلايا الثابتة (اللي لا يقدر يغيرها اليوزر)
//        boolean[][] fixed   = gameStateSvc.buildFixedMask(initialPuzzle);
//
//        // ─── نضيف كل شيء للـ Model ─────────────────────────────────
//        model.addAttribute("gameState",  gs);           // كامل الـ GameState object
//        model.addAttribute("grid",       grid);         // الشبكة كـ int[][]
//        model.addAttribute("fixed",      fixed);        // mask الخلايا الثابتة
//        model.addAttribute("level",      gs.getLevel().name()); // "EASY"/"MEDIUM"/"HARD"
//        model.addAttribute("elapsed",    gs.getTimeElapsed());  // الوقت المحفوظ (للـ resume)
//        model.addAttribute("fruits", java.util.Arrays.asList(FRUITS));      // ["","🍎","🍌","🍇","🍊"]
//        model.addAttribute("user",       user);
//        model.addAttribute("myScores",   scoreDAO.findByUserId(user.getId())); // سجل النقاط
//
//        return "game"; // → WEB-INF/templates/game.html
//    }
//
//    // ══════════════════════════════════════════════════════════════
//    //  AJAX: Auto-save — POST /game/save
//    // ══════════════════════════════════════════════════════════════
//
//    /**
//     * يُستدعى من JavaScript كل 300ms بعد كل تغيير
//     * يحفظ الشبكة والوقت في قاعدة البيانات
//     *
//     * @ResponseBody = يرجع JSON مباشرة (مش HTML)
//     * ResponseEntity = يقدر يرجع status codes مختلفة (200, 401, 500)
//     */
//    @PostMapping("/game/save")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> saveProgress(
//            @RequestParam int    gameId,
//            @RequestParam String gridJson,
//            @RequestParam long   elapsed,
//            HttpSession session) {
//
//        Map<String, Object> resp = new HashMap<>();
//
//        try {
//            // تحقق من الـ authentication
//            if (session.getAttribute("user") == null) {
//                resp.put("status", "unauthorized");
//                return ResponseEntity.status(401).body(resp);
//            }
//
//            // نحفظ التقدم في قاعدة البيانات
//            gameStateSvc.saveProgress(gameId, gridJson, elapsed);
//
//            resp.put("status", "saved");
//            return ResponseEntity.ok(resp);  // 200 OK
//
//        } catch (Exception e) {
//            resp.put("status", "error");
//            resp.put("message", e.getMessage());
//            return ResponseEntity.status(500).body(resp);
//        }
//    }
//
//    // ══════════════════════════════════════════════════════════════
//    //  AJAX: Check Win — POST /game/check
//    // ══════════════════════════════════════════════════════════════
//
//    /**
//     * يُستدعى من JavaScript كي تمتلئ كل الخلايا
//     * يتحقق من الحل على الـ server (أكثر أماناً من الـ client)
//     * إذا ربح → يحسب النقاط ويحفظها
//     */
//    @PostMapping("/game/check")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> checkWin(
//            @RequestParam int    gameId,
//            @RequestParam String gridJson,
//            @RequestParam long   elapsed,
//            HttpSession session) {
//
//        Map<String, Object> resp = new HashMap<>();
//
//        try {
//            User user = (User) session.getAttribute("user");
//            if (user == null) {
//                resp.put("won", false);
//                resp.put("error", "not logged in");
//                return ResponseEntity.ok(resp);
//            }
//
//            // تتحقق من الفوز على الـ server
//            GameState gs = gameStateSvc.checkWin(gameId, gridJson);
//            boolean won = (gs.getStatus() == GameState.GameStatus.WON);
//
//            resp.put("won", won);
//
//            if (won) {
//                // ─── احسب النقاط واحفظها ────────────────────────────
//
//                // النقاط تنقص مع الوقت — كلما كان أسرع، أكثر نقاط
//                int pts = gameSvc.calculateScore(elapsed, gs.getLevel().name());
//
//                // نبني Score object ونحفظه
//                Score score = new Score();
//                score.setUser(user);
//                score.setPoints(pts);
//                score.setTimeInSeconds(elapsed);
//                score.setLevel(gs.getLevel().name());
//                score.setPlayedAt(LocalDateTime.now());
//                scoreDAO.save(score);
//
//                // نحسب مجموع النقاط الكلي بعد الإضافة
//                int totalScore = scoreDAO.sumPointsByUserId(user.getId());
//
//                // نرجع للـ JavaScript ليعرضها في الـ win modal
//                resp.put("score",      pts);
//                resp.put("totalScore", totalScore);
//                resp.put("time",       gameSvc.formatTime(elapsed));
//            }
//
//            return ResponseEntity.ok(resp);
//
//        } catch (Exception e) {
//            resp.put("won", false);
//            resp.put("error", e.getMessage());
//            return ResponseEntity.status(500).body(resp);
//        }
//    }
//
//    // ══════════════════════════════════════════════════════════════
//    //  AJAX: New Game — POST /game/new
//    // ══════════════════════════════════════════════════════════════
//
//    /**
//     * يُستدعى كي يريد اليوزر يبدأ لعبة جديدة
//     * يخلي اللعبة الحالية ويرجع URL للـ redirect
//     *
//     * الـ redirect يحصل في JavaScript (مش مباشرة من الـ server)
//     * لأن هذا AJAX request
//     */
//    @PostMapping("/game/new")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> startNewGame(
//            @RequestParam String level,
//            HttpSession session) {
//
//        Map<String, Object> resp = new HashMap<>();
//        User user = (User) session.getAttribute("user");
//
//        if (user == null) {
//            resp.put("status", "unauthorized");
//            return ResponseEntity.status(401).body(resp);
//        }
//
//        // نخلي اللعبة الحالية
//        gameStateSvc.abandonGame(user.getId());
//
//        // نرجع URL للـ JavaScript باش يعمل redirect
//        resp.put("status",   "ok");
//        resp.put("redirect", "game?level=" + level + "&newGame=true");
//
//        return ResponseEntity.ok(resp);
//    }
//}
