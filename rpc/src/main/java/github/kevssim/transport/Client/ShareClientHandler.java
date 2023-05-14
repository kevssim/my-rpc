package github.kevssim.transport.Client;

import github.kevssim.protocol.message.RpcRequest;
import github.kevssim.protocol.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.CompletableFuture;


public class ShareClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private ChannelHandlerContext context;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        context = ctx;
    }

    //异步发送请求
    public CompletableFuture<RpcResponse> send(RpcRequest request) throws InterruptedException {
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        UnprocessedRequests.put(request.getRequestId(), resultFuture);
        context.channel().writeAndFlush(request).sync();
        return resultFuture;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) {
        try {
            System.out.println("收到消息：" + rpcResponse.toString());
            UnprocessedRequests.complete(rpcResponse);
        } finally {
            ReferenceCountUtil.release(rpcResponse);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                //读空闲
                System.out.println("请求超时");
                throw new Exception("请求服务超时");
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
