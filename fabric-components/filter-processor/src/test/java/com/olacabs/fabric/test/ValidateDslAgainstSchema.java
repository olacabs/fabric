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

package com.olacabs.fabric.test;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

/**
 * TODO javadoc.
 */
public final class ValidateDslAgainstSchema {

    private ValidateDslAgainstSchema() {}

    public static void main(String[] args) throws IOException, ProcessingException {
        String dslSchema = "dslSchema.json";
        File filter = new File("TestDsls/CompareFilterDsl.json");
        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

        JsonNode schemaJson = JsonLoader.fromResource(dslSchema);
        final JsonSchema schema = factory.getJsonSchema(schemaJson);

        JsonNode jsonNode = JsonLoader.fromFile(filter);
        ProcessingReport report;

        report = schema.validate(jsonNode);
        System.out.println(report);
    }

}
