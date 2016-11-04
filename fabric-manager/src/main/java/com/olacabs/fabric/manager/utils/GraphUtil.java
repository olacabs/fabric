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

package com.olacabs.fabric.manager.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.olacabs.fabric.manager.domain.ComponentInstanceDomain;
import com.olacabs.fabric.manager.domain.ComputationDomain;
import com.olacabs.fabric.manager.domain.ConnectionDomain;
import com.olacabs.fabric.manager.utils.graph.DirectedGraph;
import com.olacabs.fabric.manager.utils.graph.UndirectedGraph;

/**
 * Todo .
 */
final class GraphUtil {

    private GraphUtil(){
    }

    /**
     * @param spec to get directed graph
     * @return directed graph
     */
    public static DirectedGraph getDirectedGraphFromComputationSpec(final ComputationDomain spec) {
        final Set<ConnectionDomain> connections = spec.getConnections();
        final int numberOfNodes = spec.getSources().size() + spec.getProcessors().size();
        final AtomicInteger i = new AtomicInteger(0);
        final Map<String, Integer> nodes = spec.getSources().stream()
                .collect(Collectors.toMap(ComponentInstanceDomain::getId, componentInstance -> i.getAndIncrement()));
        nodes.putAll(spec.getProcessors().stream()
                .collect(Collectors.toMap(ComponentInstanceDomain::getId, componentInstance -> i.getAndIncrement())));
        final List<List<Integer>> adjacencyList = Lists.newArrayList();
        for (int j = 0; j < numberOfNodes; j++) {
            adjacencyList.add(Lists.newLinkedList());
        }
        connections.stream().forEach(connection -> adjacencyList.get(nodes.get(connection.getFromLink()))
                .add(nodes.get(connection.getToLink())));
        return new DirectedGraph(adjacencyList);
    }

    /**
     * @param spec to get undirected graph
     * @return undirected graph
     */
    public static UndirectedGraph getUndirectedGraphFromComputationSpec(final ComputationDomain spec) {
        final Set<ConnectionDomain> connections = spec.getConnections();
        final int numberOfNodes = spec.getSources().size() + spec.getProcessors().size();
        final AtomicInteger i = new AtomicInteger(0);
        final Map<String, Integer> nodes = spec.getSources().stream()
                .collect(Collectors.toMap(ComponentInstanceDomain::getId, componentInstance -> i.getAndIncrement()));
        nodes.putAll(spec.getProcessors().stream()
                .collect(Collectors.toMap(ComponentInstanceDomain::getId, componentInstance -> i.getAndIncrement())));
        final List<List<Integer>> adjacencyList = Lists.newArrayList();
        for (int j = 0; j < numberOfNodes; j++) {
            adjacencyList.add(new LinkedList<>());
        }
        connections.stream().forEach(connection -> {
            adjacencyList.get(nodes.get(connection.getFromLink())).add(nodes.get(connection.getToLink()));
            adjacencyList.get(nodes.get(connection.getToLink())).add(nodes.get(connection.getFromLink()));
        });

        return new UndirectedGraph(adjacencyList);
    }
}
