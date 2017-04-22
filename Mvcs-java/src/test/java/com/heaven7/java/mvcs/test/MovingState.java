package com.heaven7.java.mvcs.test;

import com.heaven7.java.mvcs.SimpleState;

/**
 * Created by heaven7 on 2017/4/22.
 */
public class MovingState extends SimpleState<String> {

    private String getName(){
        return getClass().getSimpleName();
    }
    @Override
    public void onEnter() {
        super.onEnter();
        System.out.println(getName() + ": onEnter() " + "---> param = " + getStateParameter());
    }

    @Override
    public void onExit() {
        super.onExit();
        System.out.println(getName() + ": onExit() "+ "---> param = " + getStateParameter());
    }

    @Override
    public void onUpdate(String param) {
        super.onUpdate(param);
        System.out.println(getName() + ": onUpdate(). param = " + param);
    }

    @Override
    public void onReenter() {
        super.onReenter();
        System.out.println(getName() + ": onReenter() "+ "---> param = " + getStateParameter());
    }

    @Override
    public String toString() {
        return getName();
    }
}
