package com.pratyush.docsearch.cluster_management;

public interface OnElectionCallback {
    void onElectedToBeLeader();
    void onWorker();
}
