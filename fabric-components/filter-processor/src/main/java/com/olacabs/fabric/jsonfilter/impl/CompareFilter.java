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

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CompareFilter implements Filter {

    private static final String LT = "lt";

    private static final String GT = "gt";

    private static final String EQ = "eq";

    private static final String GTE = "gte";

    private static final String LTE = "lte";

    @JsonProperty("field1")
    private String field1;

    @JsonProperty("field2")
    private String field2;

    @JsonProperty("constant")
    private Object constant;

    @JsonProperty("comparison_operator")
    private String comparisonOperator;


    @Override
    public boolean filter(String json) {
        Object document = JSON_PROVIDER.parse(json);
        try {
            Comparable<Object> lhsValue = JsonPath.read(document, this.field1);
            Object rhsValue = getRHS(document);
            return evaluate(lhsValue, rhsValue, comparisonOperator);
        } catch (Exception exception) {
            log.debug(exception.getMessage());
            System.err.println(exception.getMessage());
        }

        return false;
    }

    private boolean evaluate(Comparable<Object> lhsValue, Object rhsValue, String comparisonOperatorLocal) {
        if (null != rhsValue && null != lhsValue) {

            int res = lhsValue.compareTo(rhsValue);
            if (res == 0) {
                if (comparisonOperatorLocal.equals(LTE)
                        || comparisonOperatorLocal.equals(GTE)
                        || comparisonOperatorLocal.equals(EQ)) {
                    return true;
                }
            } else if (res > 0) {
                if (comparisonOperatorLocal.equals(GT) || comparisonOperatorLocal.equals(GTE)) {
                    return true;
                }
            } else {
                if (comparisonOperatorLocal.equals(LT) || comparisonOperatorLocal.equals(LTE)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Object getRHS(Object document) {
        Object comparisonObject;
        if (field2 == null) {
            comparisonObject = constant;
        } else {
            comparisonObject = JsonPath.read(document, this.field2);
        }
        return comparisonObject;
    }

}
