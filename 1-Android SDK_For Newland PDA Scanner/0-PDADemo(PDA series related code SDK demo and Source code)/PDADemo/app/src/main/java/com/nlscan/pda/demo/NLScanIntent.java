package com.nlscan.pda.demo;

import android.content.Intent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Company nlscan
 * @author Alan
 * @date 2017/12/16 23:02
 * @Description:
 */

public class NLScanIntent extends Intent {
    Map<String, Integer> paraMap;

    public NLScanIntent(String action, Map<String, Integer> para) {
        super(action);
        paraMap = (HashMap<String, Integer>) para;
        Iterator iter = paraMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String)entry.getKey();
            Integer val = (Integer)entry.getValue();
            putExtra(key,val);
        }
    }

    public NLScanIntent(String action, String paraKey, Integer paraValue){
        super(action);
        putExtra(paraKey,paraValue);

    }

    public NLScanIntent(String action, String paraKey, String paraValue){
        super(action);
        putExtra(paraKey,paraValue);

    }


    public NLScanIntent(String action){
        super(action);

    }
}
