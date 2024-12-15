package UOSense.UOSense_Backend.common.enumClass;

public enum SubDescription implements BaseEnum {
    BAR("술집"), CAFE("카페"), RESTAURANT("음식점");
    private final String value;

    SubDescription(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
