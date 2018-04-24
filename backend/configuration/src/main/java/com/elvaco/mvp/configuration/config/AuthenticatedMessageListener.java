package com.elvaco.mvp.configuration.config;

import java.io.UnsupportedEncodingException;
import javax.annotation.Nullable;

import com.elvaco.mvp.consumers.rabbitmq.message.MessageListener;
import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.web.security.MvpUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;

@Slf4j
@RequiredArgsConstructor
class AuthenticatedMessageListener {

  private final MessageListener messageListener;

  /**
   * This is used through reflection in com.elvaco.mvp.configuration.config.RabbitMqConfig.
   *
   * @param message Message received from queue.
   *
   * @return A serialized json string containing response information to be placed on the response
   *   queue. When this is {@code null} no message will be places on the response routing queue.
   */
  @Nullable
  @SuppressWarnings("unused")
  public String handleMessage(byte[] message) {
    try {
      SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(new MvpUserDetails(meteringUser(), ""), null)
      );
      String encodedMessage = toEncodedMessage(message);

      log.debug("Received message from RabbitMQ: {}", encodedMessage);

      return messageListener.onMessage(encodedMessage);
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  private static User meteringUser() {
    return new User(
      randomUUID(),
      "Metering Message RabbitMQ Consumer",
      "noone@example.com",
      "",
      Language.sv,
      ELVACO,
      singletonList(Role.SUPER_ADMIN)
    );
  }

  private static String toEncodedMessage(byte[] message) {
    try {
      return new String(message, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Unsupported encoding.", e);
    }
  }
}
