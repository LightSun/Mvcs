package com.heaven7.android.mvcs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;

import com.heaven7.core.util.Toaster;
import com.heaven7.core.util.ViewHelper;
import com.heaven7.java.mvcs.ParameterMerger;
import com.heaven7.java.mvcs.SimpleController;

import java.util.List;

/**
 * Created by heaven7 on 2017/4/24 0024.
 */

public class AndroidController<A extends AppCompatActivity> extends SimpleController<AndroidState, Bundle>{


    private static final ParameterMerger<Bundle> BUNDLE_MERGER =  new ParameterMerger<Bundle>() {
        @Override
        public Bundle merge(Bundle t1, Bundle t2) {
            if(t1 == null){
                return t2;
            }
            if(t2 != null){
                t1.putAll(t2);
            }
            return t1;
        }
    };

    private final A mActivity;
    private final Toaster mToaster;
    private final ViewHelper mViewHelper;

    public AndroidController(A activity){
        this(activity, Gravity.CENTER);
    }

    /**
     * create AndroidController .
     * @param activity the activity
     * @param gravity the gravity. {@linkplain android.view.Gravity#CENTER} and etc.
     */
    public AndroidController(A activity, int gravity) {
        super();
        this.mActivity = activity;
        this.mToaster = new Toaster(activity, gravity);
        this.mViewHelper = new ViewHelper(activity.getWindow().getDecorView());
        setParameterMerger(BUNDLE_MERGER);
    }

    public final ViewHelper getViewHelper(){
        return mViewHelper;
    }

    public final Toaster getToaster() {
        return mToaster;
    }

    public final A getActivity(){
        return mActivity;
    }

    public final Context getContext() {
        return getViewHelper().getContext();
    }

    public final View getRootView() {
        return getViewHelper().getRootView();
    }

    //=======================================================================//

    /**
     * called on create controller.
     */
    public void onCreate(){

    }

    /**
     * called on destroy controller.
     */
    public void onDestroy(){
        List<AndroidState> states = getCurrentStates();
        if(states != null) {
            for (AndroidState as : states) {
                as.onDestroy();
            }
        }
       states = getGlobalStates();
       if(states != null) {
            for (AndroidState as : states) {
                as.onDestroy();
            }
        }
    }

    /**
     * handle the intent. if return false ,the activity will be finished.
     * @param context the context
     * @param intent the intent.
     * @return true if handle the intent correctly. false otherwise.
     */
    public boolean onHandleIntent(Context context, Intent intent){

        return true;
    }


    /**
     * Returns a Parcelable describing the current state of this controller.
     * It will be passed to the {@link #onRestoreInstanceState(Bundle)}
     * method of this controller sharing the same ID later.
     * @param  outState the out state to save
     * @return The saved instance state
     */
    public void onSaveInstanceState(Bundle outState) {
    }

    /**
     * Supplies the previously saved instance state to be restored.
     *
     * @param saveInstanceState The previously saved instance state
     */
    public void onRestoreInstanceState(@Nullable Bundle saveInstanceState) {

    }

}
