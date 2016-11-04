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

package com.olacabs.fabric.manager.domain;

import java.sql.Timestamp;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.olacabs.fabric.manager.filter.UserContext;

import lombok.Getter;
import lombok.Setter;

/**
 * Base domain .
 */
@MappedSuperclass
public class BaseDomain {

    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int internalId;

    @Getter
    @Setter
    private String createdBy;

    @Getter
    @Setter
    private String updatedBy;

    @Getter
    @Setter
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Getter
    @Setter
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

    /**
     * Sets created and updated timestamp before persisting.
     */
    public void prePersist() {
        final Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        this.setCreatedAt(currentTime);
        this.setUpdatedAt(currentTime);
        this.setCreatedBy(UserContext.instance().getUser());
    }

    /**
     * Sets created and updated timestamp before saveOrUpdate.
     */
    public void preUpdate() {
        this.setUpdatedBy(UserContext.instance().getUser());
        this.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    }
}
