package com.heaven7.android.mvcs;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.heaven7.core.util.Toaster;
import com.heaven7.core.util.ViewHelper;

import java.util.List;

/**
 * Created by heaven7 on 2017/4/24 0024.
 */

public class AndroidContext<A extends AppCompatActivity, C extends AndroidController> {

    private ViewHelper mViewHelper;
    private Toaster mToaster;
    private A mActivity;

    public final ViewHelper getViewHelper(){
        return mViewHelper;
    }

    public final Toaster getToaster() {
        return mToaster;
    }

    public A getActivity(){
        return mActivity;
    }

    public Context getContext() {
        return getViewHelper().getContext();
    }

    public View getRootView() {
        return getViewHelper().getRootView();
    }

    public C getController(){
        return null;
    }

    public AndroidState getCurrentState(){
        return getController().getCurrentState();
    }

    public List<AndroidState> getCurrentStates(){
        return getController().getCurrentStates();
    }

    //getState ?

}
