package com.dankegongyu.app.common.canal;

import com.dankegongyu.app.common.mq.Proxy;

public interface Process {
    default void doProcess(Message message) {
        if (canDoProcess(message)) {
            if (message.isInsert())
                insert(message);
            if (message.isUpdate())
                update(message);
            if (message.isDelete())
                delete(message);
        }
    }

    default boolean canDoProcess(Message message) {
        return true;
    }

    public void insert(Message message);

    public void delete(Message message);

    public void update(Message message);
}
