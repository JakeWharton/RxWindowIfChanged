package com.jakewharton.rx;

final class Message {
  final String user;
  final String message;

  Message(String user, String message) {
    this.user = user;
    this.message = message;
  }

  @Override public String toString() {
    return user + " " + message;
  }
}
