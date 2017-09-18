package com.elvaco.mvp.entity.user;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Entity
@Access(AccessType.FIELD)
@Table(name = "users")
public class UserEntity {

  @Id
  @GeneratedValue
  public Long id;
  public String firstName;
  public String lastName;
  public String email;
  /**
   * Company associated with this user.
   */
  public String company; // TODO : should be an instance of Company once we have such an object!

  public UserEntity() {}

  public UserEntity(String firstName, String lastName, String email, String company) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.company = company;
  }
}
