package utc.k61.cntt2.class_management.enumeration;

public enum ClassPeriod {
    PERIOD_1("Ca 1"), PERIOD_2 ("Ca 2"), PERIOD_3 ("Ca 3"), PERIOD_4 ("Ca 4"),
    PERIOD_5 ("Ca 5"), PERIOD_6 ("Ca 6");

    String name;

    ClassPeriod(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
