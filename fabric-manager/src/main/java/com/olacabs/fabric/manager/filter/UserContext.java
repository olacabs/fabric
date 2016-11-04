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

package com.olacabs.fabric.manager.filter;

/**
 * Todo .
 */
public final class UserContext {

    private static final UserContext INSTANCE = new UserContext();

    private ThreadLocal<Context> contextThreadLocal = new ThreadLocal<>();

    private UserContext() {}

    public static UserContext instance() {
        return INSTANCE;
    }

    void clear() {
        this.contextThreadLocal.remove();
    }

    Context getContextThreadLocal() {
        Context context = this.contextThreadLocal.get();
        if (context == null) {
            context = new Context();
            this.contextThreadLocal.set(context);
        }
        return context;
    }

    public String getUser() {
        return getContextThreadLocal().getItem();
    }
}
