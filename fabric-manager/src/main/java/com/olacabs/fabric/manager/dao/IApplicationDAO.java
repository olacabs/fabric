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

import com.olacabs.fabric.manager.domain.ApplicationDomain;

/**
 * Todo .
 */
public interface IApplicationDAO {

    /**
     * Save application.
     *
     * @param application ::
     * @return created application
     */
    ApplicationDomain save(final ApplicationDomain application);

    /**
     * Get application.
     *
     * @param internalId ::
     * @return application
     */
    ApplicationDomain get(final Long internalId);

    /**
     * Soft delete application.
     *
     * @param application ::
     */
    void delete(final ApplicationDomain application);

    /**
     * Revive the application.
     *
     * @param application ::
     */
    void revive(final ApplicationDomain application);

    /**
     * Search application.
     *
     * @param appName to search
     * @param tenant to search
     * @return set of application
     */
    Set<ApplicationDomain> search(final String appName, final String tenant);

    /**
     * modify application.
     *
     * @param application ::
     */
    void modify(final ApplicationDomain application);
}
