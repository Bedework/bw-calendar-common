/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calfacade.svc;

import org.bedework.util.misc.response.GetEntityResponse;

/** Returned to show if an entity was added. entity is set to retrieved entity
 *
 * @param <T>
 * User: mike Date: 3/9/21 Time: 11:31
 */
public class EnsureEntityExistsResult<T>
        extends GetEntityResponse<T> {
  private boolean added;

  /** Was added */
  public boolean isAdded() {
    return added;
  }

  public void setAdded(boolean added) {
    this.added = added;
  }
}
