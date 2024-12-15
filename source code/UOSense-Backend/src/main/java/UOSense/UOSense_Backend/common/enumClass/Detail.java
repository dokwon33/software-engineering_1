package UOSense.UOSense_Backend.common.enumClass;

public enum Detail implements BaseEnum {
    ABUSIVE("욕설"), DEROGATORY("비하 발언"), ADVERTISEMENT("광고성 리뷰");

    private final String value;

    Detail(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
