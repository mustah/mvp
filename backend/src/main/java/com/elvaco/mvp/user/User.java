package com.elvaco.mvp.user;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class User {

  @Id
  @GeneratedValue
  Long id;

  private String firstName;
  private String lastName;
  private String company; // TODO : should be an instance of Company once we have such an object!

  public User() {
  }

  public User(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public Long getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Get the company associated with this user.
   *
   * @return the company
   */
  public String getCompany() {
    return company;
  }

  /**
   * Set the company associated with this user.
   *
   * @param company the company to set
   */
  public void setCompany(String company) {
    this.company = company;
  }
}
