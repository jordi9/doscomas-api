package com.jordi9.doscomas.scenario

import com.jordi9.kogiven.ScenarioStringSpec

class AppShould :
  ScenarioStringSpec<GivenApp, WhenApp, ThenApp, AppContext>({

    "run database migrations on startup" {
      Given.`the application has started`()
      When.`querying the database schema`()
      Then.`migrations have run`()
    }

    "respond to liveness health endpoint" {
      Given.`the application has started`()
      When.`checking the liveness health endpoint`()
      Then.`the health check is healthy`()
    }

    "respond to readiness health endpoint" {
      Given.`the application has started`()
      When.`checking the readiness health endpoint`()
      Then.`the readiness check is healthy`()
    }

    "expose metrics endpoint with Prometheus format" {
      Given.`the application has started`()
      When.`requesting the metrics endpoint`()
      Then.`metrics are returned in Prometheus format`()
    }

    "track HTTP request count metrics" {
      Given.`the application has started`()
      When
        .`making an HTTP request to hello endpoint`()
        .and()
        .`requesting the metrics endpoint`()
      Then.`metrics contain request counter for hello endpoint`()
    }

    "track HTTP request duration histogram" {
      Given.`the application has started`()
      When
        .`making an HTTP request to hello endpoint`()
        .and()
        .`requesting the metrics endpoint`()
      Then.`metrics contain request duration histogram`()
    }

    "allow CORS preflight requests from localhost" {
      Given.`the application has started`()
      When.`sending a CORS preflight request from localhost 5173`()
      Then.`CORS preflight response allows the origin`()
    }

    "include CORS headers in actual requests from localhost" {
      Given.`the application has started`()
      When.`sending an actual request with Origin header from localhost 5173`()
      Then.`CORS actual response includes allow origin header`()
    }

    "document every registered route in openapi.yaml" {
      Given.`the application has started`()
      When.`loading the OpenAPI spec`()
      Then.`every registered route is documented in openapi yaml`()
    }

    "not document routes that are not registered" {
      Given.`the application has started`()
      When.`loading the OpenAPI spec`()
      Then.`no orphaned routes exist in openapi yaml`()
    }
  })
