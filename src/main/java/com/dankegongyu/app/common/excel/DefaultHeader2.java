package com.dankegongyu.app.common.excel;

import com.dankegongyu.app.common.AbstractRegexOperator;
import com.dankegongyu.app.common.StringFormat;
import com.dankegongyu.app.common.Wrap;

import java.util.regex.Matcher;

public class DefaultHeader2 implements Header {
    private String key;

    public DefaultHeader2(String key) {
        this.key = key;
    }

    @Override
    public <T> T render(Object obj) {
        return Wrap.getWrap(obj).getValue(key);
    }
}
