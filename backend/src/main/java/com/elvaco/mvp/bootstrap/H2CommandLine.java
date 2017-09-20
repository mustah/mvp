package com.elvaco.mvp.bootstrap;

import java.math.BigInteger;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.elvaco.mvp.config.InMemory;

@InMemory
@Component
public class H2CommandLine implements CommandLineRunner {

  private final DataSource dataSource;

  @Autowired
  public H2CommandLine(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public void run(String... args) throws Exception {
    Statement statement = dataSource.getConnection().createStatement();

    statement.execute("CREATE ALIAS is_prime FOR \"com.elvaco.mvp.bootstrap.H2CommandLine.isPrime\"");
    statement.execute("CREATE ALIAS jsonb_exists FOR \"com.elvaco.mvp.bootstrap.H2CommandLine.jsonbContains\"");
    statement.execute("CREATE ALIAS jsonb_contains FOR \"com.elvaco.mvp.bootstrap.H2CommandLine.jsonbContains\"");
  }

  public static boolean isPrime(int value) {
    return new BigInteger(String.valueOf(value)).isProbablePrime(100);
  }

  public static boolean jsonbContains(Object json, Object s) {
    return true;
  }

  public static boolean jsonbExists(Object json, Object s) {
    return true;
  }
}
