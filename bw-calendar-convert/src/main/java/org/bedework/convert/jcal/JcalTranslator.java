/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.convert.jcal;

import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.ifs.IcalCallback;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.convert.EventTimeZonesRegistry;
import org.bedework.convert.IcalTranslator;

import net.fortuna.ical4j.model.Calendar;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: mike Date: 5/7/20 Time: 22:52
 */
public class JcalTranslator extends IcalTranslator {
  /**
   * Constructor:
   *
   * @param cb IcalCallback object for retrieval of entities
   */
  public JcalTranslator(
          final IcalCallback cb) {
    super(cb);
  }

  /**
   * @param val event
   * @param methodType icalendar method
   * @return JSON jcal
   */
  public String toJcal(final EventInfo val,
                       final int methodType) {
    String currentPrincipal = null;
    final BwPrincipal principal = cb.getPrincipal();

    if (principal != null) {
      currentPrincipal = principal.getPrincipalRef();
    }

    final List<EventInfo> eis = new ArrayList<>();

    eis.add(val);
    return JcalHandler.toJcal(eis, methodType,
                              currentPrincipal,
                              new EventTimeZonesRegistry(this, val.getEvent()));
  }

  /**
   * @param val calendar object
   * @return JSON jcal
   */
  public static String toJcal(final Calendar val) {
    return JcalHandler.toJcal(val);
  }

  /** Write a collection of calendar data as json
   *
   * @param vals collection of calendar data
   * @param methodType    int value fromIcalendar
   * @param wtr for output
   */
  public void writeJcal(final Collection<EventInfo> vals,
                        final int methodType,
                        final Writer wtr) {

    String currentPrincipal = null;
    final BwPrincipal principal = cb.getPrincipal();

    if (principal != null) {
      currentPrincipal = principal.getPrincipalRef();
    }

    JcalHandler.outJcal(wtr,
                        vals, methodType,
                        currentPrincipal,
                        new EventTimeZonesRegistry(this, null));
  }
}
