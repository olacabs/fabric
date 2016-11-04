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

import com.olacabs.fabric.manager.domain.GlobalPropertyDomain;

/**
 * Todo .
 */
public interface IGlobalPropertyDAO {

    /**
     * Saves property.
     *
     * @param property to saveOrUpdate
     * @return saved property
     */
    GlobalPropertyDomain save(final GlobalPropertyDomain property);

    /**
     * Gets all properties.
     *
     * @return list of global properties
     */
    Set<GlobalPropertyDomain> search(final String name, final String propertyType);

    /**
     * Get by id.
     *
     * @param id of property
     * @return property
     */
    GlobalPropertyDomain get(final int id);

    /**
     * Delete property.
     *
     * @param property to delete
     */
    void delete(final GlobalPropertyDomain property);
}
