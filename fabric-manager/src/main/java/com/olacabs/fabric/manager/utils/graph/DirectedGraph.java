/*
 * Copyright 2016 ANI Technologies Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.olacabs.fabric.manager.utils.graph;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Todo .
 */
@Getter
@Setter
@Slf4j
public class DirectedGraph extends Graph {

    private int timestamp;

    private List<DfsTimestamp> nodeTimestamps;

    public DirectedGraph(final List<List<Integer>> adjacencyList) {
        super(adjacencyList);
    }

    private void incrementTimestamp() {
        timestamp++;
    }

    private List<Integer> getNeighbours(final int node) {
        return adjacencyList.get(node);
    }

    private int getStartTime(final int node) {
        return nodeTimestamps.get(node).getStart();
    }

    private int getFinishTime(final int node) {
        return nodeTimestamps.get(node).getFinish();
    }

    private void setStartTime(final int node, final int startTime) {
        nodeTimestamps.get(node).setStart(startTime);
    }

    private void setFinishTime(final int node, final int finishTime) {
        nodeTimestamps.get(node).setFinish(finishTime);
    }

    private void preDfs() {
        this.timestamp = 0;
        this.nodeTimestamps = new ArrayList<>();
        for (int i = 0; i < adjacencyList.size(); i++) {
            nodeTimestamps.add(new DfsTimestamp(-1, -1));
        }
    }

    private void postDfs() {
        this.nodeTimestamps.clear();
    }

    public boolean hasCycle() {
        boolean result = false;
        preDfs();
        for (int j = 0; j < numberOfNodes; j++) {
            if (getStartTime(j) < 0) {
                if (hasCycle(j)) {
                    result = true;
                    break;
                }
            }
        }
        postDfs();
        return result;
    }

    private boolean hasCycle(int currentNode) {
        if (getStartTime(currentNode) < 0) {
            setStartTime(currentNode, getTimestamp());
            incrementTimestamp();
            for (int node : getNeighbours(currentNode)) {
                if (getFinishTime(node) >= 0) {
                    log.debug("found a cross edge or forward edge, skip following");
                } else if (getStartTime(node) < 0) {
                    // found a tree edge, need to follow
                    boolean hasCycle = hasCycle(node);
                    if (hasCycle) {
                        return true;
                    }
                } else if (getStartTime(node) < getStartTime(currentNode)) {
                    // found a back edge
                    return true;
                }
            }
            setFinishTime(currentNode, getTimestamp());
            incrementTimestamp();
        }
        return false;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    static class DfsTimestamp {
        private int start;
        private int finish;
    }
}
