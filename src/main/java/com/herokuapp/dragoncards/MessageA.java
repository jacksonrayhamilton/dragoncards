package com.herokuapp.dragoncards;


public class MessageA {
  private String body;

  public MessageA(String body) {
    this.setBody(body);
  }

  public String getBody() {
    return this.body;
  }

  public void setBody(String body) {
    this.body = body;
  }
}
