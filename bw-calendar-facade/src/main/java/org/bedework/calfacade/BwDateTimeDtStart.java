package org.bedework.calfacade;

/**
 * Solely to get around hibernate mapping problems.
 * Once we move to orm.xml remove this
 */
public class BwDateTimeDtStart extends BwDateTime {
  public static BwDateTimeDtStart make(final BwDateTime val) {
    if (val == null) {
      return null;
    }

    if (val instanceof BwDateTimeDtStart) {
      return (BwDateTimeDtStart)val;
    }

    final var bwd = new BwDateTimeDtStart();
    bwd.setDateType(val.getDateType());
    bwd.setDtval(val.getDtval());
    bwd.setDate(val.getDate());
    bwd.setTzid(val.getTzid());
    bwd.setFloatFlag(val.getFloatFlag());

    return bwd;
  }
}
