package com.pratyush.docsearch.cluster_management;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.zookeeper.KeeperException;

import com.pratyush.docsearch.search.SearchWorker;
import com.pratyush.docsearch.server.WebServer;

public class OnElectionAction implements OnElectionCallback {
    private final ServiceRegistry serviceRegistry;
    private final int port;
    private WebServer webServer;

    public OnElectionAction(ServiceRegistry serviceRegistry, int port) {
        this.serviceRegistry = serviceRegistry;
        this.port = port;
    }

    @Override
    public void onElectedToBeLeader() {
        try {
            serviceRegistry.unregisterFromCluster();
            serviceRegistry.registerForUpdates();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWorker() {
        try {
            SearchWorker searchWorker = new SearchWorker();
            webServer = new WebServer(port, searchWorker);
            webServer.startServer();
            String currentServerAddress = String.format("http://%s:%d",InetAddress.getLocalHost().getCanonicalHostName(),
                                                port, searchWorker.getEndpoint());
            serviceRegistry.registerToCluster(currentServerAddress);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
