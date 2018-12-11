package com.elvaco.mvp.configuration.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.elvaco.mvp.core.security.AuthenticatedUser;

import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
public class ApmInterceptor implements HandlerInterceptor {
  private final AuthenticatedUser currentUser;

  @Override
  public void afterCompletion(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler,
    @Nullable Exception ex
  ) {
    Transaction currentTransaction = ElasticApm.currentTransaction();
    if (ex != null) {
      currentTransaction.captureException(ex);
    }

    if (currentUser == null) {
      return;
    }

    currentTransaction.setUser(
      currentUser.getUserId().toString(), currentUser.getUsername(), currentUser.getUsername()
    );

    currentTransaction.addTag("organisation", currentUser.getOrganisationId().toString());
    currentTransaction.addTag("super-admin", currentUser.isSuperAdmin() ? "yes" : "no");
    currentTransaction.addTag("admin", currentUser.isAdmin() ? "yes" : "no");
  }
}
