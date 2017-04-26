package com.heaven7.android.mvcs;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.heaven7.core.util.Toaster;
import com.heaven7.core.util.ViewHelper;

import java.util.List;

/**
 * the android mvcs context, define some useful method of android.
 * Created by heaven7 on 2017/4/24 0024.
 */

public interface AndroidMvcsContext {

    /**
     * get the view helper.
     * @return the view helper
     */
    ViewHelper getViewHelper();

    /**
     * get the toaster which used for toast.
     * @return the toaster.
     */
    Toaster getToaster();

    /**
     * get the owner activity.
     * @return the owner activity.
     */
    AppCompatActivity getActivity();
    /**
     * get the owner activity and cast it to the target type.
     * @param clazz  the target activity
     * @param <T> the target activity type
     * @return the target activity
     */
    <T extends AppCompatActivity>T getTargetActivity(Class<T> clazz);

    /**
     * get the context.
     * @return the context
     */
    Context getContext();

    /**
     * get the root view of activity.
     * @return the root view.
     */
    View getRootView();

    /**
     * get the max current states.
     * @return max current states.
     */
    AndroidState getCurrentState();

    /**
     * get the all current states.
     * @return the current states.
     */
    List<AndroidState> getCurrentStates();

    /**
     * get the android controller.
     * @return the android controller
     * @see  AndroidController
     */
    AndroidController getController();

}
