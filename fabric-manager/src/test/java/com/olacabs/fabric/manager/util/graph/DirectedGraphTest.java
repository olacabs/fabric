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

package com.olacabs.fabric.manager.util.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.olacabs.fabric.manager.utils.graph.DirectedGraph;

/**
 * Todo .
 */
public class DirectedGraphTest {

    @Test
    public void testIsCyclicGraphHasNoCycles() {
        List<List<Integer>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            adjacencyList.add(new LinkedList<>());
        }
        adjacencyList.get(0).addAll(Arrays.asList(1, 2));
        adjacencyList.get(2).add(3);
        adjacencyList.get(4).addAll(Arrays.asList(2, 5));
        adjacencyList.get(5).add(6);
        adjacencyList.get(6).add(2);

        DirectedGraph graph = new DirectedGraph(adjacencyList);
        Assert.assertFalse(graph.hasCycle());
    }

    @Test
    public void testIsCyclicGraphHasCycles() {
        List<List<Integer>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            adjacencyList.add(new LinkedList<>());
        }
        adjacencyList.get(0).addAll(Arrays.asList(1, 2));
        adjacencyList.get(2).addAll(Arrays.asList(3, 4));
        adjacencyList.get(4).add(5);
        adjacencyList.get(5).add(6);
        adjacencyList.get(6).add(2);

        DirectedGraph graph = new DirectedGraph(adjacencyList);
        Assert.assertTrue(graph.hasCycle());
    }

    @Test
    public void testIsCyclicEmptyGraph() {
        List<List<Integer>> adjacencyList = new ArrayList<>();
        DirectedGraph graph = new DirectedGraph(adjacencyList);
        Assert.assertFalse(graph.hasCycle());
    }

    @Test
    public void testIsCyclicSingletonGraph() {
        List<List<Integer>> adjacencyList = new ArrayList<>();
        adjacencyList.add(new LinkedList<>());
        DirectedGraph graph = new DirectedGraph(adjacencyList);
        Assert.assertFalse(graph.hasCycle());
    }
}
