package com.heaven7.android.mvcs;

import android.os.Bundle;

import com.heaven7.java.mvcs.SimpleState;

/**
 * Created by heaven7 on 2017/4/24 0024.
 */

public class AndroidState extends SimpleState<Bundle> {


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
     * @param saveInstanceState The previously saved instance state. can't be null.
     */
    public void onRestoreInstanceState(Bundle saveInstanceState) {

    }


}
