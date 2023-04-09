package github.kevssim.cluster.loadbalance.impl;

import github.kevssim.cluster.loadbalance.LoadBalance;

import java.util.List;
import java.util.Random;

public class RandomLoadBalanceImpl implements LoadBalance {
    private static final Random random = new Random();

    @Override
    public String selectServiceAddress(List<String> serviceUrlList) {
        if (serviceUrlList == null || serviceUrlList.isEmpty()) {
            return null;
        }
        if (serviceUrlList.size() == 1) {
            return serviceUrlList.get(0);
        }
        return doSelect(serviceUrlList);
    }

    private String doSelect(List<String> serviceUrlList) {
        return serviceUrlList.get(random.nextInt(serviceUrlList.size()));
    }
}
