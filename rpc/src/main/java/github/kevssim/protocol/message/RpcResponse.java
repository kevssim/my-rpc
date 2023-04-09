package github.kevssim.protocol.message;

import java.io.Serializable;

public class RpcResponse extends Message implements Serializable {
    private static final long serialVersionUID = 6936284729174859372L;

    //状态码
    private int status;

    //信息
    private String message;

    //返回数据
    private Object data;

    public RpcResponse() {
        messageType = MessageType.RESPONSE;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
