package com.heaven7.android.mvcs;

/**
 * Created by heaven7 on 2017/4/24 0024.
 */

public interface LifeCycleComponent {

    void onCreate();

    void onStart();

    void onResume();

    void onPause();

    void onStop();

    void onDestroy();

    Object getTag();
}
