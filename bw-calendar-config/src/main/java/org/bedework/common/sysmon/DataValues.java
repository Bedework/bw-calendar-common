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
package org.bedework.common.sysmon;

import org.bedework.calfacade.MonitorStat;
import org.bedework.sysevents.events.MillisecsEvent;
import org.bedework.sysevents.events.StatsEvent;
import org.bedework.sysevents.events.StatsEvent.StatType;
import org.bedework.sysevents.events.SysEvent;
import org.bedework.sysevents.events.SysEventBase.SysCode;
import org.bedework.sysevents.events.TimedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Display some data values.
 *
 * @author douglm
 */
public class DataValues {
  private final Map<SysCode, DataAvg> dvMap = new HashMap<>();

  private final Map<String, DataAvg> timedValuesMap = new HashMap<>();

  private final Map<String, DataAvg> statMap = new HashMap<>();

  /**
   */
  public DataValues() {
    super();

    addDv("Avg web response time", SysCode.WEB_OUT);
    addDv("Avg CalDAV response time", SysCode.CALDAV_OUT);
    addDv("Avg login time", SysCode.USER_LOGIN);
    addDv("Avg service login time", SysCode.SERVICE_USER_LOGIN);
  }

  /**
   * @param ev system event
   */
  public void update(final SysEvent ev) {
    final SysCode sc = ev.getSysCode();

    if (ev instanceof TimedEvent) {
      final TimedEvent te = (TimedEvent)ev;
      final String lbl = te.getLabel();

      DataAvg dv = timedValuesMap.get(lbl);

      if (dv == null) {
        dv = new DataAvg(lbl, ev.getSysCode());

        timedValuesMap.put(lbl, dv);
      }

      dv.inc(te.getMillis());

      return;
    }

    if (ev instanceof MillisecsEvent) {
      final DataAvg dv = dvMap.get(sc);

      if (dv != null) {
        dv.inc(((MillisecsEvent)ev).getMillis());
      }
      return;
    }

    if (sc == SysCode.STATS) {
      final StatsEvent se = (StatsEvent)ev;
      final String sname = se.getName();
      DataAvg da = statMap.get(sname);

      if (da == null) {
        da = new DataAvg(sname, sc);

        statMap.put(sname, da);
      }

      final StatType st = StatsEvent.getStatType(sname);

      if (st != StatType.lnum) {
        da.inc(1);
      } else if (se.getLongValue() != null) {
        da.inc(se.getLongValue());
      }

      return;
    }
  }

  /**
   * @param vals to add to
   */
  public void getValues(final List<String> vals) {
    for (final DataAvg da: dvMap.values()) {
      vals.add(da.toString());
    }
  }

  /**
   * @param stats to add to
   */
  public void getStats(final List<MonitorStat> stats) {
    for (final DataAvg da: dvMap.values()) {
      stats.add(da.getStat());
    }

    for (final DataAvg da: timedValuesMap.values()) {
      final long val = (long)(da.getValue() / da.getCount());
      stats.add(new MonitorStat(da.getName(), (long)da.getCount(),
                                String.valueOf(val)));
    }

    for (final DataAvg da: statMap.values()) {
      final long val = (long)(da.getValue() / da.getCount());
      stats.add(new MonitorStat(da.getName(), (long)da.getCount(),
                                String.valueOf(val)));
    }
  }

  private DataAvg addDv(final String name,
                        final SysCode scode) {
    final DataAvg dv = new DataAvg(name, scode);

    dvMap.put(scode, dv);

    return dv;
  }
}
