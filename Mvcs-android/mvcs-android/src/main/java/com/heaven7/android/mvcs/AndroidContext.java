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

public class AndroidContext<C extends AndroidController<A>, A extends AppCompatActivity> {

    private C mController;

    void onAttachController(C controller){
        this.mController = controller;
    }

    void onDetach(){
        mController = null;
    }

    public final ViewHelper getViewHelper(){
        return getController().getViewHelper();
    }
    public final Toaster getToaster() {
        return getController().getToaster();
    }
    public final A getActivity(){
        return getController().getActivity();
    }
    public final  Context getContext() {
        return getViewHelper().getContext();
    }
    public final View getRootView() {
        return getViewHelper().getRootView();
    }
    public final C getController(){
        return mController;
    }
    public final AndroidState getCurrentState(){
        return getController().getCurrentState();
    }
    public final List<AndroidState> getCurrentStates(){
        return getController().getCurrentStates();
    }

}
