package github.kevssim.registry.zk;

import github.kevssim.registry.ServiceDiscovery;
import github.kevssim.cluster.loadbalance.LoadBalance;
import github.kevssim.registry.zk.utils.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 服务发现
 */
public class ZkServiceDiscovery implements ServiceDiscovery {
    private final LoadBalance loadBalance;
    private final Logger logger = LoggerFactory.getLogger(ZkServiceDiscovery.class);

    public ZkServiceDiscovery(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    public InetSocketAddress lookupService(String rpcServiceName) throws Exception {
        List<String> serviceUrlList = CuratorUtils.SERVICE_ADDRESS_MAP.get(rpcServiceName);
        if (serviceUrlList == null) {
            //获取客户端对象
            CuratorFramework zkClient = CuratorUtils.getZkClient();
            serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        }
        if (serviceUrlList == null || serviceUrlList.isEmpty())
            throw new Exception("无法找到服务");
        //负载均衡
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList);
        logger.info("成功找到服务地址：[{}]", targetServiceUrl);
        //构建socketAddress
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
