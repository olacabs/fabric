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

import com.olacabs.fabric.manager.dao.IComponentDAO;
import com.olacabs.fabric.manager.dao.IComputationDAO;
import com.olacabs.fabric.manager.domain.ComputationDomain;
import com.olacabs.fabric.manager.exception.ResourceNotFoundException;
import com.olacabs.fabric.manager.service.IComputationService;
import com.olacabs.fabric.manager.utils.ComputationUtil;
import com.olacabs.fabric.model.common.ComponentType;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Todo .
 */
@AllArgsConstructor
@Slf4j
public class ComputationService implements IComputationService {

    private IComputationDAO computationDAO;
    private IComponentDAO componentDAO;

    @Override
    public ComputationDomain saveOrUpdate(final String tenant, final ComputationDomain computation) {
        final Set<ComputationDomain> computations = computationDAO.search(tenant, computation.getName(), null);
        int newVersion = 0;
        for (final ComputationDomain c : computations) {
            if (c.getVersion() > newVersion) {
                newVersion = c.getVersion();
            }
        }
        if (newVersion > 0) {
            computation.setVersion(newVersion + 1);
        }

        computation.setTenant(tenant);
        ComputationUtil.preWriteComputationCheck(computation);

        computation.getSources().forEach(componentInstance -> {
            componentInstance.setComponent(componentDAO.read(componentInstance.getComponentId()));
            componentInstance.setType(ComponentType.SOURCE);
        });
        computation.getProcessors().forEach(componentInstance -> {
            componentInstance.setComponent(componentDAO.read(componentInstance.getComponentId()));
            componentInstance.setType(ComponentType.PROCESSOR);
        });

        return computationDAO.save(computation);
    }

    @Override
    public ComputationDomain getById(final String tenant, final int id) {
        final ComputationDomain computation = computationDAO.get(id);
        if (null == computation || !computation.getTenant().equalsIgnoreCase(tenant) || computation.isDeleted()) {
            throw new ResourceNotFoundException(
                    String.format("Computation not found with tenant %s and id %d", tenant, id));
        }
        return computation;
    }

    @Override
    public Set<ComputationDomain> search(final String tenant, final String name, final Integer version) {
        return computationDAO.search(tenant, name, version);
    }

    @Override
    public void delete(final String tenant, final int id) {
        final ComputationDomain fetchedComputation = getById(tenant, id);
        computationDAO.delete(fetchedComputation);
    }
}
