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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import com.olacabs.fabric.jsonfilter.Filter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO javadoc.
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchFilter implements Filter {

    @JsonProperty("field")
    private String field;

    @JsonProperty("value")
    private Object value;

    @Override
    public boolean filter(String json) {
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
        Object fieldValue;
        try {
            fieldValue = JsonPath.read(document, this.field);
            if (value.equals(fieldValue)) {
                return true;
            }
        } catch (Exception exception) {
            log.error("Exception - {}", exception.getMessage(), exception);
        }
        return false;
    }

}
