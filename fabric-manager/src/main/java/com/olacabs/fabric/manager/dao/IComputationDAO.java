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

import com.olacabs.fabric.manager.domain.ComputationDomain;

/**
 * Todo .
 */
public interface IComputationDAO {

    /**
     * Saves a computation.
     *
     * @param computation ::
     * @return saved entity
     */
    ComputationDomain save(final ComputationDomain computation);

    /**
     * Get by id.
     *
     * @param computationId ::
     * @return entity
     */
    ComputationDomain get(final int computationId);

    /**
     * Updates entity.
     *
     * @param computation ::
     */
    void modify(final ComputationDomain computation);

    /**
     * Deletes entity.
     *
     * @param computation ::
     */
    void delete(final ComputationDomain computation);

    /**
     * Searches entity given params.
     *
     * @param tenant ::
     * @param name ::
     * @param version ::
     * @return set of entities
     */
    Set<ComputationDomain> search(final String tenant, final String name, final Integer version);
}
