package com.heaven7.android.mvcs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.heaven7.core.util.Toaster;

/**
 * the component context.
 * Created by heaven7 on 2017/3/3.
 */
public interface AppComponentContext {


    void onPreSetContentView();

    /**
     * get the layout id.
     * @return the layout id
     */
     int getLayoutId();

    /**
     * get the toaster.
     * @return  the {@link Toaster}
     */
    Toaster getToaster();

    /**
     * on initialize
     * @param context the context
     * @param savedInstanceState the bundle of save instance
     */
     void onInitialize(Context context, @Nullable Bundle savedInstanceState);

}
