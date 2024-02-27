package org.bedework.calfacade.base;

import org.bedework.calfacade.BwPrincipal;

/** Base interface for shareable entities.
 * <p>
 * User: mike Date: 11/1/22 Time: 13:10
 */
public interface ShareableEntity extends OwnedEntity {
  /** Set the creator
   *
   * @param val     String creator of the entity
   */
  void setCreatorHref(String val);

  /**
   *
   * @return BwUser    creator of the entity
   */
  String getCreatorHref();

  /** Set the access
   *
   * @param val    String access
   */
  void setAccess(String val);

  /** Get the access
   *
   * @return String   access
   */
  String getAccess();

  /* ====================================================================
   *                   Non-db methods
   * ==================================================================== */

  /** Set the creator
   *
   * @param val     BwPrincipal creator of the entity
   */
  void setCreatorEnt(BwPrincipal<?> val);

  /**
   *
   * @return BwPrincipal    creator of the entity
   */
  BwPrincipal<?> getCreatorEnt();
}
