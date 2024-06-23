package com.pratyush.docsearch.cluster_management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ServiceRegistry implements Watcher {
    public static final String COORDINATOR_REGISTRY_ZNODE = "/coordinator_service_registry";
    public static final String WORKER_REGISTRY_ZNODE = "/worker_service_registry";
    private final ZooKeeper zooKeeper;
    private String serviceRegistryZnode = null;
    private String currentZnode = null;
    private List<String> serviceAddresses = null;

    public ServiceRegistry(ZooKeeper zooKeeper, String serviceRegistryZnode) {
        this.zooKeeper = zooKeeper;
        this.serviceRegistryZnode = serviceRegistryZnode;
        createServiceRegistryZnode();
    }

    public void registerToCluster(String metadata) throws KeeperException, InterruptedException {
        this.currentZnode = zooKeeper.create(serviceRegistryZnode + "/n_", metadata.getBytes(),
                                            ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Registered to service registry");
    }

    private void createServiceRegistryZnode() {
        try {
            if(zooKeeper.exists(serviceRegistryZnode, false) == null) {
                zooKeeper.create(serviceRegistryZnode, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void registerForUpdates() {
        try {
            this.updateAddresses();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<String> getServiceAddresses() throws KeeperException, InterruptedException {
        if(serviceAddresses == null) {
            updateAddresses();
        }
        return serviceAddresses;
    }

    public void unregisterFromCluster() throws KeeperException, InterruptedException {
        if(currentZnode != null && zooKeeper.exists(currentZnode, false) != null) {
            zooKeeper.delete(currentZnode, -1);
        }
    }

    private synchronized void updateAddresses() throws KeeperException, InterruptedException {
        List<String> workers = zooKeeper.getChildren(serviceRegistryZnode, (Watcher) this);
        List<String> addresses = new ArrayList<String>(workers.size());

        for(String worker: workers) {
            String serviceFullPath = serviceRegistryZnode + "/" + worker;
            Stat stat = zooKeeper.exists(serviceFullPath, false);
            if(stat == null) {
                continue;
            }

            byte[] addressBytes = zooKeeper.getData(serviceFullPath, false, stat);
            String address = new String(addressBytes);
            addresses.add(address);
        }

        this.serviceAddresses = Collections.unmodifiableList(addresses);
        System.out.println("Cluster Addresses: " + this.serviceAddresses);
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            updateAddresses();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
