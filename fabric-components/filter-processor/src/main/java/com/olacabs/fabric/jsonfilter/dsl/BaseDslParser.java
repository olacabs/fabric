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

package com.olacabs.fabric.jsonfilter.dsl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.olacabs.fabric.jsonfilter.Filter;

import lombok.extern.slf4j.Slf4j;

/**
 * TODO javadoc.
 */
@Slf4j
public final class BaseDslParser implements DslParser {

    private static final String SCHEMAFILE = "dslSchema.json";

    private JsonSchema schema = null;

    private static class BaseDslParserHolder {
        private static final BaseDslParser INSTANCE;

        static {
            try {
                INSTANCE = new BaseDslParser();
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    public static BaseDslParser getInstance() throws IOException, ProcessingException {
        return BaseDslParserHolder.INSTANCE;
    }

    private BaseDslParser() throws IOException, ProcessingException {

        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        JsonNode schemaJson = getSchemaFilezfromPath();
        schema = factory.getJsonSchema(schemaJson);
    }

    @Override
    public Filter parse(String dsl) throws IOException, ProcessingException {
        Filter filter = null;
        if (isValid(dsl)) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(dsl);
            jsonNode = jsonNode.at("/filter");
            filter = mapper.readValue(jsonNode.toString(), Filter.class);
        }
        log.debug(filter != null ? filter.toString() : null);
        return filter;
    }

    public boolean isValid(String dsl) throws ProcessingException, IOException {

        JsonNode jsonNode = JsonLoader.fromString(dsl);

        ProcessingReport report;
        report = schema.validate(jsonNode);

        if (!report.isSuccess()) {
            throw new RuntimeException("Invalid Dsl :\\n" + report.toString());
        }
        return report.isSuccess();
    }

    private JsonNode getSchemaFilezfromPath() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(SCHEMAFILE);
        return JsonLoader.fromReader(new InputStreamReader(inputStream));
    }

}
