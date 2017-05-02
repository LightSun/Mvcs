package com.heaven7.android.mvcs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.heaven7.core.util.Toaster;
import com.heaven7.core.util.ViewHelper;
import com.heaven7.java.mvcs.SimpleState;

import java.util.List;

/**
 * the android state.
 * Created by heaven7 on 2017/4/24 0024.
 * @see com.heaven7.java.mvcs.AbstractState
 * @see SimpleState
 * @see AndroidMvcsContext
 */
public class AndroidState extends SimpleState<Bundle> implements AndroidMvcsContext{

    @SuppressWarnings("unchecked")
    @Override
    public final AndroidController getController() {
        return (AndroidController)super.getController();
    }
    @Override
    public final ViewHelper getViewHelper(){
        return getController().getViewHelper();
    }
    @Override
    public final Toaster getToaster() {
        return getController().getToaster();
    }
    @Override
    public final AppCompatActivity getActivity(){
        return getController().getActivity();
    }
    @SuppressWarnings("unchecked")
    @Override
    public final <T extends AppCompatActivity> T getTargetActivity(Class<T> clazz) {
        return (T) getActivity();
    }
    @Override
    public final Context getContext() {
        return getViewHelper().getContext();
    }
    @Override
    public final View getRootView() {
        return getViewHelper().getRootView();
    }
    @Override
    public final AndroidState getCurrentState(){
        return  getController().getCurrentState();
    }
    @SuppressWarnings("unchecked")
    @Override
    public final List<AndroidState> getCurrentStates(){
        return getController().getCurrentStates();
    }
    /**
     * Returns a Parcelable describing the current state of this controller.
     * It will be passed to the {@link #onRestoreInstanceState(Bundle)}
     * method of this controller sharing the same ID later.
     * @param  outState the out state to save
     */
    public void onSaveInstanceState(Bundle outState) {

    }

    /**
     * Supplies the previously saved instance state to be restored.
     *
     * @param saveInstanceState The previously saved instance state. can't be null.
     */
    public void onRestoreInstanceState(Bundle saveInstanceState) {

    }


}
