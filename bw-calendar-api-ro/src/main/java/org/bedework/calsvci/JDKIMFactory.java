package org.bedework.calsvci;

import org.apache.james.jdkim.api.JDKIM;

public interface JDKIMFactory {
  JDKIM getJDKIMImpl();
}
