package com.elvaco.mvp.core.domainmodels;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Builder
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class Asset {

  public final AssetType assetType;
  public final String contentType;
  public final byte[] content;
  public final String checksum;

  @SuppressWarnings("unused") // This is a Lombok skeleton, it's not unused
  public static class AssetBuilder {
    private byte[] content;
    private String checksum;

    public AssetBuilder checksum(String checksum) {
      throw new IllegalArgumentException("Checksum is automatically calculated through content()");
    }

    public AssetBuilder content(byte[] content) {
      this.content = content;
      this.checksum = calculateChecksum(content);
      return this;
    }

    private String calculateChecksum(byte[] content) {
      byte[] md5;
      try {
        md5 = MessageDigest.getInstance("MD5").digest(content);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      }
      StringBuilder checksum = new StringBuilder(md5.length * 2);
      for (byte b : md5) {
        checksum.append(Character.forDigit((b >> 4) & 0xF, 16));
        checksum.append(Character.forDigit((b & 0xF), 16));
      }
      return checksum.toString();
    }
  }
}
