package com.dankegongyu.app.common.auth;

import com.dankegongyu.app.common.Current;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CurrentUser {

    public static void setUser(User user) {
        Current.set(CurrentUser.class.getName(), user);
    }

    public static User getUser() {
        return Current.get(CurrentUser.class.getName());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User<T> {
        Long id;
        String name;
        String email;
        String mobile;
        T originUser;
    }
}
