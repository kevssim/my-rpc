package github.kevssim.cluster.loadbalance;

import java.util.List;

/**
 * 负载均衡接口
 */
public interface LoadBalance {
    /**
     * 从List中选择一个  地址：端口
     * @param serviceUrlList
     * @return
     */
    String selectServiceAddress(List<String> serviceUrlList);
}
