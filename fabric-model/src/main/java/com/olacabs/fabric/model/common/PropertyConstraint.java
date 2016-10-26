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

package com.olacabs.fabric.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * TODO javadoc.
 */
@Slf4j
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PropertyConstraint {
    private String property;
    private ValueConstraint valueConstraint;
    private List<DependencyConstraint> dependencyConstraints;


    /**
     * TODO javadoc.
     */
    @Data
    @Builder
    public static class ValueConstraint {
        private String defaultValue;
        private List<String> valueDomain;
    }


    /**
     * TODO javadoc.
     */
    @Data
    @Builder
    public static class DependencyConstraint {
        private String property;
        private Operator operator;
        private String value;
    }


    /**
     * TODO javadoc.
     */
    public enum Operator {
        LIKE, IN, EXISTS, EQUALS, NOTEQUALS, GREATER, LESSER, GEQ, LEQ
    }
}
