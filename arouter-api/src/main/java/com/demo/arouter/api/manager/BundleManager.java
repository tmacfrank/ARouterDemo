package com.demo.arouter.api.manager;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BundleManager {

    private Bundle mBundle;

    public BundleManager() {
        mBundle = new Bundle();
    }

    public BundleManager withString(@NonNull String key, @Nullable String value) {
        mBundle.putString(key, value);
        return this;
    }

    public BundleManager withInt(@NonNull String key, @Nullable int value) {
        mBundle.putInt(key, value);
        return this;
    }

    public BundleManager withBoolean(@NonNull String key, @Nullable boolean value) {
        mBundle.putBoolean(key, value);
        return this;
    }

    public BundleManager withBundle(@NonNull Bundle bundle) {
        mBundle = bundle;
        return this;
    }

    // 更多类型自己扩展...

    public Bundle getBundle() {
        return mBundle;
    }

    public Object navigation(Context context, int requestCode) {
        return ARouter.getInstance().navigation(context, this, requestCode);
    }

    public Object navigation(Context context) {
        return navigation(context, -1);
    }
}
