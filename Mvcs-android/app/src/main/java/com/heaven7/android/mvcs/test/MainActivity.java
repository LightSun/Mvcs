package com.heaven7.android.mvcs.test;

import com.heaven7.android.mvcs.test.sample.MvcsLogSample;

import java.util.List;

/**
 * samples of mvcs.
 */
public class MainActivity extends AbsMainActivity {
    @Override
    protected void addDemos(List<ActivityInfo> list) {
        list.add(new ActivityInfo(MvcsLogSample.class, "only show log"));
        list.add(new ActivityInfo(ScrollingActivity.class, "ScrollingActivity"));
    }
}
