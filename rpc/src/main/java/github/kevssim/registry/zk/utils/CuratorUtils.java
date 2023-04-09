package github.kevssim.registry.zk.utils;


import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class CuratorUtils {
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    public static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    //路径集合
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();

    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "124.221.136.24:2181";
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;

    private static final Logger logger = LoggerFactory.getLogger(CuratorUtils.class);


    private CuratorUtils() {
    }

    /**
     * 创建临时节点
     *
     * @param path 节点路径 eg: /my-rpc/github.kevssim.HelloService/127.0.0.1:9999
     */
    public static void createNode(CuratorFramework zkClient, String path) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                logger.info("节点已存在。 节点路径：[{}]", path);
            } else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
                logger.info("节点创建成功。节点路径：[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            logger.error("节点：[{}] 创建失败", path);
        }
    }

    /**
     * 获取某个服务下的所有子节点
     *
     * @param rpcServiceName eg:github.kevssim.HelloServicetest2version1
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            registerWatcher(rpcServiceName, zkClient);
        } catch (Exception e) {
            logger.error("获取 [{}] 下的子节点失败", servicePath);
        }
        return result;
    }

    /**
     * 清除该ip和端口的所有服务
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress) {
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                if (p.endsWith(inetSocketAddress.toString())) {
                    zkClient.delete().forPath(p);
                }
            } catch (Exception e) {
                logger.error("clear registry for path [{}] fail", p);
            }
        });
        logger.info("All registered services on the server are cleared:[{}]", REGISTERED_PATH_SET.toString());
    }

    /**
     * 建立连接
     *
     * @return zkClient对象
     */
    public static CuratorFramework getZkClient() {
        //如果zkClient已存在，直接返回，否则创建zkClient
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        //连接不成功，使用间隔重试的策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        //创建zkClient
        zkClient = CuratorFrameworkFactory.newClient(DEFAULT_ZOOKEEPER_ADDRESS, retryPolicy);
        zkClient.start();
        try {
            // 等待30s直到连接到zooKeeper服务器
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Time out waiting to connect to ZK!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
    }

    /**
     * Registers to listen for changes to the specified node
     *
     * @param rpcServiceName rpc service name eg:github.kevssim.HelloServicetest2version
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        //回调函数，当service的子节点发生变化时，更新SERVICE_ADDRESS_MAP
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }
}
