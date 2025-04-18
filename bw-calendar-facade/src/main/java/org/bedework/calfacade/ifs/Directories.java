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
package org.bedework.calfacade.ifs;

import org.bedework.access.AccessPrincipal;
import org.bedework.calfacade.BwGroup;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwPrincipalInfo;
import org.bedework.calfacade.DirectoryInfo;
import org.bedework.calfacade.configs.Configurations;
import org.bedework.calfacade.svc.BwPreferences;
import org.bedework.webdav.servlet.shared.WebdavProperty;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/** An interface to handle directory information and groups.
 *
 * <p>Groups may be stored in a site specific manner so the actual
 * implementation used is a build-time configuration option. They may be
 * ldap directory based or implemented by storing in the calendar database.
 *
 * <p>Methods may throw an unimplemented exception if functions are not
 * available.
 *
 * @author Mike Douglass douglm rpi.edu
 * @version 3.3.2
 */
public interface Directories extends Serializable {

  /** Class to be implemented by caller and passed during init.
   */
  abstract class CallBack implements Serializable {
    /** Get a name uniquely.identifying this system. This should take the form <br/>
     *   name@host
     * <br/>where<ul>
     * <li>name identifies the particular calendar system at the site</li>
     * <li>host part identifies the domain of the site.</li>..
     * </ul>
     *
     * @return String    globally unique system identifier.
     */
    public abstract String getSysid();

    /**
     * @return BwUser representing current user
     */
    public abstract BwPrincipal<?> getCurrentUser();

    /** Find a group given its account name
     *
     * @param  account           String group name
     * @param admin          true for an admin group
     * @return BwGroup        group object
     * @exception RuntimeException If there's a problem
     */
    public abstract BwGroup<?> findGroup(String account,
                                         boolean admin);

    /**
     * @param  group           BwGroup group object
     * @param admin          true for an admin group
     * @return Collection
     */
    public abstract Collection<BwGroup<?>> findGroupParents(
            BwGroup<?> group,
            boolean admin);

    /**
     * @param  group           BwGroup group object to add
     * @param admin          true for an admin group
     */
    public abstract void addGroup(BwGroup<?> group,
                                  boolean admin);

    /**
     * @param  group           BwGroup group object to update
     * @param admin          true for an admin group
     */
    public abstract void updateGroup(BwGroup<?> group,
                                     boolean admin);

    /** Delete a group
     *
     * @param  group           BwGroup group object to delete
     * @param admin          true for an admin group
     */
    public abstract void removeGroup(BwGroup<?> group,
                                     boolean admin);

    /** Add a member to a group
     *
     * @param group          a group principal
     * @param val             BwPrincipal new member
     * @param admin          true for an admin group
     */
    public abstract void addMember(BwGroup<?> group,
                                   BwPrincipal<?> val,
                                   boolean admin);

    /** Remove a member from a group
     *
     * @param group          a group principal
     * @param val            BwPrincipal member
     * @param admin          true for an admin group
     */
    public abstract void removeMember(BwGroup<?> group,
                                      BwPrincipal<?> val,
                                      boolean admin);

    /** Get the direct members of the given group.
     *
     * @param  group           BwGroup group object to add
     * @param admin          true for an admin group
     * @return list of members
       */
    public abstract Collection<BwPrincipal<?>> getMembers(
            BwGroup<?> group,
            boolean admin);

    /** Return all groups to which this user has some access. Never returns null.
     *
     * @param admin          true for an admin group
     * @return Collection    of BwGroup
       */
    public abstract Collection<BwGroup<?>> getAll(boolean admin);

    /** Return all groups of which the given principal is a member. Never returns null.
     *
     * <p>Does not check the returned groups for membership of other groups.
     *
     * @param val            a principal
     * @param admin          true for an admin group
     * @return Collection    of BwGroup
       */
    public abstract Collection<BwGroup<?>> getGroups(
            BwPrincipal<?> val,
            boolean admin);
  }

  /** Provide the callback object
   *
   * @param cb callback
   * @param configs Configuratiosn object
   */
  void init(CallBack cb,
            final Configurations configs);

  /** Return the name of the configuration properties for the module,
   * e.g "module.user-ldap-group" or "module.dir-config"
   * @return String
   */
  String getConfigName();

  /** Get application visible directory information.
   *
   * @return DirectoryInfo
   */
  DirectoryInfo getDirectoryInfo();

  /** Test for a valid principal in the directory. This may have a number of
   * uses. For example, when organizing meetings we may want to send an
   * invitation to a user who has not yet logged on. This allows us to
   * distinguish between a bad account (spam maybe?) and a real account.
   *
   * <p>Sites may wish to override this method to check their directory to see
   * if the principal exists.
   *
   * @param href of principal
   * @return true if it's a valid principal
   */
  boolean validPrincipal(String href);

  /** Does the value appear to represent a valid principal?
   *
   * @param val href of principal
   * @return true if it's a (possible) principal
   */
  boolean isPrincipal(String val);

  /** If this is a valid user principal return the account.
   *
   * @param val href of principal
   * @return account or null if not valid user principal
   */
  String accountFromPrincipal(String val);

  /** Return principal for the given href.
   *
   * @param href ofprincipal
   * @return Principal
   */
  BwPrincipal<?> getPrincipal(String href);

  /** Needed for the ischedule service
   *
   * @return the default domain for the service.
   */
  String getDefaultDomain();

  /** The urls should be principal urls. principalUrl can null for the current user.
   * The result is a collection of principal urls of which the given url is a
   * member, based upon rootUrl. For example, if rootUrl points to the base of
   * the user principal hierarchy, then the rsult should be at least the current
   * user's principal url, remembering that user principals are themselves groups
   * and the user is considered a member of their own group.
   *
   * @param rootUrl - url to base search on.
   * @param principalUrl - url of principal or null for current user
   * @return Collection of urls - always non-null
   */
  Collection<String>getGroups(String rootUrl,
                              String principalUrl);

  /**
   * @param id account
   * @param whoType - from WhoDefs
   * @return String principal uri
   */
  String makePrincipalUri(String id,
                          int whoType);

  /** Given a uri return a calendar address.
   * This should handle actions such as turning<br/>
   *   auser
   * <br/>into the associated calendar address of <br/>
   *   mailto:auser@ahost.org
   *
   * <p>It should also deal with user@somewhere.org to
   * mailto:user@somewhere.org
   *
   * <p>Note: this method and userToCalAddr should be doing lookups of the
   * enterprise directory (or carddav) to determine the calendar user address.
   * For the moment we do a transform of the account to get a mailto.
   *
   * @param val        uri
   * @return caladdr for this system or null for an invalid uri
   */
  String uriToCaladdr(String val);

  /** Given a user principal return a calendar address.
   *
   * @param val        principal
   * @return caladdr
   */
  String principalToCaladdr(AccessPrincipal val);

  /** Given a user account return a calendar address.
   * For example, we might have an account<br/>
   *   auser
   * <br/>with the associated calendar address of <br/>
   *   mailto:auser@ahost.org
   *
   * <p>Note: this method and uriToCalAddr should be doing lookups of the
   * enterprise directory (or carddav) to determine the calendar user address.
   * For the moment we do a transform of the account to get a mailto.
   *
   * @param val        account
   * @return caladdr for this system
   * @throws RuntimeException  for errors
   */
  String userToCaladdr(String val);

  /** Given a calendar address return the associated calendar account.
   * For example, we might have a calendar address<br/>
   *   mailto:auser@ahost.org
   * <br/>with the associated account of <br/>
   * auser<br/>
   *
   * <p>We also allow user principals
   *
   * <p>Wherever we need a user account use the converted value. Call
   * userToCaladdr for the inverse.
   *
   * @param caladdr      calendar address
   * @return account or null if not caladdr for this system
   */
  BwPrincipal<?> caladdrToPrincipal(String caladdr);

  /** Ensure we have something that looks like a valid calendar user address.
   * Could be a mailto: or a principal
   *
   * @param val String potential calendar user address
   * @return String valid or null invalid.
   */
  String normalizeCua(String val);

  /** Return some sort of directory information for the given principal.
   *
   * @param p                principal for which we want info
   * @return BwPrincipalInfo directory information.
   */
  BwPrincipalInfo getDirInfo(BwPrincipal<?> p);

  /**
   *
   * @param principals BwPrincipalInfo objects that match.
   * @param truncated value is true if result is incomplete
   */
  record FindPrincipalsResult(List<BwPrincipalInfo> principals,
                              boolean truncated) {}

  /** Return matching principals.
   *
   * @param props     to match
   * @param returnProps  used to configure returned data
   * @param cutype    null - or type
   * @return BwPrincipalInfo objects that match.
   */
  FindPrincipalsResult find(List<WebdavProperty> props,
                            List<WebdavProperty> returnProps,
                            String cutype);

  /** Return principals tha match the CUA.
   *
   * @param cua  a calendar user address
   * @param cutype individual, group etc (null for individual)
   * @param expand if true expand any groups so that the info contains group member info.
   * @return BwPrincipalInfo objects that match.
   */
  FindPrincipalsResult find(String cua,
                            String cutype,
                            boolean expand);

  /** Uses the values in pinfo to update the supplied preferences. This may be a
   * site specific operation. It allows bedework to use directory information
   * to alter the behavior of principals. For example, we can define resources
   * in a directory and the auto-respond behavior will be turned on in bedework
   * scheduling.
   *
   * @param prefs
   * @param pinfo
   * @return boolean true if preferences updated
   */
  boolean mergePreferences(BwPreferences prefs,
                           BwPrincipalInfo pinfo);

  /** Return all groups of which the given principal is a member. Never returns null.
   *
   * <p>Does not check the returned groups for membership of other groups.
   *
   * @param val            a principal
   * @return Collection    of BwGroup
   */
  Collection<BwGroup<?>> getGroups(BwPrincipal<?> val);

  /** Return all groups of which the given principal is a member. Never returns null.
   *
   * <p>This does check the groups for membership of other groups so the
   * returned collection gives the groups of which the principal is
   * directly or indirectly a member.
   *
   * @param val            a principal
   * @return Collection    of BwGroup
   */
  Collection<BwGroup<?>> getAllGroups(BwPrincipal<?> val);

  /** Show whether entries can be modified with this
   * class. Some sites may use other mechanisms.
   *
   * @return boolean    true if group maintenance is implemented.
   */
  boolean getGroupMaintOK();

  /** Return all groups to which this user has some access. Never returns null.
   *
   * @param  populate      boolean populate with members
   * @return Collection    of BwGroup
   */
  Collection<BwGroup<?>> getAll(boolean populate);

  /** Populate the group with a (possibly empty) Collection of members. Does not
   * populate groups which are members.
   *
   * @param  group           BwGroup group object to add
   */
  void getMembers(BwGroup<?> group);

  /* ====================================================================
   *  The following are available if group maintenance is on.
   * ==================================================================== */

  /** Add a group
   *
   * @param  group           BwGroup group object to add
   */
  void addGroup(BwGroup<?> group);

  /** Find a group given its name
   *
   * @param  name           String group name
   * @return BwGroup        group object
   */
  BwGroup<?> findGroup(String name);

  /** Add a principal to a group
   *
   * @param group          a group principal
   * @param val            BwPrincipal new member
   */
  void addMember(BwGroup<?> group,
                 BwPrincipal<?> val);

  /** Remove a member from a group
   *
   * @param group          a group principal
   * @param val            BwPrincipal new member
   */
  void removeMember(BwGroup<?> group,
                    BwPrincipal<?> val);

  /** Delete a group
   *
   * @param  group           BwGroup group object to delete
   */
  void removeGroup(BwGroup<?> group);

  /** update a group. This may have no meaning in some directories.
   *
   * @param  group           BwGroup group object to update
   */
  void updateGroup(BwGroup<?> group);

  /**
   * @param group
   * @return Collection
   */
  Collection<BwGroup<?>> findGroupParents(BwGroup<?> group);

  /**
   * @return String used to prefix administrative group names to distinguish
   *         them from user group names.
   */
  String getAdminGroupsIdPrefix();
}
