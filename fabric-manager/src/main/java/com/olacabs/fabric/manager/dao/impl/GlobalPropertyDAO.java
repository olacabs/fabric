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

package com.olacabs.fabric.manager.dao.impl;

import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.olacabs.fabric.manager.dao.IGlobalPropertyDAO;
import com.olacabs.fabric.manager.domain.GlobalPropertyDomain;
import com.olacabs.fabric.manager.exception.ResourceNotFoundException;

import io.dropwizard.hibernate.AbstractDAO;

/**
 * Todo .
 */
public class GlobalPropertyDAO extends AbstractDAO<GlobalPropertyDomain> implements IGlobalPropertyDAO {

    private final SessionFactory sessionFactory;

    public GlobalPropertyDAO(final SessionFactory sessionFactory) {
        super(sessionFactory);
        this.sessionFactory = sessionFactory;
    }

    @Override
    public GlobalPropertyDomain save(final GlobalPropertyDomain properties) {
        return super.persist(properties);
    }

    @Override
    public Set<GlobalPropertyDomain> search(final String name, final String propertyType) {
        final Session session = sessionFactory.getCurrentSession();
        final Criteria criteria = session.createCriteria(GlobalPropertyDomain.class);
        criteria.add(Restrictions.eq("deleted", false));

        if (!Strings.isNullOrEmpty(name)) {
            criteria.add(Restrictions.eq("name", name));
        }

        if (!Strings.isNullOrEmpty(propertyType)) {
            criteria.add(Restrictions.eq("type", propertyType));
        }
        return Sets.newHashSet(super.list(criteria));
    }

    @Override
    public GlobalPropertyDomain get(final int id) {
        final GlobalPropertyDomain property = super.get(id);
        if (property.isDeleted()) {
            throw new ResourceNotFoundException("Global property not found with id - " + id);
        }
        return property;
    }

    @Override
    public void delete(final GlobalPropertyDomain property) {
        property.setDeleted(true);
        super.persist(property);
    }
}
