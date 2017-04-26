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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heaven7 on 2017/4/24 0024.
 */

public class AndroidController<A extends AppCompatActivity> extends SimpleController<AndroidState, Bundle>{

    private static final String KEY_BUNDLE = AndroidController.class.getName() + "_bundle";
    private static final String KEY_STATE_CACHE_ENABLED = "AndroidController_stateCacheEnabled";
    private static final String KEY_STATE_STACK_ENABLED = "AndroidController_stateStackEnabled";
    private static final String KEY_MAX_STATE_STACK     = "AndroidController_maxStateStackSize";
    private static final String KEY_LOCK_EVENTS         = "AndroidController_lockEvents";
    private static final String KEY_SHARE_PARAM         = "AndroidController_stateShareParam";
    private static final String KEY_CURRENT_STATE_FLAGS = "AndroidController_currentStateFlags";
    private static final String KEY_GLOBAL_STATE_FLAGS  = "AndroidController_globalStateFlags";


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
        super(activity);
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

    @SuppressWarnings("unchecked")
    public final A getActivity(){
        return (A) getOwner();
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
     * called on handle the intent.
     * @param context the context
     * @param intent the intent.
     */
    public void onHandleIntent(Context context, Intent intent){
    }

    /**
     * called on save instance state internal. often used if you want to save other data.
     * @param outState the out bundle
     */
    protected void onSaveInstanceStateInternal(Bundle outState){

    }

    /**
     * called on save instance state internal. often used if you want to restore other data.
     * @param saveInstanceState The previously saved instance state
     */
    protected void onRestoreInstanceStateInternal(Bundle saveInstanceState){

    }


    /**
     * Returns a Parcelable describing the current state of this controller.
     * It will be passed to the {@link #onRestoreInstanceState(Bundle)}
     * method of this controller sharing the same ID later.
     * @param  outState the out state to save
     * @return The saved instance state
     */
    public final void onSaveInstanceState(Bundle outState) {
        Bundle newBundle = new Bundle();
        if(isStateCacheEnabled()) {
            newBundle.putBoolean(KEY_STATE_CACHE_ENABLED, true);
        }
        if(isStateStackEnable()) {
            newBundle.putBoolean(KEY_STATE_STACK_ENABLED, true);
        }
        newBundle.putInt(KEY_MAX_STATE_STACK, getMaxStateStackSize());
        final List<Integer> events = getLockedEvents();
        if(events != null && events.size() > 0) {
            newBundle.putIntegerArrayList(KEY_LOCK_EVENTS, (ArrayList<Integer>) events);
        }
        Bundle shareParam = getShareStateParam();
        if(shareParam != null) {
            newBundle.putBundle(KEY_SHARE_PARAM, shareParam);
        }
        newBundle.putInt(KEY_CURRENT_STATE_FLAGS, getCurrentStateFlags());
        final int globalStateFlags = getGlobalStateFlags();
        if(globalStateFlags > 0) {
            newBundle.putInt(KEY_GLOBAL_STATE_FLAGS, globalStateFlags);
        }
        //save global states.
        List<AndroidState> states = getGlobalStates();
        if(states != null){
            for(AndroidState state : states){
                state.onSaveInstanceState(newBundle);
            }
        }
        //save current states
        states = getCurrentStates();
        if(states != null){
            for(AndroidState state : states){
                state.onSaveInstanceState(newBundle);
            }
        }
        onSaveInstanceStateInternal(newBundle);
        outState.putBundle(KEY_BUNDLE, newBundle);
    }

    /**
     * Supplies the previously saved instance state to be restored.
     *
     * @param saveInstanceState The previously saved instance state
     */
    public final void onRestoreInstanceState(@Nullable Bundle saveInstanceState) {
       if(saveInstanceState == null || !saveInstanceState.containsKey(KEY_BUNDLE)){
           return;
       }
        Bundle data = saveInstanceState.getBundle(KEY_BUNDLE);
        if(data != null) {
            setStateCacheEnabled(data.getBoolean(KEY_STATE_CACHE_ENABLED, false));
            setStateStackEnable(data.getBoolean(KEY_STATE_CACHE_ENABLED, false));
            int maxSize = data.getInt(KEY_MAX_STATE_STACK, 0);
            ArrayList<Integer> events = data.getIntegerArrayList(KEY_LOCK_EVENTS);
            Bundle shareParam = data.getBundle(KEY_SHARE_PARAM);
            int globalFlags = data.getInt(KEY_GLOBAL_STATE_FLAGS, 0);
            int currFlags = data.getInt(KEY_CURRENT_STATE_FLAGS, 0);

            if(maxSize > 0) {
                setMaxStateStackSize(maxSize);
            }
            if(events != null) {
                for (Integer val : events){
                    lockEvent(val);
                }
            }
            if(shareParam != null){
                setShareStateParam(shareParam);
            }
            if(currFlags > 0){
                setState(currFlags);
            }
            if(globalFlags > 0){
                setGlobalState(globalFlags);
            }
            //restore global states.
            List<AndroidState> states = getGlobalStates();
            if(states != null){
                for(AndroidState state : states){
                    state.onRestoreInstanceState(data);
                }
            }
            //restore current states
            states = getCurrentStates();
            if(states != null){
                for(AndroidState state : states){
                    state.onRestoreInstanceState(data);
                }
            }
            onRestoreInstanceStateInternal(data);
        }
    }


}