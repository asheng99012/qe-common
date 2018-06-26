package com.dankegongyu.app.common.excel;

import com.dankegongyu.app.common.AbstractRegexOperator;
import com.dankegongyu.app.common.StringFormat;
import com.dankegongyu.app.common.Wrap;

import java.util.regex.Matcher;

public class DefaultHeader implements Header {
    private String key;

    public DefaultHeader(String key) {
        this.key = key;
    }

    @Override
    public String render(Object obj) {
        return StringFormat.format(key, new AbstractRegexOperator() {
            @Override
            public String getPattern() {
                return "\\{([^\\}]+)\\}";
            }

            @Override
            public String exec(Matcher m) {
                return Wrap.getWrap(obj).getValue(m.group(1));
            }
        });
    }
}
