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

import org.bedework.calfacade.AliasesInfo;
import org.bedework.calfacade.BwCollection;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.CollectionAliases;
import org.bedework.synch.wsmessages.SubscriptionStatusResponseType;
import org.bedework.base.response.GetEntityResponse;
import org.bedework.base.response.Response;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/** This is the collections interface for the  bedework system.
 * The type property defines which type of collection we are dealing with.
 * The CalDAV spec defines what is allowable, e.g. no collections
 * inside a calendar collection.
 *
 * <p>To allow us to enforce access checks we wrap the object inside a wrapper
 * class which blocks access to the getChildren method. To retrieve the children
 * of a collection object call the getCollections(BwCollection) method. The resulting
 * collection is a set of access checked, wrapped objects. Only accessible
 * children will be returned.
 *
 * @author Mike Douglass
 */
public interface CollectionsI extends Serializable {
  /** Returns the href of the root of the public collections tree.
   *
   * @return String   root path
   */
  String getPublicCollectionsRootPath();

  /** Returns the root of the tree of public collections. This is NOT a
   * live hibernate object.
   *
   * @return BwCollection   root
   */
  BwCollection getPublicCollections();

  /**
   * @return the public collection flagged as the primary collection
   */
  BwCollection getPrimaryPublicPath();

  /** Returns root path of collections owned by the current user. For
   * unauthenticated this will be the public collection root.
   *
   * @return String principal home.
   */
  String getHomePath();

  /** Returns root of collections owned by the current user.
   *
   * <p>For authenticated, personal access this always returns the user
   * entry in the /user collection tree, e.g. for user smithj it would return
   * an entry /user/smithj
   *
   * @return BwCollection   user home.
   */
  BwCollection getHome();

  /** Returns root of collections owned by the given principal.
   *
   * <p>For authenticated, personal access this always returns the user
   * entry in the /user collection tree, e.g. for user smithj it would return
   * an entry smithj with path /user/smithj
   *
   * Note: the returned object is NOT a live hibernate object.
   *
   * @param  principal whose home we want
   * @param freeBusy      true if this is for freebusy access
   * @return BwCollection   user home.
   */
  BwCollection getHome(BwPrincipal principal,
                       boolean freeBusy);

  /** Returns root of collections owned by the given principal.
   *
   * <p>For authenticated, personal access this always returns the user
   * entry in the /user collection tree, e.g. for user smithj it would return
   * an entry smithj with path /user/smithj
   *
   * Note: the returned object is a live hibernate object.
   *
   * @param  principal whose home we want
   * @param freeBusy      true if this is for freebusy access
   * @return BwCollection   user home.
   */
  BwCollection getHomeDb(BwPrincipal principal,
                         boolean freeBusy);

  /** A virtual path might be for example "/user/adgrp_Eng/Lectures/Lectures"
   * which has two two components<ul>
   * <li>"/user/adgrp_Eng/Lectures" and</li>
   * <li>"Lectures"</li></ul>
   *
   * <p>
   * "/user/adgrp_Eng/Lectures" is a real path which is an alias to
   * "/public/aliases/Lectures" which is a folder containing the alias
   * "/public/aliases/Lectures/Lectures" which is aliased to the single collection.
   *
   * @param vpath A virtual path
   * @return collection of collection objects - null for bad vpath
   */
  Collection<BwCollection> decomposeVirtualPath(String vpath);

  /** Returns children of the given collection to which the current user has
   * some access.
   *
   * @param  cal          parent collection
   * @return Collection   of BwCollection (empty if no children)
   */
  Collection<BwCollection> getChildren(BwCollection cal);

  /** Returns children of the given collection to which the current user has
   * some access.
   * 
   * <p>The returned objects are from the indexer and are not live 
   * hibernate objects</p>
   *
   * @param  col          parent collection
   * @return Collection   of BwCollection
   */
  Collection<BwCollection> getChildrenIdx(BwCollection col);

  /** Return a list of collections in which collection objects can be
   * placed by the current user.
   *
   * <p>Caldav currently does not allow collections inside collections so that
   * collection collections are the leaf nodes only.
   *
   * @param includeAliases - true to include aliases - for public admin we don't
   *                    want aliases
   * @return Set   of BwCollection
   */
  Set<BwCollection> getAddContentCollections(boolean includeAliases,
                                             boolean isApprover);

  /** Check to see if a collection is empty. A collection is not empty if it
   * contains other collections or collection entities.
   *
   * @param val      BwCollection object to check
   * @return boolean true if the collection is empty
   */
  boolean isEmpty(BwCollection val);

  /** Get a collection given the path. If the path is that of a 'special'
   * collection, for example the deleted collection, it may not exist if it has
   * not been used.
   *
   * @param  path          String path of collection
   * @return BwCollection null for unknown collection
   */
  BwCollection get(String path);

  /** Get a collection given the path. If the path is that of a 'special'
   * collection, for example the deleted collection, it may not exist if it has
   * not been used. Returns the non-persisted version from the index
   *
   * @param  path          String path of collection
   * @return BwCollection null for unknown collection
   */
  BwCollection getIdx(String path);

  /** Get a special collection (e.g. Notifications) for the current user. If it does not
   * exist and is supported by the target system it will be created.
   *
   * @param  calType   int special collection type.
   * @param  create    true if we should create it if non-existent.
   * @return BwCollection null for unknown collection
   */
  BwCollection getSpecial(int calType,
                          boolean create);

  /** Get a special collection (e.g. Notifications) for the given user. If it does not
   * exist and is supported by the target system it will be created.
   *
   * @param  principal the principal href.
   * @param  calType   int special collection type.
   * @param  create    true if we should create it if non-existent.
   * @return BwCollection null for unknown collection
   */
  BwCollection getSpecial(String principal,
                          int calType,
                          boolean create);

  /** set the default collection for the current user.
   *
   * @param  val    BwCollection
   */
  void setPreferred(BwCollection val);

  /** Get the default collection for the current user for the given entity type.
   *
   * @param entityType to search for
   * @return path or null for unknown collection
   */
  String getPreferred(String entityType);

  /** Add a collection object
   *
   * <p>The new collection object will be added to the db. If the indicated parent
   * is null it will be added as a root level collection.
   *
   * <p>Certain restrictions apply, mostly because of interoperability issues.
   * A collection cannot be added to another collection which already contains
   * entities, e.g. events etc.
   *
   * <p>Names cannot contain certain characters - (complete this)
   *
   * <p>Name must be unique at this level, i.e. all paths must be unique
   *
   * @param  val     BwCollection new object
   * @param  parentPath  String path to parent.
   * @return BwCollection object as added. Parameter val MUST be discarded
   */
  BwCollection add(BwCollection val,
                   String parentPath);

  /** Change the name (path segment) of a collection object.
   *
   * @param  val         BwCollection object
   * @param  newName     String name
   */
  void rename(BwCollection val,
              String newName);

  /** Move a collection object from one parent to another
   *
   * @param  val         BwCollection object
   * @param  newParent   BwCollection potential parent
   */
  void move(BwCollection val,
            BwCollection newParent);

  /** Update a collection object
   *
   * @param  val     BwCollection object
   */
  void update(BwCollection val);

  /** Delete a collection. Also remove it from the current user preferences (if any).
   *
   * @param val      collection
   * @param emptyIt  true to delete contents
   * @param sendSchedulingMessage  true if we should send cancels
   * @return boolean  true if it was deleted.
   *                  false if it didn't exist
   */
  boolean delete(BwCollection val,
                 boolean emptyIt,
                 boolean sendSchedulingMessage);

  /** Return true if cal != null and it represents a (local) user root
   *
   * @param cal the collection
   * @return boolean
   */
  boolean isUserRoot(BwCollection cal);

  /** Attempt to get collection referenced by the alias. For an internal alias
   * the result will also be set in the aliasTarget property of the parameter.
   *
   * @param val the alias
   * @param resolveSubAlias - if true and the alias points to an alias, resolve
   *                  down to a non-alias.
   * @param freeBusy to determine trequired access
   * @return a BwCollection object
   */
  BwCollection resolveAlias(BwCollection val,
                            boolean resolveSubAlias,
                            boolean freeBusy);

  /** Attempt to get collection referenced by the alias. For an internal alias
   * the result will also be set in the aliasTarget property of the parameter.
   * 
   * <p>This uses the index only. The returned object will not be a 
   * live hibernate object</p>
   *
   * @param val the alias
   * @param resolveSubAlias - if true and the alias points to an alias, resolve
   *                  down to a non-alias.
   * @param freeBusy to determine trequired access
   * @return a BwCollection object
   */
  BwCollection resolveAliasIdx(BwCollection val,
                               boolean resolveSubAlias,
                               boolean freeBusy);

  /**
   * @param val a collection to check
   * @return response with status and info.
   */
  GetEntityResponse<CollectionAliases> getAliasInfo(BwCollection val);

  /** */
  enum CheckSubscriptionResult {
    /** No action was required */
    ok,

    /** No such collection */
    notFound,

    /** Not external subscription */
    notExternal,

    /** no subscription id */
    notsubscribed,

    /** resubscribed */
    resubscribed,

    /** Synch service is unavailable */
    noSynchService,

    /** failed */
    failed
  }

  class SynchStatusResponse {
    public CheckSubscriptionResult requestStatus;

    public SubscriptionStatusResponseType subscriptionStatus;

    public CheckSubscriptionResult getRequestStatus() {
      return requestStatus;
    }

    public SubscriptionStatusResponseType getSubscriptionStatus() {
      return subscriptionStatus;
    }
  }

  /** Called to get information about aliases to a collection 
   * containing the named entity. 
   *
   * @param collectionHref the collection
   * @param entityName the entity
   * @return the information
   */
  AliasesInfo getAliasesInfo(String collectionHref,
                             String entityName);

  /**
   *
   * @param val collection
   * @return never null - requestStatus set for not an external subscription.
   */
  SynchStatusResponse getSynchStatus(BwCollection val);

  /** Check the subscription if this is an external subscription. Will contact
   * the synch server and check the validity. If there is no subscription
   * onthe synch server will attempt to resubscribe.
   *
   * @param path to collection
   * @return result of call
   */
  CheckSubscriptionResult checkSubscription(String path);

  /** Refresh the subscription if this is an external subscription. Will contact
   * the synch server.
   *
   * @param val the collection
   * @return result of call
   */
  Response refreshSubscription(BwCollection val);

  /** Return the value to be used as the sync-token property for the given path.
   * This is effectively the max sync-token of the collection and any child
   * collections.
   *
   * @param path to collection
   * @return a sync-token
   */
  String getSyncToken(String path);

  /** Return true if the value represents a valid sync-token for
   * the given path.
   *
   * For bedework the token is effectively the max sync-token of
   * the collection and any child.
   *
   * We consider it invalid if it's not a valid date or too old
   * or an exception occurs.
   *
   * @param token to test
   * @param path to collection
   * @return true for a valid sync-token
   */
  boolean getSyncTokenIsValid(String token,
                              String path);

  Set<BwCategory> getCategorySet(String href);

  BwCollection getSpecial(BwPrincipal owner,
                          int calType,
                          boolean create,
                          int access);
}
