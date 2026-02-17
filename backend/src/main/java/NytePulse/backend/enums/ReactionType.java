package NytePulse.backend.enums;

public enum ReactionType {
    LIKE(1),
    LOVE(2),
    FIRE(3),
    KING(4),
    WATCH(5),
    CHEERS(6),
    PARTY(7),
    CRAZY(8),
    CELEBRATE(9),
    POWER(10);

    private final int id;

    ReactionType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}