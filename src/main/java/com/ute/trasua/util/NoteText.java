package com.ute.trasua.util;

/**
 * Ghép/ cắt ghi chú văn bản theo đúng giới hạn cột DB.
 *
 * Lý do tồn tại: các ghi chú nghiệp vụ (điều phối, sự cố, hoàn đơn) được nối dần vào
 * {@code orders.delivery_notes} (varchar 500) và {@code orders.incident_note} (varchar 255).
 * Chỉ cần nối quá dài một lần là Hibernate ném Data truncation, transaction bị đánh dấu
 * rollback-only và toàn bộ thao tác (bàn giao / báo sự cố / bật ca) trả về 500.
 */
public final class NoteText {

    /** Giới hạn cột orders.delivery_notes. */
    public static final int DELIVERY_NOTES_MAX = 500;
    /** Giới hạn cột orders.incident_note. */
    public static final int INCIDENT_NOTE_MAX = 255;

    private NoteText() {
    }

    /** Nối thêm 1 đoạn vào ghi chú hiện có, luôn đảm bảo không vượt độ dài cột. */
    public static String append(String existing, String addition, int maxLength) {
        String base = (existing == null) ? "" : existing.trim();
        String add = (addition == null) ? "" : addition.trim();
        if (add.isEmpty()) {
            return base.isEmpty() ? null : truncate(base, maxLength);
        }
        String merged = base.isEmpty() ? add : base + " | " + add;
        return truncate(merged, maxLength);
    }

    /** Cắt chuỗi về đúng độ dài cột. */
    public static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
