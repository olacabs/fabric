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
import com.olacabs.fabric.manager.domain.ComputationDomain;

/**
 * Todo .
 */
public interface IComputationService {

    /**
     * saveOrUpdate computation.
     *
     * @param tenant ::
     * @param computation ::
     * @return created entity
     */
    ComputationDomain saveOrUpdate(final String tenant, final ComputationDomain computation);

    /**
     * Gets entity by id.
     *
     * @param tenant ::
     * @param id ::
     * @return fetched computation
     */
    ComputationDomain getById(final String tenant, final int id);

    /**
     * Search.
     *
     * @param tenant ::
     * @param name ::
     * @return set of entities
     */
    Set<ComputationDomain> search(final String tenant, final String name, final Integer version);

    /**
     * Soft deletes a computation.
     *
     * @param tenant ::
     * @param id ::
     */
    void delete(final String tenant, final int id);
}
