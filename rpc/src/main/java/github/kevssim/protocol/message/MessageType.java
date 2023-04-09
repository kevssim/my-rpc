package github.kevssim.protocol.message;

public enum MessageType {
    REQUEST(0), RESPONSE(1);

    private int value;

    private MessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
