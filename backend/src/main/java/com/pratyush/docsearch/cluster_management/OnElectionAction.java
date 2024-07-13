package com.pratyush.docsearch.cluster_management;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.zookeeper.KeeperException;

import com.pratyush.docsearch.search.SearchCoordinator;
import com.pratyush.docsearch.search.SearchWorker;
import com.pratyush.docsearch.server.WebClient;
import com.pratyush.docsearch.server.WebServer;

public class OnElectionAction implements OnElectionCallback {
    private final ServiceRegistry workerServiceRegistry;
    private final ServiceRegistry coordinatorServiceRegistry;
    private final int port;
    private WebServer webServer;

    public OnElectionAction(ServiceRegistry workerServiceRegistry, ServiceRegistry coordinatorServiceRegistry, int port) {
        this.workerServiceRegistry = workerServiceRegistry;
        this.coordinatorServiceRegistry = coordinatorServiceRegistry;
        this.port = port;
    }

    @Override
    public void onElectedToBeLeader() {
        try {
            workerServiceRegistry.unregisterFromCluster();
            workerServiceRegistry.registerForUpdates();

            if(webServer != null) {
                webServer.stop();
            }

            SearchCoordinator searchCoordinator = new SearchCoordinator(workerServiceRegistry, new WebClient());
            WebServer webServer = new WebServer(port, searchCoordinator);
            webServer.startServer();

            try {
                String currentServerAddress = String.format("http://%s:%d",InetAddress.getLocalHost().getCanonicalHostName(),
                                                port, searchCoordinator.getEndpoint());
                coordinatorServiceRegistry.registerToCluster(currentServerAddress);
            } catch(Exception e) {
                e.printStackTrace();
                return;
            }

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
            workerServiceRegistry.registerToCluster(currentServerAddress);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
