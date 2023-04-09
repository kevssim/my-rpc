import com.github.houbb.junitperf.core.annotation.JunitPerfConfig;
import com.github.houbb.junitperf.core.report.impl.HtmlReporter;
import github.kevssim.cluster.loadbalance.impl.RandomLoadBalanceImpl;
import github.kevssim.service.HelloService;
import github.kevssim.transport.Client.ConnectHandler;
import github.kevssim.transport.Client.RpcProxyFactory;

public class HelloConsumerTest {
    private final RpcProxyFactory proxyFactory = new RpcProxyFactory(
            new RandomLoadBalanceImpl(),
            new ConnectHandler(0, 0)
    );

    private final HelloService service = (HelloService) proxyFactory.getShareProxy(HelloService.class);

    @JunitPerfConfig(threads = 64, duration = 60000, reporter = {HtmlReporter.class})
    public void helloTest() throws Exception {
        service.hello("你好");
    }
}
