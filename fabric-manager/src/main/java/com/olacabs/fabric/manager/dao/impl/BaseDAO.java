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

import java.io.Serializable;
import java.util.List;

import org.hibernate.*;

import com.olacabs.fabric.manager.domain.BaseDomain;

import io.dropwizard.hibernate.AbstractDAO;

/**
 * Extending abstract dao.
 * to execute entity pre-persist and pre-update Dropwizard-hibernate doesn't execute it by
 * default apparently TODO - find a cleaner solution for @PrePersist & @PreUpdate
 *
 * @param <E>
 */
public class BaseDAO<E extends BaseDomain> extends AbstractDAO<E> {

    public BaseDAO(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    protected Session currentSession() {
        return super.currentSession();
    }

    protected Criteria criteria() {
        return super.criteria();
    }

    protected Query namedQuery(final String queryName) throws HibernateException {
        return super.namedQuery(queryName);
    }

    public Class<E> getEntityClass() {
        return super.getEntityClass();
    }

    protected E uniqueResult(Criteria criteria) throws HibernateException {
        return super.uniqueResult(criteria);
    }

    protected E uniqueResult(Query query) throws HibernateException {
        return super.uniqueResult(query);
    }

    protected List<E> list(Criteria criteria) throws HibernateException {
        return super.list(criteria);
    }

    protected List<E> list(Query query) throws HibernateException {
        return super.list(query);
    }

    protected E get(Serializable id) {
        return super.get(id);
    }

    protected E persist(E entity) throws HibernateException {
        entity.prePersist();
        return super.persist(entity);
    }

    protected E update(E entity) throws HibernateException {
        entity.preUpdate();
        return super.persist(entity);
    }

    protected <T> T initialize(T proxy) throws HibernateException {
        return super.initialize(proxy);
    }
}
