package com.manchuan.tools.about;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author drakeet
 */
public class Contributor {

    public @DrawableRes
    int avatarResId = 0;
    public @NonNull
    final String name;
    public @NonNull
    final String desc;
    public @Nullable String url;

    public @NonNull
    String avatarUrl;

    public Contributor(@DrawableRes int avatarResId, @NonNull String name, @NonNull String desc) {
        this(avatarResId, name, desc, null);
    }

    public Contributor(String avatarUrl, @NonNull String name, @NonNull String desc) {
        this(avatarUrl, name, desc, null);
    }

    public Contributor(
            @DrawableRes int avatarResId,
            @NonNull String name,
            @NonNull String desc,
            @Nullable String url) {
        this.avatarResId = avatarResId;
        this.name = name;
        this.desc = desc;
        this.url = url;
    }

    public Contributor(
            @NonNull String avatarUrl,
            @NonNull String name,
            @NonNull String desc,
            @Nullable String url) {
        this.avatarUrl = avatarUrl;
        this.name = name;
        this.desc = desc;
        this.url = url;
    }
}