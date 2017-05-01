package com.heaven7.android.mvcs.test.state;

import android.os.Bundle;

import com.heaven7.android.mvcs.AndroidState;

/**
 * Created by heaven7 on 2017/4/22.
 */
public abstract class LogState extends AndroidState {

    private final LogOutput mOut;

    public LogState(LogOutput mOut) {
        this.mOut = mOut;
    }
    private String getName(){
        return getClass().getSimpleName();
    }
    @Override
    public void onEnter() {
        super.onEnter();
        mOut.log(getName() + ": onEnter() " + "---> param = " + getStateParameter());
       // System.out.println(getName() + ": onEnter() " + "---> param = " + getStateParameter());
    }

    @Override
    public void onExit() {
        super.onExit();
        mOut.log(getName() + ": onExit() " + "---> param = " + getStateParameter());
        //System.out.println(getName() + ": onExit() "+ "---> param = " + getStateParameter());
    }

    @Override
    public void onUpdate(Bundle param) {
        super.onUpdate(param);
        mOut.log(getName() + ": onUpdate(). param = " + param);
       // System.out.println(getName() + ": onUpdate(). param = " + param);
    }

    @Override
    public void onReenter() {
        super.onReenter();
        mOut.log(getName() + ": onReenter() "+ "---> param = " + getStateParameter());
        //System.out.println(getName() + ": onReenter() "+ "---> param = " + getStateParameter());
    }

    @Override
    public void onDispose() {
        mOut.log(getName() + ": onDispose() ");
    }

    @Override
    public String toString() {
        return getName();
    }

    public interface LogOutput{
        void log(String msg);
    }
}
