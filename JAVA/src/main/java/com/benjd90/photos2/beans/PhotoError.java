package com.benjd90.photos2.beans;

/**
 * Created by Benjamin on 24/04/2016.
 */
public class PhotoError {
  String kind;
  String path;
  String message;


  public PhotoError(String kind, String path, String message) {
    this.kind = kind;
    this.path = path;
    this.message = message;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
