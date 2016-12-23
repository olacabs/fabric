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

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.olacabs.fabric.jsonfilter.dsl.BaseDslParser;
import com.olacabs.fabric.jsonfilter.dsl.DslParser;

/**
 * TODO javadoc.
 */
public final class FilterCreator {

    private FilterCreator() {}

    public static Filter createFilter(File filterFile) throws IOException, ProcessingException {
        JsonNode jsonNode = JsonLoader.fromFile(filterFile);
        String dsl = jsonNode.toString();
        return createFilter(dsl);
    }

    public static Filter createFilter(String filterDsl) throws IOException, ProcessingException {
        DslParser dslparser = BaseDslParser.getInstance();
        return dslparser.parse(filterDsl);
    }
}
