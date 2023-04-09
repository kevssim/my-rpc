package github.kevssim.protocol.message;

import java.io.Serializable;
import java.util.Arrays;

public class RpcRequest extends Message implements Serializable {
    private static final long serialVersionUID = 3429819829549582957L;

    private String interfaceName;
    //标识请求服务的版本
    private String version;
    private String methodName;
    private Object[] params;
    private Class<?>[] paramTypes;

    public RpcRequest() {
        messageType = MessageType.REQUEST;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "interfaceName='" + interfaceName + '\'' +
                ", version='" + version + '\'' +
                ", methodName='" + methodName + '\'' +
                ", params=" + Arrays.toString(params) +
                ", paramTypes=" + Arrays.toString(paramTypes) +
                '}';
    }
}
