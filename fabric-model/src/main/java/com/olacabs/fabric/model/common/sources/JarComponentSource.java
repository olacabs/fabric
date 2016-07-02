package com.olacabs.fabric.model.common.sources;

import com.olacabs.fabric.model.common.ComponentSource;
import com.olacabs.fabric.model.common.ComponentSourceType;
import com.olacabs.fabric.model.common.ComponentSourceVisitor;
import lombok.Builder;
import lombok.Data;

/**
 * Created by santanu.s on 02/10/15.
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
