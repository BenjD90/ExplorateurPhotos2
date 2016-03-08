package com.benjd90.photos2.beans;

/**
 * Created by Benjamin on 07/03/2016.
 */
public class State {
  private String actualPath;
  private long lastEnd;
  private long lastStart;
  private String lastRunEndState;

  public void setActualPath(String actualPath) {
    this.actualPath = actualPath;
  }

  public void setLastRunEndState(String lastRunEndState) {
    this.lastRunEndState = lastRunEndState;
  }

  public void setLastStart(long lastStart) {
    this.lastStart = lastStart;
  }

  public void setLastEnd(long lastEnd) {
    this.lastEnd = lastEnd;
  }

  public String getActualPath() {
    return actualPath;
  }

  public long getLastEnd() {
    return lastEnd;
  }

  public long getLastStart() {
    return lastStart;
  }

  public String getLastRunEndState() {
    return lastRunEndState;
  }
}
