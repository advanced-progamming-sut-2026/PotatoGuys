package com.pvz.utils;

import com.pvz.models.AppContext;
import com.pvz.models.user.User;

/** Central accessor for the Debug Mode toggle stored in the user's settings. */
public final class DebugMode {

    private DebugMode() {
    }

    /** True only when a logged-in user exists with the Debug Mode setting enabled. */
    public static boolean isEnabled() {
        User user = AppContext.getInstance().getCurrentUser();
        return user != null && user.getSetting() != null && user.getSetting().isDebugMode();
    }
}
