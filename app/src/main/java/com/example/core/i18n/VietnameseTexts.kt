package com.example.core.i18n

/**
 * =========================================================================================
 * 🇻🇳 FILE QUẢN LÝ TOÀN BỘ CÂU CHỮ VÀ TÊN HIỂN THỊ TIẾNG VIỆT TRONG GAME
 * =========================================================================================
 * 
 * BẠN CÓ THỂ TÙY Ý THAY ĐỔI BẤT KỲ DÒNG CHỮ NÀO BẠN THÍCH TẠI ĐÂY!
 * Tất cả các từ ngữ, câu thông báo, nhãn nút bấm, tên vật phẩm và hướng dẫn của bản
 * Tiếng Việt đều được gom tập trung ở file này để bạn dễ dàng tìm kiếm và chỉnh sửa.
 * =========================================================================================
 */
object VietnameseTexts {

    // =====================================================================================
    // 1. TÊN GAME, SLOGAN & THÔNG TIN CHUNG
    // =====================================================================================
    var APP_TITLE = "MazeX"
    var APP_SUBTITLE = "Mê Cung Từng Bước Chính Xác"
    var APP_VERSION = "Phiên bản 2.5 Pro"

    // =====================================================================================
    // 2. CÁC NÚT ĐIỀU HƯỚNG & HÀNH ĐỘNG DÙNG CHUNG
    // =====================================================================================
    var BTN_BACK = "Quay lại"
    var BTN_CLOSE = "Đóng"
    var BTN_CANCEL = "Hủy"
    var BTN_CONFIRM = "Xác nhận"
    var BTN_UNDERSTOOD = "Đã Hiểu"
    var BTN_PLAY = "Chơi"
    var BTN_PLAY_NOW = "Chơi Ngay"
    var BTN_PLAY_AGAIN = "Chơi Lại Màn Này"
    var BTN_RESUME = "Chơi Tiếp"
    var BTN_RETRY = "Thử Lại"
    var BTN_PAUSE = "Tạm dừng"
    var BTN_CONTINUE = "Tiếp tục"
    var BTN_NEXT_LEVEL = "Màn Tiếp Theo"
    var BTN_SKIP = "Bỏ qua"
    var BTN_UNDO = "Lùi lại"
    var BTN_RESET = "Vẽ lại"
    var BTN_HINT = "Gợi ý"
    var BTN_HIDE_HINT = "Ẩn mẫu"
    var BTN_FREE_HINT = "Gợi ý Free"
    var BTN_USED_HINT = "Đã dùng"

    // =====================================================================================
    // 3. THANH ĐIỀU HƯỚNG DƯỚI ĐÁY MÀN HÌNH (BOTTOM NAVIGATION)
    // =====================================================================================
    var NAV_HOME = "Mê Cung"
    var NAV_DAILY = "Thử Thách"
    var NAV_SHOP = "Cửa Hàng"
    var NAV_ONELINE = "Vẽ 1 Nét"
    var NAV_LEADERBOARD = "Xếp Hạng"
    var NAV_ACHIEVEMENTS = "Thành Tựu"

    // =====================================================================================
    // 4. TRANG CHỦ & BẢNG CHỌN LEVEL (LEVEL SELECT)
    // =====================================================================================
    var HOME_HEADER_TITLE = "CHỌN MÀN CHƠI"
    var HOME_HEADER_SUBTITLE = "Khám phá từ 5×5 đến 69×69 ô cờ siêu mượt"
    var LEVEL_SELECT_TITLE = "DANH SÁCH CẤP ĐỘ"
    var LEVEL_CURRENT_BADGE = "MÀN HIỆN TẠI"
    var LEVEL_CURRENT_PLAYING = "ĐANG CHƠI"
    var LEVEL_NEXT_TARGET = "MỤC TIÊU TIẾP THEO"
    var LEVEL_LOCKED = "Đã khóa"
    var LEVEL_COMPLETED = "Đã hoàn thành"
    var LEVEL_STARS_REQ = "Cần %d★ để mở khóa"
    var LEVEL_SHOW_MORE = "Xem Thêm 20 Màn Nữa ↓"
    var LEVEL_FILTER_ALL = "Tất Cả"
    var LEVEL_FILTER_PREFIX = "Màn %d-%d"

    // Tiếp tục ván lưu
    var RESUME_CARD_TITLE = "TIẾP TỤC VÁN CHƠI DỞ"
    var RESUME_CARD_SUBTITLE = "Màn %s · Đã đi %d bước · %ds"

    // Thử thách ngày 3 Lượt (14x14, 15x15, 16x16)
    var DAILY_BANNER_TITLE = "Thử Thách Mê Cung Hôm Nay"
    var DAILY_BANNER_SUBTITLE = "3 Lượt Đấu: 14×14, 15×15, 16×16 · Reset 0h00 Hàng Ngày"
    var DAILY_BANNER_BTN = "Thi Đấu ⚡"
    var DAILY_STAGE_LABEL = "Lượt %d / 3"
    var DAILY_STAGE_1_TITLE = "Lượt 1: Mê Cung 14×14"
    var DAILY_STAGE_2_TITLE = "Lượt 2: Mê Cung 15×15"
    var DAILY_STAGE_3_TITLE = "Lượt 3: Mê Cung 16×16"
    var DAILY_STAGE_CLEARED = "Đã Vượt Qua (%ds)"
    var DAILY_STAGE_PLAYING = "Sẵn Sàng Chơi"
    var DAILY_STAGE_LOCKED = "Cần Qua Lượt Trước"
    var DAILY_COOLDOWN_PREFIX = "Làm mới lượt chơi sau:"
    var DAILY_COOLDOWN_TITLE = "⏳ Đã Hết 3 Lượt Hôm Nay"
    var DAILY_COOLDOWN_DESC = "Hệ thống sẽ tự động khôi phục 3 lượt chơi mới vào 00:00 (0h00) mỗi ngày!"
    var DAILY_START_STAGE_BTN = "Bắt Đầu Lượt %d (%d×%d)"
    var DAILY_CONTINUE_STAGE_BTN = "Tiếp Tục Lượt %d (%d×%d)"
    var DAILY_ALL_DONE_BTN = "Đã Hoàn Thành 3/3 Lượt Hôm Nay"
    var DAILY_REWARD_STAGE_1 = "+50 🪙 Xu Thưởng"
    var DAILY_REWARD_STAGE_2 = "+75 🪙 Xu Thưởng"
    var DAILY_REWARD_STAGE_3 = "+150 🪙 + 5 💎 + Danh Hiệu"

    // One-Line Continue / Replay / Level Map Options
    var ONELINE_CONTINUE_BTN = "Chơi Tiếp (Màn %d)"
    var ONELINE_REPLAY_BTN = "Chơi Lại Từ Màn 1"
    var ONELINE_SELECT_MAP_BTN = "Chọn Màn / Bản Đồ"
    var ONELINE_PROGRESS_STATUS = "Tiến độ: %d / %d màn đã vượt qua"
    var ONELINE_LEVEL_CLEARED_BADGE = "ĐÃ QUA"
    var ONELINE_CURRENT_TARGET_BADGE = "ĐANG CHƠI"

    // Tiến trình Mở khóa Impossible Mode
    var IMPOSSIBLE_PROGRESS_TITLE = "Mở Khóa Chế Độ Bất Khả Thi"
    var IMPOSSIBLE_PROGRESS_SUB = "%d / %d màn đã vượt qua"
    var IMPOSSIBLE_SECTION_TITLE = "CHẾ ĐỘ IMPOSSIBLE"
    var IMPOSSIBLE_LOCKED_DESC = "Vượt qua 50 màn mê cung đầu tiên để mở khóa chế độ kích thước khổng lồ từ 100×100 đến 1000×1000 (Tích hợp Bản Đồ Radar Gợi Ý Hướng Đích)."
    var IMPOSSIBLE_UNLOCKED_DESC = "Thử thách đỉnh cao từ 100×100 đến 1000×1000 ô — Đã trang bị Bản Đồ Radar Gợi Ý Hướng Đích trực tiếp!"
    var SUPER_TIER_TITLE = "SUPER MAZE 👑"

    // Thẻ Mini-game One-Line
    var ONELINE_CARD_TITLE = "MINI-GAME: VẼ MỘT NÉT (ONE-LINE)"
    var ONELINE_CARD_SUB_UNLOCKED = "Nối kín 100% ô không nhấc tay · Độ khó cực cao 3×3 → 15×15 & Hình Độc Lạ!"
    var ONELINE_CARD_SUB_LOCKED = "Mở khóa sau khi vượt qua Màn 20 Mê Cung chính"
    var ONELINE_LOCKED_DIALOG_TITLE = "Chưa Mở Khóa One-Line Puzzle"
    var ONELINE_LOCKED_DIALOG_BODY = "Bạn cần vượt qua ít nhất 20 màn Mê Cung chính để mở khóa Mini-Game vẽ một nét đỉnh cao!\n(Hiện tại bạn đã đạt: %d/20 màn)."

    // =====================================================================================
    // 5. MÀN HÌNH CHƠI GAME & ĐIỀU KHIỂN (GAME HUD & PLAYING)
    // =====================================================================================
    var HUD_STEPS = "Bước đi"
    var HUD_TIME = "Thời gian"
    var HUD_COORDINATES = "Tọa độ"
    var HUD_TARGET_STEPS = "Mục tiêu: %d bước"
    var HUD_CURRENT_LEVEL = "Màn %d"

    // Chế độ điều khiển
    var CONTROLS_LABEL = "Chế độ điều khiển:"
    var CONTROL_AUTO = "Tự Động"
    var CONTROL_AUTO_SUB = "Kéo 1 nét liên tục"
    var CONTROL_STEP = "Từng Bước"
    var CONTROL_STEP_SUB = "1 chạm = 1 ô"

    // Trợ giúp & Gợi ý
    var HINT_FREE_ON_MSG = "Đã kích hoạt Hướng Dẫn đường đi tối ưu (Free)!"
    var HINT_ONCE_PER_LEVEL_MSG = "Mỗi màn chơi chỉ được nhận 1 lượt Hướng Dẫn Free duy nhất!"

    // Hộp thoại thoát ván chơi
    var EXIT_DIALOG_TITLE = "Tạm dừng trò chơi"
    var EXIT_DIALOG_BODY = "Bạn muốn lưu tiến trình hiện tại để chơi tiếp sau hay thoát hẳn?"
    var EXIT_DIALOG_SAVE_BTN = "Lưu & Ra Trang Chủ"
    var EXIT_DIALOG_QUIT_BTN = "Thoát Không Lưu"

    // Màn hình Chiến Thắng
    var VICTORY_TITLE = "🎉 CHIẾN THẮNG XUẤT SẮC!"
    var VICTORY_SUBTITLE = "Bạn đã chinh phục thành công mê cung!"
    var VICTORY_MOVES_RESULT = "Tổng bước đi: %d (Mục tiêu: %d)"
    var VICTORY_TIME_RESULT = "Thời gian hoàn thành: %ds"
    var VICTORY_COINS_EARNED = "+%d Xu Thưởng"
    var VICTORY_SPEED_RECORD = "Tốc độ: %.2f giây/ô"
    var VICTORY_REPLAY_BTN = "Xem Lại Đường Đi (Replay)"
    var VICTORY_NEXT_BTN = "Sang Màn Tiếp Theo →"

    // =====================================================================================
    // 6. CỬA HÀNG VẬT PHẨM & NHÂN VẬT (SHOP & SKINS)
    // =====================================================================================
    var SHOP_TITLE = "Cửa Hàng"
    var SHOP_COIN_BALANCE = "%d Xu"
    var SHOP_TAB_SKINS = "Nhân Vật / Skins"
    var SHOP_TAB_THEMES = "Chủ Đề Mê Cung"
    var SHOP_TAB_VIP = "Nạp Xu & VIP"

    var SHOP_PREVIEW_TITLE = "PHÒNG THỬ TRANG BỊ"
    var SHOP_PREVIEW_SUB = "Nhân vật được trang bị sẽ xuất hiện trực tiếp trong trò chơi!"
    var SHOP_EQUIPPED_BADGE = "ĐANG SỬ DỤNG"
    var SHOP_OWNED_BADGE = "ĐÃ SỞ HỮU"
    var SHOP_EQUIP_BTN = "Trang Bị Ngay"
    var SHOP_BUY_BTN = "Mua (%d Xu)"
    var SHOP_VIP_UNLOCK_BTN = "Mở Khóa VIP"
    var SHOP_FREE_BADGE = "Miễn Phí"
    var SHOP_NOT_ENOUGH_COINS = "Bạn không đủ Xu để mua vật phẩm này! Hãy chơi thêm màn hoặc xem video nhận thưởng."
    var SHOP_BUY_SUCCESS = "Chúc mừng! Bạn đã sở hữu thành công vật phẩm %s!"

    // Tên & Mô tả các Nhân Vật / Skins
    var SKIN_CLASSIC_BLUE_NAME = "Neon Sphere"
    var SKIN_CLASSIC_BLUE_DESC = "Quả cầu năng lượng phát quang Neon xanh dương thuần khiết"

    var SKIN_CYBER_BOT_NAME = "Cyber Bot"
    var SKIN_CYBER_BOT_DESC = "Robot chiến binh công nghệ số tương lai với ánh mắt Cyan sắc sảo"

    var SKIN_GOLDEN_STAR_NAME = "Sao Hoàng Kim"
    var SKIN_GOLDEN_STAR_DESC = "Ngôi sao vàng rực rỡ lấp lánh biểu tượng của tốc độ và danh vọng"

    var SKIN_LUCKY_CAT_NAME = "Mèo May Mắn"
    var SKIN_LUCKY_CAT_DESC = "Mèo Maneki Neko linh hoạt, mang lại tài lộc và sự nhanh nhẹn"

    var SKIN_FIRE_DRAGON_NAME = "Rồng Lửa Bất Diệt"
    var SKIN_FIRE_DRAGON_DESC = "Ngọn lửa bùng cháy thiêu rụi mọi chướng ngại vật trong mê cung"

    var SKIN_ROYAL_CROWN_NAME = "Vương Miện Hoàng Gia"
    var SKIN_ROYAL_CROWN_DESC = "Vương miện nạm ngọc quyền quý dành riêng cho thành viên VIP danh dự"

    var SKIN_SPACE_ROCKET_NAME = "Phi Thuyền Không Gian"
    var SKIN_SPACE_ROCKET_DESC = "Tên lửa du hành siêu thanh xuyên qua không gian ma trận"

    var SKIN_NINJA_SHADOW_NAME = "Ninja Bóng Đêm"
    var SKIN_NINJA_SHADOW_DESC = "Chiến binh nhẫn giả thoắt ẩn thoắt hiện trong bóng tối mê cung"

    var SKIN_DIAMOND_GEM_NAME = "Kim Cương Tối Thượng"
    var SKIN_DIAMOND_GEM_DESC = "Viên kim cương ngũ sắc vĩnh cửu tỏa ánh hào quang rực rỡ"

    var SKIN_MAGIC_GHOST_NAME = "Bóng Ma Tinh Nghịch"
    var SKIN_MAGIC_GHOST_DESC = "Hồn ma Neon dễ thương lướt nhẹ nhàng xuyên qua các góc cua"

    // Gói Nạp & VIP
    var VIP_TITLE = "Gói VIP Đặc Quyền Không Quảng Cáo"
    var VIP_DESC = "Tắt vĩnh viễn quảng cáo phiền toái + Mở khóa ngay Skin Vương Miện Hoàng Gia VIP + Nhận ngay +1,000 Xu"
    var VIP_BTN = "Kích Hoạt VIP (59.000đ)"
    var VIP_ACTIVE_BADGE = "★ THÀNH VIÊN VIP ★"

    var AD_REWARD_TITLE = "Xem Video Nhận Xu Miễn Phí"
    var AD_REWARD_DESC = "Xem video ngắn nhận ngay +100 Xu để sắm nhân vật yêu thích!"
    var AD_REWARD_BTN = "📺 Nhận +100 Xu"

    // =====================================================================================
    // 7. MINI-GAME VẼ MỘT NÉT (ONE-LINE PUZZLE)
    // =====================================================================================
    var ONELINE_TITLE = "VẼ MỘT NÉT (ONE-LINE)"
    var ONELINE_SUBTITLE = "Hamiltonian Path · 100% Phủ Kín Ô Trống"
    var ONELINE_TAB_SQUARE = "Hình Vuông (3×3 → 15×15)"
    var ONELINE_TAB_SPECIAL = "Hình Đặc Biệt"
    var ONELINE_LEVEL_HEADER = "Màn %d · Lưới %s"
    var ONELINE_SHAPE_HEADER = "%s · %d Ô Trống"
    var ONELINE_CELLS_COUNT = "Đã vẽ: %d / %d ô"
    var ONELINE_COMPLETE_TITLE = "🎉 XUẤT SẮC HOÀN THÀNH!"
    var ONELINE_COMPLETE_SUB = "Bạn đã lấp đầy 100% đường nối một nét không trùng lặp!"

    // Tên các hình dạng đặc biệt
    var SHAPE_STAR_NAME = "Ngôi Sao Năm Cánh ⭐"
    var SHAPE_TREX_NAME = "Khủng Long T-Rex 🦖"
    var SHAPE_HEART_NAME = "Trái Tim Yêu Thương ❤️"
    var SHAPE_CROWN_NAME = "Vương Miện Quý Tộc 👑"
    var SHAPE_DIAMOND_NAME = "Kim Cương Đa Giác 💎"
    var SHAPE_HOUSE_NAME = "Ngôi Nhà Nhỏ 🏠"
    var SHAPE_TREE_NAME = "Cây Thông Xanh 🌲"
    var SHAPE_SWORD_NAME = "Thanh Kiếm Thần 🗡️"
    var SHAPE_CROSS_NAME = "Dấu Thập Chữ Thập ➕"
    var SHAPE_RECT_TALL_NAME = "Chữ Nhật Đứng 5×9"
    var SHAPE_RECT_WIDE_NAME = "Chữ Nhật Ngang 9×5"

    // =====================================================================================
    // 8. BẢNG XẾP HẠNG, THÀNH TỰU & XEM LẠI (REPLAY)
    // =====================================================================================
    var LEADERBOARD_TITLE = "BẢNG XẾP HẠNG TỐC ĐỘ"
    var LEADERBOARD_SELECT_LEVEL = "Chọn Màn Để Xem:"
    var LEADERBOARD_RANK_COL = "Hạng"
    var LEADERBOARD_NAME_COL = "Người chơi"
    var LEADERBOARD_TIME_COL = "Thời gian"
    var LEADERBOARD_MOVES_COL = "Số bước"

    var ACHIEVEMENTS_TITLE = "BỘ THÀNH TỰU"
    var ACHIEVEMENTS_PROGRESS = "Đã mở khóa: %d / %d thành tựu"

    var REPLAY_TITLE = "XEM LẠI ĐƯỜNG ĐI (REPLAY)"
    var REPLAY_STEP_COUNTER = "Bước %d / %d"
    var REPLAY_SPEED_LABEL = "Tốc độ: %.1fx"

    // =====================================================================================
    // 9. CÀI ĐẶT & HỘP THOẠI XÁC NHẬN
    // =====================================================================================
    var SOUND_LABEL = "Âm thanh"
    var MUSIC_LABEL = "Nhạc nền"
    var HAPTIC_LABEL = "Rung phản hồi"
    var LANGUAGE_LABEL = "Ngôn ngữ"

    var RESET_DIALOG_TITLE = "Đặt Lại Dữ Liệu Trò Chơi?"
    var RESET_DIALOG_BODY = "Hành động này sẽ xóa toàn bộ số màn đã vượt qua, kỷ lục sao, xu thưởng và trang bị. Hành động không thể hoàn tác!"
    var RESET_CHECKBOX_LABEL = "Tôi xác nhận muốn xóa sạch toàn bộ dữ liệu"
    var RESET_CONFIRM_BTN = "Xóa Sạch Dữ Liệu"

    var HELP_DIALOG_TITLE = "Hướng Dẫn Luật Chơi"
    var HELP_LINE_1 = "1. MỤC TIÊU CHÍNH"
    var HELP_LINE_2 = "Điều khiển nhân vật từ điểm Xuất phát đến Lá cờ Đích đến với số bước ít nhất."
    var HELP_LINE_3 = "2. ĐIỀU KHIỂN LINH HOẠT"
    var HELP_LINE_4 = "Hỗ trợ chế độ Kéo thả 1 nét tự động hoặc Chạm vuốt từng ô tùy theo thói quen của bạn."
    var HELP_LINE_5 = "3. SỞ HỮU TRANG BỊ"
    var HELP_LINE_6 = "Tích lũy xu sau mỗi ván thắng để mở khóa vô vàn nhân vật siêu đẹp trong Cửa Hàng!"
}
