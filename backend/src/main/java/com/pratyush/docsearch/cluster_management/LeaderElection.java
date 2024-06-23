package com.pratyush.docsearch.cluster_management;

import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class LeaderElection implements Watcher {
    private String currentZnodeName;
    private ZooKeeper zooKeeper;
    private final OnElectionCallback onElectionCallback;
    private static final String ELECTION_NAMESPACE = "/election";

    public LeaderElection(ZooKeeper zooKeeper, OnElectionCallback onElectionCallback) {
        this.zooKeeper = zooKeeper;
        this.onElectionCallback = onElectionCallback;
    }

    public void volunteerForLeadership() throws KeeperException, InterruptedException {
        String zooPrefix = ELECTION_NAMESPACE + "/c";
        String znodeFullPath = zooKeeper.create(zooPrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        this.currentZnodeName = znodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
        System.out.println(currentZnodeName);
    }

    public void reelectLeader() throws KeeperException, InterruptedException {
        Stat predecessorStat = null;
        String predecessorZnodeName = "";

        // loop to avoid race condition
        while(predecessorStat == null) {
            List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
            Collections.sort(children);
            System.out.println(children);
            String smallestChild = children.get(0);

            if(smallestChild.equals(this.currentZnodeName)) {
                System.out.println("I am the leader: " + this.currentZnodeName);
                this.onElectionCallback.onElectedToBeLeader();
                return;
            } else {
                System.out.println("I am not the leader, " + smallestChild + " is the leader.");
                int predecessorIndex = Collections.binarySearch(children, currentZnodeName) - 1;
                predecessorZnodeName = children.get(predecessorIndex);
                predecessorStat = zooKeeper.exists(ELECTION_NAMESPACE + "/" + predecessorZnodeName, this);
                System.out.println("Watching node: " + predecessorZnodeName);
            }
        }
        this.onElectionCallback.onWorker();
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case NodeDeleted:
            try {
                reelectLeader();
            } catch(KeeperException e) {                
            } catch(InterruptedException e) {
            }
            default:
                break;
        }
    }
}
