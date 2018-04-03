package com.elvaco.mvp.configuration.config;

import java.util.Collections;
import java.util.UUID;

import com.elvaco.mvp.consumers.rabbitmq.MeteringMessageReceiver;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.web.security.MvpUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;

class AuthenticatingMeteringMessageReceiver {

  private final MeteringMessageReceiver meteringMessageReceiver;

  AuthenticatingMeteringMessageReceiver(MeteringMessageReceiver meteringMessageReceiver) {
    this.meteringMessageReceiver = meteringMessageReceiver;
  }

  @SuppressWarnings("unused")
  //this is used through reflection in com.elvaco.mvp.configuration.config.RabbitMqConfig
  public String receiveMessage(byte[] message) {
    User user = new User(
      UUID.randomUUID(),
      "Metering Message "
        + "RabbitMQ Consumer",
      "noone@example.com",
      "",
      ELVACO,
      Collections.singletonList(
        Role.SUPER_ADMIN
      )
    );
    SecurityContextHolder.getContext().setAuthentication(
      new UsernamePasswordAuthenticationToken(new MvpUserDetails(user, ""), null)
    );
    try {
      return meteringMessageReceiver.receiveMessage(message);
    } finally {
      SecurityContextHolder.clearContext();
    }
  }
}
