/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.lzx.extension_flac2120;

import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.util.LibraryLoader;

/**
 * Configures and queries the underlying native library.
 */
public final class FlacLibrary {

    static {
        ExoPlayerLibraryInfo.registerModule("goog.exo.flac");
    }

    private static final LibraryLoader LOADER = new LibraryLoader("libflacJNI") {
        /**
         * Should be implemented to call {@code System.loadLibrary(name)}.
         *
         * <p>It's necessary for each subclass to implement this method because {@link
         * System#loadLibrary(String)} uses reflection to obtain the calling class, which is then used to
         * obtain the class loader to use when loading the native library. If this class were to implement
         * the method directly, and if a subclass were to have a different class loader, then loading of
         * the native library would fail.
         *
         * @param name The name of the library to load.
         */
        @Override
        protected void loadLibrary(String name) {
            System.loadLibrary(name);
        }
    };

    private FlacLibrary() {
    }

    /**
     * Override the names of the Flac native libraries. If an application wishes to call this method,
     * it must do so before calling any other method defined by this class, and before instantiating
     * any {@link LibflacAudioRenderer} and {@link FlacExtractor} instances.
     *
     * @param libraries The names of the Flac native libraries.
     */
    public static void setLibraries(String... libraries) {
        LOADER.setLibraries(libraries);
    }

    /**
     * Returns whether the underlying library is available, loading it if necessary.
     */
    public static boolean isAvailable() {
        return LOADER.isAvailable();
    }

}
