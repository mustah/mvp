package com.elvaco.rabbitmq;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import com.google.gson.Gson;

public class DirectoryMeterMessageProducer implements Iterator<MeterMessage> {

  private final Iterator<Path> directoryIterator;

  DirectoryMeterMessageProducer(File inputDirectory) throws IOException {
    if (!inputDirectory.isDirectory() || !inputDirectory.exists() || !inputDirectory.canRead()) {
      throw new IllegalArgumentException(
        "Invalid input directory: " + inputDirectory.getAbsolutePath()
      );
    }
    directoryIterator = Files.newDirectoryStream(inputDirectory.toPath()).iterator();
  }

  @Override
  public boolean hasNext() {
    return directoryIterator.hasNext();
  }

  @Override
  public MeterMessage next() {
    Path p = directoryIterator.next();
    Gson gson = new Gson();
    byte[] contents;
    try {
      contents = Files.readAllBytes(p);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
    return gson.fromJson(new String(contents), MeterMessage.class);
  }
}
