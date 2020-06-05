/*
 * Copyright 2020 The Embulk project
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

package org.embulk.util.config;

import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * A utility class defining a constant of Jackson's Module version.
 */
final class Version {
    static final com.fasterxml.jackson.core.Version JACKSON_MODULE_VERSION = getVersion();

    private Version() {
        // No instantiation.
    }

    static com.fasterxml.jackson.core.Version parseVersionForTesting(final String version) {
        return parseVersion(version);
    }

    private static com.fasterxml.jackson.core.Version getVersion() {
        try {
            return parseVersion(getImplementationVersion());
        } catch (final Throwable any) {  // It must not fail.
            return com.fasterxml.jackson.core.Version.unknownVersion();
        }
    }

    private static com.fasterxml.jackson.core.Version parseVersion(final String implementationVersion) {
        final String[] versionAndSnapshotInfo = implementationVersion.split("-", 2);
        final String version;
        final String snapshotInfo;
        if (versionAndSnapshotInfo.length == 1) {
            version = versionAndSnapshotInfo[0];
            snapshotInfo = null;
        } else if (versionAndSnapshotInfo.length == 2) {
            version = versionAndSnapshotInfo[0];
            snapshotInfo = versionAndSnapshotInfo[1];
        } else {
            throw new IllegalArgumentException("Empty version string.");
        }

        final String[] versionSplit = version.split("\\.");
        if (versionSplit.length == 1) {
            return new com.fasterxml.jackson.core.Version(
                    Integer.parseInt(versionSplit[0]),
                    0,
                    0,
                    snapshotInfo,
                    GROUP_ID,
                    ARTIFACT_ID);
        } else if (versionSplit.length == 2) {
            return new com.fasterxml.jackson.core.Version(
                    Integer.parseInt(versionSplit[0]),
                    Integer.parseInt(versionSplit[1]),
                    0,
                    snapshotInfo,
                    GROUP_ID,
                    ARTIFACT_ID);
        } else if (versionSplit.length == 3) {
            return new com.fasterxml.jackson.core.Version(
                    Integer.parseInt(versionSplit[0]),
                    Integer.parseInt(versionSplit[1]),
                    Integer.parseInt(versionSplit[2]),
                    snapshotInfo,
                    GROUP_ID,
                    ARTIFACT_ID);
        } else if (versionSplit.length == 0) {
            throw new IllegalArgumentException("Empty version.");
        } else {
            throw new IllegalArgumentException("3+ digits in version.");
        }
    }

    private static String getImplementationVersion() {
        try {
            return getManifestImplementationVersion();
        } catch (final Throwable any) {
            // Pass-through.
        }

        try {
            // Trying to get a version string from system properties. It is almost just for testing.
            return System.getProperty("org.embulk.embulk_util_config.version");
        } catch (final Throwable any) {
            // Pass-through.
        }

        return null;
    }

    private static String getManifestImplementationVersion() throws Exception {
        final URL selfJarUrl = Version.class.getProtectionDomain().getCodeSource().getLocation();
        if (selfJarUrl == null || !selfJarUrl.getProtocol().equals("file")) {
            throw new Exception();
        }

        final String selfJarPathString = selfJarUrl.getPath();
        if (selfJarPathString == null || selfJarPathString.isEmpty()) {
            throw new Exception();
        }

        final Manifest manifest;
        try (final JarFile selfJarFile = new JarFile(selfJarPathString)) {
            manifest = selfJarFile.getManifest();
        }
        if (manifest == null) {
            throw new Exception();
        }

        final Attributes mainAttributes = manifest.getMainAttributes();
        final String implementationVersion = mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        if (implementationVersion == null) {
            throw new Exception();
        }
        return implementationVersion;
    }

    private static final String GROUP_ID = "org.embulk";
    private static final String ARTIFACT_ID = "embulk-util-config";
}
