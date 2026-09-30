package com.example.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OneLineGameView - Custom View điều khiển trò chơi giải đố "One-line" (Fill / Vẽ một nét).
 * 
 * Đặc điểm kỹ thuật:
 * - Thuần Android SDK (Canvas và Paint), không sử dụng Game Engine.
 * - Tối ưu hóa hiệu năng: Toàn bộ Paint, Path, RectF được khởi tạo sẵn trong init(), KHÔNG cấp phát bộ nhớ trong onDraw().
 * - Xử lý sự kiện vuốt/kéo (onTouchEvent) với độ nhạy cao, tính toán khoảng cách Manhattan = 1 để di chuyển từng ô một.
 * - Hỗ trợ tính năng "Undo" tự động khi người chơi vuốt ngược lại ô liền trước.
 * - Hỗ trợ tính năng "Hướng dẫn (Hint)" hiển thị đường đi mẫu phủ kín bàn cờ với đường nét đứt neon và số thứ tự bước đi.
 * - Căn giữa lưới (Grid) hoàn hảo trên mọi kích thước màn hình.
 */
public class OneLineGameView extends View {

    // ==========================================
    // 1. CẤU TRÚC DỮ LIỆU & TRẠNG THÁI GAME
    // ==========================================

    /**
     * Lớp biểu diễn tọa độ một ô trên lưới: cột (col - trục X) và hàng (row - trục Y).
     */
    public static class Point {
        public int col;
        public int row;

        public Point(int col, int row) {
            this.col = col;
            this.row = row;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return col == point.col && row == point.row;
        }

        @Override
        public int hashCode() {
            return Objects.hash(col, row);
        }

        @Override
        public String toString() {
            return "(" + col + ", " + row + ")";
        }
    }

    public static final int CELL_EMPTY = 0;
    public static final int CELL_VISITED = 1;
    public static final int CELL_OBSTACLE = 2;

    // Kích thước mặc định của lưới (ví dụ: 5x5)
    private int numRows = 5;
    private int numCols = 5;

    // Ma trận trạng thái lưới hiện tại
    private int[][] grid;

    // Ma trận lưu cấu hình gốc của màn chơi (dùng để reset ván đấu)
    private int[][] initialGrid;

    // Danh sách lưu trữ tọa độ các điểm mà người chơi đã đi qua (tạo thành 1 nét liền mạch)
    private final List<Point> playerPath = new ArrayList<>();
    // Lưu trạng thái màu Đậm (0) / Nhạt (1) cho từng bước đi (xen kẽ mỗi lần di chuyển & dừng lại)
    private final List<Integer> stepColorParities = new ArrayList<>();
    private int currentMoveParity = 0;
    private boolean hasMovedInCurrentGesture = false;
    private int lastTouchCol = -1;
    private int lastTouchRow = -1;
    private static final int MAX_MULTI_TURN_CORNERS = 6;

    // Danh sách đường đi giải mẫu (Hint / Solution Path)
    private final List<Point> solutionPath = new ArrayList<>();
    private boolean isHintVisible = false;

    // Vị trí xuất phát ban đầu và vị trí nhân vật hiện tại
    private Point startPoint = new Point(0, 0);
    private Point currentPos = new Point(0, 0);
    private String currentSkinId = "classic_blue";

    // Cờ trạng thái: người chơi có đang nhấn giữ và kéo ngón tay hay không
    private boolean isDragging = false;

    // Tổng số ô cần phải đi qua để hoàn thành màn chơi (tổng số ô không phải vật cản)
    private int totalTargetCells = 0;

    // Trạng thái đã thắng màn chơi
    private boolean isGameWon = false;
    private long lastAutoStepTime = 0L;
    private static final long AUTO_STEP_INTERVAL_MS = 32L;

    // ==========================================
    // 2. TÍNH TOÁN KÍCH THƯỚC & TỌA ĐỘ VẼ
    // ==========================================
    private float cellSize = 0f;          // Kích thước cạnh của mỗi ô (Pixel)
    private float cellSpacing = 6f;       // Khoảng cách giữa các ô (Pixel)
    private float cellCornerRadius = 12f; // Bo góc cho từng ô
    private float offsetX = 0f;           // Khoảng cách lề trái để căn giữa lưới vào tâm View
    private float offsetY = 0f;           // Khoảng cách lề trên để căn giữa lưới vào tâm View

    // ==========================================
    // 3. ĐỐI TƯỢNG VẼ (PAINT & GRAPHICS)
    // ==========================================
    private Paint gridBackgroundPaint;
    private Paint ambientLightPaint;
    private Paint cellEmptyPaint;
    private Paint cellEmptyBorderPaint;
    private Paint cellObstaclePaint;
    private Paint cellObstacleBorderPaint;
    private Paint cellObstaclePatternPaint;
    private Paint cellVisitedPaint;
    private Paint cellVisitedBorderPaint;
    private Paint cellSpecularPaint;
    private Paint pathGlowPaint;
    private Paint pathLinePaint;
    private Paint pathLineLightPaint;
    private Paint pathCorePaint;
    private Paint hintLinePaint;
    private Paint hintStepPaint;
    private Paint playerGlowPaint;
    private Paint playerBodyPaint;
    private Paint playerCenterPaint;
    private Paint startMarkerPaint;
    private Paint textPaint;

    // Tái sử dụng đối tượng Path và RectF trong onDraw để tránh rác bộ nhớ (GC allocation)
    private final Path drawPath = new Path();
    private final Path lightDrawPath = new Path();
    private final Path hintDrawPath = new Path();
    private final RectF tempCellRect = new RectF();

    // Rung phản hồi cảm ứng (Haptic feedback)
    private Vibrator vibrator;

    // Interface lắng nghe sự kiện của game
    public interface OnGameStateListener {
        void onPathChanged(List<Point> currentPath, int remainingCells);
        void onGameWin(int totalSteps);
        void onUndo(Point removedPoint);
    }

    private OnGameStateListener gameStateListener;

    // ==========================================
    // 4. CONSTRUCTORS
    // ==========================================
    public OneLineGameView(Context context) {
        super(context);
        init();
    }

    public OneLineGameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OneLineGameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // ==========================================
    // 5. KHỞI TẠO CẤU HÌNH & PAINT
    // ==========================================
    private void init() {
        try {
            vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Exception ignored) {}

        // --- Cấu hình các Paint cho đồ họa đẹp mắt, chuẩn Material Design 3 ---

        // 1. Nền bảng lưới (Studio Obsidian Navy độ tương phản cao)
        gridBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridBackgroundPaint.setColor(Color.parseColor("#0B1120"));
        gridBackgroundPaint.setStyle(Paint.Style.FILL);

        ambientLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ambientLightPaint.setColor(Color.parseColor("#2638BDF8"));
        ambientLightPaint.setStyle(Paint.Style.FILL);

        // 2. Ô trống có thể đi (Màu Slate-Blue rõ nét + viền sáng tương phản cao trên nền tối)
        cellEmptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellEmptyPaint.setColor(Color.parseColor("#1E293B"));
        cellEmptyPaint.setStyle(Paint.Style.FILL);

        cellEmptyBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellEmptyBorderPaint.setColor(Color.parseColor("#64748B"));
        cellEmptyBorderPaint.setStyle(Paint.Style.STROKE);
        cellEmptyBorderPaint.setStrokeWidth(2.2f);

        // 3. Ô vật cản / tường (Màu Đỏ Sẫm Studio tách biệt hoàn toàn với ô trống)
        cellObstaclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellObstaclePaint.setColor(Color.parseColor("#3B121E"));
        cellObstaclePaint.setStyle(Paint.Style.FILL);

        cellObstacleBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellObstacleBorderPaint.setColor(Color.parseColor("#F43F5E"));
        cellObstacleBorderPaint.setStyle(Paint.Style.STROKE);
        cellObstacleBorderPaint.setStrokeWidth(2.2f);

        cellObstaclePatternPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellObstaclePatternPaint.setColor(Color.parseColor("#CCFB7185"));
        cellObstaclePatternPaint.setStyle(Paint.Style.STROKE);
        cellObstaclePatternPaint.setStrokeCap(Paint.Cap.ROUND);
        cellObstaclePatternPaint.setStrokeWidth(2.4f);

        // 4. Ô đã đi qua (Màu phát sáng nổi bật, tương phản rõ rệt với ô trống chưa đi)
        cellVisitedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellVisitedPaint.setColor(Color.parseColor("#3338BDF8"));
        cellVisitedPaint.setStyle(Paint.Style.FILL);

        cellVisitedBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellVisitedBorderPaint.setColor(Color.parseColor("#38BDF8"));
        cellVisitedBorderPaint.setStyle(Paint.Style.STROKE);
        cellVisitedBorderPaint.setStrokeWidth(2.6f);

        cellSpecularPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellSpecularPaint.setColor(Color.parseColor("#33FFFFFF"));
        cellSpecularPaint.setStyle(Paint.Style.STROKE);
        cellSpecularPaint.setStrokeCap(Paint.Cap.ROUND);
        cellSpecularPaint.setStrokeWidth(2.0f);

        // 5. Đường nét liền nối tâm các ô 3 lớp ánh sáng (3-Layer Studio Stroke)
        pathGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathGlowPaint.setColor(Color.parseColor("#592DD4BF"));
        pathGlowPaint.setStyle(Paint.Style.STROKE);
        pathGlowPaint.setStrokeCap(Paint.Cap.ROUND);
        pathGlowPaint.setStrokeJoin(Paint.Join.ROUND);

        pathLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathLinePaint.setColor(Color.parseColor("#2DD4BF")); // Teal neon đậm
        pathLinePaint.setStyle(Paint.Style.STROKE);
        pathLinePaint.setStrokeCap(Paint.Cap.ROUND);
        pathLinePaint.setStrokeJoin(Paint.Join.ROUND);

        pathLineLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathLineLightPaint.setColor(Color.parseColor("#99F6E4")); // Teal nhạt tương phản
        pathLineLightPaint.setStyle(Paint.Style.STROKE);
        pathLineLightPaint.setStrokeCap(Paint.Cap.ROUND);
        pathLineLightPaint.setStrokeJoin(Paint.Join.ROUND);

        pathCorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathCorePaint.setColor(Color.parseColor("#E6FFFFFF"));
        pathCorePaint.setStyle(Paint.Style.STROKE);
        pathCorePaint.setStrokeCap(Paint.Cap.ROUND);
        pathCorePaint.setStrokeJoin(Paint.Join.ROUND);

        // 6. Đường Hướng Dẫn / Gợi Ý (Hint Line - Vàng Cam phát sáng nét đứt)
        hintLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hintLinePaint.setColor(Color.parseColor("#F59E0B")); // Amber 500
        hintLinePaint.setStyle(Paint.Style.STROKE);
        hintLinePaint.setStrokeCap(Paint.Cap.ROUND);
        hintLinePaint.setStrokeJoin(Paint.Join.ROUND);
        hintLinePaint.setPathEffect(new DashPathEffect(new float[]{14f, 10f}, 0));

        hintStepPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hintStepPaint.setColor(Color.parseColor("#FDE047")); // Bright yellow
        hintStepPaint.setTextAlign(Paint.Align.CENTER);
        hintStepPaint.setFakeBoldText(true);

        // 7. Quầng sáng phát quang của nhân vật (Player Glow)
        playerGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        playerGlowPaint.setColor(Color.parseColor("#4DFBBF24")); // Vàng hổ phách bán trong suốt
        playerGlowPaint.setStyle(Paint.Style.FILL);

        // 8. Thân nhân vật chính (Hình tròn vàng rực rỡ)
        playerBodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        playerBodyPaint.setColor(Color.parseColor("#F59E0B")); // Amber 500
        playerBodyPaint.setStyle(Paint.Style.FILL);

        // 9. Điểm sáng tâm nhân vật (Highlight 3D)
        playerCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        playerCenterPaint.setColor(Color.WHITE);
        playerCenterPaint.setStyle(Paint.Style.FILL);

        // 10. Điểm đánh dấu ô xuất phát
        startMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        startMarkerPaint.setColor(Color.parseColor("#38BDF8")); // Sky blue
        startMarkerPaint.setStyle(Paint.Style.STROKE);
        startMarkerPaint.setStrokeWidth(3f);

        // 11. Chữ hiển thị
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(24f);

        setupDefaultLevel();
    }

    private void setupDefaultLevel() {
        int[][] sampleGrid = new int[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        };
        loadLevel(3, 3, sampleGrid, new Point(0, 0));
    }

    public void setPlayerSkinId(String skinId) {
        if (skinId != null && !skinId.isEmpty()) {
            this.currentSkinId = skinId;
            invalidate();
        }
    }

    public void updateStudioColors(int primaryColor, int brightGlowColor, int surfaceColor) {
        int playerColor = com.example.data.shop.GameItemAssets.INSTANCE.getSkinPrimaryColorArgb(currentSkinId);
        // Luôn sử dụng bảng màu Studio tương phản cao để các ô trống, ô đã đi và ô vật cản tách biệt rõ ràng 100%
        int r = Math.min(45, Math.max(24, Color.red(surfaceColor) + 12));
        int g = Math.min(62, Math.max(36, Color.green(surfaceColor) + 16));
        int b = Math.min(88, Math.max(56, Color.blue(surfaceColor) + 22));
        cellEmptyPaint.setColor(Color.rgb(r, g, b));
        cellEmptyBorderPaint.setColor(Color.parseColor("#64748B"));

        cellVisitedPaint.setColor(Color.argb(72, Color.red(playerColor), Color.green(playerColor), Color.blue(playerColor)));
        cellVisitedBorderPaint.setColor(playerColor);

        cellObstaclePaint.setColor(Color.parseColor("#3B121E"));
        cellObstacleBorderPaint.setColor(Color.parseColor("#F43F5E"));
        cellObstaclePatternPaint.setColor(Color.parseColor("#CCFB7185"));

        pathGlowPaint.setColor(Color.argb(110, Color.red(playerColor), Color.green(playerColor), Color.blue(playerColor)));
        pathLinePaint.setColor(playerColor);
        ambientLightPaint.setColor(Color.argb(45, Color.red(playerColor), Color.green(playerColor), Color.blue(playerColor)));
        startMarkerPaint.setColor(Color.parseColor("#FBBF24"));
        invalidate();
    }

    public void loadLevel(int rows, int cols, int[][] initialLayout, Point start) {
        loadLevel(rows, cols, initialLayout, start, null);
    }

    public void loadLevel(int rows, int cols, int[][] initialLayout, Point start, @Nullable List<Point> solution) {
        this.numRows = rows;
        this.numCols = cols;
        this.startPoint = new Point(start.col, start.row);
        this.currentPos = new Point(start.col, start.row);
        this.isGameWon = false;
        this.isDragging = false;
        this.isHintVisible = false;

        this.solutionPath.clear();
        if (solution != null) {
            this.solutionPath.addAll(solution);
        }

        this.initialGrid = new int[rows][cols];
        this.grid = new int[rows][cols];
        this.totalTargetCells = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int val = (initialLayout != null && r < initialLayout.length && c < initialLayout[r].length)
                        ? initialLayout[r][c] : CELL_EMPTY;
                this.initialGrid[r][c] = val;
                this.grid[r][c] = val;
                if (val != CELL_OBSTACLE) {
                    totalTargetCells++;
                }
            }
        }

        playerPath.clear();
        playerPath.add(new Point(start.col, start.row));
        stepColorParities.clear();
        stepColorParities.add(0);
        currentMoveParity = 0;
        hasMovedInCurrentGesture = false;
        this.grid[start.row][start.col] = CELL_VISITED;

        notifyPathChanged();
        invalidate();
    }

    public void resetLevel() {
        if (initialGrid == null) return;
        loadLevel(numRows, numCols, initialGrid, startPoint, solutionPath);
    }

    public boolean undoStep() {
        if (isGameWon || playerPath.size() <= 1) {
            return false;
        }

        Point removed = playerPath.remove(playerPath.size() - 1);
        if (stepColorParities.size() > 1) {
            stepColorParities.remove(stepColorParities.size() - 1);
        }
        grid[removed.row][removed.col] = CELL_EMPTY;

        currentPos = playerPath.get(playerPath.size() - 1);

        performHaptic(false);
        if (gameStateListener != null) {
            gameStateListener.onUndo(removed);
        }
        notifyPathChanged();
        invalidate();
        return true;
    }

    /**
     * Bật / Tắt chế độ Hướng dẫn (Hint)
     */
    public void toggleHint(boolean visible) {
        this.isHintVisible = visible;
        invalidate();
    }

    public boolean isHintVisible() {
        return isHintVisible;
    }

    public void setSolutionPath(List<Point> solution) {
        this.solutionPath.clear();
        if (solution != null) {
            this.solutionPath.addAll(solution);
        }
        invalidate();
    }

    public void setOnGameStateListener(OnGameStateListener listener) {
        this.gameStateListener = listener;
    }

    public List<Point> getPlayerPath() {
        return new ArrayList<>(playerPath);
    }

    public int[][] getGrid() {
        return grid;
    }

    public boolean isGameWon() {
        return isGameWon;
    }

    // ==========================================
    // 6. XỬ LÝ SỰ KIỆN VUỐT / CHẠM (ONTOUCHEVENT)
    // ==========================================
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isGameWon) {
            return super.onTouchEvent(event);
        }

        float touchX = event.getX();
        float touchY = event.getY();

        int targetCol = (int) Math.floor((touchX - offsetX) / cellSize);
        int targetRow = (int) Math.floor((touchY - offsetY) / cellSize);
        long now = System.currentTimeMillis();

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                hasMovedInCurrentGesture = false;
                lastTouchCol = targetCol;
                lastTouchRow = targetRow;
                lastAutoStepTime = now;

                if (isInsideGrid(targetCol, targetRow)) {
                    isDragging = true;
                    // Nếu người chơi nhấn vào 1 ô khác vị trí hiện tại (Chế độ đi từng bước / Nhấn ô đích):
                    // Nhận diện ý muốn người chơi: tự động cua qua 4, 5 ngã rẽ / khúc cua để tới ô đó hoặc lùi lại nếu nhấn vào ô đã đi!
                    if (targetCol != currentPos.col || targetRow != currentPos.row) {
                        if (trySmartMoveOrRewindTo(targetCol, targetRow)) {
                            hasMovedInCurrentGesture = true;
                            lastAutoStepTime = now;
                        }
                    }
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (!isDragging) {
                    if (isInsideGrid(targetCol, targetRow)) {
                        isDragging = true;
                    } else {
                        return true;
                    }
                }

                if (!isInsideGrid(targetCol, targetRow)) {
                    return true;
                }

                if (targetCol == currentPos.col && targetRow == currentPos.row) {
                    return true;
                }

                // Nếu người chơi dừng ngón tay tại 1 chỗ rồi mới di chuyển tiếp sang ô mới -> đổi màu Đậm/Nhạt 1 lần
                if (hasMovedInCurrentGesture && (targetCol != lastTouchCol || targetRow != lastTouchRow)
                        && (now - lastAutoStepTime > 180L)) {
                    currentMoveParity = 1 - currentMoveParity;
                }
                lastTouchCol = targetCol;
                lastTouchRow = targetRow;

                if (now - lastAutoStepTime < AUTO_STEP_INTERVAL_MS) {
                    return true;
                }

                // 1. XỬ LÝ TỰ ĐỘNG UNDO KHI VUỐT NGƯỢC VỀ Ô ĐÃ ĐI
                int visitedIdx = playerPath.indexOf(new Point(targetCol, targetRow));
                if (visitedIdx >= 0 && visitedIdx == playerPath.size() - 2) {
                    lastAutoStepTime = now;
                    undoStep();
                    return true;
                }

                // 2. DI CHUYỂN THÔNG MINH QUA NHIỀU NGÃ RẼ / KHÚC CUA (HỖ TRỢ CUA 4-5 KHÚC CUA LIÊN TIẾP)
                if (trySmartMoveOrRewindTo(targetCol, targetRow)) {
                    hasMovedInCurrentGesture = true;
                    lastAutoStepTime = now;
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (hasMovedInCurrentGesture) {
                    // Cứ dừng lại là đổi màu Đậm <-> Nhạt cho lần di chuyển tiếp theo
                    currentMoveParity = 1 - currentMoveParity;
                    hasMovedInCurrentGesture = false;
                }
                isDragging = false;
                return true;
        }

        return super.onTouchEvent(event);
    }

    /**
     * Nhận diện ý muốn người chơi khi nhấn hoặc kéo tới ô (targetCol, targetRow):
     * - Nếu ô đó nằm trên đường vừa đi qua (gần đầu mút), lùi lại tới đúng ô đó.
     * - Nếu ô đó là ô trống, tìm đường đi hợp lệ có thể cua liên tiếp 4, 5, 6 ngã rẽ/khúc cua để tự động đi tới đó.
     */
    private boolean trySmartMoveOrRewindTo(int targetCol, int targetRow) {
        if (!isInsideGrid(targetCol, targetRow)) return false;
        if (targetCol == currentPos.col && targetRow == currentPos.row) return false;

        // Trường hợp 1: Nhấn vào ô đã đi qua -> Lùi (Undo) về đúng ô đó
        int existingIdx = playerPath.indexOf(new Point(targetCol, targetRow));
        if (existingIdx >= 0 && existingIdx < playerPath.size() - 1) {
            boolean undid = false;
            while (playerPath.size() - 1 > existingIdx) {
                Point removed = playerPath.remove(playerPath.size() - 1);
                if (stepColorParities.size() > 1) {
                    stepColorParities.remove(stepColorParities.size() - 1);
                }
                grid[removed.row][removed.col] = CELL_EMPTY;
                currentPos = playerPath.get(playerPath.size() - 1);
                if (gameStateListener != null) {
                    gameStateListener.onUndo(removed);
                }
                undid = true;
            }
            if (undid) {
                // Sau khi lùi về một điểm, cập nhật màu tiếp theo ngược với màu đoạn trước đó
                if (stepColorParities.size() > 1) {
                    currentMoveParity = 1 - stepColorParities.get(stepColorParities.size() - 1);
                } else {
                    currentMoveParity = 0;
                }
                hasMovedInCurrentGesture = false;
                performHaptic(false);
                notifyPathChanged();
                invalidate();
            }
            return undid;
        }

        // Trường hợp 2: Ô đích là ô trống -> Tìm đường đi qua tối đa 5-6 khúc cua (ngã rẽ)
        if (grid[targetRow][targetCol] != CELL_EMPTY) return false;

        List<Point> multiTurnPath = findMultiTurnPathToTarget(targetCol, targetRow, MAX_MULTI_TURN_CORNERS);
        if (multiTurnPath != null && !multiTurnPath.isEmpty()) {
            boolean moved = false;
            for (Point step : multiTurnPath) {
                int dc = step.col - currentPos.col;
                int dr = step.row - currentPos.row;
                if (tryMoveOneLine(dc, dr)) {
                    moved = true;
                    if (isGameWon) break;
                } else {
                    break;
                }
            }
            if (moved) {
                performHaptic(false);
                notifyPathChanged();
                checkWinCondition();
                invalidate();
            }
            return moved;
        }
        return false;
    }

    /**
     * Tìm đường đi ngắn nhất & ít khúc cua nhất từ currentPos đến (targetCol, targetRow)
     * với khả năng cua liên tiếp 4, 5, 6 ngã rẽ (khúc cua) để bắt đúng ý muốn của người chơi.
     */
    private List<Point> findMultiTurnPathToTarget(int targetCol, int targetRow, int maxTurns) {
        final int[] dCols = {1, -1, 0, 0};
        final int[] dRows = {0, 0, 1, -1};

        class SearchNode {
            final int col, row, dir, turns;
            final SearchNode parent;
            SearchNode(int col, int row, int dir, int turns, SearchNode parent) {
                this.col = col;
                this.row = row;
                this.dir = dir;
                this.turns = turns;
                this.parent = parent;
            }
        }

        int[][][] bestTurns = new int[numRows][numCols][5];
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                for (int d = 0; d < 5; d++) {
                    bestTurns[r][c][d] = Integer.MAX_VALUE;
                }
            }
        }

        List<SearchNode> queue = new ArrayList<>();
        queue.add(new SearchNode(currentPos.col, currentPos.row, 4, 0, null));
        bestTurns[currentPos.row][currentPos.col][4] = 0;

        int head = 0;
        SearchNode bestGoalNode = null;

        while (head < queue.size()) {
            SearchNode curr = queue.get(head++);
            if (curr.col == targetCol && curr.row == targetRow) {
                if (bestGoalNode == null || curr.turns < bestGoalNode.turns) {
                    bestGoalNode = curr;
                    if (curr.turns <= 1) break;
                }
                continue;
            }

            for (int d = 0; d < 4; d++) {
                int nc = curr.col + dCols[d];
                int nr = curr.row + dRows[d];
                if (!isInsideGrid(nc, nr) || grid[nr][nc] != CELL_EMPTY) continue;

                int nextTurns = (curr.dir == 4 || curr.dir == d) ? curr.turns : (curr.turns + 1);
                if (nextTurns > maxTurns) continue;

                // Tránh đi vòng lặp tự cắt chính đường đang tìm trong queue
                boolean alreadyInBranch = false;
                SearchNode scan = curr;
                while (scan != null) {
                    if (scan.col == nc && scan.row == nr) {
                        alreadyInBranch = true;
                        break;
                    }
                    scan = scan.parent;
                }
                if (alreadyInBranch) continue;

                if (nextTurns < bestTurns[nr][nc][d]) {
                    bestTurns[nr][nc][d] = nextTurns;
                    queue.add(new SearchNode(nc, nr, d, nextTurns, curr));
                }
            }
        }

        if (bestGoalNode == null) return null;

        List<Point> result = new ArrayList<>();
        SearchNode node = bestGoalNode;
        while (node != null && node.parent != null) {
            result.add(0, new Point(node.col, node.row));
            node = node.parent;
        }
        return result;
    }

    private boolean tryMoveOneLine(int deltaCol, int deltaRow) {
        if (deltaCol == 0 && deltaRow == 0) return false;
        int nextCol = currentPos.col + deltaCol;
        int nextRow = currentPos.row + deltaRow;

        if (!isInsideGrid(nextCol, nextRow)) return false;

        if (grid[nextRow][nextCol] == CELL_EMPTY) {
            Point newCell = new Point(nextCol, nextRow);
            playerPath.add(newCell);
            stepColorParities.add(currentMoveParity);
            grid[nextRow][nextCol] = CELL_VISITED;
            currentPos = newCell;
            return true;
        }
        return false;
    }

    private boolean isInsideGrid(int col, int row) {
        return col >= 0 && col < numCols && row >= 0 && row < numRows;
    }

    private void checkWinCondition() {
        if (playerPath.size() == totalTargetCells) {
            isGameWon = true;
            isDragging = false;
            // Đã tắt rung chiến thắng theo yêu cầu
            if (gameStateListener != null) {
                gameStateListener.onGameWin(playerPath.size());
            }
        }
    }

    private void notifyPathChanged() {
        if (gameStateListener != null) {
            int remaining = Math.max(0, totalTargetCells - playerPath.size());
            gameStateListener.onPathChanged(getPlayerPath(), remaining);
        }
    }

    private void performHaptic(boolean isSuccess) {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                long duration = isSuccess ? 80 : 15;
                int amplitude = isSuccess ? VibrationEffect.DEFAULT_AMPLITUDE : 50;
                vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude));
            } else {
                vibrator.vibrate(isSuccess ? 80 : 15);
            }
        } catch (Exception ignored) {}
    }

    // ==========================================
    // 7. HIỂN THỊ ĐỒ HỌA TRÊN CANVAS (ONDRAW)
    // ==========================================
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (grid == null || numRows <= 0 || numCols <= 0) {
            return;
        }

        int viewWidth = getWidth();
        int viewHeight = getHeight();

        if (viewWidth <= 0 || viewHeight <= 0) {
            return;
        }

        /*
         * BƯỚC 1: TÍNH TOÁN KÍCH THƯỚC VÀ CĂN GIỮA LƯỚI
         */
        float availableWidth = viewWidth - getPaddingLeft() - getPaddingRight();
        float availableHeight = viewHeight - getPaddingTop() - getPaddingBottom();

        cellSize = Math.min(availableWidth / numCols, availableHeight / numRows);
        cellSpacing = (cellSize < 8f) ? 0f : Math.max(1f, cellSize * 0.08f);
        cellCornerRadius = (cellSize < 8f) ? 0f : Math.max(2f, cellSize * 0.18f);

        float totalGridWidth = cellSize * numCols;
        float totalGridHeight = cellSize * numRows;

        offsetX = getPaddingLeft() + (availableWidth - totalGridWidth) / 2f;
        offsetY = getPaddingTop() + (availableHeight - totalGridHeight) / 2f;

        float lineStroke = Math.max(3.0f, cellSize * 0.30f);
        pathLinePaint.setStrokeWidth(lineStroke);
        pathLineLightPaint.setStrokeWidth(lineStroke);
        pathCorePaint.setStrokeWidth(Math.max(1.2f, cellSize * 0.095f));
        hintLinePaint.setStrokeWidth(Math.max(2f, cellSize * 0.16f));
        hintStepPaint.setTextSize(Math.max(8f, cellSize * 0.28f));

        int playerColor = com.example.data.shop.GameItemAssets.INSTANCE.getSkinPrimaryColorArgb(currentSkinId);
        // Đường line đi theo người chơi: màu neon sáng rõ + viền phát quang tương phản cao
        pathLinePaint.setColor(playerColor);
        pathGlowPaint.setColor(Color.argb(95, Color.red(playerColor), Color.green(playerColor), Color.blue(playerColor)));
        pathGlowPaint.setStrokeWidth(lineStroke * 1.55f);
        cellVisitedPaint.setColor(Color.argb(72, Color.red(playerColor), Color.green(playerColor), Color.blue(playerColor)));
        cellVisitedBorderPaint.setColor(playerColor);
        startMarkerPaint.setColor(Color.parseColor("#FBBF24"));
        startMarkerPaint.setStrokeWidth(Math.max(2.6f, cellSize * 0.07f));

        /*
         * BƯỚC 2: VẼ NỀN TẤT CẢ CÁC Ô TRONG LƯỚI VỚI ĐỘ TƯƠNG PHẢN CAO (Ô TRỐNG vs Ô ĐÃ ĐI vs VẬT CẢN)
         */
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                float left = offsetX + c * cellSize + cellSpacing / 2f;
                float top = offsetY + r * cellSize + cellSpacing / 2f;
                float right = left + cellSize - cellSpacing;
                float bottom = top + cellSize - cellSpacing;

                tempCellRect.set(left, top, right, bottom);

                int cellState = grid[r][c];

                if (cellState == CELL_OBSTACLE) {
                    canvas.drawRoundRect(tempCellRect, cellCornerRadius, cellCornerRadius, cellObstaclePaint);
                    canvas.drawRoundRect(tempCellRect, cellCornerRadius, cellCornerRadius, cellObstacleBorderPaint);
                    float inset = Math.max(4f, cellSize * 0.26f);
                    if (right - left > inset * 2f) {
                        canvas.drawLine(left + inset, top + inset, right - inset, bottom - inset, cellObstaclePatternPaint);
                        canvas.drawLine(right - inset, top + inset, left + inset, bottom - inset, cellObstaclePatternPaint);
                    }
                } else if (cellState == CELL_VISITED) {
                    canvas.drawRoundRect(tempCellRect, cellCornerRadius, cellCornerRadius, cellEmptyPaint);
                    canvas.drawRoundRect(tempCellRect, cellCornerRadius, cellCornerRadius, cellVisitedPaint);
                    canvas.drawRoundRect(tempCellRect, cellCornerRadius, cellCornerRadius, cellVisitedBorderPaint);
                } else {
                    canvas.drawRoundRect(tempCellRect, cellCornerRadius, cellCornerRadius, cellEmptyPaint);
                    canvas.drawRoundRect(tempCellRect, cellCornerRadius, cellCornerRadius, cellEmptyBorderPaint);
                }

                if (r == startPoint.row && c == startPoint.col) {
                    canvas.drawRoundRect(tempCellRect, cellCornerRadius, cellCornerRadius, startMarkerPaint);
                }
            }
        }

        /*
         * BƯỚC 2.5: VẼ HƯỚNG DẪN (HINT PATH) NẾU ĐƯỢC BẬT
         */
        if (isHintVisible && solutionPath.size() > 1) {
            hintDrawPath.reset();
            Point first = solutionPath.get(0);
            float startCenterX = offsetX + first.col * cellSize + cellSize / 2f;
            float startCenterY = offsetY + first.row * cellSize + cellSize / 2f;
            hintDrawPath.moveTo(startCenterX, startCenterY);

            for (int i = 1; i < solutionPath.size(); i++) {
                Point p = solutionPath.get(i);
                float cx = offsetX + p.col * cellSize + cellSize / 2f;
                float cy = offsetY + p.row * cellSize + cellSize / 2f;
                hintDrawPath.lineTo(cx, cy);
            }
            canvas.drawPath(hintDrawPath, hintLinePaint);

            // Vẽ số thứ tự bước đi nhỏ trên mỗi ô nếu kích thước ô đủ lớn
            if (cellSize >= 20f) {
                for (int i = 0; i < solutionPath.size(); i++) {
                    Point p = solutionPath.get(i);
                    float cx = offsetX + p.col * cellSize + cellSize / 2f;
                    float cy = offsetY + p.row * cellSize + cellSize / 2f + (cellSize * 0.10f);
                    canvas.drawText(String.valueOf(i + 1), cx, cy, hintStepPaint);
                }
            }
        }

        /*
         * BƯỚC 3: VẼ ĐƯỜNG NỐI TÂM TƯƠNG PHẢN CAO ĐI THEO NGƯỜI CHƠI (ONE-LINE PATH)
         */
        if (playerPath.size() > 1) {
            drawPath.reset();
            for (int i = 1; i < playerPath.size(); i++) {
                Point prev = playerPath.get(i - 1);
                Point curr = playerPath.get(i);
                float x1 = offsetX + prev.col * cellSize + cellSize / 2f;
                float y1 = offsetY + prev.row * cellSize + cellSize / 2f;
                float x2 = offsetX + curr.col * cellSize + cellSize / 2f;
                float y2 = offsetY + curr.row * cellSize + cellSize / 2f;

                drawPath.moveTo(x1, y1);
                drawPath.lineTo(x2, y2);
            }
            canvas.drawPath(drawPath, pathGlowPaint);
            canvas.drawPath(drawPath, pathLinePaint);
            canvas.drawPath(drawPath, pathCorePaint);
        }

        /*
         * BƯỚC 4: VẼ NHÂN VẬT TẠI VỊ TRÍ HIỆN TẠI (ĐÃ XÓA BỚT VẬT THỂ XUNG QUANH NHÂN VẬT)
         */
        if (currentPos != null && isInsideGrid(currentPos.col, currentPos.row)) {
            float playerCenterX = offsetX + currentPos.col * cellSize + cellSize / 2f;
            float playerCenterY = offsetY + currentPos.row * cellSize + cellSize / 2f;

            com.example.data.shop.GameItemAssets.INSTANCE.drawPlayerOnCanvas(
                    canvas,
                    playerCenterX,
                    playerCenterY,
                    cellSize,
                    currentSkinId
            );
        }
    }
}
