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

package com.olacabs.fabric.manager.service;

import java.util.Map;

import com.olacabs.fabric.manager.domain.ApplicationDomain;

import mesosphere.marathon.client.Marathon;

/**
 * Todo .
 */
public interface IApplicationService {

    /**
     * Get the app name.
     *
     * @param tenant ::
     * @param name :: computation name
     * @return name
     */
    String name(final String tenant, final String name);

    /**
     * Persist and deploys the app.
     *
     * @param application ::
     * @param tenant ::
     * @param instances ::
     */
    ApplicationDomain create(final ApplicationDomain application, final String tenant, final int instances);

    /**
     * Soft delete from db and deletes from marathon.
     *
     * @param appName ::
     * @param tenant ::
     */
    void delete(final String appName, final String tenant);

    /**
     * Get application from db.
     *
     * @param appName ::
     * @param tenant ::
     * @return App domain
     */
    ApplicationDomain get(final String appName, final String tenant);

    /**
     * Get app from deployed env (marathon).
     *
     * @param appName ::
     * @return response
     */
    Map getAppFromDeploymentEnv(final String appName);

    /**
     * Scale the app.
     *
     * @param appName ::
     * @param tenant ::
     * @param scaleTo ::
     */
    void scale(final String appName, final String tenant, final int scaleTo);

    /**
     * Updates the app.
     *
     * @param appName ::
     * @param tenant ::
     * @param updateParam ::
     */
    void update(final String appName, final String tenant, final ApplicationDomain updateParam);

    /**
     * Restarts the app.
     *
     * @param appName ::
     * @param tenant ::
     * @param forcefully ::
     */
    void restart(final String appName, final String tenant, final boolean forcefully);

    /**
     * Get marathon client.
     *
     * @return marathon
     */
    Marathon getMarathon();
}
