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

import org.bedework.access.Access.AccessStatsEntry;
import org.bedework.database.db.StatsEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/** Some statistics for the Bedework calendar. These are not necessarily
 * absolutely correct. We don't lock, just increment and decrement but
 * they work well enough to get an idea of how we're performing.
 *
 * @author Mike Douglass       douglm@bedework.edu
 */
public class BwStats implements Serializable {
  /**
   */
  public static class CacheStats {
    protected String name;

    protected long cached;
    protected long hits;
    protected long misses;
    protected long flushes;
    protected long refetches;

    CacheStats(final String name) {
      this.name = name;
    }

    /**
     * @return name for these stats
     */
    public String getName() {
      return name;
    }

    /**
     * @param val num cached
     */
    public void setCached(final long val) {
      cached = val;
    }

    /**
     * @return Number of values cached
     */
    public long getCached() {
      return cached;
    }

    /**
     */
    public void incCached() {
      cached++;
    }

    /**
     * @param val num hits
     */
    public void setHits(final long val) {
      hits = val;
    }

    /**
     * @return cache hits
     */
    public long getHits() {
      return hits;
    }

    /**
     */
    public void incHits() {
      hits++;
    }

    /**
     * @param val misses
     */
    public void setMisses(final long val) {
      misses = val;
    }

    /**
     * @return cache misses.
     */
    public long getMisses() {
      return misses;
    }

    /**
     */
    public void incMisses() {
      misses++;
    }

    /**
     * @param val flushes
     */
    public void setFlushes(final long val) {
      flushes = val;
    }

    /** A flush may clear the cache or reset flags causing a possible a
     * revalidation of data
     *
     * @return cache flushes
     */
    public long getFlushes() {
      return flushes;
    }

    /** A flush may clear the cache or reset flags causing a possible a
     * revalidation of data
     */
    public void incFlushes() {
      flushes++;
    }

    /**
     * @param val refetches
     */
    public void setRefetches(final long val) {
      refetches = val;
    }

    /** Data has to be refetched because invalid. May be 0 if we simply empty the
     * cache on flush.
     *
     * @return cache refetches.
     */
    public long getRefetches() {
      return refetches;
    }

    /**
     */
    public void incRefetches() {
      refetches++;
    }
  }

  /* Collection entity fetch data */
  protected CacheStats collectionCacheStats = new CacheStats("Collections");

  protected int tzFetches;

  protected int systemTzFetches;

  protected int tzStores;

  protected double eventFetchTime;

  protected long eventFetches;

  /* Dates cache stats */
  protected CacheStats dateCacheStats = new CacheStats("UTC Dates");

  /* Access stats */
  protected Collection<AccessStatsEntry> accessStats;

  /**
   * @return Collection stats
   */
  public CacheStats getCollectionCacheStats() {
    return collectionCacheStats;
  }

  /**
   * @return int   total num timezone fetches.
   */
  public int getTzFetches() {
    return tzFetches;
  }

  /**
   */
  public void incTzFetches() {
    tzFetches++;
  }

  /**
   * @return int   num system timezone fetches.
   */
  public int getSystemTzFetches() {
    return systemTzFetches;
  }

  /**
   */
  public void incSystemTzFetches() {
    systemTzFetches++;
  }

  /**
   * @return int   num timezone stores.
   */
  public int getTzStores() {
    return tzStores;
  }

  /**
   */
  public void incTzStores() {
    tzStores++;
  }

  /**
   * @return double   event fetch millis.
   */
  public double getEventFetchTime() {
    return eventFetchTime;
  }

  /**
   * @param  val  double event fetch millis.
   */
  public void incEventFetchTime(final double val) {
    eventFetchTime += val;
  }

  /**
   * @return long   event fetches.
   */
  public long getEventFetches() {
    return eventFetches;
  }

  /**
   * @param val   long event fetches.
   */
  public void incEventFetches(final long val) {
    eventFetches += val;
  }

  /**
   * @return date cache Stats
   */
  public CacheStats getDateCacheStats() {
    return dateCacheStats;
  }

  /**
   * @param val access Stats
   */
  public void setAccessStats(final Collection<AccessStatsEntry> val) {
    accessStats = val;
  }

  /**
   * @return access Stats
   */
  public Collection<AccessStatsEntry> getAccessStats() {
    return accessStats;
  }

  /**
   * @return Collection of StatsEntry
   */
  public Collection<StatsEntry> getStats() {
    final ArrayList<StatsEntry> al = new ArrayList<>();

    al.add(new StatsEntry("Bedework statistics."));

    cacheStatsToString(al, collectionCacheStats);

    al.add(new StatsEntry("tzFetches", getTzFetches()));
    al.add(new StatsEntry("systemTzFetches", getSystemTzFetches()));
    al.add(new StatsEntry("tzStores", getTzStores()));

    al.add(new StatsEntry("event fetch time", getEventFetchTime()));
    al.add(new StatsEntry("event fetches", getEventFetches()));

    cacheStatsToString(al, dateCacheStats);

    if (getAccessStats() != null) {
      al.add(new StatsEntry("Access statistics."));

      for (final AccessStatsEntry ase: getAccessStats()) {
        al.add(new StatsEntry(ase.name, ase.count));
      }
    }

    return al;
  }

  /**
   * @param al list of stats entries
   * @param cs cachestats
   */
  public void cacheStatsToString(final ArrayList<StatsEntry> al,
                                 final CacheStats cs) {
    final String name = cs.getName() + " ";

    al.add(new StatsEntry(name + "cached", cs.getCached()));
    al.add(new StatsEntry(name + " hits", cs.getHits()));
    al.add(new StatsEntry(name + " misses", cs.getMisses()));
    al.add(new StatsEntry(name + " flushes", cs.getFlushes()));
    al.add(new StatsEntry(name + " refetches", cs.getRefetches()));
  }

  public String toString() {
    return StatsEntry.toString(getStats());
  }
}
