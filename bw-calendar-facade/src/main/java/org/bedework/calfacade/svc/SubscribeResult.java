/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calfacade.svc;

/**
 * User: mike Date: 3/9/21 Time: 13:15
 */
public class SubscribeResult {
  private String path;

  private boolean alreadySubscribed;

  /** Path to alias */
  public String getPath() {
    return path;
  }

  public void setPath(final String val) {
    path = val;
  }

  /** True if user was already subscribed */
  public boolean isAlreadySubscribed() {
    return alreadySubscribed;
  }

  public void setAlreadySubscribed(final boolean val) {
    alreadySubscribed = val;
  }
}
