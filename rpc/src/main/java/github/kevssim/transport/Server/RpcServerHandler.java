package github.kevssim.transport.Server;


import github.kevssim.protocol.message.RpcRequest;
import github.kevssim.protocol.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.*;

public class RpcServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);

    //设置处理超时时间
    private final int timeout = 2000;

    //存放service对应的impl类
    private final Map<String, Object> handlerMap;

    private final ExecutorService executors = new ThreadPoolExecutor(
            1,
            1,
            1,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(200),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    public RpcServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //msg类型为RequestBody
        RpcRequest rpcRequest = (RpcRequest) msg;
        logger.info(rpcRequest.toString());

        RpcResponse response = new RpcResponse();
        response.setSerialType(1);
        try {
            Object result = null;
            FutureTask<Object> futureTask = new FutureTask<>(() -> {
                try {
                    return handle(rpcRequest);
                } catch (InvocationTargetException e) {
                    throw new Exception(e.getTargetException());
                }
            });
            //执行任务
            executors.submit(futureTask);
            result = futureTask.get(2, TimeUnit.SECONDS);

            //不使用超时机制，性能会更高
            //result = handle(rpcRequest);

            response.setRequestId(rpcRequest.getRequestId());
            response.setStatus(200);
            response.setMessage("success");
            response.setData(result);
        } catch (TimeoutException e) {
            response.setRequestId(rpcRequest.getRequestId());
            response.setStatus(500);
            response.setMessage("请求处理超时");
        } catch (Exception e) {
            response.setRequestId(rpcRequest.getRequestId());
            response.setStatus(500);
            response.setMessage(e.getMessage());
        }

        logger.info("发送响应数据：" + response.toString());
        ctx.channel().writeAndFlush(response);
    }

    private Object handle(RpcRequest rpcRequest) throws Exception {
        String serviceName = rpcRequest.getInterfaceName();
        String serviceVersion = rpcRequest.getVersion();
        if (serviceVersion.trim().length() > 0)
            serviceName += "-" + serviceVersion;

        //获取impl实例
        Object serviceImpl = handlerMap.get(serviceName);
        if (serviceImpl == null) {
            throw new Exception(String.format("can not find service bean by key: %s", serviceName));
        }

        //获取接口Class对象
        Class<?> interfaceClazz = Class.forName(rpcRequest.getInterfaceName());
        //获取调用的方法
        Method method = interfaceClazz.getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        method.setAccessible(true);
        //调用方法
        return method.invoke(serviceImpl, rpcRequest.getParams());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
