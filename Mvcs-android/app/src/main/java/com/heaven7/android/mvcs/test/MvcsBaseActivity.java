package com.heaven7.android.mvcs.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.heaven7.android.mvcs.AndroidController;
import com.heaven7.android.mvcs.AppComponentContext;
import com.heaven7.core.util.Toaster;

/**
 * this is a sample mvcs base activity.
 * Created by heaven7 on 2017/4/24 0024.
 */

public abstract class MvcsBaseActivity<C extends AndroidController>
        extends AppCompatActivity implements AppComponentContext{

    private C mController;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        onPreSetContentView();
        setContentView(getLayoutId());
        mController = createController();
       // mController = new AndroidController<MvcsBaseActivity>(this, Gravity.CENTER);
        mController.onCreate();
        onInitialize(this, savedInstanceState);
        mController.onHandleIntent(this, getIntent());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mController.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onPreSetContentView() {
        //often used to set flags.
    }

    @Override
    protected void onDestroy() {
        mController.dispose();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mController.onSaveInstanceState(outState);
    }

    public C getController(){
        return mController;
    }

    @Override
    public final Toaster getToaster() {
        return getController().getToaster();
    }

    protected abstract C createController();
}
