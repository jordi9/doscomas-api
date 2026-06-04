package com.jordi9.doscomas.scenario

import com.jordi9.kogiven.ScenarioStringSpec

class PlanningShould : ScenarioStringSpec<GivenPlanning, WhenPlanning, ThenPlanning, PlanningContext>({

  "list spaces returns empty list initially" {
    Given.`no spaces exist`()
    When.`listing spaces`()
    Then.`the response is successful`()
      .and().`no resources are returned`()
  }

  "create a space" {
    Given.`no spaces exist`()
    When.`creating a space`()
    Then.`a resource was created`()
      .and().`the space id is public`()
      .and().`the space has name FIRE`()
      .and().`timestamps are current`()
  }

  "get a space by ID" {
    Given.`a space exists`()
    When.`getting the current space`()
    Then.`the response is successful`()
      .and().`the space has name FIRE`()
  }

  "create an account in a space" {
    Given.`a space exists`()
    When.`creating an account in the current space`()
    Then.`a resource was created`()
      .and().`the account id is public`()
      .and().`the account belongs to the current space`()
      .and().`the account has create defaults`()
  }

  "list accounts scoped to a space" {
    Given.`a space exists`()
      .and().`an account exists in the current space`("Cash")
    When.`listing accounts in the current space`()
    Then.`the response is successful`()
      .and().`one resource is returned`()
      .and().`the listed account belongs to the current space`()
  }

  "get account scoped to a space" {
    Given.`a space exists`()
      .and().`an account exists in the current space`("Cash")
    When.`getting the current account`()
    Then.`the response is successful`()
      .and().`the account has name`("Cash")
      .and().`the account belongs to the current space`()
  }

  "account in another space returns 404" {
    Given
      .`two spaces exist`()
      .and().`an account exists in the second space`()
    When.`getting the other space account from the first space`()
    Then.`the response is not found`()
  }

  "patch core account fields" {
    Given
      .`a space exists`()
      .and().`an account exists in the current space`("Cash", note = "old note")
    When.`patching core account fields`()
    Then
      .`the response is successful`()
      .and().`core account fields were patched`()
      .and().`updated at is current`()
  }

  "patch updates balance and balanceUpdatedAt" {
    Given.`a space exists`()
      .and().`an account exists in the current space`("Cash")
    When.`patching account balance`()
    Then
      .`the response is successful`()
      .and().`balance was patched`()
      .and().`balance updated at is current`()
  }

  "patch display fields" {
    Given.`a space exists`()
      .and().`an account exists in the current space`("Cash")
    When.`patching account display`()
    Then
      .`the response is successful`()
      .and().`display fields were patched`()
      .and().`updated at is current`()
  }

  "clear display fields and return empty display" {
    Given.`a space exists`()
      .and().`an account exists with display in the current space`()
    When.`clearing account display`()
    Then
      .`the response is successful`()
      .and().`display is empty`()
      .and().`display row was deleted`()
  }

  "clear nullable note" {
    Given.`a space exists`()
      .and().`an account exists in the current space`("Cash", note = "clear me")
    When.`clearing account note`()
    Then
      .`the response is successful`()
      .and().`note is null`()
  }

  "reject invalid money precision" {
    Given.`a space exists`()
    When.`creating an account with invalid money precision`()
    Then.`the response is bad request`()
  }

  "reject negative balance" {
    Given.`a space exists`()
    When.`creating an account with negative balance`()
    Then.`the response is bad request`()
  }

  "reject invalid category" {
    Given.`a space exists`()
    When.`creating an account with invalid category`()
    Then.`the response is bad request`()
  }

  "reject invalid ID prefix or format" {
    Given.`no spaces exist`()
    When.`getting a space with invalid id`()
    Then.`the response is bad request`()
  }

  "reject account creation for missing space" {
    Given.`no spaces exist`()
    When.`creating an account in a missing space`()
    Then.`the response is not found`()
  }
})
