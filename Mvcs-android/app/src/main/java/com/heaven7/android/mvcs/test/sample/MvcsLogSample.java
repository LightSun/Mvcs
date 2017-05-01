package com.heaven7.android.mvcs.test.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.heaven7.android.mvcs.AndroidController;
import com.heaven7.android.mvcs.AndroidState;
import com.heaven7.android.mvcs.AndroidStateFactory;
import com.heaven7.android.mvcs.test.MvcsBaseActivity;
import com.heaven7.android.mvcs.test.R;
import com.heaven7.android.mvcs.test.state.LogState;
import com.heaven7.core.util.Logger;
import com.heaven7.core.util.ViewHelper;
import com.heaven7.core.util.viewhelper.action.Getters;

import java.util.List;
import java.util.Random;

import butterknife.OnClick;

/**
 * a log sample that only show log of Mvcs.
 * Created by heaven7 on 2017/5/1.
 */
public class MvcsLogSample extends MvcsBaseActivity<MvcsLogSample.LogController> {

    private static final String TAG = "MvcsLogSample";
    private static final Random RANDOM = new Random();

    private static final int STATE_EAT   = 1;
    private static final int STATE_WORK  = 2;
    private static final int STATE_SLEEP = 4;

    private static final int [] STATES = {STATE_EAT, STATE_WORK, STATE_SLEEP};
    private static final String [] STATE_STRS = {"STATE_EAT", "STATE_WORK", "STATE_SLEEP"};
    private int index;

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
         getController().setStateFactory(new StateFactoryImpl());
    }
    static class LogController extends AndroidController {
        public LogController(AppCompatActivity activity) {
            super(activity);
        }
    }

    private int randomState(){
        return RANDOM.nextInt(3);
    }

    @OnClick(R.id.bt_add)
    public void onClickAdd(View v){
        final int index = randomState();
        Logger.i(TAG, "onClickAdd", STATE_STRS[index]);
        getController().addState(STATES[index]);
    }
    @OnClick(R.id.bt_set)
    public void onClickSet(View v){
        final int index =  randomState();
        Logger.i(TAG, "onClickSet", STATE_STRS[index]);
        getController().setState(STATES[index]);
    }
    @OnClick(R.id.bt_remove)
    public void onClickRemove(View v){
        final int index =  randomState();
        Logger.i(TAG, "onClickRemove", STATE_STRS[index]);
        if(!getController().removeState(STATES[index])){
            syso("remove failed.", true);
        }else{
            syso("remove success.", true);
        }
    }
    @OnClick(R.id.bt_get)
    public void onClickGet(View v){
        final int index =  randomState();
        Logger.i(TAG, "onClickGet", STATE_STRS[index]);
        final List<AndroidState> states = getController().getCurrentStates();
        syso(states.toString(), true);
    }
    @OnClick(R.id.bt_mutex)
    public void onClickMutex(View v){
        final int index =  randomState();
        Logger.i(TAG, "onClickMutex", STATE_STRS[index]);
        //todo
    }
    @OnClick(R.id.bt_update)
    public void onClickUpdate(View v){

    }
    @OnClick(R.id.bt_dispose)
    public void onClickDispose(View v){

    }

    @OnClick(R.id.bt_lock_event)
    public void onClicklockEvent(View v){

    }
    @OnClick(R.id.bt_unlock_event)
    public void onClickUnlockEvent(View v){

    }

    private static class EatState extends LogState{
        public EatState(LogOutput mOut) {
            super(mOut);
        }
    }
    private static class SleepState extends LogState{
        public SleepState(LogOutput mOut) {
            super(mOut);
        }
    }
    private static class WorkState extends LogState{
        public WorkState(LogOutput mOut) {
            super(mOut);
        }
    }

    /**
     * @param msg 要显示的日志消息
     * @param clearBefore 是否清除之前日志
     */
    void syso(final String msg, final boolean clearBefore){
        getController().getViewHelper().performViewGetter(R.id.et_log, new Getters.EditTextGetter() {
            @Override
            public void onGotView(EditText view, ViewHelper vp) {
                if(clearBefore){
                    view.setText(msg + "\n");
                }else{
                    view.append(msg + "\n");
                }
                view.setSelection(view.getText().length());
            }
        });
    }

    private class StateFactoryImpl implements AndroidStateFactory{
        @Override
        public AndroidState createState(int stateKey, Bundle bundle) {
            LogState.LogOutput lot = new LogState.LogOutput() {
                public void log(String msg) {
                    syso(msg, false);
                }
            };
            switch (stateKey){
                case STATE_EAT:
                    return new EatState(lot);
                case STATE_WORK:
                    return new WorkState(lot);
                case STATE_SLEEP:
                    return new SleepState(lot);
                default:
                    throw new RuntimeException();
            }
        }
    }
}
