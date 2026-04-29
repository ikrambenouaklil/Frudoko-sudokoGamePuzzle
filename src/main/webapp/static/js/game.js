/**
 * ════════════════════════════════════════════════════════════════
 *  game.js — محرك اللعبة من جهة الـ Client
 *
 *  المسؤوليات:
 *   1. Timer      — يعد الوقت من المحفوظ
 *   2. Grid       — يضع الفواكه، يتتبع الشبكة
 *   3. Validate   — يلوّن الأخطاء في الوقت الفعلي
 *   4. Auto-save  — يحفظ كل 300ms بعد كل تغيير
 *   5. Win Check  — يتحقق من الفوز كي تمتلئ كل الخلايا
 *   6. Pause      — يوقف اللعبة مؤقتاً ويعرض قائمة
 *   7. Confetti   — يطلق الاحتفال عند الفوز
 *   8. Progress   — يحدث شريط التقدم
 *
 *  المتغيرات اللي يحقنها Thymeleaf من الـ server:
 *    BASE_URL  : string  (context path مثلاً "/FrudokoGame/")
 *    GAME_ID   : int     (id اللعبة الحالية)
 *    LEVEL     : string  ("EASY" / "MEDIUM" / "HARD")
 *    FRUITS    : array   (["","🍎","🍌","🍇","🍊"])
 *    ELAPSED_0 : long    (الثواني المحفوظة — للـ resume)
 *    GRID_INIT : string  (JSON الشبكة الحالية)
 * ════════════════════════════════════════════════════════════════
 */

'use strict'; // وضع صارم — يمنع أخطاء JavaScript شائعة

/* ════════════════════════════════════════════════════════════════
   STATE — المتغيرات العامة تاع اللعبة
════════════════════════════════════════════════════════════════ */

let selectedFruit = 1;              // الفاكهة المختارة حالياً (1-4) أو 0 للمحو
let elapsed       = ELAPSED_0;      // الوقت المنقضي بالثواني (يبدأ من المحفوظ)
let timerInterval = null;           // reference للـ setInterval باش نقدر نوقفه
let grid          = JSON.parse(GRID_INIT); // الشبكة الحالية كـ int[][] (نسخة حية)
let isSaving      = false;          // لمنع طلبات save متعددة في نفس الوقت
let saveDebounce  = null;           // reference للـ setTimeout تاع الـ debounce
let gameOver      = false;          // true كي ينتهي الوقت أو يربح اليوزر

// ── ✨ متغير الـ Pause الجديد ──────────────────────────────────
let isPaused      = false;          // true = اللعبة موقوفة مؤقتاً

/* ════════════════════════════════════════════════════════════════
   INIT — يُنفَّذ كي تنتهي الصفحة من التحميل
════════════════════════════════════════════════════════════════ */

window.addEventListener('DOMContentLoaded', () => {
    setTimeout(() => {
        const screen = document.getElementById('loadingScreen');
        if (screen) {
            screen.style.opacity = '0';
            setTimeout(() => screen.style.display = 'none', 500);
        }
    }, 1200);
    // نبدأ العداد من الوقت المحفوظ (ELAPSED_0 قادم من الـ server)
    startTimer();

    // نحدث شريط التقدم حسب الشبكة المحفوظة
    updateProgress();

    // نفعل التنقل بالـ keyboard (الأسهم + أرقام)
    addKeyboardSupport();
});

/* ════════════════════════════════════════════════════════════════
   SAVE ON EXIT — يحفظ كي يغلق اليوزر التاب أو المتصفح
════════════════════════════════════════════════════════════════ */

window.addEventListener('beforeunload', () => {
    // keepalive: true = يكمل الطلب حتى بعد ما تغلق الصفحة
    triggerSave(true);
});

/* ════════════════════════════════════════════════════════════════
   TIMER — العداد
════════════════════════════════════════════════════════════════ */

/**
 * startTimer — يبدأ العداد
 * setInterval يستدعي الـ function كل 1000ms (= ثانية واحدة)
 */
function startTimer() {
    // نعرض الوقت الحالي مباشرة بدون انتظار ثانية
    document.getElementById('timerDisplay').textContent = formatTime(elapsed);

    timerInterval = setInterval(() => {
        // نزيد الوقت فقط إذا اللعبة شغالة (مش gameOver ومش isPaused)
        if (!gameOver && !isPaused) {
            elapsed++;
            document.getElementById('timerDisplay').textContent = formatTime(elapsed);
        }
    }, 1000);
}

/**
 * stopTimer — يوقف العداد نهائياً
 * clearInterval يلغي الـ setInterval
 */
function stopTimer() {
    clearInterval(timerInterval);
    timerInterval = null;
}

/**
 * formatTime — يحول الثواني لـ "mm:ss"
 * مثال: 90 → "01:30"
 */
function formatTime(secs) {
    const m = String(Math.floor(secs / 60)).padStart(2, '0'); // دقائق مع صفر بالبداية
    const s = String(secs % 60).padStart(2, '0');             // ثواني مع صفر بالبداية
    return `${m}:${s}`;
}

/* ════════════════════════════════════════════════════════════════
   PAUSE / RESUME — ✨ الخاصية الجديدة
════════════════════════════════════════════════════════════════ */

/**
 * pauseGame — يوقف اللعبة مؤقتاً
 * - يوقف العداد (بس ما يمحوش — يكمل من وين وقف)
 * - يحفظ التقدم احتياطياً
 * - يعرض قائمة الـ pause
 */
function pauseGame() {
    // ما نوقفش إذا اللعبة خلصت أو كانت موقوفة أصلاً
    if (gameOver || isPaused) return;

    isPaused = true; // ← هذا يوقف العداد (شرط في startTimer)

    // نحفظ التقدم الحالي قبل ما نوقف
    triggerSave(false);

    // نعرض قائمة الـ pause
    document.getElementById('pauseMenu').style.display = 'flex';

    // نغير زر الـ pause لـ "يلعب"
    const pauseBtn = document.getElementById('pauseBtn');
    if (pauseBtn) {
        pauseBtn.textContent = '▶️ Continuer';
        pauseBtn.onclick = resumeGame; // كي يضغطه يرجع لـ resume
    }
}

/**
 * resumeGame — يكمل اللعبة من وين وقفت
 * - يخفي قائمة الـ pause
 * - يرجع العداد يشتغل
 */
function resumeGame() {
    isPaused = false; // ← العداد يشتغل تاني (شرط في startTimer)

    // نخفي قائمة الـ pause
    document.getElementById('pauseMenu').style.display = 'none';

    // نرجع زر الـ pause لحالتو الأصلية
    const pauseBtn = document.getElementById('pauseBtn');
    if (pauseBtn) {
        pauseBtn.textContent = '⏸ Pause';
        pauseBtn.onclick = pauseGame;
    }
}

/* ════════════════════════════════════════════════════════════════
   NEW GAME
════════════════════════════════════════════════════════════════ */
function newGame() {
    fetch(BASE_URL + '/game/new', {
        method:  'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body:    new URLSearchParams({ level: LEVEL }).toString()
    })
        .then(r => r.json())
        .then(data => {
            if (data.redirect) window.location.href = BASE_URL + data.redirect;
        })
        .catch(() => {
            window.location.href = BASE_URL + 'game?level=' + LEVEL + '&newGame=true';
        });
}


/* ════════════════════════════════════════════════════════════════
   FRUIT SELECTION — اختيار الفاكهة
════════════════════════════════════════════════════════════════ */

/**
 * selectFruit — يُستدعى كي يضغط اليوزر على زر فاكهة
 * @param btn — زر الفاكهة (HTML element)
 */
function selectFruit(btn) {
    // نقرأ قيمة الفاكهة من الـ data-val attribute
    selectedFruit = parseInt(btn.dataset.val);

    // نزيل "selected" من كل الأزرار ثم نضيفه للمضغوط فقط
    document.querySelectorAll('.fruit-btn').forEach(b => b.classList.remove('selected'));
    btn.classList.add('selected');
}

/* ════════════════════════════════════════════════════════════════
   CELL PLACEMENT — وضع فاكهة في خلية
════════════════════════════════════════════════════════════════ */

/**
 * isFixedCell — يتحقق إذا الخلية ثابتة (لا يقدر يغيرها اليوزر)
 * data-fixed="true" يحقنه Thymeleaf من الـ server
 */
function isFixedCell(cell) {
    return cell.dataset.fixed === 'true';
}

/**
 * placeSelected — يضع الفاكهة المختارة في الخلية المضغوطة
 * @param cell — عنصر HTML الخاصة بالخلية
 */
function placeSelected(cell) {
    if (gameOver)       return; // اللعبة خلصت
    if (isPaused)       return; // ✨ ما نضعش فاكهة وقت الـ pause
    if (isFixedCell(cell)) return; // خلية ثابتة

    const row = parseInt(cell.dataset.row); // رقم الصف (0-3)
    const col = parseInt(cell.dataset.col); // رقم العمود (0-3)

    // نحدث الشبكة المحلية
    grid[row][col] = selectedFruit;
    cell.dataset.val = selectedFruit;

    // نحدث النص في الخلية (الـ emoji)
    if (selectedFruit > 0) {
        cell.innerHTML = `<img src="${FRUIT_IMGS[selectedFruit]}" class="fruit-icon">`;
    } else {
        cell.innerHTML = '';
    }
    // animation كي نضع فاكهة (مش وقت المحو)
    if (selectedFruit > 0) {
        cell.classList.remove('filled');
        void cell.offsetWidth; // reflow باش تبدأ الـ animation من الأول
        cell.classList.add('filled');
    }

    // نتحقق من الأخطاء في الوقت الفعلي
    validateAll();

    // نحدث شريط التقدم
    updateProgress();

    // debounced save — ننتظر 300ms بعد آخر تغيير
    scheduleSave();

    // إذا امتلأت كل الخلايا → نتحقق من الفوز
    if (allFilled()) {
        checkWin();
    }
}

/* ════════════════════════════════════════════════════════════════
   KEYBOARD SUPPORT — التنقل بالـ keyboard
════════════════════════════════════════════════════════════════ */

let focusedRow = 0; // الصف المحدد حالياً بالـ keyboard
let focusedCol = 0; // العمود المحدد حالياً بالـ keyboard

function addKeyboardSupport() {
    document.addEventListener('keydown', (e) => {
        if (gameOver) return;

        switch (e.key) {
            // أسهم التنقل
            case 'ArrowUp':    e.preventDefault(); moveFocus(focusedRow - 1, focusedCol); break;
            case 'ArrowDown':  e.preventDefault(); moveFocus(focusedRow + 1, focusedCol); break;
            case 'ArrowLeft':  e.preventDefault(); moveFocus(focusedRow, focusedCol - 1); break;
            case 'ArrowRight': e.preventDefault(); moveFocus(focusedRow, focusedCol + 1); break;

            // أرقام لاختيار الفاكهة
            case '1': placeAtFocus(1); break;
            case '2': placeAtFocus(2); break;
            case '3': placeAtFocus(3); break;
            case '4': placeAtFocus(4); break;

            // محو الخلية
            case 'Delete':
            case 'Backspace': placeAtFocus(0); break;

            // ✨ Escape = pause/resume
            case 'Escape':
                if (isPaused) resumeGame();
                else          pauseGame();
                break;
        }
    });
}

/** moveFocus — يحرك التركيز لخلية معينة */
function moveFocus(r, c) {
    // Math.max/min تمنع الخروج عن حدود الشبكة (0-3)
    r = Math.max(0, Math.min(3, r));
    c = Math.max(0, Math.min(3, c));
    focusedRow = r;
    focusedCol = c;

    // نزيل ring من كل الخلايا ثم نضيفه للخلية الجديدة
    document.querySelectorAll('.grid-cell').forEach(cell => cell.classList.remove('keyboard-focus'));
    const target = document.getElementById(`cell-${r}-${c}`);
    if (target) target.classList.add('keyboard-focus');
}

/** placeAtFocus — يضع فاكهة في الخلية المحددة بالـ keyboard */
function placeAtFocus(val) {
    const cell = document.getElementById(`cell-${focusedRow}-${focusedCol}`);
    if (cell) {
        selectedFruit = val;
        // نحدث الـ palette بصرياً
        document.querySelectorAll('.fruit-btn').forEach(b => b.classList.remove('selected'));
        const btn = document.querySelector(`.fruit-btn[data-val="${val}"]`);
        if (btn) btn.classList.add('selected');
        placeSelected(cell);
    }
}

/* ════════════════════════════════════════════════════════════════
   VALIDATION — تلوين الأخطاء في الوقت الفعلي
════════════════════════════════════════════════════════════════ */

/**
 * validateAll — يتحقق من كل الخلايا ويلوّن الأخطاء
 * يُستدعى بعد كل تغيير في الشبكة
 */
function validateAll() {
    // نزيل كل تلوين قديم
    document.querySelectorAll('.grid-cell').forEach(cell => {
        cell.classList.remove('error');
    });

    // نتحقق من كل خلية غير فارغة
    for (let r = 0; r < 4; r++) {
        for (let c = 0; c < 4; c++) {
            const val = grid[r][c];
            if (val === 0) continue; // فارغة → نتجاهل

            if (!isValidPlacement(r, c, val)) {
                // خطأ → نلوّن الخلية بالأحمر
                const cell = document.getElementById(`cell-${r}-${c}`);
                if (cell) cell.classList.add('error');
            }
        }
    }
}

/**
 * isValidPlacement — يتحقق من قواعد الـ Sudoku لخلية واحدة
 * القواعد: كل صف / عمود / مربع 2×2 لا يحتوي على نفس الفاكهة مرتين
 */
function isValidPlacement(row, col, val) {
    // فحص الصف — هل نفس الفاكهة موجودة في عمود آخر؟
    for (let c = 0; c < 4; c++)
        if (c !== col && grid[row][c] === val) return false;

    // فحص العمود — هل نفس الفاكهة موجودة في صف آخر؟
    for (let r = 0; r < 4; r++)
        if (r !== row && grid[r][col] === val) return false;

    // فحص المربع 2×2 — نحسب بداية المربع
    const boxRow = Math.floor(row / 2) * 2; // 0 أو 2
    const boxCol = Math.floor(col / 2) * 2; // 0 أو 2
    for (let r = boxRow; r < boxRow + 2; r++)
        for (let c = boxCol; c < boxCol + 2; c++)
            if ((r !== row || c !== col) && grid[r][c] === val) return false;

    return true; // كل شيء صح
}

/**
 * allFilled — يتحقق إذا كل الخلايا الـ 16 ممتلئة
 * every() يرجع true فقط إذا الشرط صحيح لكل عنصر
 */
function allFilled() {
    return grid.every(row => row.every(cell => cell !== 0));
}

/* ════════════════════════════════════════════════════════════════
   PROGRESS BAR — شريط التقدم
════════════════════════════════════════════════════════════════ */

/**
 * updateProgress — يحسب نسبة الإنجاز ويحدث الشريط
 */
function updateProgress() {
    // flat() يحول الـ 2D array لـ 1D ثم نعد الخلايا غير الصفرية
    const filled = grid.flat().filter(v => v !== 0).length;
    const pct    = Math.round((filled / 16) * 100); // نسبة مئوية

    document.getElementById('progressBar').style.width = pct + '%';
    document.getElementById('progressText').textContent = `${filled} / 16`;
}

/* ════════════════════════════════════════════════════════════════
   AUTO-SAVE — الحفظ التلقائي
════════════════════════════════════════════════════════════════ */

/**
 * scheduleSave — debounce: ننتظر 300ms بعد آخر تغيير قبل ما نحفظ
 * هذا يمنع إرسال طلب لكل ضغطة مفتاح
 */
function scheduleSave() {
    clearTimeout(saveDebounce);
    saveDebounce = setTimeout(() => triggerSave(false), 300);
}

/**
 * triggerSave — يرسل طلب AJAX لحفظ التقدم
 * @param synchronous — true = keepalive (للـ beforeunload)
 */
function triggerSave(synchronous = false) {
    if (isSaving) return; // نمنع طلبات متوازية
    isSaving = true;

    const body = new URLSearchParams({
        gameId:   GAME_ID,            // id اللعبة من الـ server
        gridJson: JSON.stringify(grid), // الشبكة الحالية كـ JSON
        elapsed:  elapsed              // الوقت المنقضي بالثواني
    });

    const fetchOptions = {
        method:  'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body:    body.toString()
    };

    // keepalive يسمح للطلب يكمل حتى بعد ما تغلق الصفحة
    if (synchronous) fetchOptions.keepalive = true;

    fetch(BASE_URL + 'game/save', fetchOptions)
        .catch(() => { /* نتجاهل الفشل — اللعبة تكمل بدون نت */ })
        .finally(() => { isSaving = false; }); // نحرر الـ flag
}

/* ════════════════════════════════════════════════════════════════
   WIN CHECK — التحقق من الفوز
════════════════════════════════════════════════════════════════ */

/**
 * checkWin — يُستدعى كي تمتلئ كل الخلايا
 * أولاً: تحقق محلي من الأخطاء (سريع)
 * ثانياً: تحقق على الـ server (موثوق)
 */
function checkWin() {
    // تحقق محلي — إذا في أخطاء، ما نرسلش للـ server
    let hasErrors = false;
    outer: for (let r = 0; r < 4; r++) {
        for (let c = 0; c < 4; c++) {
            if (!isValidPlacement(r, c, grid[r][c])) {
                hasErrors = true;
                break outer; // نخرج من الحلقتين مباشرة
            }
        }
    }
    if (hasErrors) return; // الشبكة ممتلئة لكن فيها أخطاء

    // نوقف العداد قبل ما نتحقق (لا نضيف وقت أثناء التحقق)
    stopTimer();

    fetch(BASE_URL + 'game/check', {
        method:  'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body:    new URLSearchParams({
            gameId:   GAME_ID,
            gridJson: JSON.stringify(grid),
            elapsed:  elapsed
        }).toString()
    })
        .then(r => r.json())
        .then(data => {
            console.log('checkWin response:', data);
            if (data.won) {
                // 🎉 اليوزر ربح!
                gameOver = true;
                showWinModal(data.score, data.totalScore);
            } else {
                // الـ server يقول لا — نكمل العداد
                startTimer();
            }
        })
        .catch(() => {
            // خطأ شبكة → نكمل اللعبة
            startTimer();
        });
}

/* ════════════════════════════════════════════════════════════════
   WIN MODAL — نافذة الفوز
════════════════════════════════════════════════════════════════ */

/**
 * showWinModal — يعرض نافذة الفوز مع الإحصائيات
 * @param score      النقاط المكتسبة
 * @param totalScore المجموع الكلي للنقاط
 */
function showWinModal(score, totalScore) {
    document.getElementById('winTime').textContent  = formatTime(elapsed);
    document.getElementById('winScore').textContent = (score || '—') + ' pts';
    document.getElementById('winLevel').textContent = LEVEL;

    // نطلق الـ confetti احتفالاً
    spawnConfetti();

    // نعرض النافذة
    document.getElementById('winModal').style.display = 'flex';
}

/**
 * spawnConfetti — يطلق فواكه متطايرة للاحتفال 🎉
 * يخلق عناصر span متحركة بـ CSS animation
 */
function spawnConfetti() {
    const container = document.getElementById('confetti');
    // استعمل FRUIT_IMGS بدل الـ emoji
    const imgs = FRUIT_IMGS.filter(u => u !== '');
    container.innerHTML = '';

    for (let i = 0; i < 22; i++) {
        const src  = imgs[Math.floor(Math.random() * imgs.length)];
        const span = document.createElement('span');
        span.innerHTML = `<img src="${src}" style="width:1.5rem;">`;
        span.style.cssText = `
            position: absolute;
            left: ${Math.random() * 100}%;
            top: 0;
            font-size: ${1 + Math.random() * 1.2}rem;
            animation: confettiFall ${1.5 + Math.random()}s linear ${Math.random() * 0.8}s forwards;
            pointer-events: none;
        `;
        container.appendChild(span);
    }
}

/* ════════════════════════════════════════════════════════════════
   KEYBOARD FOCUS STYLE — نحقن style ديناميكياً
════════════════════════════════════════════════════════════════ */

// نضيف CSS للخلية المحددة بالـ keyboard بدون ما نعدل ملف CSS
(function() {
    const style = document.createElement('style');
    style.textContent = `
        /* الخلية المحددة بالـ keyboard تأخذ border أصفر */
        .grid-cell.keyboard-focus {
            outline: 3px solid var(--fruit-yellow, #f1c40f);
            outline-offset: 2px;
            z-index: 5;
        }

        /* ✨ animation تاع الـ confetti */
        @keyframes confettiFall {
            0%   { transform: translateY(0) rotate(0deg);   opacity: 1; }
            100% { transform: translateY(300px) rotate(360deg); opacity: 0; }
        }
    `;
    document.head.appendChild(style);
})();


