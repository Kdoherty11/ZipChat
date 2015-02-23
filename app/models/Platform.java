package models;

import com.avaje.ebean.annotation.EnumValue;

/**
 * Created by kevindoherty on 2/22/15.
 */
public enum Platform {
    @EnumValue("android")
    android,

    @EnumValue("ios")
    ios
}
