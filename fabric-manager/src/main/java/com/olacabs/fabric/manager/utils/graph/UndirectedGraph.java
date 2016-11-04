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

import java.util.BitSet;
import java.util.List;

/**
 * Todo .
 */
public class UndirectedGraph extends Graph {
    private BitSet visited;

    public UndirectedGraph(final List<List<Integer>> adjacencyList) {
        super(adjacencyList);
    }

    private void preDfs() {
        if (visited == null) {
            visited = new BitSet(numberOfNodes);
        }
    }

    private void postDfs() {
        visited.clear();
    }

    public int numberOfComponents() {
        int components = 0;
        preDfs();
        for (int i = 0; i < numberOfNodes; i++) {
            if (!visited.get(i)) {
                components++;
                dfs(i);
            }
        }
        postDfs();
        return components;
    }

    private void dfs(int startNode) {
        if (visited.get(startNode)) {
            return;
        } else {
            visited.set(startNode);
            List<Integer> neighbours = adjacencyList.get(startNode);
            for (int neighbour : neighbours) {
                if (!visited.get(neighbour)) {
                    dfs(neighbour);
                }
            }
        }
    }

}
