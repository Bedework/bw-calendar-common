/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calfacade.util;

/**
 * User: mike Date: 12/27/21 Time: 00:33
 */
public class Sysprop {
  public static boolean readOnlySystem() {
    return Boolean.getBoolean("org.bedework.readonly-system");
  }
}
