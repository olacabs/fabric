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

import com.olacabs.fabric.manager.utils.graph.UndirectedGraph;

/**
 * Todo .
 */
public class UndirectedGraphTest {

    @Test
    public void testNumberOfComponentsMoreThanOne() {
        List<List<Integer>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            adjacencyList.add(new LinkedList<>());
        }
        adjacencyList.get(0).addAll(Arrays.asList(1, 2));
        adjacencyList.get(1).add(0);
        adjacencyList.get(2).addAll(Arrays.asList(0, 3, 4));
        adjacencyList.get(3).add(2);
        adjacencyList.get(4).addAll(Arrays.asList(2, 5));
        adjacencyList.get(5).addAll(Arrays.asList(4, 6));
        adjacencyList.get(6).add(5);
        adjacencyList.get(7).add(8);
        adjacencyList.get(8).add(7);
        UndirectedGraph graph = new UndirectedGraph(adjacencyList);
        Assert.assertEquals(2, graph.numberOfComponents());
    }

    @Test
    public void testNumberOfComponentsEmptyGraph() {
        List<List<Integer>> adjacencyList = new ArrayList<>();
        UndirectedGraph graph = new UndirectedGraph(adjacencyList);
        Assert.assertEquals(0, graph.numberOfComponents());
    }

    @Test
    public void testNumberOfComponentsSingletonGraph() {
        List<List<Integer>> adjacencyList = new ArrayList<>();
        adjacencyList.add(new LinkedList<>());
        UndirectedGraph graph = new UndirectedGraph(adjacencyList);
        Assert.assertEquals(1, graph.numberOfComponents());
    }

    @Test
    public void testNumberOfComponentsOne() {
        List<List<Integer>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            adjacencyList.add(new LinkedList<>());
        }
        adjacencyList.get(0).add(2);
        adjacencyList.get(1).add(3);
        adjacencyList.get(2).addAll(Arrays.asList(0, 3));
        adjacencyList.get(3).addAll(Arrays.asList(1, 2));
        UndirectedGraph graph = new UndirectedGraph(adjacencyList);
        Assert.assertEquals(1, graph.numberOfComponents());
    }
}
