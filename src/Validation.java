package devicemanagement;

final class Validation {
    private Validation() {}
    static String text(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " không được rỗng.");
        return value.trim();
    }
}
