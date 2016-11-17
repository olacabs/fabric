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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.olacabs.fabric.manager.dao.IComputationDAO;
import com.olacabs.fabric.manager.domain.ComputationDomain;

/**
 * Todo .
 */
public class ComputationDAO extends BaseDAO<ComputationDomain> implements IComputationDAO {

    private final SessionFactory sessionFactory;

    public ComputationDAO(final SessionFactory sessionFactory) {
        super(sessionFactory);
        this.sessionFactory = sessionFactory;
    }

    @Override
    public ComputationDomain save(final ComputationDomain computation) {
        return super.persist(computation);
    }

    @Override
    public ComputationDomain get(final int computationId) {
        return super.get(computationId);
    }

    @Override
    public void modify(final ComputationDomain computation) {
        super.update(computation);
    }

    @Override
    public void delete(final ComputationDomain computation) {
        computation.setDeleted(true);
        super.persist(computation);
    }

    @Override
    public Set<ComputationDomain> search(final String tenant, final String name, final Integer version) {
        final Session session = sessionFactory.getCurrentSession();
        final Criteria criteria = session.createCriteria(ComputationDomain.class);
        criteria.add(Restrictions.eq("deleted", false));

        criteria.addOrder(Order.desc("version"));

        if (!Strings.isNullOrEmpty(tenant)) {
            criteria.add(Restrictions.eq("tenant", tenant));
        }
        if (!Strings.isNullOrEmpty(name)) {
            criteria.add(Restrictions.eq("name", name));
        }
        if (version != null) {
            criteria.add(Restrictions.eq("version", version));
        }

        final Set<ComputationDomain> result = Sets.newHashSet();
        result.addAll(super.list(criteria));
        return result;
    }
}
