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

package com.olacabs.fabric.manager.service;

import java.util.Set;

import com.olacabs.fabric.manager.domain.ComponentDomain;

/**
 * Todo .
 */
public interface IComponentService {

    /**
     * Saves Component.
     *
     * @param component to saveOrUpdate
     * @return component
     */
    ComponentDomain save(final ComponentDomain component);

    /**
     * Search Component.
     *
     * @param namespace to search
     * @param name to search
     * @param version to search
     * @return set of components
     */
    Set<ComponentDomain> search(final String namespace, final String name, final String version);

    /**
     * Read by id.
     *
     * @param id to read
     * @return component
     */
    ComponentDomain get(final int id);

    /**
     * Update component.
     *
     * @param id to saveOrUpdate
     * @param component to saveOrUpdate
     */
    void update(final int id, final ComponentDomain component);

    /**
     * Delete Component.
     *
     * @param namespace to deleteApp
     * @param name to deleteApp
     * @param version to deleteApp
     */
    void delete(final String namespace, final String name, final String version);
}
