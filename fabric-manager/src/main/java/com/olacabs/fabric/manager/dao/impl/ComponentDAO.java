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
import com.olacabs.fabric.manager.dao.IComponentDAO;
import com.olacabs.fabric.manager.domain.ComponentDomain;
import com.olacabs.fabric.manager.exception.ResourceNotFoundException;

/**
 * Todo .
 */
public class ComponentDAO extends BaseDAO<ComponentDomain> implements IComponentDAO {

    private final SessionFactory sessionFactory;

    public ComponentDAO(final SessionFactory sessionFactory) {
        super(sessionFactory);
        this.sessionFactory = sessionFactory;
    }

    @Override
    public ComponentDomain save(final ComponentDomain component) {
        return super.persist(component);
    }

    @Override
    public ComponentDomain read(final int componentId) {
        final ComponentDomain component = super.get(componentId);
        if (component == null || component.isDeleted()) {
            throw new ResourceNotFoundException(String.format("Component not found with id - %d", componentId));
        }
        return super.get(componentId);
    }

    @Override
    public Set<ComponentDomain> search(final String namespace, final String name, final String version) {
        final Session session = sessionFactory.getCurrentSession();
        final Criteria criteria = session.createCriteria(ComponentDomain.class);
        criteria.add(Restrictions.eq("deleted", false));

        if (!Strings.isNullOrEmpty(namespace)) {
            criteria.add(Restrictions.eq("namespace", namespace));
        }
        if (!Strings.isNullOrEmpty(name)) {
            criteria.add(Restrictions.eq("name", name));
        }
        if (!Strings.isNullOrEmpty(version)) {
            criteria.add(Restrictions.eq("version", version));
        }

        final Set<ComponentDomain> result = Sets.newHashSet();
        result.addAll(super.list(criteria));
        return result;
    }

    @Override
    public void update(final int componentId, final ComponentDomain component) {
        final ComponentDomain readComp = read(componentId);
        readComp.update(component);
        super.update(readComp);
    }

    @Override
    public void delete(final int componentId) {
        final ComponentDomain component = read(componentId);
        component.setDeleted(true);
        super.persist(component);
    }
}
