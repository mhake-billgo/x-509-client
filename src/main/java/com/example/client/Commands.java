package com.example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class Commands {
  private static final Logger log = LoggerFactory.getLogger(Commands.class);

  @ShellMethod("Invoke Server API")
  public String invoke(@ShellOption(value = "--text", defaultValue = ShellOption.NULL) String text) {
    log.info("Invoking API call with text = {}", text);
    return text;
  }
}
