package com.frudoko.service;

import com.frudoko.model.Score;
import java.util.List;

public interface GameService {

    // تحسب النقاط حسب الوقت والمستوى
    int calculateScore(long elapsedSeconds, String level);

    // تحفظ النقاط في قاعدة البيانات بعد الفوز
    void saveScore(int userId, int points, long elapsed, String level);

    // تجيب كل نقاط يوزر معين
    List<Score> getUserScores(int userId);

    // تحسب مجموع النقاط الكلي
    int getTotalScore(int userId);

    // تحول الثواني لـ "mm:ss"
    String formatTime(long seconds);
}