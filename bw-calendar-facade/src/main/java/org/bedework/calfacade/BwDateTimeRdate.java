package org.bedework.calfacade;

/**
 * Solely to get around hibernate mapping problems.
 * Once we move to orm.xml remove this
 */
public class BwDateTimeRdate extends BwDateTime {
  public static BwDateTimeRdate make(final BwDateTime val) {
    if (val == null) {
      return null;
    }
    if (val instanceof BwDateTimeRdate) {
      return (BwDateTimeRdate)val;
    }

    final var bwd = new BwDateTimeRdate();
    bwd.setDateType(val.getDateType());
    bwd.setDtval(val.getDtval());
    bwd.setDate(val.getDate());
    bwd.setTzid(val.getTzid());
    bwd.setFloatFlag(val.getFloatFlag());

    return bwd;
  }
}
