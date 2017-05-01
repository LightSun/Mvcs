package com.heaven7.android.mvcs.test.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.heaven7.android.mvcs.AndroidController;
import com.heaven7.android.mvcs.test.MvcsBaseActivity;
import com.heaven7.android.mvcs.test.R;

/**
 * a log sample that only show log of Mvcs.
 * Created by heaven7 on 2017/5/1.
 */
public class MvcsLogSample extends MvcsBaseActivity<MvcsLogSample.LogController> {

    @Override
    protected LogController createController() {
        return new LogController(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.ac_mvcs_log;
    }

    @Override
    public void onInitialize(Context context, @Nullable Bundle savedInstanceState) {

    }

    static class LogController extends AndroidController{
        public LogController(AppCompatActivity activity) {
            super(activity);
        }
    }

}
