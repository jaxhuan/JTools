package com.jax.jtools.Util;

import com.jax.jtools.App;

import java.io.File;

/**
 * Created by userdev1 on 3/24/2017.
 */

public class FileUtil {

    public static File createCacheFile(String name) {
        final String cache_path = App.mContext.getFilesDir().getPath();
        return new File(cache_path + "/" + name);
    }
}
