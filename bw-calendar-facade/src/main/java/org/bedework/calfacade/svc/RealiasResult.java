/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calfacade.svc;

import org.bedework.calfacade.BwCategory;
import org.bedework.base.response.Response;

import java.util.Set;

/**
 * User: mike Date: 3/9/21 Time: 11:27
 */
public class RealiasResult  extends Response {
  private final Set<BwCategory> cats;

  public RealiasResult(final Set<BwCategory> cats) {
    this.cats = cats;
  }

  public Set<BwCategory> getCats() {
    return cats;
  }
}
