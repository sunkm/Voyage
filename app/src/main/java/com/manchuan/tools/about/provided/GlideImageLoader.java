package com.manchuan.tools.about.provided;

import androidx.annotation.NonNull;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.manchuan.tools.about.ImageLoader;

/**
 * @author drakeet
 */
public class GlideImageLoader implements ImageLoader {

  @Override
  public void load(@NonNull ImageView imageView, @NonNull String url) {
    Glide.with(imageView.getContext()).load(url).into(imageView);
  }
}
