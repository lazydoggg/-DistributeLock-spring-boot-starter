package com.nodomain.autoconfigure.distributelock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author: zph
 * @date: 2024/02/04
 * @description: 分布式锁配置类
 */
@ConfigurationProperties(prefix = DistributeLockConfig.PREFIX)
public class DistributeLockConfig {

    public static final String PREFIX = "spring.distributelock";
    //redisson
    private String address;
    private String password;
    private int database = 15;
    private ClusterServer clusterServer;
    private long waitTime = 60;
    private long leaseTime = 60;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }

    public long getLeaseTime() {
        return leaseTime;
    }

    public void setLeaseTime(long leaseTime) {
        this.leaseTime = leaseTime;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public ClusterServer getClusterServer() {
        return clusterServer;
    }

    public void setClusterServer(ClusterServer clusterServer) {
        this.clusterServer = clusterServer;
    }

    public static class ClusterServer{

        private String[] nodeAddresses;

        public String[] getNodeAddresses() {
            return nodeAddresses;
        }

        public void setNodeAddresses(String[] nodeAddresses) {
            this.nodeAddresses = nodeAddresses;
        }
    }



}
