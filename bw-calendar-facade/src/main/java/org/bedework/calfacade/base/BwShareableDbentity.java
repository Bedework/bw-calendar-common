/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/

package org.bedework.calfacade.base;

import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.annotations.NoDump;
import org.bedework.calfacade.annotations.ical.IcalProperty;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.base.ToString;

import com.fasterxml.jackson.annotation.JsonIgnore;

/** Base class for shareable database entities.
 *
 * @author Mike Douglass
 * @version 1.0
 *
 * @param <T>
 */
public abstract class BwShareableDbentity<T>
        extends BwOwnedDbentity<T>
        implements ShareableEntity {
  private String creatorHref;

  /** Encoded access rights
   */
  private String access;

  /* Non-db field */

  /** The user who created the entity */
  private BwPrincipal<?> creatorEnt;

  /** No-arg constructor
   *
   */
  public BwShareableDbentity() {
    super();
  }

  @IcalProperty(pindex = PropertyInfoIndex.CREATOR,
                required = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true,
                freeBusyProperty = true,
                vavailabilityProperty = true)
  @Override
  public void setCreatorHref(final String val) {
    creatorHref = val;
  }

  @Override
  public String getCreatorHref() {
    return creatorHref;
  }

  @Override
  @IcalProperty(pindex = PropertyInfoIndex.ACL,
                jname = "acl",
                required = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true,
                freeBusyProperty = true,
                vavailabilityProperty = true)
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
  protected void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);
    ts.newLine();
    ts.append("creator", getCreatorHref());
    ts.append("access", getAccess());
  }

  /** Copy this objects fields into the parameter. Don't clone many of the
   * referenced objects
   *
   * @param val Object to copy into
   */
  public void shallowCopyTo(final BwShareableDbentity<?> val) {
    super.shallowCopyTo(val);
    val.setCreatorHref(getCreatorHref());
    val.setAccess(getAccess());

    val.setCreatorEnt(getCreatorEnt());
  }

  /** Copy this objects fields into the parameter
   *
   * @param val Object to copy into
   */
  public void copyTo(final BwShareableDbentity<?> val) {
    super.copyTo(val);
    val.setCreatorHref(getCreatorHref());
    // CLONE val.setCreator((BwUser)getCreator().clone());
    val.setAccess(getAccess());

    val.setCreatorEnt(getCreatorEnt());
  }
}
