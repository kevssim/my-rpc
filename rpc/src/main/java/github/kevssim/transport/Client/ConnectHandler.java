package github.kevssim.transport.Client;

public class ConnectHandler {
    int timeout = 5;

    int retries = 3;

    public ConnectHandler() {
    }

    public ConnectHandler(int timeout, int retries) {
        this.timeout = timeout;
        this.retries = retries;
    }


}
