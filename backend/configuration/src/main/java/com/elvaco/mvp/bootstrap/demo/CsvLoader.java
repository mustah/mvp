package com.elvaco.mvp.bootstrap.demo;

import java.io.File;
import java.net.URL;
import javax.annotation.Nullable;

import lombok.ToString;
import org.simpleflatmapper.csv.CsvMapper;
import org.simpleflatmapper.csv.CsvMapperFactory;
import org.simpleflatmapper.csv.CsvParser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;

import static java.util.Objects.requireNonNull;

@Profile("demo")
public class CsvLoader implements CommandLineRunner {

  private CsvLoader() {}

  public static void main(String[] args) throws Exception {
    testing();
  }

  @Override
  public void run(String... args) throws Exception {
    testing();
  }

  private static void testing() throws java.io.IOException {
    URL resource = CsvLoader.class.getClassLoader()
      .getResource("data/perstorp_metering_export_meters.csv");

    File file = new File(requireNonNull(resource).getFile());

    CsvMapper<MyObject> mapper = CsvMapperFactory.newInstance().newMapper(MyObject.class);

    CsvParser.separator(';')
      .mapWith(mapper)
      .stream(file, stream -> {
        stream.forEach(System.out::println);
        return stream;
      });
  }

  @ToString
  public static class MyObject {

    public final Long meterId;
    public final String meterStatus;
    public final String facility;
    public final String address;
    public final String city;
    public final String medium;
    public final String meterManufacturer;
    public final String phone;
    @Nullable
    public final String ip;
    @Nullable
    public final String port;

    public final Long gatewayId;
    public final String gatewayProductModel;
    public final String gatewayStatus;

    public MyObject(
      String facility,
      String address,
      String city,
      String medium,
      Long meterId,
      String meterManufacturer,
      Long gatewayId,
      String gatewayProductModel,
      String phone,
      String ip,
      String port,
      String meterStatus,
      String gatewayStatus
    ) {
      this.facility = facility;
      this.address = address;
      this.city = city;
      this.medium = medium;
      this.meterId = meterId;
      this.meterManufacturer = meterManufacturer;
      this.gatewayId = gatewayId;
      this.gatewayProductModel = gatewayProductModel;
      this.phone = phone;
      this.ip = toNull(ip);
      this.port = toNull(port);
      this.meterStatus = meterStatus;
      this.gatewayStatus = gatewayStatus;
    }
  }

  @Nullable
  private static String toNull(String value) {
    return value.equals("NULL") ? null : value;
  }
}
