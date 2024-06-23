package com.pratyush.docsearch;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import com.pratyush.docsearch.cluster_management.LeaderElection;
import com.pratyush.docsearch.cluster_management.OnElectionAction;
import com.pratyush.docsearch.cluster_management.ServiceRegistry;

public class Application implements Watcher {
	private ZooKeeper zooKeeper;
	private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
	private static final int DEFAULT_PORT = 8080;


	public static void main(String[] args) throws KeeperException, InterruptedException, IOException {
		int currentServicePort = args.length == 1 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
		Application application = new Application();
		ZooKeeper zooKeeper = application.connectToZookeeper();

		ServiceRegistry workerServiceRegistry = new ServiceRegistry(zooKeeper, ServiceRegistry.WORKER_REGISTRY_ZNODE);
		OnElectionAction onElectionAction = new OnElectionAction(workerServiceRegistry, currentServicePort);

		LeaderElection leaderElection = new LeaderElection(zooKeeper, onElectionAction);
        leaderElection.volunteerForLeadership();
        leaderElection.reelectLeader();

		application.run();
		application.close();
        System.out.println("Disconnected");
	}

	public ZooKeeper connectToZookeeper() throws IOException {
		this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, (Watcher) this);
		return zooKeeper;
    }

	public void run() throws InterruptedException {
        synchronized (zooKeeper) {
            this.zooKeeper.wait();
        }
    }

    public void close() throws InterruptedException {
        synchronized(zooKeeper) {
            zooKeeper.close();
        }
    }

	@Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Connected");
                } else {
                    synchronized(zooKeeper) {
                        System.out.println("Disconnected from zookeeper");
                        zooKeeper.notifyAll();
                    }
                }
                break;
            default:
                break;
        }
    }

}
