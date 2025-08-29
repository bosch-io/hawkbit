/**
 * Copyright (c) 2025 Bosch Digital GmbH, Germany. All rights reserved.
 */
package org.eclipse.hawkbit.repository.artifact;

import java.io.IOException;
import java.io.InputStream;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = false)
public class ArtifactStream extends InputStream {

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final InputStream inputStream;
    @Getter
    private final long size;
    @Getter
    private final String sha1Hash;

    public ArtifactStream(InputStream inputStream, long size, String sha1Hash) {
        this.inputStream = inputStream;
        this.size = size;
        this.sha1Hash = sha1Hash;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}