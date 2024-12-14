package UOSense.UOSense_Backend.common.enumClass;

public enum DoorType implements BaseEnum {
    FRONT("정문"), SIDE("쪽문"), BACK("후문"), SOUTH("남문");
    private final String value;

    DoorType(String name) {
        this.value = name;
    }

    @Override
    public String getValue() {
        return value;
    }
}
