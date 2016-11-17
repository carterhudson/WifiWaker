package com.dgaf.ch.wifiwaker;


abstract class Constants {
  private static final String PACKAGE                        = Constants.class.getPackage()
                                                                              .toString();
  static final         String KEY_PREFERENCES                = PACKAGE + "_PREFERENCES";
  static final         String KEY_WHEN                       = PACKAGE + "_WHEN";
  static final         String KEY_COUNT                      = PACKAGE + "_COUNT";
  static final         String ACTION_RESET                   = PACKAGE + ".action.RESET";
  static final         String EMPTY_STRING                   = "";
  static final         int    REQUEST_CODE_FROM_NOTIFICATION = 101;
}
