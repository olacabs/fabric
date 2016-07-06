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

package com.olacabs.fabric.model.common.sources;

import com.olacabs.fabric.model.common.ComponentSource;
import com.olacabs.fabric.model.common.ComponentSourceType;
import com.olacabs.fabric.model.common.ComponentSourceVisitor;
import lombok.Builder;
import lombok.Data;

/**
 * TODO Add more.
 */
@Data
public class JarComponentSource extends ComponentSource {
    private String url;

    public JarComponentSource() {
        super(ComponentSourceType.jar);
    }

    @Builder
    public JarComponentSource(final String url) {
        this();
        this.url = url;
    }

    @Override
    public void accept(ComponentSourceVisitor visitor) {
        visitor.visit(this);
    }
}
