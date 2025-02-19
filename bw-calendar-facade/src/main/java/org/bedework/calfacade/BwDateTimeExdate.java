package org.bedework.calfacade;

/**
 * Solely to get around hibernate mapping problems.
 * Once we move to orm.xml remove this
 */
public class BwDateTimeExdate extends BwDateTime {
  public static BwDateTimeExdate make(final BwDateTime val) {
    if (val == null) {
      return null;
    }
    if (val instanceof BwDateTimeExdate) {
      return (BwDateTimeExdate)val;
    }

    final var bwd = new BwDateTimeExdate();
    bwd.setDateType(val.getDateType());
    bwd.setDtval(val.getDtval());
    bwd.setDate(val.getDate());
    bwd.setTzid(val.getTzid());
    bwd.setFloatFlag(val.getFloatFlag());

    return bwd;
  }
}
