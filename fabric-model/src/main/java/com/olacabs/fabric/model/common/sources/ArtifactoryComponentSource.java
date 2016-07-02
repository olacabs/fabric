package com.olacabs.fabric.model.common.sources;

import com.olacabs.fabric.model.common.ComponentSource;
import com.olacabs.fabric.model.common.ComponentSourceType;
import com.olacabs.fabric.model.common.ComponentSourceVisitor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by santanu.s on 02/10/15.
 */

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
