package github.kevssim.transport.Client;

import github.kevssim.cluster.loadbalance.LoadBalance;
import github.kevssim.protocol.message.RpcRequest;
import github.kevssim.protocol.message.RpcResponse;
import github.kevssim.registry.ServiceDiscovery;
import github.kevssim.registry.zk.ZkServiceDiscovery;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RpcProxyFactory {
    private final ConnectHandler ch;

    private final ServiceDiscovery serviceDiscovery;

    public RpcProxyFactory(LoadBalance loadBalance, ConnectHandler ch) {
        this.serviceDiscovery = new ZkServiceDiscovery(loadBalance);
        this.ch = ch;
    }

    final ShareClient shareClient = new ShareClient();

    private final Lock lock = new ReentrantLock();


    public Object getShareProxy(final Class<?> serviceClass) {
        return Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class<?>[]{serviceClass},
                (proxy, method, args) -> {
                    lock.lock();
                    try {
                        if (!shareClient.isInited()) {
                            InetSocketAddress socketAddress = serviceDiscovery.lookupService(serviceClass.getName());
                            shareClient.init(socketAddress);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                    RpcRequest request = new RpcRequest();
                    String requestId = UUID.randomUUID().toString().replace("-", "");
                    request.setRequestId(requestId);
                    request.setSerialType(1);
                    request.setInterfaceName(serviceClass.getName());
                    request.setVersion("1.0");
                    request.setMethodName(method.getName());
                    request.setParams(args);
                    request.setParamTypes(method.getParameterTypes());

                    RpcResponse response = null;
                    try {
                        response = shareClient.send(request);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (response != null && response.getStatus() >= 200 && response.getStatus() < 300) {
                        //如果调用成功
                        return response.getData();
                    } else if (response == null) {
                        throw new Exception("结果为空");
                    } else {
                        throw new Exception(response.getMessage());
                    }
                }
        );
    }

    public void closeShareClient() {
        shareClient.close();
    }

    public Object getProxy(final Class<?> serviceClass) {
        return Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class<?>[]{serviceClass},
                (proxy, method, args) -> {
                    RpcRequest request = new RpcRequest();
                    request.setSerialType(1);
                    request.setInterfaceName(serviceClass.getName());
                    request.setVersion("1.0");
                    request.setMethodName(method.getName());
                    request.setParams(args);
                    request.setParamTypes(method.getParameterTypes());

                    int i = ch.retries + 1;
                    RpcResponse response = null;
                    boolean isTimeout;
                    while (i > 0) {
                        isTimeout = false;
                        i--;
                        //发现服务
                        InetSocketAddress socketAddress = serviceDiscovery.lookupService(serviceClass.getName());
                        //InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 7000);
                        //调用服务
                        RpcClient client = new RpcClient(socketAddress, ch.timeout);
                        try {
                            response = client.send(request);
                        } catch (TimeoutException e) {
                            if (i == 0) {
                                throw e;
                            }
                            isTimeout = true;
                        }
                        if (!isTimeout) {
                            if (response != null && response.getStatus() >= 200 && response.getStatus() < 300) {
                                //如果调用成功
                                return response.getData();
                            } else if (response == null) {
                                throw new Exception("结果为空");
                            } else {
                                throw new Exception(response.getMessage());
                            }
                        }
                    }
                    return null;
                }
        );
    }
}
