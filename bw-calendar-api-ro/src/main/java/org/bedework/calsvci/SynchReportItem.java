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
package org.bedework.calsvci;

import org.bedework.calfacade.BwCollection;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.util.misc.Util;

/** The changed entity may be an event, a resource or a collection. If it is
 * deleted then it will be marked as tombstoned.
 *
 * <p>The parent indicates what collection is visible in the hierarchy. It may
 * be an alias to the actual parent.
 *
 * @author douglm
 */
public class SynchReportItem implements Comparable<SynchReportItem> {
  /**
   */
  private final String token;

  /** Non-null if this is for an event */
  private EventInfo event;

  /** Non-null if this is for a resource - will not have its content */
  private BwResource resource;

  private BwCollection col;

  private final String vpath;

  /** true if we can provide sync info for this - usually false for aliases */
  private boolean canSync;

  /**
   * @param vpath the virtual path
   * @param event the event
   */
  public SynchReportItem(final String vpath,
                         final EventInfo event) {
    this.vpath = vpath;
    this.event = event;
    token = event.getEvent().getCtoken();
  }

  /**
   * @param vpath the virtual path
   * @param resource the resource
   */
  public SynchReportItem(final String vpath,
                         final BwResource resource) {
    this.vpath = vpath;
    this.resource = resource;
    token = resource.getEtagValue();
  }

  /**
   * @param vpath the virtual path
   * @param col the collection
   * @param canSync false if this cannot do sync report
   */
  public SynchReportItem(final String vpath,
                         final BwCollection col,
                         final boolean canSync) {
    this.vpath = vpath;
    this.col = col;
    this.canSync = canSync;
    token = col.getLastmod().getTagValue();
  }

  /** For an alias where the target lastmod is the token
   * 
   * @param vpath the virtual path
   * @param col the collection
   * @param canSync false if this cannot do sync report
   * @param token to use
   */
  public SynchReportItem(final String vpath,
                         final BwCollection col,
                         final boolean canSync,
                         final String token) {
    this.vpath = vpath;
    this.col = col;
    this.canSync = canSync;

    // token is the greater of the collection token or the supplied token.
    final String colToken =  col.getLastmod().getTagValue();
    
    if (colToken.compareTo(token) > 0) {
      this.token = colToken;
    } else {
      this.token = token;
    }
  }

  /**
   *
   * @return The token
   */
  public String getToken() {
    return token;
  }

  /** Non-null if this is for an event
   *
   * @return event or null
   */
  public EventInfo getEvent() {
    return event;
  }

  /** Non-null if this is for a resource
   *
   * @return resource or null
   */
  public BwResource getResource() {
    return resource;
  }

  /** Non-null if this is for a collection
   *
   * @return collection or null
   */
  public BwCollection getCol() {
    return col;
  }

  /** Always non-null - virtual path to the element this object represents (not
   * including this elements name).
   *
   * <p>For example, if (x) represents a collection x and [x] represents an alias
   * x then for element c we have:<pre>
   * (a)->(b)->(c) has the vpath and path a/b/c
   * while
   * (a)->[b]
   *       |
   *       v
   * (x)->(y)->(c) has the vpath a/b/c and path) x/y/c
   * </pre>
   *
   * @return parent collection
   */
  public String getVpath() {
    return vpath;
  }

  /** False if we can't do a direct sync report.
   *
   * @return boolean
   */
  public boolean getCanSync() {
    return canSync;
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final SynchReportItem that) {
    int res = token.compareTo(that.token);
    if (res != 0) {
      return res;
    }

    res = Util.cmpObjval(this.getResource(), that.getResource());
    if (res != 0) {
      return res;
    }

    res = Util.cmpObjval(this.getEvent(), that.getEvent());
    if (res != 0) {
      return res;
    }

    return Util.cmpObjval(this.getCol(), that.getCol());
  }

  @Override
  public int hashCode() {
    return token.hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof SynchReportItem)) {
      return false;
    }
    return compareTo((SynchReportItem)o) == 0;
  }
}