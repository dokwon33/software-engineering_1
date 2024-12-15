package UOSense.UOSense_Backend.common.enumClass;

public enum Tag implements BaseEnum {
    GOOD_SERVICE("서비스가 좋아요"), SOLO_POSSIBLE("혼밥 가능해요"),
    DATE_PLACE("데이트를 추천해요"), KIND_BOSS("사장님이 친절해요"),
    NICE_INTERIOR("인테리어가 멋져요");
    private final String value;

    Tag(String name) {
        this.value = name;
    }

    @Override
    public String getValue() {
        return value;
    }
}
