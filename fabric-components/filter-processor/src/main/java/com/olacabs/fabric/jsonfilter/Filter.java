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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.olacabs.fabric.jsonfilter.impl.*;

/**
 * TODO javadoc.
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "filter_type")
@JsonSubTypes({@Type(value = MatchFilter.class, name = "match"), @Type(value = InFilter.class, name = "in"),
        @Type(value = ExistFilter.class, name = "exist"), @Type(value = BoolFilter.class, name = "bool"),
        @Type(value = CompareFilter.class, name = "compare")})
public interface Filter {

    JsonProvider JSON_PROVIDER = Configuration.defaultConfiguration().jsonProvider();

    boolean filter(String json);

}
