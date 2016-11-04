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

package com.olacabs.fabric.manager.utils;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.olacabs.fabric.manager.domain.ComponentInstanceDomain;
import com.olacabs.fabric.manager.domain.ComputationDomain;
import com.olacabs.fabric.manager.exception.UnProcessableException;
import com.olacabs.fabric.manager.utils.graph.DirectedGraph;
import com.olacabs.fabric.model.common.ComponentType;

/**
 * Todo .
 */
public final class ComputationUtil {

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^(\\/?((\\.{2})|([a-z0-9][a-z0-9\\-.]*[a-z0-9]+)|([a-z0-9]*))($|\\/))+$");
    private static final Pattern EMAIL_PATTERN = Pattern
            .compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    private ComputationUtil(){
    }

    private static void validateConnection(final String from, final String to, final String fromType,
            final String toType) {
        if (Objects.equals(from, to)) {
            throw new UnProcessableException(
                    String.format("Invalid computation spec - component with id=%s connected to itself", from));
        }
        if (Strings.isNullOrEmpty(toType) || Strings.isNullOrEmpty(fromType)) {
            throw new UnProcessableException(
                    "Invalid computation spec - connecting two components one of which doesn't exist");
        }
        if ("source".equals(fromType) && "source".equals(toType)) {
            throw new UnProcessableException("Invalid computation spec - invalid connection source -> source");
        }
        if ("processor".equals(fromType) && "source".equals(toType)) {
            throw new UnProcessableException("Invalid computation spec - invalid connection processor -> source");
        }
    }

    public static void validateConnections(final ComputationDomain computation) {
        Map<String, ComponentInstanceDomain> sources = computation.getSources().parallelStream()
                .collect(Collectors.toMap(ComponentInstanceDomain::getId, Function.identity()));
        Map<String, ComponentInstanceDomain> processors = computation.getProcessors().parallelStream()
                .collect(Collectors.toMap(ComponentInstanceDomain::getId, Function.identity()));

        final String sourceType = ComponentType.SOURCE.toString().toLowerCase();
        final String processorType = ComponentType.PROCESSOR.toString().toLowerCase();

        computation.getConnections().forEach(connection -> {
            final String from = connection.getFromLink();
            final String to = connection.getToLink();

            String fromType = "";
            String toType = "";
            if (sources.containsKey(from)) {
                fromType = sourceType;
            }
            if (processors.containsKey(from)) {
                fromType = processorType;
            }
            if (sources.containsKey(to)) {
                toType = ComponentType.SOURCE.toString().toLowerCase();
            }
            if (processors.containsKey(to)) {
                toType = ComponentType.PROCESSOR.toString().toLowerCase();
            }

            validateConnection(from, to, fromType, toType);
        });
    }

    public static void validateGraph(final ComputationDomain computation) {
        final DirectedGraph g = GraphUtil.getDirectedGraphFromComputationSpec(computation);
        if (g.hasCycle()) {
            throw new UnProcessableException("Invalid computation spec - computation contains one or more cycles");
        }
    }

    private static void validateEmail(final String email) {
        if (!com.google.common.base.Strings.isNullOrEmpty(email)) {
            final Matcher matcher = EMAIL_PATTERN.matcher(email);
            if (!matcher.matches()){
                throw new UnProcessableException(email + " not a valid ownerEmail");
            }
        } else {
            throw new UnProcessableException("Email can't be null");
        }
    }

    private static void validateComputationName(final String name) {
        if (name == null || name.isEmpty()) {
            throw new UnProcessableException("Invalid computation spec - computation name must not be empty or null");
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new UnProcessableException(
                    "Invalid computation spec - computation name must only contain lowercase characters,"
                            + " digits, hyphens, .");
        }
    }

    private static void validateComputation(final ComputationDomain computation) {
        validateEmail(computation.getOwnerEmail());
        validateComputationName(computation.getName());
        validateConnections(computation);
        validateGraph(computation);
        validateEmail(computation.getOwnerEmail());
    }

    public static void transform(final ComputationDomain computation) {
        final Map<String, ComponentInstanceDomain> sources = computation.getSources().parallelStream()
                .collect(Collectors.toMap(ComponentInstanceDomain::getId, Function.identity()));
        final Map<String, ComponentInstanceDomain> processors = computation.getProcessors().parallelStream()
                .collect(Collectors.toMap(ComponentInstanceDomain::getId, Function.identity()));

        computation.getConnections().forEach(connection -> {
            final String from = connection.getFromLink();
            if (sources.containsKey(from)) {
                connection.setFromType(ComponentType.SOURCE);
            }
            if (processors.containsKey(from)) {
                connection.setFromType(ComponentType.PROCESSOR);
            }
        });
    }

    public static void preWriteComputationCheck(final ComputationDomain spec) {
        ComputationUtil.transform(spec);
        ComputationUtil.validateComputation(spec);
    }

    public static String id(final String team, final String name) {
        return UUID.nameUUIDFromBytes(String.format("%s:%s", team, name).getBytes()).toString();
    }
}
