/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calfacade;

import org.bedework.calfacade.annotations.NoDump;
import org.bedework.calfacade.base.OwnedEntity;
import org.bedework.calfacade.base.ShareableEntity;
import org.bedework.base.ToString;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * User: mike Date: 11/1/22 Time: 13:17
 */
public class BwShareablePrincipal<T extends BwPrincipal<?>>
        extends BwPrincipal<T>
        implements ShareableEntity, OwnedEntity {
  private String ownerHref;

  private Boolean publick;

  private String creatorHref;

  /** Encoded access rights
   */
  private String access;

  /* Non-db field */

  /** The user who created the entity */
  private BwPrincipal<?> creatorEnt;

  @Override
  public int getKind() {
    return -1;
  }

  /* ====================================================================
   *                   Bean methods
   * ==================================================================== */

  @Override
  public void setOwnerHref(final String val) {
    ownerHref = val;
  }

  @Override
  public String getOwnerHref() {
    return ownerHref;
  }

  @Override
  public void setPublick(final Boolean val) {
    publick = val;
  }

  @Override
  public Boolean getPublick() {
    return publick;
  }

  @Override
  public void setCreatorHref(final String val) {
    creatorHref = val;
  }

  @Override
  public String getCreatorHref() {
    return creatorHref;
  }

  @Override
  public void setAccess(final String val) {
    access = val;
  }

  @Override
  public String getAccess() {
    return access;
  }

  /* ====================================================================
   *                   Non-db methods
   * ==================================================================== */

  @Override
  @NoDump
  public void setCreatorEnt(final BwPrincipal<?> val) {
    creatorEnt = val;
  }

  @Override
  @JsonIgnore
  public BwPrincipal<?> getCreatorEnt() {
    return creatorEnt;
  }

  /* ====================================================================
   *                   Convenience methods
   * ==================================================================== */

  /** Add our stuff to the ToString object
   *
   * @param ts    ToString for result
   */
  @Override
  protected ToString toStringSegment(final ToString ts) {
    return super.toStringSegment(ts)
                .newLine()
                .append("creator", getCreatorHref())
                .append("access", getAccess());
  }

  /** Copy this objects fields into the parameter
   *
   * @param val Object to copy into
   */
  public void copyTo(final BwShareablePrincipal<?> val) {
    super.copyTo(val);
    val.setCreatorHref(getCreatorHref());
    // CLONE val.setCreator((BwUser)getCreator().clone());
    val.setAccess(getAccess());

    val.setCreatorEnt(getCreatorEnt());
  }

  @Override
  public Object clone() {
    final BwShareablePrincipal<?> sp = new BwShareablePrincipal<>();

    copyTo(sp);

    return sp;
  }
}
