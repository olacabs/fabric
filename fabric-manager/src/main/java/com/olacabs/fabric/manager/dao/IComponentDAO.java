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

package com.olacabs.fabric.manager.dao;

import java.util.Set;

import com.olacabs.fabric.manager.domain.ComponentDomain;

/**
 * Todo .
 */
public interface IComponentDAO {

    /**
     * Save a component.
     *
     * @param component :: to saveOrUpdate
     * @return created component
     */
    ComponentDomain save(final ComponentDomain component);

    /**
     * Reads a component.
     *
     * @param componentId to read
     * @return read component
     */
    ComponentDomain read(final int componentId);

    /**
     * Search component based on params.
     *
     * @param namespace to search
     * @param name to search
     * @param version to search
     * @return set of searched components
     */
    Set<ComponentDomain> search(final String namespace, final String name, final String version);

    /**
     * Update component.
     *
     * @param componentId to saveOrUpdate
     * @param component to saveOrUpdate
     */
    void update(final int componentId, final ComponentDomain component);

    /**
     * Soft Delete component.
     *
     * @param componentId to deleteApp
     */
    void delete(final int componentId);
}
