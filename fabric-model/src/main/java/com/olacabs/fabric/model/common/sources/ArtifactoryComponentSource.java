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
import lombok.Getter;
import lombok.Setter;

public class ArtifactoryComponentSource extends ComponentSource {
    @Getter
    @Setter
    private String artifactoryUrl;

    @Getter
    @Setter
    private String groupId;

    @Getter
    @Setter
    private String artifactId;

    @Getter
    @Setter
    private String version;

    public ArtifactoryComponentSource() {
        super(ComponentSourceType.artifactory);
    }

    @Builder
    public ArtifactoryComponentSource(final String artifactoryUrl, final String groupId, final String artifactId, final String version) {
        this();
        this.artifactoryUrl = artifactoryUrl;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArtifactoryComponentSource that = (ArtifactoryComponentSource) o;

        if (!artifactoryUrl.equals(that.artifactoryUrl)) return false;
        if (!groupId.equals(that.groupId)) return false;
        if (!artifactId.equals(that.artifactId)) return false;
        return version.equals(that.version);

    }

    @Override
    public int hashCode() {
        int result = artifactoryUrl.hashCode();
        result = 31 * result + groupId.hashCode();
        result = 31 * result + artifactId.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public void accept(ComponentSourceVisitor visitor) {
        visitor.visit(this);
    }
}
