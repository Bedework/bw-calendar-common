/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calfacade.util;

import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.svc.BwPreferences.CategoryMapping;
import org.bedework.calfacade.svc.BwPreferences.CategoryMappings;
import org.bedework.util.misc.Util;

import java.util.Set;

/**
 * User: mike Date: 10/4/21 Time: 00:04
 */
public class CategoryMapInfo {
  private final CategoryMappings catMaps;
  private final Set<BwCalendar> topicalAreas;

  public CategoryMapInfo(final CategoryMappings catMaps,
                         final Set<BwCalendar> topicalAreas) {
    this.catMaps = catMaps;
    this.topicalAreas = topicalAreas;
  }

  public CategoryMappings getCatMaps() {
    return catMaps;
  }

  public Set<BwCalendar> getTopicalAreas() {
    return topicalAreas;
  }

  public boolean getNoMapping() {
    return (getCatMaps() == null) || (getTopicalAreas() == null);
  }

  /**
   * @param val category to match
   * @return mapping if defined or null
   */
  public CategoryMapping findMapping(final String val) {
    if ((catMaps == null) || Util.isEmpty(catMaps.getMappings())) {
      return null;
    }

    for (final CategoryMapping m: catMaps.getMappings()) {
      if (m.getFrom().equals(val)) {
        return m;
      }
    }

    return null;
  }

  public BwCalendar getTopicalArea(final CategoryMapping catMap) {
    final Set<BwCalendar> topicalAreas = getTopicalAreas();
    if (Util.isEmpty(topicalAreas)) {
      return null;
    }

    for (final BwCalendar col: topicalAreas) {
      if (col.getSummary().equals(catMap.getTo())) {
        return col;
      }
    }

    return null;
  }
}
