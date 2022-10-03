package org.bedework.calfacade.base;

import org.bedework.calfacade.BwXproperty;

import java.util.List;

/**
 * User: mike Date: 10/2/22 Time: 23:09
 */
public interface ConceptEntity {
  List<BwXproperty> getConcepts();

  BwXproperty makeConcept(String val);
}
