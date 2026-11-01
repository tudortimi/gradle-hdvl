/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.verificationgentleman.gradle.hdvl

import java.nio.file.Files
import java.nio.file.Path

final class TestProjectDir {
    private TestProjectDir() {
    }

    static File newFile(File root, String... path) {
        Path file = resolve(root, path)
        Files.createDirectories(file.parent)
        Files.createFile(file).toFile()
    }

    static File newFolder(File root, String... path) {
        Files.createDirectories(resolve(root, path)).toFile()
    }

    private static Path resolve(File root, String... path) {
        path.inject(root.toPath()) { Path current, String segment -> current.resolve(segment) }
    }
}
