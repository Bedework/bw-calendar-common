/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calfacade.indexing;

import org.bedework.base.ToString;
import org.bedework.base.response.Response;

/**
 * User: mike Date: 10/7/17 Time: 13:27
 */
public class IndexStatsResponse
        extends Response<IndexStatsResponse> {
  private final String name;

  private long processed;

  private long masters;

  private long recurring;

  private long overrides;

  private long instances;

  private long totalFailed;

  /** */
  private final IndexStatistics stats;

  public IndexStatsResponse(final String name) {
    this.name = name;
    stats = new IndexStatistics(name);
  }

  public String getName() {
    return name;
  }

  /**
   * @return Total number processed
   */
  public long getProcessed() {
    return processed;
  }

  public void incProcessed() {
    processed++;
  }

  /**
   * @return Total number masters processed
   */
  public long getMasters() {
    return masters;
  }

  public void incMasters() {
    masters++;
  }

  /**
   * @return Total number recurring processed
   */
  public long getRecurring() {
    return recurring;
  }

  public void incRecurring() {
    recurring++;
  }

  /**
   * @return Total number overrides processed
   */
  public long getOverrides() {
    return overrides;
  }

  public void incOverrides() {
    overrides++;
  }

  /**
   * @return Total number instances processed
   */
  public long getInstances() {
    return instances;
  }

  public void incInstances() {
    instances++;
  }

  /**
   * @return Total number failed
   */
  public long getTotalFailed() {
    return totalFailed;
  }

  public void incTotalFailed() {
    totalFailed++;
  }
    
  public IndexStatistics getStats() {
    return stats;
  }

  public ToString toStringSegment(final ToString ts) {
    return super.toStringSegment(ts)
                .append("name", getName())
                .append("processed", getProcessed())
                .append("masters", getMasters())
                .append("recurring", getRecurring())
                .append("overrides", getOverrides())
                .append("instances", getInstances())
                .append("totalFailed", getTotalFailed())
                .append("stats", getStats());
  }
}
