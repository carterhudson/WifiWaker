package com.dgaf.ch.wifiwaker;


abstract class Constants {
  static final  String PACKAGE                        = Constants.class.getPackage()
                                                                       .toString();
  static final  String KEY_PREFERENCES                = PACKAGE + "_PREFERENCES";
  public static String KEY_WHEN                       = PACKAGE + "_WHEN";
  static final  String KEY_COUNT                      = PACKAGE + "_COUNT";
  static        int    REQUEST_CODE_FROM_NOTIFICATION = 101;
  public static String ACTION_RESET                   = PACKAGE + ".action.RESET";
}
