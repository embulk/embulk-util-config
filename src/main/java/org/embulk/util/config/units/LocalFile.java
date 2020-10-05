/*
 * Copyright 2015 The Embulk project
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

package org.embulk.util.config.units;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.embulk.spi.Exec;
import org.embulk.spi.TempFileException;
import org.embulk.spi.TempFileSpace;

public class LocalFile {
    private LocalFile(final Path path, final byte[] content) {
        this.path = path;
        this.content = content;
    }

    private LocalFile(final byte[] content) {
        this.path = null;
        this.content = content;
    }

    public static LocalFile of(final File path) throws IOException {
        return of(path.toPath());
    }

    public static LocalFile of(final Path path) throws IOException {
        return new LocalFile(path, Files.readAllBytes(path));
    }

    public static LocalFile of(final String path) throws IOException {
        return of(Paths.get(path));
    }

    public static LocalFile ofContent(final byte[] content) {
        return new LocalFile(content);
    }

    public static LocalFile ofContent(final String content) {
        return new LocalFile(content.getBytes(StandardCharsets.UTF_8));
    }

    public File getFile() {
        return this.getPath(Exec.getTempFileSpace()).toFile();
    }

    public File getFile(final TempFileSpace space) {
        return this.getPath(space).toFile();
    }

    public Path getPath() {
        return this.getPath(Exec.getTempFileSpace());
    }

    public synchronized Path getPath(final TempFileSpace tempFileSpace) {
        if (this.path == null) {
            final Path temp = tempFileSpace.createTempFile().toPath();
            try {
                Files.write(temp, content);
            } catch (final IOException ex) {
                throw new TempFileException(ex);
            }
            this.path = temp;
        }
        return path;
    }

    public byte[] getContent() {
        return this.content;
    }

    public String getContentAsString() {
        return new String(this.content);
    }

    public String getContentAsString(final Charset charset) {
        return new String(this.content, charset);
    }

    public InputStream newContentInputStream() {
        return new ByteArrayInputStream(this.content);
    }

    private final byte[] content;

    private Path path;
}
