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
    try {
      Path path = directoryIterator.next();
      byte[] contents = Files.readAllBytes(path);
      return new Gson().fromJson(new String(contents, "UTF-8"), MeterMessage.class);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
