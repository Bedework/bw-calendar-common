package org.bedework.calfacade;

/**
 * Solely to get around hibernate mapping problems.
 * Once we move to orm.xml remove this
 */
public class BwDateTimeDtEnd extends BwDateTime {
  public static BwDateTimeDtEnd make(final BwDateTime val) {
    if (val == null) {
      return null;
    }

    if (val instanceof BwDateTimeDtEnd) {
      return (BwDateTimeDtEnd)val;
    }

    final BwDateTimeDtEnd bwd = new BwDateTimeDtEnd();
    bwd.setDateType(val.getDateType());
    bwd.setDtval(val.getDtval());
    bwd.setDate(val.getDate());
    bwd.setTzid(val.getTzid());
    bwd.setFloatFlag(val.getFloatFlag());

    return bwd;
  }
}
