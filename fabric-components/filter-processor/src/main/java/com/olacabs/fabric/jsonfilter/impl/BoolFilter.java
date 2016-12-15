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

package com.olacabs.fabric.jsonfilter.impl;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import com.olacabs.fabric.jsonfilter.Filter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * TODO javadoc.
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BoolFilter implements Filter {

    @JsonProperty("must")
    private List<Filter> must = Lists.newArrayList();

    @JsonProperty("should")
    private List<Filter> should = new ArrayList<>();

    @JsonProperty("must_not")
    private List<Filter> mustNot = new ArrayList<>();

    @Override
    public boolean filter(String json) {
        return (must.isEmpty() || must.stream().allMatch(filter -> filter.filter(json)))
                & (should.isEmpty() || should.stream().anyMatch(filter -> filter.filter(json)))
                & (mustNot.isEmpty() || mustNot.stream().noneMatch(filter -> filter.filter(json)));
    }

}
