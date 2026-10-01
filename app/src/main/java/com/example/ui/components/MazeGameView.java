package com.example.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MazeGameView - Custom View điều khiển trò chơi Mê Cung bằng Kéo thả 1 nét (One-Line Drag)
 * hoặc Chế độ Vuốt từng bước (1 ô / 1 lần vuốt), chạy trên Canvas và Paint thuần của Android SDK,
 * tối ưu hóa 60/120 FPS.
 *
 * - Không khởi tạo đối tượng trong onDraw().
 * - Hiển thị Đích đến (Goal) là hình Lá cờ màu đỏ (Red Flag) sống động với cột cờ, chân đế và chóp vàng.
 * - Hỗ trợ chế độ Từng Bước (STEP_BY_STEP): Chỉ vuốt di chuyển đúng 1 ô mỗi lần chạm/vuốt.
 * - Hỗ trợ chế độ Tự Động (AUTO): Kéo lướt 1 nét mượt mà liên tục.
 * - Tự động Hoàn tác (Undo) khi kéo ngón tay lùi lại ô trước đó.
 * - ĐÃ TẮT RUNG KHI DI CHUYỂN / VA TƯỜNG (Chỉ rung khi chiến thắng).
 */
public class MazeGameView extends View {

    public static final int NORTH = 1;
    public static final int EAST = 2;
    public static final int SOUTH = 4;
    public static final int WEST = 8;

    public static class MazePoint {
        public int x;
        public int y;

        public MazePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MazePoint that = (MazePoint) o;
            return x == that.x && y == that.y;
        }

        @Override
        public int hashCode() {
            return 31 * x + y;
        }
    }

    public static class MazeCollectibleItem {
        public int x;
        public int y;
        public String type;

        public MazeCollectibleItem(int x, int y, String type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
    }

    public interface OnMazeMoveListener {
        void onMove(int dx, int dy, MazePoint newPlayer, int totalMoves);
        void onUndo(MazePoint restoredPlayer);
        void onWallHit();
        void onGameWin(int totalMoves);
    }

    private OnMazeMoveListener moveListener;

    // Maze structure
    private int mazeW = 5;
    private int mazeH = 5;
    private int[] masks = new int[0];
    private MazePoint startPoint = new MazePoint(0, 0);
    private MazePoint goalPoint = new MazePoint(4, 4);
    private MazePoint playerPos = new MazePoint(0, 0);
    private Integer visionWindow = null;

    private final Set<Integer> visitedSet = new HashSet<>();
    private final List<MazePoint> pathHistory = new ArrayList<>();
    private final List<Integer> stepColorParities = new ArrayList<>();
    private int currentMoveParity = 0;
    private boolean hasMovedInCurrentGesture = false;
    private int lastTouchCellX = -1;
    private int lastTouchCellY = -1;
    private static final int MAX_MAZE_TURN_CORNERS = 10;
    private final List<MazePoint> hintPoints = new ArrayList<>();
    private final List<MazeCollectibleItem> collectibleItems = new ArrayList<>();
    @Nullable
    private MazePoint wolfPos = null;
    @Nullable
    private MazePoint wolfCubPos = null;
    private boolean isWolfFrenzy = false;
    private boolean isWolfStunned = false;
    private boolean hasShieldProtection = false;
    private boolean isPlayerFrozen = false;
    private boolean isIceBeamActive = false;
    private String currentSkinId = "classic_blue";

    private boolean isDragging = false;
    private boolean isGameWon = false;
    private boolean isCelebratingWin = false;
    private long winAnimStartTime = 0L;
    private static final long WIN_ANIM_DURATION_MS = 1000L;
    private Paint winBurstPaint;
    private Paint winParticlePaint;
    private boolean isStepByStepMode = false;
    private boolean stepModeHasMoved = false;
    private float touchDownX = 0f;
    private float touchDownY = 0f;
    private long lastStepTime = 0L;
    private long lastAutoStepTime = 0L;

    // Hằng số nhịp độ di chuyển: chế độ từng bước có tốc độ tối đa 0.1 giây / bước (100ms / ô, đi 20 ô mất 2 giây)
    private static final long AUTO_STEP_INTERVAL_MS = 0L;
    private static final long STEP_MODE_COOLDOWN_MS = 100L;
    private float moveSensitivity = 1.0f;
    private final List<MazePoint> pendingStepsQueue = new ArrayList<>();
    private final Runnable stepQueueRunnable = new Runnable() {
        @Override
        public void run() {
            processNextPendingStep();
        }
    };

    // Layout measurements
    private float cellSize = 0f;
    private float offX = 0f;
    private float offY = 0f;
    private int viewW = 5;
    private int viewH = 5;
    private int originX = 0;
    private int originY = 0;

    // Pre-allocated Paint and Draw Objects (Studio Lighting Engine)
    private Paint bgPaint;
    private Paint gridFloorPaint;
    private Paint boardAmbientPaint;
    private Paint ambientSpotlightPaint;
    private Paint ambientSpotlightInnerPaint;
    private Paint wallGlowPaint;
    private Paint wallPaint;
    private Paint wallCorePaint;
    private Paint visitedCellPaint;
    private Paint visitedCellBorderPaint;
    private Paint visitedSpecularPaint;
    private Paint startPortalGlowPaint;
    private Paint startPortalPaint;
    private Paint hintPaint;
    private Paint hintCorePaint;

    // Red Flag (Goal) Paints
    private Paint goalGlowOuterPaint;
    private Paint goalGlowPaint;
    private Paint flagPolePaint;
    private Paint flagTopperPaint;
    private Paint flagClothPaint;
    private Paint flagClothShadowPaint;
    private Paint flagBasePaint;
    private Paint flagEmblemPaint;

    // Player & Trail Paints
    private Paint playerGlowPaint;
    private Paint playerBodyPaint;
    private Paint playerHighlightPaint;
    private Paint pathTrailGlowPaint;
    private Paint pathTrailPaint;
    private Paint pathTrailLightPaint;
    private Paint pathTrailCorePaint;

    // Viewport Frame Paint
    private Paint viewportFramePaint;

    private final RectF tempRect = new RectF();
    private final Path tempPath = new Path();
    private final Path tempLightPath = new Path();
    private final Path wallPath = new Path();
    private final Path flagPath = new Path();

    public MazeGameView(Context context) {
        super(context);
        init();
    }

    public MazeGameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MazeGameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#FFFFFF"));
        bgPaint.setStyle(Paint.Style.FILL);

        gridFloorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridFloorPaint.setColor(Color.parseColor("#160F172A"));
        gridFloorPaint.setStyle(Paint.Style.STROKE);
        gridFloorPaint.setStrokeWidth(1.1f);

        boardAmbientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boardAmbientPaint.setColor(Color.parseColor("#102563EB"));
        boardAmbientPaint.setStyle(Paint.Style.FILL);

        ambientSpotlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ambientSpotlightPaint.setColor(Color.parseColor("#182563EB"));
        ambientSpotlightPaint.setStyle(Paint.Style.FILL);

        ambientSpotlightInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ambientSpotlightInnerPaint.setColor(Color.parseColor("#242563EB"));
        ambientSpotlightInnerPaint.setStyle(Paint.Style.FILL);

        wallGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wallGlowPaint.setColor(Color.parseColor("#331E293B"));
        wallGlowPaint.setStyle(Paint.Style.STROKE);
        wallGlowPaint.setStrokeCap(Paint.Cap.ROUND);
        wallGlowPaint.setStrokeJoin(Paint.Join.ROUND);

        wallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wallPaint.setColor(Color.parseColor("#1E293B"));
        wallPaint.setStyle(Paint.Style.STROKE);
        wallPaint.setStrokeCap(Paint.Cap.ROUND);
        wallPaint.setStrokeJoin(Paint.Join.ROUND);

        wallCorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wallCorePaint.setColor(Color.parseColor("#B3FFFFFF"));
        wallCorePaint.setStyle(Paint.Style.STROKE);
        wallCorePaint.setStrokeCap(Paint.Cap.ROUND);
        wallCorePaint.setStrokeJoin(Paint.Join.ROUND);

        visitedCellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        visitedCellPaint.setColor(Color.parseColor("#4D14B8A6"));
        visitedCellPaint.setStyle(Paint.Style.FILL);

        visitedCellBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        visitedCellBorderPaint.setColor(Color.parseColor("#752DD4BF"));
        visitedCellBorderPaint.setStyle(Paint.Style.STROKE);
        visitedCellBorderPaint.setStrokeWidth(1.6f);

        visitedSpecularPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        visitedSpecularPaint.setColor(Color.parseColor("#66FFFFFF"));
        visitedSpecularPaint.setStyle(Paint.Style.STROKE);
        visitedSpecularPaint.setStrokeCap(Paint.Cap.ROUND);
        visitedSpecularPaint.setStrokeWidth(1.8f);

        startPortalGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        startPortalGlowPaint.setColor(Color.parseColor("#3838BDF8"));
        startPortalGlowPaint.setStyle(Paint.Style.FILL);

        startPortalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        startPortalPaint.setColor(Color.parseColor("#CC38BDF8"));
        startPortalPaint.setStyle(Paint.Style.STROKE);
        startPortalPaint.setStrokeWidth(2.8f);

        pathTrailGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathTrailGlowPaint.setColor(Color.parseColor("#662DD4BF"));
        pathTrailGlowPaint.setStyle(Paint.Style.STROKE);
        pathTrailGlowPaint.setStrokeCap(Paint.Cap.ROUND);
        pathTrailGlowPaint.setStrokeJoin(Paint.Join.ROUND);

        pathTrailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathTrailPaint.setColor(Color.parseColor("#2DD4BF"));
        pathTrailPaint.setStyle(Paint.Style.STROKE);
        pathTrailPaint.setStrokeCap(Paint.Cap.ROUND);
        pathTrailPaint.setStrokeJoin(Paint.Join.ROUND);

        pathTrailLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathTrailLightPaint.setColor(Color.parseColor("#99F6E4"));
        pathTrailLightPaint.setStyle(Paint.Style.STROKE);
        pathTrailLightPaint.setStrokeCap(Paint.Cap.ROUND);
        pathTrailLightPaint.setStrokeJoin(Paint.Join.ROUND);

        pathTrailCorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathTrailCorePaint.setColor(Color.parseColor("#E6FFFFFF"));
        pathTrailCorePaint.setStyle(Paint.Style.STROKE);
        pathTrailCorePaint.setStrokeCap(Paint.Cap.ROUND);
        pathTrailCorePaint.setStrokeJoin(Paint.Join.ROUND);

        hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hintPaint.setColor(Color.parseColor("#77F59E0B"));
        hintPaint.setStyle(Paint.Style.FILL);

        hintCorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hintCorePaint.setColor(Color.parseColor("#FFFDE047"));
        hintCorePaint.setStyle(Paint.Style.FILL);

        // ĐÍCH ĐẾN: LÁ CỜ ĐỎ (RED FLAG)
        goalGlowOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        goalGlowOuterPaint.setColor(Color.parseColor("#2EEF4444"));
        goalGlowOuterPaint.setStyle(Paint.Style.FILL);

        goalGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        goalGlowPaint.setColor(Color.parseColor("#59EF4444")); // Hào quang đỏ rõ ràng
        goalGlowPaint.setStyle(Paint.Style.FILL);

        flagPolePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        flagPolePaint.setColor(Color.parseColor("#F8FAFC")); // Cột cờ bạc sáng
        flagPolePaint.setStyle(Paint.Style.STROKE);
        flagPolePaint.setStrokeCap(Paint.Cap.ROUND);

        flagTopperPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        flagTopperPaint.setColor(Color.parseColor("#FBBF24")); // Chóp cột cờ tròn vàng
        flagTopperPaint.setStyle(Paint.Style.FILL);

        flagClothPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        flagClothPaint.setColor(Color.parseColor("#EF4444")); // Lá cờ màu đỏ tươi rực rỡ
        flagClothPaint.setStyle(Paint.Style.FILL);

        flagClothShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        flagClothShadowPaint.setColor(Color.parseColor("#FECACA")); // Viền bắt sáng lá cờ
        flagClothShadowPaint.setStyle(Paint.Style.STROKE);
        flagClothShadowPaint.setStrokeWidth(2.0f);

        flagBasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        flagBasePaint.setColor(Color.parseColor("#CBD5E1")); // Chân đế cột cờ sáng rõ
        flagBasePaint.setStyle(Paint.Style.FILL);

        flagEmblemPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        flagEmblemPaint.setColor(Color.parseColor("#FEF08A")); // Ngôi sao vàng sáng trên cờ
        flagEmblemPaint.setStyle(Paint.Style.FILL);

        winBurstPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        winBurstPaint.setStyle(Paint.Style.STROKE);
        winBurstPaint.setStrokeCap(Paint.Cap.ROUND);

        winParticlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        winParticlePaint.setStyle(Paint.Style.FILL);

        // NHÂN VẬT
        playerGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        playerGlowPaint.setColor(Color.parseColor("#4DFBBF24"));
        playerGlowPaint.setStyle(Paint.Style.FILL);

        playerBodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        playerBodyPaint.setColor(Color.parseColor("#F59E0B"));
        playerBodyPaint.setStyle(Paint.Style.FILL);

        playerHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        playerHighlightPaint.setColor(Color.WHITE);
        playerHighlightPaint.setStyle(Paint.Style.FILL);

        // Khung viền góc nhìn 10x10
        viewportFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        viewportFramePaint.setColor(Color.parseColor("#4438BDF8"));
        viewportFramePaint.setStyle(Paint.Style.STROKE);
        viewportFramePaint.setStrokeWidth(2.5f);
    }

    public void setOnMazeMoveListener(OnMazeMoveListener listener) {
        this.moveListener = listener;
    }

    public void setStepByStepMode(boolean stepByStep) {
        if (this.isStepByStepMode != stepByStep) {
            removeCallbacks(stepQueueRunnable);
            pendingStepsQueue.clear();
        }
        this.isStepByStepMode = stepByStep;
        this.stepModeHasMoved = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(stepQueueRunnable);
        pendingStepsQueue.clear();
        super.onDetachedFromWindow();
    }

    public void setMoveSensitivity(float sensitivity) {
        this.moveSensitivity = Math.max(0.4f, Math.min(2.5f, sensitivity));
    }

    public void updateColors(int wallColor, int visitedColor, int accentColor) {
        wallPaint.setColor(wallColor);
        wallGlowPaint.setColor(withAlpha(wallColor, 88));
        visitedCellPaint.setColor(visitedColor);
        visitedCellBorderPaint.setColor(withAlpha(accentColor, 135));
        pathTrailGlowPaint.setColor(withAlpha(accentColor, 110));
        pathTrailPaint.setColor(accentColor);
        startPortalGlowPaint.setColor(withAlpha(accentColor, 52));
        startPortalPaint.setColor(withAlpha(accentColor, 200));
        boardAmbientPaint.setColor(withAlpha(wallColor, 22));
        ambientSpotlightPaint.setColor(withAlpha(accentColor, 36));
        ambientSpotlightInnerPaint.setColor(withAlpha(accentColor, 62));
        playerBodyPaint.setColor(accentColor);
        viewportFramePaint.setColor(withAlpha(wallColor, 120));
        invalidate();
    }

    public void updateStudioTheme(int panelColor, int wallColor, int visitedColor, int accentColor) {
        bgPaint.setColor(panelColor);
        int lum = (Color.red(panelColor) * 299 + Color.green(panelColor) * 587 + Color.blue(panelColor) * 114) / 1000;
        if (lum >= 160) {
            gridFloorPaint.setColor(Color.parseColor("#160F172A"));
            flagPolePaint.setColor(Color.parseColor("#334155"));
            flagBasePaint.setColor(Color.parseColor("#64748B"));
        } else {
            gridFloorPaint.setColor(Color.parseColor("#22FFFFFF"));
            flagPolePaint.setColor(Color.parseColor("#F8FAFC"));
            flagBasePaint.setColor(Color.parseColor("#CBD5E1"));
        }
        updateColors(wallColor, visitedColor, accentColor);
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(
                Math.max(0, Math.min(255, alpha)),
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        );
    }

    public void setPlayerSkinId(String skinId) {
        if (skinId != null && !skinId.isEmpty()) {
            this.currentSkinId = skinId;
            invalidate();
        }
    }

    public void setDynamicEntities(
            @Nullable List<MazeCollectibleItem> collectibles,
            @Nullable MazePoint wolf,
            @Nullable MazePoint wolfCub,
            boolean wolfFrenzy,
            boolean wolfStunned,
            boolean shieldActive,
            boolean playerFrozen,
            boolean iceBeamActive
    ) {
        this.collectibleItems.clear();
        if (collectibles != null) {
            this.collectibleItems.addAll(collectibles);
        }
        this.wolfPos = (wolf != null) ? new MazePoint(wolf.x, wolf.y) : null;
        this.wolfCubPos = (wolfCub != null) ? new MazePoint(wolfCub.x, wolfCub.y) : null;
        this.isWolfFrenzy = wolfFrenzy;
        this.isWolfStunned = wolfStunned;
        this.hasShieldProtection = shieldActive;
        this.isPlayerFrozen = playerFrozen;
        this.isIceBeamActive = iceBeamActive;
        invalidate();
    }

    public void setMazeData(
            int w,
            int h,
            int[] masksArray,
            MazePoint start,
            MazePoint goal,
            MazePoint player,
            Set<Integer> visited,
            List<MazePoint> history,
            @Nullable List<MazePoint> hint,
            @Nullable Integer vision
    ) {
        this.mazeW = w;
        this.mazeH = h;
        this.masks = (masksArray != null) ? masksArray : new int[0];
        this.startPoint = new MazePoint(start.x, start.y);
        this.goalPoint = new MazePoint(goal.x, goal.y);
        this.playerPos = new MazePoint(player.x, player.y);
        this.visionWindow = vision;
        boolean atGoal = (player.x == goal.x && player.y == goal.y && visited != null && visited.size() > 1);
        if (atGoal) {
            if (!this.isCelebratingWin && !this.isGameWon) {
                this.isCelebratingWin = true;
                this.winAnimStartTime = System.currentTimeMillis();
                this.isDragging = false;
                postInvalidate();
            }
        } else {
            this.isCelebratingWin = false;
            this.isGameWon = false;
        }

        this.visitedSet.clear();
        if (visited != null) {
            this.visitedSet.addAll(visited);
        } else {
            this.visitedSet.add(player.y * mazeW + player.x);
        }

        this.pathHistory.clear();
        if (history != null && !history.isEmpty()) {
            this.pathHistory.addAll(history);
        } else {
            this.pathHistory.add(new MazePoint(player.x, player.y));
        }
        if (this.pathHistory.size() <= 1) {
            this.stepColorParities.clear();
            this.stepColorParities.add(0);
            this.currentMoveParity = 0;
            this.hasMovedInCurrentGesture = false;
        } else {
            while (this.stepColorParities.size() < this.pathHistory.size()) {
                this.stepColorParities.add(this.currentMoveParity);
            }
            while (this.stepColorParities.size() > this.pathHistory.size()) {
                this.stepColorParities.remove(this.stepColorParities.size() - 1);
            }
        }

        this.hintPoints.clear();
        if (hint != null) {
            this.hintPoints.addAll(hint);
        }

        invalidate();
    }

    public boolean undoStep() {
        if (isGameWon || isCelebratingWin || pathHistory.size() <= 1) return false;
        pathHistory.remove(pathHistory.size() - 1);
        if (stepColorParities.size() > 1) {
            stepColorParities.remove(stepColorParities.size() - 1);
        }
        MazePoint prev = pathHistory.get(pathHistory.size() - 1);
        playerPos = new MazePoint(prev.x, prev.y);

        // Rebuild visited set from history
        visitedSet.clear();
        for (MazePoint p : pathHistory) {
            visitedSet.add(p.y * mazeW + p.x);
        }

        if (moveListener != null) {
            moveListener.onUndo(playerPos);
        }
        invalidate();
        return true;
    }

    public void resetLevel() {
        removeCallbacks(stepQueueRunnable);
        pendingStepsQueue.clear();
        if (startPoint == null) return;
        playerPos = new MazePoint(startPoint.x, startPoint.y);
        pathHistory.clear();
        pathHistory.add(new MazePoint(startPoint.x, startPoint.y));
        stepColorParities.clear();
        stepColorParities.add(0);
        currentMoveParity = 0;
        hasMovedInCurrentGesture = false;
        visitedSet.clear();
        visitedSet.add(startPoint.y * mazeW + startPoint.x);
        isGameWon = false;
        isCelebratingWin = false;
        isDragging = false;
        stepModeHasMoved = false;
        if (moveListener != null) {
            moveListener.onUndo(playerPos);
        }
        invalidate();
    }

    public int cellAt(int x, int y) {
        if (x < 0 || x >= mazeW || y < 0 || y >= mazeH) return 0;
        int idx = y * mazeW + x;
        if (idx < 0 || idx >= masks.length) return 0;
        return masks[idx];
    }

    // ==========================================
    // XỬ LÝ SỰ KIỆN KÉO THẢ / VUỐT TỪNG BƯỚC
    // ==========================================
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isGameWon || isCelebratingWin) return super.onTouchEvent(event);

        float touchX = event.getX();
        float touchY = event.getY();

        if (cellSize <= 0f) return super.onTouchEvent(event);

        // Chuyển đổi Pixel sang Grid (col: x, row: y)
        int localCol = (int) Math.floor((touchX - offX) / cellSize);
        int localRow = (int) Math.floor(((offY + viewH * cellSize) - touchY) / cellSize);

        int targetX = originX + localCol;
        int targetY = originY + localRow;

        long now = System.currentTimeMillis();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchDownX = touchX;
                touchDownY = touchY;
                stepModeHasMoved = false;
                hasMovedInCurrentGesture = false;
                lastTouchCellX = targetX;
                lastTouchCellY = targetY;
                lastAutoStepTime = now;

                // CHẾ ĐỘ DI CHUYỂN TỪNG BƯỚC (NHẤN HOẶC KÉO TỚI Ô MUỐN ĐẾN - TỐC ĐỘ TỐI ĐA 0.1S / BƯỚC):
                if (isStepByStepMode) {
                    isDragging = true;
                    if (isInsideMaze(targetX, targetY) && (targetX != playerPos.x || targetY != playerPos.y)) {
                        tryMoveToTappedCell(targetX, targetY);
                    }
                    return true;
                }

                isDragging = true;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (!isDragging) return true;

                if (!isInsideMaze(targetX, targetY)) return true;
                if (targetX == playerPos.x && targetY == playerPos.y) return true;

                // Ở chế độ từng bước: di chuyển ngón tay liên tục sẽ tiếp tục bước theo lộ trình với tốc độ tối đa 0.1s/bước
                if (isStepByStepMode) {
                    if (targetX != lastTouchCellX || targetY != lastTouchCellY) {
                        lastTouchCellX = targetX;
                        lastTouchCellY = targetY;
                        tryMoveToTappedCell(targetX, targetY);
                    }
                    return true;
                }

                lastTouchCellX = targetX;
                lastTouchCellY = targetY;

                // 1. TÍNH NĂNG HOÀN TÁC (Tự động Undo khi kéo/vuốt ngược về ô trước)
                int existingHistoryIdx = -1;
                for (int i = 0; i < pathHistory.size() - 1; i++) {
                    MazePoint p = pathHistory.get(i);
                    if (p.x == targetX && p.y == targetY) {
                        existingHistoryIdx = i;
                        break;
                    }
                }
                if (existingHistoryIdx >= 0) {
                    while (pathHistory.size() - 1 > existingHistoryIdx) {
                        undoStep();
                    }
                    lastAutoStepTime = now;
                    invalidate();
                    return true;
                }

                // 2. VUỐT ĐỂ TRƯỢT: DỪNG LẠI KHI GẶP NGÃ BA, NGÃ TƯ HOẶC TƯỜNG (AUTO MODE)
                int swipeDx = targetX - playerPos.x;
                int swipeDy = targetY - playerPos.y;
                if (Math.abs(swipeDx) > 0 || Math.abs(swipeDy) > 0) {
                    if (slideInDirection(swipeDx, swipeDy)) {
                        hasMovedInCurrentGesture = true;
                        lastAutoStepTime = now;
                        invalidate();
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (hasMovedInCurrentGesture) {
                    currentMoveParity = 1 - currentMoveParity;
                    hasMovedInCurrentGesture = false;
                }
                isDragging = false;
                stepModeHasMoved = false;
                return true;
        }

        return super.onTouchEvent(event);
    }

    private boolean slideInDirection(int initialDx, int initialDy) {
        if (initialDx == 0 && initialDy == 0) return false;
        int dx = Integer.compare(initialDx, 0);
        int dy = Integer.compare(initialDy, 0);
        if (dx != 0 && dy != 0) {
            if (Math.abs(initialDx) >= Math.abs(initialDy)) {
                dy = 0;
            } else {
                dx = 0;
            }
        }

        if (moveListener != null) {
            moveListener.onMove(dx, dy, playerPos, 0);
            return true;
        }
        return false;
    }

    /**
     * Di chuyển nhân vật về hướng ô (targetX, targetY) thông qua GameViewModel authority:
     */
    private boolean tryMoveToTappedCell(int targetX, int targetY) {
        if (!isInsideMaze(targetX, targetY)) return false;
        if (targetX == playerPos.x && targetY == playerPos.y) return false;

        int dx = targetX - playerPos.x;
        int dy = targetY - playerPos.y;
        if (Math.abs(dx) >= Math.abs(dy)) {
            dx = (dx > 0) ? 1 : -1;
            dy = 0;
        } else {
            dy = (dy > 0) ? 1 : -1;
            dx = 0;
        }

        if (moveListener != null) {
            moveListener.onMove(dx, dy, playerPos, 0);
            return true;
        }
        return false;
    }

    /**
     * Thực hiện từng bước đi trong hàng đợi với tốc độ điều tiết chuẩn xác:
     * - Chế độ từng bước: Tối đa 0.1 giây / bước (100ms / ô).
     * - Chế độ tự động: Tốc độ tối đa 10 ô / giây (100ms / ô) tại 200% độ nhạy.
     */
    private void processNextPendingStep() {
        removeCallbacks(stepQueueRunnable);
        if (isGameWon || isCelebratingWin || pendingStepsQueue.isEmpty()) {
            pendingStepsQueue.clear();
            return;
        }
        if (isPlayerFrozen) {
            pendingStepsQueue.clear();
            if (moveListener != null) {
                moveListener.onMove(0, 0, playerPos, Math.max(0, pathHistory.size() - 1));
            }
            return;
        }

        long now = System.currentTimeMillis();
        long stepInterval;
        if (isStepByStepMode) {
            stepInterval = STEP_MODE_COOLDOWN_MS; // 100ms = 0.1s / ô
        } else {
            // Chế độ tự động: Tốc độ tối đa 10 ô / 1 giây (100ms / ô) tương đương 200% độ nhạy
            stepInterval = (long) Math.max(100L, 200L / Math.max(0.5f, Math.min(2.0f, moveSensitivity)));
        }

        long elapsed = now - lastStepTime;
        if (elapsed < stepInterval) {
            postDelayed(stepQueueRunnable, stepInterval - elapsed);
            return;
        }

        MazePoint nextStep = pendingStepsQueue.remove(0);
        int dx = nextStep.x - playerPos.x;
        int dy = nextStep.y - playerPos.y;

        if ((Math.abs(dx) + Math.abs(dy) == 1) && tryMoveSingleCell(dx, dy)) {
            lastStepTime = System.currentTimeMillis();
            stepModeHasMoved = true;
            hasMovedInCurrentGesture = true;
            invalidate();

            if (!pendingStepsQueue.isEmpty() && !isGameWon && !isCelebratingWin && !isPlayerFrozen) {
                postDelayed(stepQueueRunnable, stepInterval);
            } else {
                pendingStepsQueue.clear();
                currentMoveParity = 1 - currentMoveParity;
                invalidate();
            }
        } else {
            pendingStepsQueue.clear();
        }
    }

    /**
     * Tìm đường đi ngắn nhất & ít khúc cua nhất từ playerPos đến (targetX, targetY)
     * trong phạm vi gần ngón tay người chơi.
     */
    private List<MazePoint> findMultiTurnMazePath(int targetX, int targetY, int maxTurns) {
        final int[] dxs = {1, -1, 0, 0};
        final int[] dys = {0, 0, 1, -1};
        final int[] dirMasks = {EAST, WEST, NORTH, SOUTH};

        class MazeSearchNode {
            final int x, y, dir, turns;
            final MazeSearchNode parent;
            MazeSearchNode(int x, int y, int dir, int turns, MazeSearchNode parent) {
                this.x = x;
                this.y = y;
                this.dir = dir;
                this.turns = turns;
                this.parent = parent;
            }
        }

        int maxSearchDist = 80;
        if (Math.abs(targetX - playerPos.x) + Math.abs(targetY - playerPos.y) > maxSearchDist) {
            return null;
        }

        List<MazeSearchNode> queue = new ArrayList<>();
        Set<Integer> visitedStates = new HashSet<>();
        queue.add(new MazeSearchNode(playerPos.x, playerPos.y, 4, 0, null));
        visitedStates.add(((playerPos.y * mazeW + playerPos.x) * 5) + 4);

        int head = 0;
        MazeSearchNode bestGoal = null;

        while (head < queue.size() && head < 4000) {
            MazeSearchNode curr = queue.get(head++);
            if (curr.x == targetX && curr.y == targetY) {
                if (bestGoal == null || curr.turns < bestGoal.turns) {
                    bestGoal = curr;
                    if (curr.turns <= 1) break;
                }
                continue;
            }

            int mask = cellAt(curr.x, curr.y);
            for (int d = 0; d < 4; d++) {
                if ((mask & dirMasks[d]) == 0) continue;
                int nx = curr.x + dxs[d];
                int ny = curr.y + dys[d];
                if (!isInsideMaze(nx, ny)) continue;

                int nextTurns = (curr.dir == 4 || curr.dir == d) ? curr.turns : (curr.turns + 1);
                if (nextTurns > maxTurns) continue;

                int stateKey = ((ny * mazeW + nx) * 5) + d;
                if (visitedStates.contains(stateKey)) continue;
                visitedStates.add(stateKey);

                queue.add(new MazeSearchNode(nx, ny, d, nextTurns, curr));
            }
        }

        if (bestGoal == null) return null;

        List<MazePoint> steps = new ArrayList<>();
        MazeSearchNode node = bestGoal;
        while (node != null && node.parent != null) {
            steps.add(0, new MazePoint(node.x, node.y));
            node = node.parent;
        }
        return steps;
    }

    private boolean tryMoveSingleCell(int dx, int dy) {
        if (dx == 0 && dy == 0) return false;
        if (isPlayerFrozen) {
            if (moveListener != null) {
                moveListener.onMove(dx, dy, playerPos, Math.max(0, pathHistory.size() - 1));
            }
            return false;
        }
        int nextX = playerPos.x + dx;
        int nextY = playerPos.y + dy;

        if (!isInsideMaze(nextX, nextY)) return false;

        int dirMask = 0;
        if (dx == 1 && dy == 0) dirMask = EAST;
        else if (dx == -1 && dy == 0) dirMask = WEST;
        else if (dx == 0 && dy == 1) dirMask = NORTH;
        else if (dx == 0 && dy == -1) dirMask = SOUTH;

        int currentMask = cellAt(playerPos.x, playerPos.y);

        if ((currentMask & dirMask) != 0) {
            if (pathHistory.size() >= 2) {
                MazePoint prev = pathHistory.get(pathHistory.size() - 2);
                if (nextX == prev.x && nextY == prev.y) {
                    pathHistory.remove(pathHistory.size() - 1);
                    if (stepColorParities.size() > 1) {
                        stepColorParities.remove(stepColorParities.size() - 1);
                    }
                    playerPos = new MazePoint(prev.x, prev.y);

                    visitedSet.clear();
                    for (MazePoint p : pathHistory) {
                        visitedSet.add(p.y * mazeW + p.x);
                    }

                    if (moveListener != null) {
                        moveListener.onUndo(playerPos);
                    }
                    return true;
                }
            }

            playerPos = new MazePoint(nextX, nextY);
            pathHistory.add(new MazePoint(nextX, nextY));
            stepColorParities.add(currentMoveParity);
            visitedSet.add(nextY * mazeW + nextX);

            if (moveListener != null) {
                moveListener.onMove(dx, dy, playerPos, pathHistory.size() - 1);
            }

            if (playerPos.x == goalPoint.x && playerPos.y == goalPoint.y) {
                if (!isCelebratingWin && !isGameWon) {
                    isCelebratingWin = true;
                    winAnimStartTime = System.currentTimeMillis();
                    isDragging = false;
                    postInvalidate();
                }
            }
            return true;
        } else {
            if (moveListener != null) {
                moveListener.onWallHit();
            }
            return false;
        }
    }

    private boolean isInsideMaze(int x, int y) {
        return x >= 0 && x < mazeW && y >= 0 && y < mazeH;
    }

    private boolean isPointInViewport(MazePoint p) {
        return p.x >= originX && p.x < originX + viewW && p.y >= originY && p.y < originY + viewH;
    }

    private boolean isConnectedStep(MazePoint a, MazePoint b) {
        int dx = b.x - a.x;
        int dy = b.y - a.y;
        if (Math.abs(dx) + Math.abs(dy) != 1) return false;
        int mask = cellAt(a.x, a.y);
        if (dx == 1) return (mask & EAST) != 0;
        if (dx == -1) return (mask & WEST) != 0;
        if (dy == 1) return (mask & NORTH) != 0;
        if (dy == -1) return (mask & SOUTH) != 0;
        return false;
    }

    // ==========================================
    // HIỂN THỊ ĐỒ HỌA TRÊN CANVAS (ONDRAW) - 60/120 FPS
    // ==========================================
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float canvasW = getWidth();
        float canvasH = getHeight();
        if (canvasW <= 0 || canvasH <= 0 || mazeW <= 0 || mazeH <= 0) return;

        viewW = (visionWindow != null) ? Math.min(visionWindow, mazeW) : mazeW;
        viewH = (visionWindow != null) ? Math.min(visionWindow, mazeH) : mazeH;

        if (visionWindow != null) {
            originX = Math.min(Math.max(0, playerPos.x - viewW / 2), mazeW - viewW);
            originY = Math.min(Math.max(0, playerPos.y - viewH / 2), mazeH - viewH);
        } else {
            originX = 0;
            originY = 0;
        }

        cellSize = Math.min(canvasW / viewW, canvasH / viewH);
        offX = (canvasW - viewW * cellSize) / 2f;
        offY = (canvasH - viewH * cellSize) / 2f;

        float strokeWidth = Math.max(2.6f, cellSize * 0.09f);
        wallPaint.setStrokeWidth(strokeWidth);
        wallGlowPaint.setStrokeWidth(strokeWidth * 2.8f);
        wallCorePaint.setStrokeWidth(Math.max(1.1f, strokeWidth * 0.34f));
        float trailStroke = cellSize * 0.27f;
        pathTrailGlowPaint.setStrokeWidth(cellSize * 0.46f);
        pathTrailPaint.setStrokeWidth(trailStroke);
        pathTrailLightPaint.setStrokeWidth(trailStroke);
        pathTrailCorePaint.setStrokeWidth(Math.max(1.5f, cellSize * 0.095f));
        flagPolePaint.setStrokeWidth(Math.max(2.2f, cellSize * 0.08f));

        // 0. Nền bàn cờ rõ nét & khung viền góc nhìn 10x10
        tempRect.set(offX, offY, offX + viewW * cellSize, offY + viewH * cellSize);
        canvas.drawRoundRect(tempRect, 14f, 14f, bgPaint);
        canvas.drawRoundRect(tempRect, 14f, 14f, viewportFramePaint);

        // Vẽ lưới nền mảnh (Floor Grid) giúp nhìn rõ từng ô trong mê cung
        if (cellSize >= 14f) {
            for (int ly = 0; ly <= viewH; ly++) {
                float yLine = offY + ly * cellSize;
                canvas.drawLine(offX, yLine, offX + viewW * cellSize, yLine, gridFloorPaint);
            }
            for (int lx = 0; lx <= viewW; lx++) {
                float xLine = offX + lx * cellSize;
                canvas.drawLine(xLine, offY, xLine, offY + viewH * cellSize, gridFloorPaint);
            }
        }

        int playerColor = com.example.data.shop.GameItemAssets.INSTANCE.getSkinPrimaryColorArgb(currentSkinId);
        // Đường line đi theo người chơi: 1 màu đồng nhất rõ nét
        pathTrailPaint.setColor(playerColor);
        startPortalPaint.setColor(withAlpha(playerColor, 200));

        // 1.5. Vẽ Cổng Xuất Phát gọn gàng
        if (startPoint.x >= originX && startPoint.x < originX + viewW &&
                startPoint.y >= originY && startPoint.y < originY + viewH) {
            float sxCenter = offX + (startPoint.x - originX) * cellSize + cellSize / 2f;
            float syCenter = offY + (viewH - 1 - (startPoint.y - originY)) * cellSize + cellSize / 2f;
            canvas.drawCircle(sxCenter, syCenter, cellSize * 0.28f, startPortalPaint);
        }

        // 2. Vẽ đường nối 1 màu duy nhất đi theo người chơi
        if (pathHistory.size() > 1) {
            tempPath.reset();
            boolean hasSegment = false;

            for (int i = 1; i < pathHistory.size(); i++) {
                MazePoint prev = pathHistory.get(i - 1);
                MazePoint curr = pathHistory.get(i);

                if (!isConnectedStep(prev, curr)) {
                    continue;
                }

                boolean prevIn = isPointInViewport(prev);
                boolean currIn = isPointInViewport(curr);

                if (!prevIn && !currIn) {
                    continue;
                }

                float prevPx = offX + (prev.x - originX) * cellSize + cellSize / 2f;
                float prevPy = offY + (viewH - 1 - (prev.y - originY)) * cellSize + cellSize / 2f;
                float currPx = offX + (curr.x - originX) * cellSize + cellSize / 2f;
                float currPy = offY + (viewH - 1 - (curr.y - originY)) * cellSize + cellSize / 2f;

                tempPath.moveTo(prevPx, prevPy);
                tempPath.lineTo(currPx, currPy);
                hasSegment = true;
            }

            if (hasSegment) {
                canvas.save();
                canvas.clipRect(offX, offY, offX + viewW * cellSize, offY + viewH * cellSize);
                canvas.drawPath(tempPath, pathTrailPaint);
                canvas.restore();
            }
        }

        // 3. Vẽ gợi ý đường đi
        if (!hintPoints.isEmpty()) {
            for (MazePoint p : hintPoints) {
                if (p.x >= originX && p.x < originX + viewW && p.y >= originY && p.y < originY + viewH) {
                    float hx = offX + (p.x - originX) * cellSize + cellSize / 2f;
                    float hy = offY + (viewH - 1 - (p.y - originY)) * cellSize + cellSize / 2f;
                    canvas.drawCircle(hx, hy, cellSize * 0.18f, hintPaint);
                }
            }
        }

        // 4. Vẽ tường mê cung sắc nét, viền rõ ràng giúp người chơi nhìn rõ từng hướng đi không bị chặn
        wallPath.reset();
        for (int ly = 0; ly < viewH; ly++) {
            for (int lx = 0; lx < viewW; lx++) {
                int gx = originX + lx;
                int gy = originY + ly;
                int mask = cellAt(gx, gy);
                float sx = offX + lx * cellSize;
                float sy = offY + (viewH - 1 - ly) * cellSize;

                // Tường BẮC (NORTH - phía trên trên màn hình)
                if ((mask & NORTH) == 0) {
                    wallPath.moveTo(sx, sy);
                    wallPath.lineTo(sx + cellSize, sy);
                }
                // Tường NAM (SOUTH - phía dưới trên màn hình)
                if ((mask & SOUTH) == 0) {
                    wallPath.moveTo(sx, sy + cellSize);
                    wallPath.lineTo(sx + cellSize, sy + cellSize);
                }
                // Tường TÂY (WEST - bên trái)
                if ((mask & WEST) == 0) {
                    wallPath.moveTo(sx, sy);
                    wallPath.lineTo(sx, sy + cellSize);
                }
                // Tường ĐÔNG (EAST - bên phải)
                if ((mask & EAST) == 0) {
                    wallPath.moveTo(sx + cellSize, sy);
                    wallPath.lineTo(sx + cellSize, sy + cellSize);
                }
            }
        }
        canvas.drawPath(wallPath, wallPaint);
        canvas.drawPath(wallPath, wallCorePaint);

        // 5. VẼ ĐÍCH ĐẾN: LÁ CỜ MÀU ĐỎ (không vẽ hào quang bên cạnh)
        if (goalPoint.x >= originX && goalPoint.x < originX + viewW &&
                goalPoint.y >= originY && goalPoint.y < originY + viewH) {
            float gLeft = offX + (goalPoint.x - originX) * cellSize;
            float gTop = offY + (viewH - 1 - (goalPoint.y - originY)) * cellSize;

            // Chân đế cột cờ (Base)
            float baseLeft = gLeft + cellSize * 0.22f;
            float baseTop = gTop + cellSize * 0.78f;
            float baseRight = gLeft + cellSize * 0.50f;
            float baseBottom = gTop + cellSize * 0.86f;
            tempRect.set(baseLeft, baseTop, baseRight, baseBottom);
            canvas.drawRoundRect(tempRect, cellSize * 0.04f, cellSize * 0.04f, flagBasePaint);

            // Cột cờ (Flagpole)
            float poleX = gLeft + cellSize * 0.36f;
            float poleTopY = gTop + cellSize * 0.16f;
            float poleBottomY = gTop + cellSize * 0.82f;
            canvas.drawLine(poleX, poleTopY, poleX, poleBottomY, flagPolePaint);

            // Chóp cột cờ tròn mạ vàng (Gold Finial Topper)
            canvas.drawCircle(poleX, poleTopY, cellSize * 0.06f, flagTopperPaint);

            // Lá cờ màu đỏ (Red Flag Cloth) hình tam giác lượn sóng
            flagPath.reset();
            flagPath.moveTo(poleX + cellSize * 0.02f, poleTopY + cellSize * 0.04f);
            flagPath.lineTo(poleX + cellSize * 0.46f, poleTopY + cellSize * 0.22f); // Đỉnh cờ nhọn vươn sang phải
            flagPath.lineTo(poleX + cellSize * 0.02f, poleTopY + cellSize * 0.40f);
            flagPath.close();

            canvas.drawPath(flagPath, flagClothPaint);
            canvas.drawPath(flagPath, flagClothShadowPaint);

            // Điểm nhấn sao/huy hiệu trắng ở giữa lá cờ
            canvas.drawCircle(poleX + cellSize * 0.16f, poleTopY + cellSize * 0.22f, cellSize * 0.045f, flagEmblemPaint);
        }

        // 6. Vẽ Nhân vật (Player) gọn gàng, không bị che khuất bởi vật phẩm rườm rà
        if (playerPos.x >= originX && playerPos.x < originX + viewW &&
                playerPos.y >= originY && playerPos.y < originY + viewH) {
            float pxCenter = offX + (playerPos.x - originX) * cellSize + cellSize / 2f;
            float pyCenter = offY + (viewH - 1 - (playerPos.y - originY)) * cellSize + cellSize / 2f;

            com.example.data.shop.GameItemAssets.INSTANCE.drawPlayerOnCanvas(
                canvas,
                pxCenter,
                pyCenter,
                cellSize,
                currentSkinId
            );

            if (isPlayerFrozen) {
                com.example.data.shop.GameItemAssets.INSTANCE.drawFrozenEffectOnPlayer(
                    canvas,
                    pxCenter,
                    pyCenter,
                    cellSize
                );
            }
        }

        // 6.5. Vẽ Tia Băng Xuyên Tường từ Sói tới Người Chơi (nếu vừa ném băng)
        if ((isIceBeamActive || isPlayerFrozen) && wolfPos != null) {
            float wxCenter = offX + (wolfPos.x - originX) * cellSize + cellSize / 2f;
            float wyCenter = offY + (viewH - 1 - (wolfPos.y - originY)) * cellSize + cellSize / 2f;
            float pxCenter = offX + (playerPos.x - originX) * cellSize + cellSize / 2f;
            float pyCenter = offY + (viewH - 1 - (playerPos.y - originY)) * cellSize + cellSize / 2f;
            com.example.data.shop.GameItemAssets.INSTANCE.drawIceBeamOnCanvas(
                canvas,
                wxCenter,
                wyCenter,
                pxCenter,
                pyCenter,
                cellSize
            );
        }

        // 7. Vẽ Con Sói Truy Đuổi (Wolf Chase Mode)
        if (wolfPos != null && wolfPos.x >= originX && wolfPos.x < originX + viewW &&
                wolfPos.y >= originY && wolfPos.y < originY + viewH) {
            float wxCenter = offX + (wolfPos.x - originX) * cellSize + cellSize / 2f;
            float wyCenter = offY + (viewH - 1 - (wolfPos.y - originY)) * cellSize + cellSize / 2f;

            com.example.data.shop.GameItemAssets.INSTANCE.drawWolfOnCanvas(
                canvas,
                wxCenter,
                wyCenter,
                cellSize,
                isWolfFrenzy,
                isWolfStunned
            );
        }

        // 7.5. Vẽ Sói Con (Wolf Cub - nhỏ và chạy nhanh)
        if (wolfCubPos != null && wolfCubPos.x >= originX && wolfCubPos.x < originX + viewW &&
                wolfCubPos.y >= originY && wolfCubPos.y < originY + viewH) {
            float cxCenter = offX + (wolfCubPos.x - originX) * cellSize + cellSize / 2f;
            float cyCenter = offY + (viewH - 1 - (wolfCubPos.y - originY)) * cellSize + cellSize / 2f;
            com.example.data.shop.GameItemAssets.INSTANCE.drawWolfOnCanvas(
                canvas,
                cxCenter,
                cyCenter,
                cellSize * 0.65f,
                true,
                false
            );
        }

        // 8. Hiệu ứng pháo hoa / tia sáng hoàn thành kéo dài đúng 1 GIÂY (1000ms) khi chạm lá cờ
        if (isCelebratingWin) {
            long elapsed = System.currentTimeMillis() - winAnimStartTime;
            if (elapsed < WIN_ANIM_DURATION_MS) {
                float progress = (float) elapsed / WIN_ANIM_DURATION_MS; // 0.0 -> 1.0 trong 1 giây
                if (goalPoint.x >= originX && goalPoint.x < originX + viewW &&
                    goalPoint.y >= originY && goalPoint.y < originY + viewH) {
                    float gCenterX = offX + (goalPoint.x - originX + 0.5f) * cellSize;
                    float gCenterY = offY + (viewH - 1 - (goalPoint.y - originY) + 0.5f) * cellSize;

                    // 8.1. Hào quang tâm lá cờ nhấp nháy rực sáng suốt 2 giây
                    float pulse = (float) (0.5f + 0.5f * Math.sin(progress * Math.PI * 8.0));
                    int haloAlpha = (int) (150 * (1f - progress * 0.35f));
                    winParticlePaint.setColor(Color.argb(haloAlpha, 254, 240, 138));
                    canvas.drawCircle(gCenterX, gCenterY, cellSize * (0.85f + 0.45f * pulse), winParticlePaint);
                    winParticlePaint.setColor(Color.argb((int) (190 * (1f - progress * 0.25f)), 255, 255, 255));
                    canvas.drawCircle(gCenterX, gCenterY, cellSize * (0.42f + 0.20f * pulse), winParticlePaint);

                    // 8.2. 16 Tia sáng hào quang xoay tròn bắn ra từ lá cờ
                    int rayCount = 16;
                    double rotationOffset = progress * Math.PI * 2.5;
                    for (int i = 0; i < rayCount; i++) {
                        double angle = i * (2.0 * Math.PI / rayCount) + rotationOffset;
                        boolean isMajorRay = (i % 2 == 0);
                        float rayWave = (float) (0.75f + 0.25f * Math.sin(progress * Math.PI * 6.0 + i));
                        float rInner = cellSize * 0.35f;
                        float rOuter = cellSize * (isMajorRay ? 4.2f : 2.8f) * rayWave;
                        float x1 = gCenterX + (float) (rInner * Math.cos(angle));
                        float y1 = gCenterY + (float) (rInner * Math.sin(angle));
                        float x2 = gCenterX + (float) (rOuter * Math.cos(angle));
                        float y2 = gCenterY + (float) (rOuter * Math.sin(angle));

                        winBurstPaint.setStrokeWidth(Math.max(2.5f, cellSize * (isMajorRay ? 0.14f : 0.09f)));
                        int rayAlpha = (int) (235 * (1f - progress * 0.3f));
                        if (isMajorRay) {
                            winBurstPaint.setColor(Color.argb(rayAlpha, 255, 215, 0)); // Gold
                        } else {
                            winBurstPaint.setColor(Color.argb(rayAlpha, 254, 240, 138)); // Warm light yellow
                        }
                        canvas.drawLine(x1, y1, x2, y2, winBurstPaint);
                    }

                    // 8.3. 3 Đợt sóng vòng tròn ánh sáng lan tỏa liên hoàn trong 2 giây
                    for (int wave = 0; wave < 3; wave++) {
                        float waveProgress = (progress * 2.2f + wave * 0.33f) % 1.0f;
                        float waveRadius = cellSize * (0.5f + 4.8f * waveProgress);
                        float waveFade = 1.0f - waveProgress;
                        winBurstPaint.setStrokeWidth(Math.max(2f, cellSize * 0.18f * waveFade));

                        if (wave == 0) {
                            winBurstPaint.setColor(Color.argb((int) (255 * waveFade), 255, 215, 0)); // Vàng kim
                        } else if (wave == 1) {
                            winBurstPaint.setColor(Color.argb((int) (230 * waveFade), 239, 68, 68)); // Đỏ rực
                        } else {
                            winBurstPaint.setColor(Color.argb((int) (230 * waveFade), 56, 189, 248)); // Xanh ngọc ánh sáng
                        }
                        canvas.drawCircle(gCenterX, gCenterY, waveRadius, winBurstPaint);
                    }

                    // 8.4. 20 Hạt pháo hoa ánh sáng bắn tung tóe 2 đợt
                    int particleCount = 20;
                    float burstCycle = (progress * 2.0f) % 1.0f;
                    float particleFade = 1.0f - burstCycle * 0.85f;
                    for (int p = 0; p < particleCount; p++) {
                        double pAngle = p * (2.0 * Math.PI / particleCount) + burstCycle * 0.6;
                        float speedFactor = 0.65f + (p % 4) * 0.25f;
                        float pDist = cellSize * (0.4f + 4.0f * burstCycle * speedFactor);
                        float px = gCenterX + (float) (pDist * Math.cos(pAngle));
                        float py = gCenterY + (float) (pDist * Math.sin(pAngle));
                        float pRadius = Math.max(2.5f, cellSize * (0.13f - 0.06f * burstCycle));

                        int pAlpha = (int) (255 * particleFade);
                        if (p % 3 == 0) {
                            winParticlePaint.setColor(Color.argb(pAlpha, 255, 215, 0));
                        } else if (p % 3 == 1) {
                            winParticlePaint.setColor(Color.argb(pAlpha, 255, 255, 255));
                        } else {
                            winParticlePaint.setColor(Color.argb(pAlpha, 249, 115, 22));
                        }
                        canvas.drawCircle(px, py, pRadius, winParticlePaint);
                    }
                }
                postInvalidateDelayed(16);
            } else {
                isCelebratingWin = false;
                isGameWon = true;
                if (moveListener != null) {
                    moveListener.onGameWin(pathHistory.size() - 1);
                }
            }
        }
    }

    // =========================================================================
    // CẤU TRÚC DỮ LIỆU CƠ SỞ & THUẬT TOÁN TẠO MÊ CUNG THUẦN JAVA THEO CẤP ĐỘ
    // =========================================================================

    public static class Cell {
        public int x, y;
        public boolean topWall = true;
        public boolean rightWall = true;
        public boolean bottomWall = true;
        public boolean leftWall = true;
        public boolean visited = false;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class Edge {
        public Cell cell1, cell2;
        public Edge(Cell c1, Cell c2) {
            this.cell1 = c1;
            this.cell2 = c2;
        }
    }

    public Cell[][] initGrid(int cols, int rows) {
        Cell[][] grid = new Cell[cols][rows];
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                grid[i][j] = new Cell(i, j);
            }
        }
        return grid;
    }

    public static void removeWalls(Cell current, Cell next) {
        int dx = current.x - next.x;
        if (dx == 1) { current.leftWall = false; next.rightWall = false; }
        else if (dx == -1) { current.rightWall = false; next.leftWall = false; }

        int dy = current.y - next.y;
        if (dy == 1) { current.bottomWall = false; next.topWall = false; }
        else if (dy == -1) { current.topWall = false; next.bottomWall = false; }
    }

    /**
     * 1. Cấp độ nhỏ (5x5 đến 20x20): Thuật toán Quay lui (Backtracking)
     */
    public static void generateBacktracking(Cell[][] grid, int cols, int rows, java.util.Random random) {
        java.util.Stack<Cell> stack = new java.util.Stack<>();
        Cell current = grid[0][0];
        current.visited = true;

        do {
            List<Cell> unvisitedNeighbors = getUnvisitedNeighbors(grid, cols, rows, current);
            if (!unvisitedNeighbors.isEmpty()) {
                Cell next = unvisitedNeighbors.get(random.nextInt(unvisitedNeighbors.size()));
                stack.push(current);
                removeWalls(current, next);
                current = next;
                current.visited = true;
            } else if (!stack.isEmpty()) {
                current = stack.pop();
            }
        } while (!stack.isEmpty());
    }

    private static List<Cell> getUnvisitedNeighbors(Cell[][] grid, int cols, int rows, Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        int x = cell.x;
        int y = cell.y;
        if (y < rows - 1 && !grid[x][y + 1].visited) neighbors.add(grid[x][y + 1]); // Top (y+1)
        if (x < cols - 1 && !grid[x + 1][y].visited) neighbors.add(grid[x + 1][y]); // Right (x+1)
        if (y > 0 && !grid[x][y - 1].visited) neighbors.add(grid[x][y - 1]); // Bottom (y-1)
        if (x > 0 && !grid[x - 1][y].visited) neighbors.add(grid[x - 1][y]); // Left (x-1)
        return neighbors;
    }

    /**
     * 2. Cấp độ trung bình (21x21 đến 50x50): Thuật toán Kruskal ngẫu nhiên
     */
    public static void generateKruskal(Cell[][] grid, int cols, int rows, java.util.Random random) {
        List<Edge> edges = new ArrayList<>();
        int[] parent = new int[cols * rows];
        for (int i = 0; i < cols * rows; i++) parent[i] = i;

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                if (x < cols - 1) edges.add(new Edge(grid[x][y], grid[x + 1][y]));
                if (y < rows - 1) edges.add(new Edge(grid[x][y], grid[x][y + 1]));
            }
        }

        java.util.Collections.shuffle(edges, random);

        for (Edge edge : edges) {
            int id1 = edge.cell1.x + edge.cell1.y * cols;
            int id2 = edge.cell2.x + edge.cell2.y * cols;
            int root1 = findParent(parent, id1);
            int root2 = findParent(parent, id2);

            if (root1 != root2) {
                removeWalls(edge.cell1, edge.cell2);
                parent[root1] = root2;
            }
        }
    }

    private static int findParent(int[] parent, int i) {
        if (parent[i] == i) return i;
        return parent[i] = findParent(parent, parent[i]);
    }

    /**
     * 3. Cấp độ lớn (Trên 50x50): Thuật toán Wilson (Uniform Spanning Tree)
     */
    public static void generateWilson(Cell[][] grid, int cols, int rows, java.util.Random random) {
        int unvisitedCount = cols * rows;
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                grid[x][y].visited = false;
            }
        }

        int startX = random.nextInt(cols);
        int startY = random.nextInt(rows);
        grid[startX][startY].visited = true;
        unvisitedCount--;

        int[][] walk = new int[cols][rows];

        while (unvisitedCount > 0) {
            int cx = random.nextInt(cols);
            int cy = random.nextInt(rows);
            while (grid[cx][cy].visited) {
                cx = random.nextInt(cols);
                cy = random.nextInt(rows);
            }

            int wx = cx, wy = cy;
            while (!grid[wx][wy].visited) {
                int dir = random.nextInt(4);
                if (dir == 0 && wy < rows - 1) { walk[wx][wy] = 0; wy++; } // Lên (y+1)
                else if (dir == 1 && wx < cols - 1) { walk[wx][wy] = 1; wx++; } // Phải (x+1)
                else if (dir == 2 && wy > 0) { walk[wx][wy] = 2; wy--; } // Xuống (y-1)
                else if (dir == 3 && wx > 0) { walk[wx][wy] = 3; wx--; } // Trái (x-1)
            }

            wx = cx; wy = cy;
            while (!grid[wx][wy].visited) {
                grid[wx][wy].visited = true;
                unvisitedCount--;

                int dir = walk[wx][wy];
                Cell currentCell = grid[wx][wy];
                Cell nextCell = null;

                if (dir == 0) nextCell = grid[wx][wy + 1];
                else if (dir == 1) nextCell = grid[wx + 1][wy];
                else if (dir == 2) nextCell = grid[wx][wy - 1];
                else if (dir == 3) nextCell = grid[wx - 1][wy];

                if (nextCell != null) {
                    removeWalls(currentCell, nextCell);
                    wx = nextCell.x;
                    wy = nextCell.y;
                }
            }
        }
    }

    /**
     * Thuật toán tìm vị trí Start và End khó nhất theo từng cấp độ hình học (Java)
     */
    public static MazePoint[] determineEndpoints(Cell[][] grid, int cols, int rows, java.util.Random random) {
        int maxDim = Math.max(cols, rows);

        // 1. Cấp độ nhỏ: Start ở Trung tâm, End ở Góc xa nhất
        if (maxDim <= 20) {
            MazePoint start = new MazePoint(cols / 2, rows / 2);
            int[][] dist = bfsDistances(grid, cols, rows, start);

            MazePoint[] corners = new MazePoint[] {
                new MazePoint(0, 0),
                new MazePoint(cols - 1, 0),
                new MazePoint(0, rows - 1),
                new MazePoint(cols - 1, rows - 1)
            };

            MazePoint bestEnd = corners[0];
            int maxDist = dist[corners[0].x][corners[0].y];

            for (MazePoint c : corners) {
                if (dist[c.x][c.y] > maxDist) {
                    maxDist = dist[c.x][c.y];
                    bestEnd = c;
                }
            }
            return new MazePoint[] { start, bestEnd };
        }
        // 2. Cấp độ trung bình: Start ở giữa cạnh Trái, End ở cạnh Phải đối diện
        else if (maxDim <= 50) {
            MazePoint start = new MazePoint(0, rows / 2);
            int[][] dist = bfsDistances(grid, cols, rows, start);

            MazePoint bestEnd = new MazePoint(cols - 1, rows / 2);
            int maxDist = dist[bestEnd.x][bestEnd.y];

            for (int y = 0; y < rows; y++) {
                if (dist[cols - 1][y] > maxDist) {
                    maxDist = dist[cols - 1][y];
                    bestEnd = new MazePoint(cols - 1, y);
                }
            }
            return new MazePoint[] { start, bestEnd };
        }
        // 3. Cấp độ lớn: 2-step BFS tìm khoảng cách xa nhất tuyệt đối (hoặc cặp sát vách)
        else {
            return findFarthestEndpointsDoubleBFS(grid, cols, rows);
        }
    }

    public static int[][] bfsDistances(Cell[][] grid, int cols, int rows, MazePoint start) {
        int[][] dist = new int[cols][rows];
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                dist[x][y] = -1;
            }
        }

        java.util.Queue<MazePoint> queue = new java.util.LinkedList<>();
        dist[start.x][start.y] = 0;
        queue.add(start);

        while (!queue.isEmpty()) {
            MazePoint curr = queue.poll();
            if (curr == null) break;
            Cell c = grid[curr.x][curr.y];
            int d = dist[curr.x][curr.y];

            // Top (y + 1)
            if (!c.topWall && curr.y < rows - 1 && dist[curr.x][curr.y + 1] == -1) {
                dist[curr.x][curr.y + 1] = d + 1;
                queue.add(new MazePoint(curr.x, curr.y + 1));
            }
            // Right (x + 1)
            if (!c.rightWall && curr.x < cols - 1 && dist[curr.x + 1][curr.y] == -1) {
                dist[curr.x + 1][curr.y] = d + 1;
                queue.add(new MazePoint(curr.x + 1, curr.y));
            }
            // Bottom (y - 1)
            if (!c.bottomWall && curr.y > 0 && dist[curr.x][curr.y - 1] == -1) {
                dist[curr.x][curr.y - 1] = d + 1;
                queue.add(new MazePoint(curr.x, curr.y - 1));
            }
            // Left (x - 1)
            if (!c.leftWall && curr.x > 0 && dist[curr.x - 1][curr.y] == -1) {
                dist[curr.x - 1][curr.y] = d + 1;
                queue.add(new MazePoint(curr.x - 1, curr.y));
            }
        }
        return dist;
    }

    public static MazePoint[] findFarthestEndpointsDoubleBFS(Cell[][] grid, int cols, int rows) {
        // Bước 1: BFS từ [0][0] tìm điểm A xa nhất
        int[][] distA = bfsDistances(grid, cols, rows, new MazePoint(0, 0));
        MazePoint pointA = new MazePoint(0, 0);
        int maxDistA = 0;

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                if (distA[x][y] > maxDistA) {
                    maxDistA = distA[x][y];
                    pointA = new MazePoint(x, y);
                }
            }
        }

        // Bước 2: BFS từ điểm A tìm điểm B xa nhất
        int[][] distB = bfsDistances(grid, cols, rows, pointA);
        MazePoint pointB = pointA;
        int maxDistB = 0;

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                if (distB[x][y] > maxDistB) {
                    maxDistB = distB[x][y];
                    pointB = new MazePoint(x, y);
                }
            }
        }

        return new MazePoint[] { pointA, pointB };
    }
}
