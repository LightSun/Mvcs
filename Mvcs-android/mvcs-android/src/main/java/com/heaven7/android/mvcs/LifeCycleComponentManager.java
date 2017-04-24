package com.heaven7.android.mvcs;

/**
 * Created by heaven7 on 2017/4/24 0024.
 */

public interface LifeCycleComponentManager {

    int STAGE_CREATE = 1;
    int STAGE_START = 1;
    int STAGE_RESUME = 1;
    int STAGE_PAUSE      = 1;
    int STAGE_STOP     = 1;
    int STAGE_DESTROY  = 1;

    /**
     * get component by target tag
     *
     * @param tag the tag
     * @return the component.
     */
    LifeCycleComponent getComponentBy(Object tag);

    /**
     * register the life cycle component
     *
     * @param c the life cycle component
     */
    void registerComponent(LifeCycleComponent c);


    /**
     * unregister the life cycle component
     *
     * @param c the life cycle component
     */
    void unregisterComponent(LifeCycleComponent c);


    /**
     * unregister the last {@linkplain LifeCycleComponent}.
     */
    void unregisterLastComponent();


    /**
     * unregister the life cycle component by target tag.
     *
     * @param tag the tag which to find life cycle component
     */
    void unregisterComponentBy(Object tag);


    void callActivityOnCreate();
    void callActivityOnStart();
    void callActivityOnResume();
    void callActivityOnPause();
    void callActivityOnStop();
    void callActivityOnDestroy();

}
