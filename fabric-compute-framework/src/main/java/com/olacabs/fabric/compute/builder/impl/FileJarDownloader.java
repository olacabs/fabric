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

package com.olacabs.fabric.compute.builder.impl;

import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TODO javadoc.
 */
public class FileJarDownloader implements JarDownloader {

    public Path download(final String url) {
        URI uri = URI.create(url);
        if (!Files.exists(Paths.get(uri))) {
            throw new RuntimeException(new FileNotFoundException(Paths.get(uri).toAbsolutePath() + " does not exist"));
        } else {
            return Paths.get(URI.create(url));
        }
    }
}
