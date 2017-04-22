package com.heaven7.java.mvcs.test;

import com.heaven7.java.mvcs.ParameterMerger;

/**
 * Created by heaven7 on 2017/4/22.
 */
public class ParamepterMergerImpl implements ParameterMerger<String> {
    @Override
    public String merge(String t1, String t2) {
        if(t1 != null){
            return t2 != null ? t1 + "__" + t2 : t1;
        }
        return t2;
    }
}
