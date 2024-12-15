package UOSense.UOSense_Backend.common.enumClass;

public enum Category implements BaseEnum {
    KOREAN("한식"), CHINESE("중식"), JAPANESE("일식"), WESTERN("양식"), OTHER("기타");
    private final String value;

    Category(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
