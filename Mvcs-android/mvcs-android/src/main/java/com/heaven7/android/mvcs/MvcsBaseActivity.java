package com.heaven7.android.mvcs;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;

/**
 * Created by heaven7 on 2017/4/24 0024.
 */

public abstract class MvcsBaseActivity extends AppCompatActivity implements AppComponentContext{

    private AndroidController<MvcsBaseActivity> mController;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        onPreSetContentView();
        setContentView(getLayoutId());
        mController = new AndroidController<MvcsBaseActivity>(this, Gravity.CENTER);
        mController.onCreate();
        mController.onRestoreInstanceState(savedInstanceState);
        if(!mController.onHandleIntent(this, getIntent())){
            finish();
        }
    }

    @Override
    public void onPreSetContentView() {
        //often used to set flags.
    }

    @Override
    protected void onDestroy() {
        mController.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mController.onSaveInstanceState(outState);
    }

    public final AndroidController<MvcsBaseActivity> getController(){
        return mController;
    }


}
