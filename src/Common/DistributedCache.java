package Common;


import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.InterfacesConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.Map;

public class DistributedCache<T> extends Cache<T> {

    private IMap<Integer, T> cacheMap;
    private HazelcastInstance hazelcastInstance;
    private HazelcastInstance hazelcastClient;


    public DistributedCache(String clusterName, String bindIp, int bindPort, boolean clientOnly, String cacheName) {
        if (clusterName.isBlank())
            throw new RuntimeException("Cache cluster name is not set!");

        if(!clientOnly)
            this.hazelcastInstance =  createHazelcastInstance(clusterName, bindIp, bindPort);
        this.hazelcastClient =  createHazelcastClient(clusterName, bindIp, bindPort);

        cacheMap = this.hazelcastClient.getMap(cacheName);
        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(this::shutdown));
    }

    private static HazelcastInstance createHazelcastInstance(String clusterName, String bindIp, int bindPort) {
        if (clusterName.isBlank())
            throw new RuntimeException("Cache cluster name is not set!");

        HazelcastInstance hazelcastInstance = Hazelcast.getHazelcastInstanceByName(clusterName+"-member");
        if(hazelcastInstance != null)
            return hazelcastInstance;

        Config hConfig = new Config();
        hConfig.setClusterName(clusterName);
        hConfig.setInstanceName(clusterName+"-member");
        if (bindIp != null) {
            NetworkConfig network = hConfig.getNetworkConfig();
            network.setPort(bindPort);
            InterfacesConfig interfaceConfig = network.getInterfaces();
            interfaceConfig.setEnabled(true).addInterface(bindIp);
        }
        return Hazelcast.getOrCreateHazelcastInstance(hConfig);
    }

    private static HazelcastInstance createHazelcastClient(String clusterName, String bindIp, int bindPort) {
        HazelcastInstance hazelcastInstance = HazelcastClient.getHazelcastClientByName(clusterName+"-client");
        if(hazelcastInstance != null)
            return hazelcastInstance;

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setInstanceName(clusterName+"-client");
        clientConfig.setClusterName(clusterName);
        clientConfig.getNetworkConfig().addAddress(bindIp + ":" + bindPort);
        return HazelcastClient.getOrCreateHazelcastClient(clientConfig);
    }

    @Override
    public void resetCache() {
        cacheMap.clear();
    }

    @Override
    public void updateItem(Integer id, T item) {
        cacheMap.put(id, item);
    }

    @Override
    public T getItem(int id) {
        return cacheMap.get(id);
    }

    @Override
    public boolean hasItem(int id) {
        return cacheMap.containsKey(id);
    }

    @Override
    public void removeItem(int id) {
        cacheMap.remove(id);
    }

    @Override
    public void shutdown() {
        if (hazelcastClient != null) {
            hazelcastClient.shutdown();
            hazelcastClient = null;
        }
        if (hazelcastInstance != null) {
            hazelcastInstance.shutdown();
            hazelcastInstance = null;
        }
    }

    @Override
    public Map<Integer, T> getMap() {
        return cacheMap;
    }

    public IMap<Integer, T> getIMap() {
        return cacheMap;
    }


}
