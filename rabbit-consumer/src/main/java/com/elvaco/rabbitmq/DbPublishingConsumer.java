package com.elvaco.rabbitmq;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class DbPublishingConsumer extends DefaultConsumer {

  public static final String SQL_UNIQUE_VIOLATION_ERROR = "23505";

  private final int batchSz;

  private static ScheduledFuture<?> timerFuture;
  private static Runnable timerTask;
  private static ScheduledExecutorService ses;
  private Connection connection;

  DbPublishingConsumer(Channel channel, int batchSz) {
    super(channel);
    ses = Executors.newSingleThreadScheduledExecutor();
    this.batchSz = batchSz;
    timerTask = () -> {
      try {
        MeterMessageFlushResult result = flushQueue();
        handleFlushResult(result);
      } catch (IOException ex) {
        System.out.println("Error: " + ex.getMessage());
      }
    };
    scheduleTimer();
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not find PostgreSQL driver :(");
    }
  }

  class TaggedMessage {
    private final long tag;
    private final MeterMessage message;

    public TaggedMessage(long tag, MeterMessage message) {
      this.tag = tag;
      this.message = message;
    }

    @Override
    public String toString() {
      return "TaggedMessage{" + "tag=" + tag + ", message=" + message + '}';
    }
  }

  class MeterMessageFlushResult {

    private final List<TaggedMessage> successful;
    private final List<TaggedMessage> failed;

    public MeterMessageFlushResult() {
      successful = new ArrayList<>();
      failed = new ArrayList<>();
    }

    public void addSuccessful(Collection<TaggedMessage> messages) {
      successful.addAll(messages);
    }

    public void addSuccessful(TaggedMessage message) {
      successful.add(message);
    }

    public void addFailed(TaggedMessage message) {
      failed.add(message);
    }

    public boolean allSuccessful() {
      return failed.isEmpty();
    }

    public long getHighestTag() {
      TaggedMessage max = Collections.max(successful, (o1, o2) -> Long.compare(o1.tag, o2.tag));
      return max.tag;
    }

    public int size() {
      return successful.size() + failed.size();
    }
  }

  private static void scheduleTimer() {
    timerFuture = ses.scheduleAtFixedRate(timerTask, 10, 10, TimeUnit.SECONDS);
  }

  private void resetTimer() {
    if (timerFuture != null) {
      timerFuture.cancel(false);
    }
    scheduleTimer();
  }

  private void addMeasurementToStatement(
    PreparedStatement statement,
    String organisationId,
    String meterId,
    String medium,
    String quantity,
    String unit,
    LocalDateTime timestamp,
    double value
  ) throws SQLException {
    statement.setString(1, organisationId); //organisation.name
    statement.setString(2, meterId); //organisation.name
    statement.setString(3, medium);
    statement.setString(4, quantity);
    statement.setString(5, unit);
    statement.setObject(6, timestamp);
    statement.setDouble(7, value);
  }

  private PreparedStatement newStatement() throws SQLException {
    if (connection == null) {
      connection = DriverManager.getConnection(
        "jdbc:postgresql://127.0.0.1:5432/mvp",
        "mvp",
        "mvp"
      );
    }
    return connection.prepareStatement("SELECT add_measurement(?, ?, ?, ?, ?, ?, ?)");
  }

  private MeterMessageFlushResult flushQueue() {
    System.out.println(" [x] Flushing queue!");
    MeterMessageFlushResult result = new MeterMessageFlushResult();
    PreparedStatement statement;
    try {
      statement = newStatement();
    } catch (SQLException e) {
      System.out.println(" [E!] Database error: " + e.getMessage());
      return result;
    }

    List<TaggedMessage> batchedMessages = new ArrayList<>();
    for (; ; ) {
      try {
        TaggedMessage tm = unackedMessages.remove();
        batchedMessages.add(tm);
        MeterMessage mm = tm.message;
        for (Value v : mm.getValues()) {
          addMeasurementToStatement(
            statement,
            mm.getOrganisationId(),
            mm.getMeterId(),
            mm.getMedium(),
            v.getQuantity(),
            v.getUnit(),
            LocalDateTime.parse(v.getTimestamp()),
            (double) v.getValue()
          );
          statement.addBatch();
        }
      } catch (NoSuchElementException ex) {
        break;
      } catch (SQLException e) {
        System.out.println(" [E!] SQL error while adding batch: " + e.getMessage());
        //TODO: reject/nack here?
        break;
      }
    }

    try (PreparedStatement stmt = statement) {
      stmt.executeBatch();
      result.addSuccessful(batchedMessages);
    } catch (SQLException e) {
      System.out.println(" [E!] SQL error while adding batch: " + e.getMessage());
      result = flushMessages(batchedMessages);
    }
    System.out.println(String.format(" [x] Flushed %d messages!", result.successful.size()));
    return result;
  }

  private MeterMessageFlushResult flushMessages(List<TaggedMessage> batchedMessages) {
    MeterMessageFlushResult result = new MeterMessageFlushResult();
    PreparedStatement statement;
    try {
      statement = newStatement();
    } catch (SQLException e) {
      System.out.println(" [E!] Database error: " + e.getMessage());
      return result;
    }
    for (TaggedMessage message : batchedMessages) {
      try {
        MeterMessage mm = message.message;
        for (Value v : mm.getValues()) {
          statement.clearParameters();
          addMeasurementToStatement(
            statement,
            mm.getOrganisationId(),
            mm.getMeterId(),
            mm.getMedium(),
            v.getQuantity(),
            v.getUnit(),
            LocalDateTime.parse(v.getTimestamp()),
            (double) v.getValue()
          );
          try {
            statement.execute();
          } catch (SQLException ex) {
            // we don't want to retry these because we already have them
            if (!ex.getSQLState().equals(SQL_UNIQUE_VIOLATION_ERROR)) {
              throw ex;
            }
          }
          result.addSuccessful(message);
        }
      } catch (SQLException e) {
        result.addFailed(message);
      }
    }
    return result;
  }

  private void handleFlushResult(MeterMessageFlushResult result) throws IOException {
    if (result.allSuccessful()) {
      getChannel().basicAck(result.getHighestTag(), true);
    } else {
      System.out.println(String.format(
        " [W] %d/%d messages successfully flushed, NACK'ing failed messages!",
        result.successful.size(),
        result.size()
      ));
      for (TaggedMessage m : result.failed) {
        getChannel().basicNack(m.tag, false, true);
      }
      for (TaggedMessage m : result.successful) {
        getChannel().basicAck(m.tag, false);
      }
    }
  }

  private final Gson gson = new Gson();
  private final Queue<TaggedMessage> unackedMessages = new ConcurrentLinkedQueue<>();

  @Override
  public void handleDelivery(
    String consumerTag,
    Envelope envelope,
    AMQP.BasicProperties properties,
    byte[] body
  ) throws IOException {
    String message = new String(body, "UTF-8");
    try {
      MeterMessage meterMessage = gson.fromJson(message, MeterMessage.class);
      unackedMessages.add(new TaggedMessage(envelope.getDeliveryTag(), meterMessage));
    } catch (JsonParseException ex) {
      System.out.println(" [W] An unparseable message was received, rejecting without requeue!");
      getChannel().basicNack(envelope.getDeliveryTag(), false, false);
    } catch (RuntimeException ex) {
      System.out.println(
        " [E!] Runtime error while queueing message - seeing this means you've found a bug! ");
      ex.printStackTrace();
    } finally {
      resetTimer();
      if (unackedMessages.size() >= batchSz) {
        try {
          MeterMessageFlushResult result = flushQueue();
          handleFlushResult(result);
        } catch (Exception ex) {
          System.out.println("Exception while flushing message(s): ");
          ex.printStackTrace();
        }
      }
    }
  }
}
