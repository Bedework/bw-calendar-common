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
package org.bedework.calfacade.indexing;

import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwEventProperty;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.BwGroup;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.BwResourceContent;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.filter.SortTerm;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.BwPreferences;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.util.indexing.ContextInfo;
import org.bedework.util.misc.response.GetEntitiesResponse;
import org.bedework.util.misc.response.GetEntityResponse;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author douglm
 *
 */
public interface BwIndexer extends Serializable {
  // Types of entity we index
  String docTypeUnknown = "unknown";
  String docTypePrincipal = "principal";
  String docTypePreferences = "preferences";
  String docTypeCollection = "collection";
  String docTypeCategory = "category";
  String docTypeLocation = "location";
  String docTypeContact = "contact";
  String docTypeFilter = "filter";
  String docTypeEvent = "event";
  String docTypeResource = "resource";
  String docTypeResourceContent = "resourceContent";

  String[] allDocTypes = {
          docTypeUnknown,
          docTypePrincipal,
          docTypePreferences,
          docTypeCollection,
          docTypeCategory,
          docTypeLocation,
          docTypeContact,
          docTypeFilter,
          docTypeEvent,
          docTypeResource,
          docTypeResourceContent
  };

  /** */
  enum IndexedType {
    /** */
    unreachableEntities(docTypeUnknown),

    /** */
    principals(docTypePrincipal),

    /** */
    preferences(docTypePreferences),

    /** */
    collections(docTypeCollection),

    /** */
    categories(docTypeCategory),

    /** */
    locations(docTypeLocation),

    /** */
    contacts(docTypeContact),

    /** */
    filters(docTypeFilter),

    /** */
    events(docTypeEvent),

    /** */
    resources(docTypeResource),

    /** */
    resourceContents(docTypeResourceContent);

    private final String docType;

    IndexedType(final String docType) {
      this.docType = docType;
    }

    public String getDocType() {
      return docType;
    }
  }
  
  /* Following used for the id */
  String[] masterDocTypes = {
          "masterEvent",
          null,  // alarm
          "masterTask",
          "masterJournal",
          null,   // freebusy
          null,   // vavail
          "masterAvailable",
          "masterVpoll",   // vpoll
  };

  String[] overrideDocTypes = {
          "overrideEvent",
          null,  // alarm
          "overrideTask",
          "overrideJournal",
          null,   // freebusy
          null,   // vavail
          "overrideAvailable",
          "overrideVpoll",   // vpoll
  };

  /** Used for fetching master + override
   *
   */
  String[] masterOverrideEventTypes = {
          "masterEvent",
          "masterTask",
          "overrideEvent",
          "overrideTask",
          "masterAvailable",
          "overrideAvailable",
  };

  /* Other types are those defined in IcalDefs.entityTypeNames */

  /**
   * Release any rsources etc.
   */
  void close();

  /**
   * @return true if this is a public indexer
   */
  boolean getPublic();

  /** Flag the end of a transaction - updates the updateTracker if any
   * changes were made to the index.
   *
   */
  void markTransaction();

  /**
   * @return a token based on the update tracker value.
   */
  String currentChangeToken();

  /** Indexes the current datafor the current docType into a new index.
   * Will return a response indicating what happened. An immediate
   * response with status processing indicates a process is already 
   * running.
   * 
   * @return final statistics
   */
  ReindexResponse reindex();

  /**
   *
   * @param indexName of index
   * @return current statistics
   */
  ReindexResponse getReindexStatus(String indexName);

  /**
   *
   * @param indexName of index
   * @return current statistics
   */
  IndexStatsResponse getIndexStats(String indexName);

  enum DeletedState {
    onlyDeleted, // Only deleted entities in result
    noDeleted,   // No deleted entities in result
    includeDeleted // Deleted and non-deleted in result
  }

  /** Called to find entries that match the search string. This string may
   * be a simple sequence of keywords or some sort of query the syntax of
   * which is determined by the underlying implementation.
   *
   * <p>defaultFilterContext is a temporary fix until the client is
   * fully upgraded. This is applied as the context for the search if
   * present and no other context is provided. For example, in the user
   * client the default context includes all the user calendars,
   * not the inbox. If no path is selected we apply the default. If a
   * path IS selected we do not apply the default. This allows, for
   * instance, selection of the inbox.</p>
   *
   * @param query        Query string
   * @param relevance    true for a relevance style query
   * @param filter       parsed filter
   * @param sort  list of fields to sort by - may be null
   * @param defaultFilterContext  - see above
   * @param start - if non-null limit to this and after
   * @param end - if non-null limit to before this
   * @param pageSize - stored in the search result for future calls.
   * @param recurRetrieval How recurring event is returned.
   * @return  SearchResult - never null
   */
  SearchResult search(String query,
                      boolean relevance,
                      FilterBase filter,
                      List<SortTerm> sort,
                      FilterBase defaultFilterContext,
                      String start,
                      String end,
                      int pageSize,
                      DeletedState deletedState,
                      RecurringRetrievalMode recurRetrieval);

  enum Position {
    previous,  // Move to previous batch
    current,   // Return the current set
    next       // Move to next batch
  }

  /** Called to retrieve results after a search of the index. Updates
   * the current search result.
   *
   * @param  sres     result of previous search
   * @param pos - specify movement in result set
   * @param desiredAccess  to the entities
   */
  List<SearchResultEntry> getSearchResult(SearchResult sres,
                                          Position pos,
                                          int desiredAccess);

  /** Called to retrieve results after a search of the index. Updates
   * the SearchResult object
   *
   * @param  sres     result of previous search
   * @param offset from first record
   * @param num number of entries
   * @param desiredAccess  to the entities
   * @return list of results - possibly empty - never null.
   */
  List<SearchResultEntry> getSearchResult(SearchResult sres,
                                          int offset,
                                          int num,
                                          int desiredAccess);

  /** Called to unindex a tombstoned entity.
   *
   * @param docType type of document
   * @param   href     of entity to delete
   */
  void unindexTombstoned(String docType,
                         String href);

  /** Called to unindex entities in a collection. This is used when
   * deleting a collection. All entities should be tombstoned
   *
   * @param   colPath     of entities to delete
   */
  void unindexContained(String colPath);

  /** Called to unindex an entity
   *
   * @param   val     an event property
   */
  void unindexEntity(BwEventProperty<?> val);

  /** Called to unindex an entity
   *
   * @param href     the entities href
   */
  void unindexEntity(String href);

  /** Called to index a record
   *
   * @param rec an indexable object
   */
  void indexEntity(Object rec);

  /** Called to index a record with optional wait
   *
   * @param rec an indexable object
   * @param waitForIt true if we wait for it to appear in the index
   * @param forTouch true if we ignore versioning exceptions - will return null
   */
  void indexEntity(Object rec,
                   boolean waitForIt,
                   boolean forTouch);

  /** Set to > 1 to enable batching
   *
   * @param val batch size
   */
  void setBatchSize(int val);

  /** Called at the end of a batch of updates.
   *
   */
  void endBwBatch();

  /** Flush any batched entities.
   */
  void flush();

  String getDocType();

  /** create a new index for current doctype and start using it.
   *
   * @return name of created index.
   */
  String newIndex();

  class IndexInfo implements Comparable<IndexInfo>, Serializable {
    private final String indexName;

    private Set<String> aliases;

    /**
     * @param indexName name of the index
     */
    public IndexInfo(final String indexName) {
      this.indexName = indexName;
    }

    /**
     *
     * @return index name
     */
    public String getIndexName() {
      return indexName;
    }

    /**
     * @param val - set of aliases - never null
     */
    public void setAliases(final Set<String> val) {
      aliases = val;
    }

    /**
     * @return - set of aliases - never null
     */
    public Set<String> getAliases() {
      return aliases;
    }

    /**
     * @param val - an alias - never null
     */
    public void addAlias(final String val) {
      if (aliases == null) {
        aliases = new TreeSet<>();
      }

      aliases.add(val);
    }

    @Override
    public int compareTo(final IndexInfo o) {
      return getIndexName().compareTo(o.getIndexName());
    }
  }

  /** Get info on indexes maintained by server
   *
   * @return list of index info.
   */
  Set<IndexInfo> getIndexInfo();

  /** return the context info for the cluster.
   * Somewhat opensearch specific
   *
   * @return possibly empty list.
   */
  List<ContextInfo> getContextInfo();

  /** Purge non-current indexes maintained by server.
   *
   * @return names of indexes removed.
   */
  List<String> purgeIndexes();

  /** Set alias on the given index - to make it the production index.
   *
   * @param index   name of index to be aliased
   * @return 0 for OK or HTTP status from indexer
   */
  int setAlias(String index);

  /** Href of event with possible anchor tag for recurrence id. This
   * returns the master + overrides if there is no recurrence id or
   * a fully populated instance otherwise.
   *
   * @param href of event
   * @return entity is EventInfo with overrides if present
   */
  GetEntityResponse<EventInfo> fetchEvent(String href);

  /** Colpath and guid supplied. May be multiple results for inbox
   *
   * @param colPath to event collection
   * @param guid of event
   * @return entities are EventInfo objects with overrides if present
   */
  GetEntitiesResponse<EventInfo> fetchEvent(String colPath,
                                            String guid);

  /** Return all or first count events
   *
   * @param path - to events
   * @param lastmod - if non-null use for sync check
   * @param lastmodSeq - if lastmod non-null use for sync check
   * @param count - <0 for all
   * @return events for owner
   */
  List<EventInfo> fetchEvents(String path,
                              String lastmod,
                              int lastmodSeq,
                              int count);

  /** Find a category owned by the current user which has a named
   * field which matches the value.
   *
   * @param val - expected full value
   * @param index e.g. UID or CN, VALUE
   * @return null or category object
   */
  BwCategory fetchCat(String val,
                      PropertyInfoIndex... index);

  /** Fetch all for the current principal.
   *
   * @return possibly empty list
   */
  List<BwCategory> fetchAllCats();

  /** Find a collection which has a named
   * field which matches the value.
   *
   * @param val - expected full value
   * @param index e.g. HREF, UID or CN, VALUE
   * @return response with status and possible collection object
   */
  GetEntityResponse<BwCalendar> fetchCol(String val,
                                         int desiredAccess,
                                         PropertyInfoIndex... index);

  /** Fetch children of the collection with the given href. Tombstoned
   * collections are excluded
   *
   * @param href of parent
   * @return possibly empty list of children
   */
  Collection<BwCalendar> fetchChildren(String href);

  /** Fetch children of the collection with the given href.
   *
   * @param href of parent
   *
   * @return possibly empty list of children
   */
  Collection<BwCalendar> fetchChildren(String href,
                                       boolean excludeTombstoned);

  /** Fetch children at any depth of the collection with the given href.
   *
   * @param href of parent
   * @return possibly empty list of children
   */
  Collection<BwCalendar> fetchChildrenDeep(String href);

  /** Find a principal by href.
   *
   * @param href - of principal
   * @return null or BwPrincipal object
   */
  BwPrincipal<?> fetchPrincipal(String href);

  /** Fetch all groups.
   *
   * @param admin - true for admin groups
   * @return status and List of groups
   */
  GetEntitiesResponse<BwGroup<?>> fetchGroups(boolean admin);

  /** Fetch all groups.
   *
   * @return status and List of groups
   */
  GetEntitiesResponse<BwAdminGroup> fetchAdminGroups();

  /** Fetch all groups of which href is a member.
   *
   * @param admin - true for admin groups
   * @param memberHref - of member
   * @return status and List of groups
   */
  GetEntitiesResponse<BwGroup<?>> fetchGroups(boolean admin,
                                           String memberHref);

  /** Fetch all admin groups of which href is a member.
   *
   * @param memberHref - of member
   * @return status and List of groups
   */
  GetEntitiesResponse<BwAdminGroup> fetchAdminGroups(String memberHref);

  /** Find a preference owned by the given href.
   *
   * @param href - of owner principal
   * @return null or contact object
   */
  BwPreferences fetchPreferences(String href);

  /** Find a filter with the given href.
   *
   * @param href - of filter
   * @return null or filter object
   */
  BwFilterDef fetchFilter(String href);

  /** Return all or first count filters
   *
   * @param fb - possibly null filter
   * @param count - <0 for all
   * @return filter for owner
   */
  List<BwFilterDef> fetchFilters(FilterBase fb,
                                 int count);

  /** Find a resource with the given href.
   *
   * @param href - of resource
   * @return null or resource object
   */
  BwResource fetchResource(String href);

  /** Return all or first count resources
   *
   * @param path - to resources
   * @param lastmod - if non-null use for sync check
   * @param lastmodSeq - if lastmod non-null use for sync check
   * @param count - <0 for all
   * @return resources for owner
   */
  List<BwResource> fetchResources(String path,
                                  String lastmod,
                                  int lastmodSeq,
                                  int count);

  /** Find a resource content with the given href.
   *
   * @param href - of resource content
   * @return null or resource object
   */
  BwResourceContent fetchResourceContent(String href);

  /** Find a contact owned by the current user which has a named
   * field which matches the value.
   *
   * @param val - expected full value
   * @param index e.g. UID or CN, VALUE
   * @return null or contact object
   */
  BwContact fetchContact(String val,
                         PropertyInfoIndex... index);

  /** Fetch all for the current principal.
   *
   * @return possibly empty list
   */
  List<BwContact> fetchAllContacts();

  /**
   *
   * @param filter expression
   * @param from start for result
   * @param size max number
   * @return status and locations
   */
  GetEntitiesResponse<BwContact> findContacts(FilterBase filter,
                                              int from,
                                              int size);

  /** Find a location owned by the current user which has a named
   * field which matches the value.
   *
   * @param val - expected full value
   * @param index e.g. UID or CN, VALUE
   * @return null or location object
   */
  BwLocation fetchLocation(String val,
                           PropertyInfoIndex... index);

  /** Find a location owned by the current user which has a named
   * key field which matches the value.
   *
   * @param name - of key field
   * @param val - expected full value
   * @return null or location object
   */
  GetEntityResponse<BwLocation> fetchLocationByKey(String name,
                                                   String val);

  /**
   *
   * @param filter expression
   * @param from start for result
   * @param size max number
   * @return status and locations
   */
  GetEntitiesResponse<BwLocation> findLocations(FilterBase filter,
                                                int from,
                                                int size);

  /** Fetch all for the current principal.
   *
   * @return possibly empty list
   */
  List<BwLocation> fetchAllLocations();

  /**
   *
   * @param filter expression
   * @param from start for result
   * @param size max number
   * @return status and categories
   */
  GetEntitiesResponse<BwCategory> findCategories(FilterBase filter,
                                                 int from,
                                                 int size);
}
