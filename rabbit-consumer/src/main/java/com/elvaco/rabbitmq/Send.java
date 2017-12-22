package com.elvaco.rabbitmq;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public final class Send {

  private static final String QUEUE_NAME = "meter-messages";

  private Send() {}

  public static void main(String[] argv) throws IOException, TimeoutException {
    Options options = new Options();
    Option inputDirectory = new Option("i", "input", true, "message input directory");
    options.addOption(inputDirectory);
    CommandLineParser parser = new BasicParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd;
    try {
      cmd = parser.parse(options, argv);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("meter-message-producer", options);

      System.exit(1);
      return;
    }

    String inputDirectoryPath = cmd.getOptionValue("input");
    Iterator<MeterMessage> messageProducer;
    if (inputDirectoryPath == null) {
      System.out.println("No input directory specified, will generate random seed data instead!");
      messageProducer = new RandomMeterMessageProducer();
    } else {
      messageProducer = new DirectoryMeterMessageProducer(new File(inputDirectoryPath));
    }
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    factory.setPort(5672);
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    try {
      produceMessages(channel, messageProducer, 10);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      channel.close();
      connection.close();
    }
  }

  private static void produceMessages(
    Channel channel,
    Iterator<MeterMessage> messageProducer,
    int throttleBound
  ) throws InterruptedException, IOException {
    while (messageProducer.hasNext()) {
      MeterMessage meterMessage = messageProducer.next();
      Gson gson = new Gson();
      String message = gson.toJson(meterMessage);
      channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
      System.out.println(" [x] Sent '" + message + "'");
      Thread.sleep(new Random().nextInt(throttleBound));
    }
  }
}
