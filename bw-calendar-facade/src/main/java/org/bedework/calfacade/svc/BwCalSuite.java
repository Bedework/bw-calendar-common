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

import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwShareablePrincipal;
import org.bedework.calfacade.annotations.Dump;
import org.bedework.calfacade.annotations.NoDump;
import org.bedework.calfacade.util.FieldSplitter;
import org.bedework.util.misc.ToString;
import org.bedework.util.misc.Util;

/** This object represents a calendar suite in bedework. The calendar suites all
 * share common data but have their own set of preferences associated with a
 * run-as user.
 *
 * <p>All public views of the calendar system are based on the root calendar. At
 * the moment this generally points at the root of the calendar system but in a
 * hosted environment it would point to the root for the hosted organization.
 *
 *  @author Mike Douglass douglm@bedework.edu
 *  @version 1.0
 */
@Dump(elementName="cal-suite", keyFields={"name"})
public class BwCalSuite extends BwShareablePrincipal<BwCalSuite> {
  /** A unique name for the calendar suite. This name is mostly for internal
   * use and only presented to administrators. It must be unique for the system.
   */
  private String name;

  /** We can easily limit this name
   */
  public final static int maxNameLength = 255;

  /** The admin group which 'owns' this calendar suite
   */
  private BwAdminGroup group;

  private String groupHref;

  /** Was the root collection
   */
  private String fields1;
  private FieldSplitter fields1Split;

  /** Was the submissions root
   */
  private String fields2;
  private FieldSplitter fields2Split;

  /* properties packed into field1 and field2.
     Field1 was already populated with the  root collection -
             used but always the same, so eventually we might discard it.

     submissions root was unused.
   */

  public static final String fieldDelimiter = "\t";

  // Fields1
  private static final int rootCollectionPathIndex = 0;

  // Fields2
  private static final int descriptionIndex = 0;

  /* =================== Non-db fields ===================================== */

  /** The root collection
   */
  private BwCalendar rootCollection;

  private String context;

  private boolean defaultContext;

  /** Constructor
   *
   */
  public BwCalSuite() {
    super();
  }

  /* ==============================================================
   *                   Override principal method
   * ============================================================== */

  @Override
  public String getPrincipalRef() {
    return Util.buildPath(true,
                          BwPrincipal.calsuitePrincipalRoot,
                          "/", getName());
  }

  /* ==============================================================
   *                   Bean methods
   * ============================================================== */

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

  /** Set the owning group
   *
   * @param val    BwAdminGroup group
   */
  public void setGroup(final BwAdminGroup val) {
    group = val;
    if (val != null) {
      setGroupHref(val.getHref());
    }
  }

  /** Get the owning group
   *
   * @return BwAdminGroup   group
   */
  public BwAdminGroup getGroup() {
    return group;
  }

  /** Set the fields1 value
   *
   * @param val    String combined fields 1
   */
  public void setFields1(final String val) {
    fields1 = val;
  }

  /**
   *
   * @return String   combined fields 1
   */
  public String getFields1() {
    return fields1;
  }

  /** Set the submissions root path
   *
   * @param val    String combined fields 2
   */
  public void setFields2(final String val) {
    fields2 = val;
  }

  /** Get the combined fields 2
   *
   * @return String   combined fields 2
   */
  public String getFields2() {
    return fields2;
  }

  /* ====================================================================
   *                   Non-db methods
   * ==================================================================== */

  /** Set the owning group
   *
   * @param val    BwAdminGroup group
   */
  public void setGroupHref(final String val) {
    groupHref = val;
  }

  /** Get the owning group
   *
   * @return BwAdminGroup   group
   */
  public String getGroupHref() {
    return groupHref;
  }

  /** Set the root collection
   *
   * @param val    BwCalendar rootCalendar
   */
  @NoDump
  public void setRootCollection(final BwCalendar val) {
    rootCollection = val;
  }

  /** Get the root collection
   *
   * @return BwCalendar   rootCollection
   */
  @NoDump
  public BwCalendar getRootCollection() {
    return rootCollection;
  }

  /**
   * @return the name of the sub-context for this suite (may be null)
   */
  public String getContext() {
    return context;
  }

  /**
   * @param context the web context
   */
  public void setContext(final String context) {
    this.context = context;
  }

  /**
   * @return true if this is the default context
   */
  public boolean isDefaultContext() {
    return defaultContext;
  }

  /**
   * @param defaultContext the default web context
   */
  public void setDefaultContext(final boolean defaultContext) {
    this.defaultContext = defaultContext;
  }

  /* ====================================================================
   *          Fields in fields1
   * ==================================================================== */

  /** Set the root collection path
   *
   * @param val    String rootCalendar path
   */
  public void setRootCollectionPath(final String val) {
    assignFields1Field(rootCollectionPathIndex, val);
  }

  /** Get the root collection path
   *
   * @return String   rootCollection path
   */
  public String getRootCollectionPath() {
    return fetchFields1Split().getFld(rootCollectionPathIndex);
  }

  /* ====================================================================
   *          Fields in fields2
   * ==================================================================== */

  @Override
  public void setDescription(final String val) {
    assignFields2Field(descriptionIndex, val);
  }

  @Override
  public String getDescription() {
    return fetchFields2Split().getFld(descriptionIndex);
  }

  /** Add our stuff to the StringBuilder
   *
   * @param ts    StringBuilder for result
   */
   @Override
   protected void toStringSegment(final ToString ts) {
     super.toStringSegment(ts);

    ts.append("name", getName());
    ts.append("group", getGroup());
    ts.append("rootCollection", getRootCollectionPath());
    ts.append("description", getDescription());
  }

  private FieldSplitter fetchFields1Split() {
    if (fields1Split == null) {
      fields1Split = new FieldSplitter(fieldDelimiter);
      fields1Split.setVal(getFields1());
    }

    return fields1Split;
  }

  private void assignFields1Field(final int index, final String val) {
    fetchFields1Split().setFld(index, val);
    setFields1(fetchFields1Split().getCombined());
  }

  private FieldSplitter fetchFields2Split() {
    if (fields2Split == null) {
      fields2Split = new FieldSplitter(fieldDelimiter);
      fields2Split.setVal(getFields2());
    }

    return fields2Split;
  }

  private void assignFields2Field(final int index, final String val) {
    fetchFields2Split().setFld(index, val);
    setFields2(fetchFields2Split().getCombined());
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  @Override
  public int compare(final BwCalSuite cs1, final BwCalSuite cs2) {
    final var res = super.compare(cs1, cs2);
    if (res != 0) {
      return res;
    }

    return cs1.getName().compareTo(cs2.getName());
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }

  @Override
  public Object clone() {
    final BwCalSuite cs = new BwCalSuite();

    copyTo(cs);
    cs.setName(getName());
    cs.setGroup((BwAdminGroup)getGroup().clone());
    cs.setRootCollectionPath(getRootCollectionPath());
    cs.setDescription(getDescription());

    return cs;
  }
}
