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

package com.olacabs.fabric.jsonfilter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import com.olacabs.fabric.jsonfilter.impl.BoolFilter;
import com.olacabs.fabric.jsonfilter.impl.InFilter;
import com.olacabs.fabric.jsonfilter.impl.MatchFilter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

/**
 * TODO javadoc.
 */
public class BoolFilterTest {

    private MatchFilter matchFilter;

    private InFilter inFilter;

    @Before
    public void setup() {
        matchFilter = Mockito.mock(MatchFilter.class);
        when(matchFilter.filter("true")).thenReturn(true);
        when(matchFilter.filter("false")).thenReturn(false);

        inFilter = Mockito.mock(InFilter.class);
        when(inFilter.filter("true")).thenReturn(true);
        when(inFilter.filter("false")).thenReturn(false);
    }

    private List<Filter> must = Lists.newArrayList();

    private List<Filter> should = Lists.newArrayList();

    private List<Filter> mustNot = Lists.newArrayList();

    @Test
    public final void testFilter() {
        must.clear();
        should.clear();
        mustNot.clear();
        Filter filter = new BoolFilter(must, should, mustNot);
        assertTrue(filter.filter(""));
    }

    @Test
    public final void testFilterOnlyMust() {
        must.clear();
        should.clear();
        mustNot.clear();
        must.add(matchFilter);
        must.add(inFilter);
        Filter filter = new BoolFilter(must, should, mustNot);
        assertTrue(filter.filter("true"));
        assertFalse(filter.filter("false"));
    }

    @Test
    public final void testFilterOnlyShould() {
        must.clear();
        should.clear();
        mustNot.clear();
        should.add(matchFilter);
        should.add(inFilter);
        Filter filter = new BoolFilter(must, should, mustNot);
        assertTrue(filter.filter("true"));
        assertFalse(filter.filter("false"));
    }

    @Test
    public final void testFilterOnlyMustNot() {
        must.clear();
        should.clear();
        mustNot.clear();
        mustNot.add(matchFilter);
        mustNot.add(inFilter);
        Filter filter = new BoolFilter(must, should, mustNot);
        assertFalse(filter.filter("true"));
        assertTrue(filter.filter("false"));
    }

    @Test
    public final void testFilterWithAllConditions() {
        must.clear();
        should.clear();
        mustNot.clear();

        must.add(matchFilter);
        must.add(inFilter);
        should.add(matchFilter);
        should.add(inFilter);
        mustNot.add(matchFilter);
        mustNot.add(inFilter);

        Filter filter = new BoolFilter(must, should, mustNot);
        assertFalse(filter.filter("true"));
        assertFalse(filter.filter("false"));

        mustNot.clear();
        assertTrue(filter.filter("true"));
        assertFalse(filter.filter("false"));
    }
}
