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
package org.bedework.calfacade;

import org.bedework.calfacade.annotations.NoDump;
import org.bedework.calfacade.annotations.ical.IcalProperty;
import org.bedework.calfacade.base.BwShareableContainedDbentity;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.base.ToString;
import org.bedework.util.misc.Uid;

/** Base for those classes that can be a property of an event and are all
 * treated in the same manner, being Category, Location and Sponsor.
 *
 * <p>Each has a single field which together with the owner makes a unique
 * key and all operations on those classes are the same.
 *
 * @author Mike Douglass
 * @version 1.0
 *
 * @param <T>
 */
public abstract class BwEventProperty<T> extends
        BwShareableContainedDbentity<T> {
  private String uid;

  /* Non-db fields */

  private float score;

  /** Constructor
   *
   */
  public BwEventProperty() {
    super();
  }

  /**
   * @return Finder Key value from this object.
   */
  public abstract BwString getFinderKeyValue();

  /** ColPath and href need to be derived
   */
  abstract void fixNames();

  public final static String statusDeleted = "deleted";

  /**
   * @return String
   */
  @IcalProperty(pindex = PropertyInfoIndex.STATUS)
  public abstract String getStatus();

  /**
   * @return String
   */
  @IcalProperty(pindex = PropertyInfoIndex.DELETED)
  public boolean getDeleted() {
    return statusDeleted.equals(getStatus());
  }

  /** Set the uid
   *
   * @param val    String uid
   */
  public void setUid(final String val) {
    uid = val;
  }

  /** Get the uid
   *
   * @return String   uid
   */
  public String getUid() {
    return uid;
  }

  /**
   * fill in the uid.
   *
   * @return this object
   */
  public BwEventProperty<T> initUid() {
    setUid(Uid.getUid());

    return this;
  }

  public void setScore(final float val) {
    score = val;
  }

  /**
   * @return score from search
   */
  @NoDump
  public float getScore() {
    return score;
  }

  @Override
  public String getColPath(){
    if (super.getColPath() == null) {
      fixNames();
    }
    return super.getColPath();
  }

  @Override
  public String getHref(){
    if (super.getHref() == null) {
      fixNames();
    }
    return super.getHref();
  }

  /* ====================================================================
   *                   Convenience methods
   * ==================================================================== */

  /** Copy this objects fields into the parameter
   *
   * @param val to copy to
   */
  public void copyTo(final BwEventProperty<?> val) {
    val.setUid(getUid());
  }

  @Override
  protected void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);
    ts.append("uid", getUid());
  }
}
