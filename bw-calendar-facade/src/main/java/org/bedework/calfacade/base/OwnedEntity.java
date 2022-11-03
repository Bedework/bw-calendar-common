package org.bedework.calfacade.base;

/**
 * User: mike Date: 11/1/22 Time: 13:28
 */
public interface OwnedEntity {
  /** Set the owner
   *
   * @param val     String owner path of the entity e.g. /principals/users/jim
   */
  void setOwnerHref(String val);

  /**
   *
   * @return String    owner of the entity
   */
  String getOwnerHref();

  /**
   * @param val public/not public
   */
  void setPublick(Boolean val);

  /**
   * @return Boolean true for public
   */
  Boolean getPublick();
}
