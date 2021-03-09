/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calfacade.svc;

import org.bedework.caldav.util.sharing.SharedAsType;

/**
 * User: mike Date: 3/9/21 Time: 13:12
 */
public class SharingReplyResult {
  /** true for fine */
  private boolean ok;

  /** message if !ok */
  private String failMsg;

  /** Path to new alias */
  private SharedAsType sharedAs;

  /**
   * @param msg reason
   * @return a failure result
   */
  public static SharingReplyResult failed(final String msg) {
    final SharingReplyResult rr = new SharingReplyResult();

    rr.failMsg = msg;

    return rr;
  }

  /**
   * @param href display name for new sharee
   * @return a successful result
   */
  public static SharingReplyResult success(final String href) {
    final SharingReplyResult rr = new SharingReplyResult();

    rr.ok = true;
    rr.sharedAs = new SharedAsType(href);

    return rr;
  }

  /**
   * @return the ok flag
   */
  public boolean getOk() {
    return ok;
  }

  /**
   * @return the failure msg
   */
  public String getFailMsg() {
    return failMsg;
  }

  /**
   * @return the Sharedas object
   */
  public SharedAsType getSharedAs() {
    return sharedAs;
  }
}
