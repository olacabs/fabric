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

package com.olacabs.fabric.manager.service.impl;

import java.util.Set;

import com.olacabs.fabric.manager.dao.IGlobalPropertyDAO;
import com.olacabs.fabric.manager.domain.GlobalPropertyDomain;
import com.olacabs.fabric.manager.service.IGlobalPropertyService;

import lombok.AllArgsConstructor;

/**
 * Todo .
 */
@AllArgsConstructor
public class GlobalPropertyService implements IGlobalPropertyService {

    private IGlobalPropertyDAO dao;

    @Override
    public GlobalPropertyDomain save(final GlobalPropertyDomain property) {
        return dao.save(property);
    }

    @Override
    public Set<GlobalPropertyDomain> search(final String name, final String propertyType) {
        return dao.search(name, propertyType);
    }

    @Override
    public GlobalPropertyDomain get(final int id) {
        return dao.get(id);
    }

    @Override
    public void delete(int id) {
        final GlobalPropertyDomain property = get(id);
        dao.delete(property);
    }
}
