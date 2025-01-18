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

import org.bedework.calfacade.util.Sysprop;
import org.bedework.base.ToString;

import java.io.Serializable;

/** These are global parameters used by the CalSvc interface.
 *
 * @author Mike Douglass       douglm  rpi.edu
 */
public class CalSvcIPars implements Serializable {
  /** The id prefix for the ro client
   */
  public final static String logIdRoClient = "roclient-";

  /** The id prefix for the rw client
   */
  public final static String logIdRwClient = "rwclient-";

  /** The id prefix for the admin client
   */
  public final static String logIdAdminClient = "admin-client-";

  /** The id for the tools util process
   */
  public final static String logIdTools = "tools";

  /** The id for the indexer process
   */
  public final static String logIdIndexer = "indexer";

  /** The id for the dump process
   */
  public final static String logIdDump = "dump";

  /** The id for the restore process
   */
  public final static String logIdRestore = "restore";

  /** The authenticated user - null for guest
   */
  private String authUser;

  /** The current user we run as. null to use calSuite
   */
  private final String user;

  /** The calendar suite name. user and calSuite null for guest.
   */
  private String calSuite;

  /** True if this is for public admin
   */
  private final boolean publicAdmin;

  /** True if this is for authenticated public client
   */
  private final boolean publicAuth;

  /** True if this is for public event submission
   */
  private final boolean publicSubmission;

  /** True if this is for a background service
   */
  private final boolean service;

  /** The clientid from headers.
   */
  private final String clientId;

  /** Id for logging.
   */
  private String logId;

  /** True if we should allow super user mode in non public admin
   */
  private final boolean allowSuperUser;

  private final boolean adminCanEditAllPublicCategories;
  private final boolean adminCanEditAllPublicLocations;
  private final boolean adminCanEditAllPublicContacts;

  /** True if this is a sessionless client, e.g. caldav or rss
   */
  private final boolean sessionless;

  /** True if this is a system process, e.g. restore
   */
  private final boolean dontKill;

  private final boolean forRestore;

  private final boolean indexRebuild;

  private boolean readonly;

  /** True if this is a web application
   */
  private boolean webMode;

  /** Constructor for this object.
   *
   * @param authUser    String authenticated user of the application
   * @param user        String user to act as
   * @param calSuite    String calSuite name
   * @param publicAdmin true for admin
   * @param publicAuth  true for authenticated public client
   * @param allowSuperUser  true to allow superuser mode in non-admin mode
   * @param service     true for a service
   * @param publicSubmission true for the submit app
   * @param adminCanEditAllPublicCategories true/false
   * @param adminCanEditAllPublicLocations true/false
   * @param adminCanEditAllPublicContacts true/false
   * @param sessionless true if this is a sessionless client
   * @param dontKill true if this is a system process
   * @param readonly true if this is a readonly process
   */
  public CalSvcIPars(final String logId,
                     final String authUser,
                     final String user,
                     final String calSuite,

                     final boolean publicAdmin,
                     final boolean publicAuth,
                     final boolean allowSuperUser,
                     final boolean service,
                     final boolean publicSubmission,

                     final boolean adminCanEditAllPublicCategories,
                     final boolean adminCanEditAllPublicLocations,
                     final boolean adminCanEditAllPublicContacts,

                     final boolean sessionless,
                     final boolean dontKill,
                     final boolean readonly,
                     final String clientId,
                     final boolean forRestore,
                     final boolean indexRebuild) {
    this.logId = logId;
    this.authUser = authUser;
    this.calSuite = calSuite;
    this.publicAdmin = publicAdmin;
    this.publicAuth = publicAuth;
    this.allowSuperUser = allowSuperUser;
    this.service = service;
    this.adminCanEditAllPublicCategories = adminCanEditAllPublicCategories;
    this.adminCanEditAllPublicLocations = adminCanEditAllPublicLocations;
    this.adminCanEditAllPublicContacts = adminCanEditAllPublicContacts;
    this.sessionless = sessionless;
    this.dontKill = dontKill;
    this.user = user;
    this.publicSubmission = publicSubmission;
    this.readonly = readonly;

    this.clientId = clientId;
    this.forRestore = forRestore;
    this.indexRebuild = indexRebuild;
  }

  public static CalSvcIPars getRoClientPars(
          final String id,
          final String authUser,
          final String user,
          final String calSuite,
          final boolean publicAuth) {
    return new CalSvcIPars(logIdRoClient + id,
                           authUser,
                           user,
                           calSuite,
                           false, // publicAdmin,
                           publicAuth,
                           false, // Allow non-admin super user
                           false, // service
                           false, // public submission
                           false, // adminCanEditAllPublicCategories,
                           false, // adminCanEditAllPublicLocations,
                           false, // adminCanEditAllPublicSponsors,
                           false, // sessionless
                           false, // dontKill
                           true, // readOnly
                           null, // clientId
                           false, // forRestore
                           false);
  }

  public static CalSvcIPars getRwClientPars(
          final String id,
          final String authUser,
          final String user) {
    return new CalSvcIPars(logIdRwClient + id,
                           authUser,
                           user,
                           null,  // calSuiteName,
                           false, // publicAdmin,
                           false, // publicauth
                           false, // Allow non-admin super user
                           false, // service
                           false, // public submission
                           false, // adminCanEditAllPublicCategories,
                           false, // adminCanEditAllPublicLocations,
                           false, // adminCanEditAllPublicSponsors,
                           false, // sessionless
                           false, // dontKill
                           false, // readOnly
                           null, // clientId
                           false, // forRestore
                           false);
  }

  public static CalSvcIPars getAdminClientPars(
          final String id,
          final String authUser,
          final String user,
          final String calSuite,

          final boolean adminCanEditAllPublicCategories,
          final boolean adminCanEditAllPublicLocations,
          final boolean adminCanEditAllPublicContacts) {
    return new CalSvcIPars(logIdAdminClient + id,
                           authUser,
                           user,
                           calSuite,
                           true,
                           false, // publicauth
                           false, // Allow non-admin super user
                           false, // service
                           false, // public submission
                           adminCanEditAllPublicCategories,
                           adminCanEditAllPublicLocations,
                           adminCanEditAllPublicContacts,
                           false, // sessionless
                           false, // dontKill
                           false, // readOnly
                           null, // clientId
                           false, // forRestore
                           false);
  }

  public static CalSvcIPars getToolPars(
          final String user,
          final boolean superUser) {
    return new CalSvcIPars(logIdTools,
                           user,
                           user,
                           null, // calSuite,
                           true,   // publicAdmin
                           false, // publicauth
                           superUser,
                           true,   // service
                           false,// publicSubmission
                           true,// adminCanEditAllPublicCategories
                           true,// adminCanEditAllPublicLocations
                           true,// adminCanEditAllPublicSponsors
                           false, // sessionless
                           true, // system
                           false, // readonly
                           null, // clientId
                           false, // forRestore
                           false);
  }

  /** Return new parameters for a service
   *
   * @param logId       String used for labelling
   * @param account - the account
   * @param publicAdmin - true for public admin
   * @param allowSuperUser - trie/false
   * @return CalSvcIPars
   */
  public static CalSvcIPars getServicePars(final String logId,
                                           final String account,
                                           final boolean publicAdmin,
                                           final boolean allowSuperUser) {
    return new CalSvcIPars(logId,
                           account,
                           null,    // user
                           null,   // calsuite
                           publicAdmin,
                           false, // public auth
                           allowSuperUser,
                           true,   // service
                           false,  // public submission
                           false,  // adminCanEditAllPublicCategories
                           false,  // adminCanEditAllPublicLocations
                           false,  // adminCanEditAllPublicSponsors
                           false, // sessionless
                           true,  // dontKill
                           false, // readOnly
                           null, // clientId
                           false, // forRestore
                           false); // indexRebuild

  }

  /** Return new pars for an index rebuild
   *
   * @param account - the account
   * @param publicAdmin - true for public admin
   * @return CalSvcIPars
   */
  public static CalSvcIPars getIndexerPars(final String account,
                                           final boolean publicAdmin) {
    return new CalSvcIPars(logIdIndexer,
                           account,
                           null,    // user
                           null,   // calsuite
                           publicAdmin,
                           false, // public auth
                           true,
                           true,   // service
                           false,  // public submission
                           false,  // adminCanEditAllPublicCategories
                           false,  // adminCanEditAllPublicLocations
                           false,  // adminCanEditAllPublicSponsors
                           false, // sessionless
                           true,  // dontKill
                           Sysprop.readOnlySystem(), // readOnly
                           null, // clientId
                           false, // forRestore
                           true); // indexRebuild
  }


  /** Return new pars for a system restore
   *
   * @param account - the account
   * @return CalSvcIPars
   */
  public static CalSvcIPars getDumpRestorePars(final String id,
                                               final String account,
                                               final boolean superUser) {
    return new CalSvcIPars(id,
                           account,
                           null,    // user
                           null,   // calsuite
                           superUser,   // publicAdmin,
                           false,  // public auth
                           superUser,
                           true,   // service
                           false,  // public submission
                           true,   // adminCanEditAllPublicCategories
                           true,   // adminCanEditAllPublicLocations
                           true,   // adminCanEditAllPublicSponsors
                           false, // sessionless
                           true, // dontKill
                           false, // readOnly
                           null,
                           true,
                           true);
  }

  /** Return new parameters for caldav.
   *
   * @param logId       String used for labelling
   * @param authUser    String authenticated user of the application
   * @param runAsUser   String user to run as
   * @param clientId    The application we're acting for.
   * @param allowSuperUser  true to allow superuser mode in non-admin mode
   * @param service - true if this is a service call - e.g. iSchedule -
   *                rather than a real user.
   * @param publicAdmin - true for public admin
   * @param allowCreateEprops  - allow create of new event properties, locations etc
   * @return CalSvcIPars
   */
  public static CalSvcIPars getCaldavPars(final String logId,
                                          final String authUser,
                                          final String runAsUser,
                                          final String clientId,
                                          final boolean allowSuperUser,
                                          final boolean service,
                                          final boolean publicAdmin,
                                          final String calSuite,
                                          final boolean allowCreateEprops,
                                          final boolean readonly) {
    return new CalSvcIPars(logId,
                           authUser,
                           runAsUser,
                           calSuite,
                           publicAdmin,
                           false, // public auth
                           allowSuperUser,   // allow SuperUser
                           service,
                           false, // publicSubmission
                           allowCreateEprops, // adminCanEditAllPublicCategories
                           allowCreateEprops,  // adminCanEditAllPublicLocations
                           allowCreateEprops,  // adminCanEditAllPublicSponsors
                           true, // sessionless
                           false, // system
                           readonly,
                           clientId,
                           false,
                           false);
  }

  /**
   * @param val String auth user
   */
  public void setAuthUser(final String val) {
    authUser = val;
  }

  /**
   * @return String auth user
   */
  public String getAuthUser() {
    return authUser;
  }

  /**
   * @return String current user
   */
  public String getUser() {
    return user;
  }

  /**
   * @param val String calSuite
   */
  public void setCalSuite(final String val) {
    calSuite = val;
  }

  /**
   * @return String
   */
  public String getCalSuite() {
    return calSuite;
  }

  /**
   * @return String
   */
  public String getClientId() {
    return clientId;
  }

  /**
   * @param val String logId
   */
  public void setLogId(final String val) {
    logId = val;
  }

  /**
   * @return String
   */
  public String getLogId() {
    return logId;
  }

  /**
   * @return boolean true if this is a public admin object.
   */
  public boolean getPublicAdmin() {
    return publicAdmin;
  }

  /**
   * @return boolean true if this is a public authenticated client.
   */
  public boolean getPublicAuth() {
    return publicAuth;
  }

  public boolean getPublicSubmission() {
    return publicSubmission;
  }

  /**
   * @return boolean true if this is a service.
   */
  public boolean getService() {
    return service;
  }

  /**
   * @return boolean true if we allow superuser mode in non-admin.
   */
  public boolean getAllowSuperUser() {
    return allowSuperUser;
  }

  /**
   * @return boolean
   */
  public boolean getAdminCanEditAllPublicCategories() {
    return adminCanEditAllPublicCategories;
  }

  /**
   * @return boolean
   */
  public boolean getAdminCanEditAllPublicLocations() {
    return adminCanEditAllPublicLocations;
  }

  /**
   * @return boolean
   */
  public boolean getAdminCanEditAllPublicContacts() {
    return adminCanEditAllPublicContacts;
  }

  /**
   * @return boolean true if this is a sessionless client..
   */
  public boolean getSessionsless() {
    return sessionless;
  }

  /**
   * @return boolean true if this is a system process.
   */
  public boolean getDontKill() {
    return dontKill;
  }

  /**
   * @return boolean true if this is a web client..
   */
  public boolean getWebMode() {
    return webMode;
  }

  /**
   * @param val readonly flag
   */
  public void setReadonly(final Boolean val) {
    readonly = val;
  }

  /**
   * @return boolean true if this is a read-only request.
   */
  public boolean getReadonly() {
    return readonly;
  }

  /**
   * @return boolean true if this is for restore of system..
   */
  public boolean getForRestore() {
    return forRestore;
  }

  /**
   * @return boolean true if this is for an index rebuild.
   */
  public boolean getIndexRebuild() {
    return indexRebuild;
  }

  /**
   * @return boolean true for guest
   */
  public boolean isGuest() {
    return authUser == null;
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    ts.append("authUser", getAuthUser());
    ts.append("user", getUser());
    ts.append("calSuite", getCalSuite());
    ts.append("publicAdmin", getPublicAdmin());
    ts.append("publicAuth", getPublicAuth());
    ts.append("service", getService());
    ts.append("adminCanEditAllPublicCategories()", getAdminCanEditAllPublicCategories());
    ts.append("adminCanEditAllPublicLocations()", getAdminCanEditAllPublicLocations());
    ts.append("adminCanEditAllPublicSponsors()", getAdminCanEditAllPublicContacts());
    ts.append("sessionless", getSessionsless());
    ts.append("forRestore", getForRestore());
    ts.append("indexRebuild", getIndexRebuild());

    return ts.toString();
  }

  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  public Object clone() {
    return new CalSvcIPars(getLogId(),
                           getAuthUser(),
                           getUser(),
                           getCalSuite(),
                           getPublicAdmin(),
                           getPublicAuth(),
                           getAllowSuperUser(),
                           getService(),
                           getPublicSubmission(),
                           getAdminCanEditAllPublicCategories(),
                           getAdminCanEditAllPublicLocations(),
                           getAdminCanEditAllPublicContacts(),
                           getSessionsless(),
                           getDontKill(),
                           getReadonly(), // system
                           getClientId(), // clientId
                           getForRestore(), // forRestore
                           getIndexRebuild()); // indexRebuild
  }

  public CalSvcIPars cloneRo() {
    final CalSvcIPars pars = getRoClientPars("",
                                             getAuthUser(),
                                             getUser(),
                                             getCalSuite(),
                                             getPublicAuth());

    pars.logId = getLogId();
    return pars;
  }
}
