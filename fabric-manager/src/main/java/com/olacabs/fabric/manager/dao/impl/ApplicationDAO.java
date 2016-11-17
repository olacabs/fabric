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
import com.olacabs.fabric.manager.dao.IApplicationDAO;
import com.olacabs.fabric.manager.domain.ApplicationDomain;

/**
 * Todo .
 */
public class ApplicationDAO extends BaseDAO<ApplicationDomain> implements IApplicationDAO {

    private final SessionFactory sessionFactory;

    public ApplicationDAO(final SessionFactory sessionFactory) {
        super(sessionFactory);
        this.sessionFactory = sessionFactory;
    }

    @Override
    public ApplicationDomain save(final ApplicationDomain application) {
        application.setActive(true);
        return super.persist(application);
    }

    @Override
    public ApplicationDomain get(final Long internalId) {
        return super.get(internalId);
    }

    @Override
    public void delete(final ApplicationDomain application) {
        application.setActive(false);
        application.setInstances(0);
        super.update(application);
    }

    @Override
    public void revive(final ApplicationDomain application) {
        application.setActive(true);
        super.update(application);
    }

    @Override
    public Set<ApplicationDomain> search(final String appName, final String tenant) {
        final Session session = sessionFactory.getCurrentSession();
        final Criteria criteria = session.createCriteria(ApplicationDomain.class);

        if (!Strings.isNullOrEmpty(appName)) {
            criteria.add(Restrictions.eq("name", appName));
        }
        if (!Strings.isNullOrEmpty(tenant)) {
            criteria.add(Restrictions.eq("tenant", tenant));
        }

        return Sets.newHashSet(super.list(criteria));
    }

    @Override
    public void modify(final ApplicationDomain application) {
        super.update(application);
    }
}
