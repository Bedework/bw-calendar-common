package org.bedework.calfacade;

import org.bedework.calfacade.base.Differable;
import org.bedework.util.misc.ToString;
import org.bedework.util.misc.Util;

import net.fortuna.ical4j.model.component.Vote;
import net.fortuna.ical4j.model.property.PollItemId;
import net.fortuna.ical4j.model.property.Response;

import static net.fortuna.ical4j.model.Property.POLL_ITEM_ID;
import static net.fortuna.ical4j.model.Property.RESPONSE;

public class BwVote
        implements Comparable<BwVote>, Differable<BwVote> {
  private final BwParticipant parent;

  // Derived from the participant object.
  private String stringRepresentation;

  private Vote vote;

  /** Constructor
   *
   */
  public BwVote(final BwParticipant parent,
                final Vote vote) {
    this.parent = parent;
    this.vote = vote;
  }

  public void setPollItemId(final int val) {
    final var props = getVote().getProperties();
    final var p = (PollItemId)props.getProperty(POLL_ITEM_ID);
    final String sval = String.valueOf(val);

    if (p == null) {
      props.add(new PollItemId(val));
    } else if (!sval.equals(p.getValue())) {
      p.setValue(sval);
    }
    parent.changed();
  }

  /**
   *
   * @return int    the poll item id
   */
  public int getPollItemId() {
    final var p = (PollItemId)getVote().
            getProperties().
            getProperty(POLL_ITEM_ID);
    if (p == null) {
      return -1;
    }
    return p.getPollitemid();
  }

  public void setResponse(final int val) {
    final var props = getVote().getProperties();
    final var p = (Response)props.getProperty(RESPONSE);

    if (p == null) {
      props.add(new Response(val));
    } else if (val != p.getResponse()) {
      p.setValue(String.valueOf(val));
    }
    parent.changed();
  }

  /**
   *
   * @return int    the vote response
   */
  public int getResponse() {
    final var p = (Response)getVote().
            getProperties().
            getProperty(RESPONSE);
    if (p == null) {
      return -1;
    }
    return p.getResponse();
  }

  public Vote getVote() {
    if (vote == null) {
      vote = new Vote();
    }

    return vote;
  }

  @Override
  public int hashCode() {
    return 19 + 17 * (getPollItemId() + 3) *
            (getResponse() + 3);
  }

  @Override
  public int compareTo(final BwVote that) {
    if (this == that) {
      return 0;
    }

    final var res = Util.cmpIntval(getPollItemId(),
                                   that.getPollItemId());
    if (res != 0) {
      return res;
    }

    return Util.cmpIntval(getResponse(), that.getResponse());
  }

  @Override
  public boolean differsFrom(final BwVote that) {
    return (Util.cmpIntval(getPollItemId(),
                           that.getPollItemId()) != 0) ||
            (Util.cmpIntval(getResponse(),
                            that.getResponse()) != 0);
  }

  public String asString() {
    if (stringRepresentation == null) {
      stringRepresentation = vote.toString();
    }
    return stringRepresentation;
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    return ts.newLine().
             append(asString()).
             toString();
  }
}
