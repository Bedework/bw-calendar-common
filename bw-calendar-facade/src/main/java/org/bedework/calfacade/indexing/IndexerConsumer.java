/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calfacade.indexing;


/**
 * User: mike Date: 7/5/19 Time: 23:27
 */
public class IndexerConsumer {
  final BwIndexer indexer;

  public IndexerConsumer(final BwIndexer indexer) {
    this.indexer = indexer;
  }

  public void consume(final Object o) {
    indexer.indexEntity(0);
  }
}
