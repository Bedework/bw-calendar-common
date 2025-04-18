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
package org.bedework.calfacade.svc;

import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.BwCollection;
import org.bedework.calfacade.annotations.Dump;
import org.bedework.calfacade.annotations.NoDump;
import org.bedework.calfacade.base.BwDbentity;
import org.bedework.base.ToString;

import java.util.ArrayList;
import java.util.List;

/** A view in Bedework. This is a named collection of collections used to
 * provide different views of the events.
 *
 * @author Mike Douglass douglm  rpi.edu
 */
@Dump(elementName="view", keyFields={"owner", "name"})
public class BwView extends BwDbentity<BwView> {
  /** A printable name for the view
   */
  private String name;

  /** The collection paths
   */
  private List<String> collectionPaths;

  /* ============================================================
   *                   Non db fields
   * ============================================================ */

  // Non db for the moment
  private boolean conjunction;

  private FilterBase filter;

  /** The collections
   */
  private List<BwCollection> collections;

  /** Constructor
   *
   */
  public BwView() {
    super();
  }

  /* ============================================================
   *                   Bean methods
   * ============================================================ */

  /** Set the name
   *
   * @param val    String name
   */
  public void setName(final String val) {
    name = val;
  }

  /** Get the name
   *
   * @return String   name
   */
  public String getName() {
    return name;
  }

  /** List of collection paths for this view
   *
   * @param val        List of string paths
   */
  public void setCollectionPaths(final List<String> val) {
    collectionPaths = val;
  }

  /** Get the collection paths for this view
   *
   * @return List    of String
   */
  @Dump(collectionElementName = "path")
  public List<String> getCollectionPaths() {
    return collectionPaths;
  }

  /* ====================================================================
   *                   Convenience methods
   * ==================================================================== */

  /** Add a collection path
   *
   * @param val a collection path
   */
  public void addCollectionPath(final String val) {
    List<String> c = getCollectionPaths();
    if (c == null) {
      c = new ArrayList<>();
      setCollectionPaths(c);
    }
    c.add(val);
  }

  /** Remove a collection path
   *
   * @param val a collection path
   */
  public void removeCollectionPath(final String val) {
    final List<String> c = getCollectionPaths();
    if (c != null) {
      c.remove(val);
    }
  }

  /* ============================================================
   *                   Non db methods
   * ============================================================ */

  /**
   *
   * @param val true if we AND the expressions for each path
   */
  public void setConjunction(final boolean val) {
    conjunction = val;
  }

  /**
   * @return true if we AND the expressions for each path
   */
  @NoDump
  public boolean getConjunction() {
    return conjunction;
  }

  /**
   *
   * @param val the filter or null
   */
  public void setFilter(final FilterBase val) {
    filter = val;
  }

  /**
   *
   * @return the filter or null
   */
  @NoDump
  public FilterBase getFilter() {
    return filter;
  }

  /**
   *
   * @param val the list of collections or null
   */
  public void setCollections(final List<BwCollection> val) {
    collections = val;
  }

  /**
   */
  @NoDump
  public List<BwCollection> getCollections() {
    return collections;
  }

  /* ============================================================
   *                   Object methods
   * ============================================================ */

  /** Comapre this view and an object
   *
   * @param  that    object to compare.
   * @return int -1, 0, 1
   */
  @Override
  public int compareTo(final BwView that) {
    if (that == this) {
      return 0;
    }

    if (that == null) {
      return -1;
    }

    return getName().compareTo(that.getName());
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    toStringSegment(ts);

    ts.append("name", getName());
    ts.append("collectionPaths", getCollectionPaths());

    return ts.toString();
  }
}
