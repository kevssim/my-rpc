import github.kevssim.cluster.loadbalance.impl.RandomLoadBalanceImpl;
import github.kevssim.service.HelloService;
import github.kevssim.transport.Client.ConnectHandler;
import github.kevssim.transport.Client.RpcProxyFactory;

public class HelloConsumer {

    public static void main(String[] args) {
        RpcProxyFactory proxyFactory = new RpcProxyFactory(
                new RandomLoadBalanceImpl(),
                new ConnectHandler(0, 0)
        );

        //创建代理对象
        HelloService service = (HelloService) proxyFactory.getShareProxy(HelloService.class);

        for (int j = 0; j < 10; j++) {
            String res = null;
            try {
                res = service.hello(String.valueOf(j));
                System.out.println(res);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

