package com.jordi9.doscomas.feature.planning.outbound.account

import org.intellij.lang.annotations.Language

@Language("SQL")
const val INSERT_ACCOUNT = """
  INSERT INTO accounts (
    id,
    space_id,
    name,
    category,
    balance_cents,
    monthly_contribution_cents,
    currency,
    note,
    display_json,
    created_at,
    updated_at
  ) VALUES (
    :id,
    :spaceId,
    :name,
    :category,
    :balanceCents,
    :monthlyContributionCents,
    :currency,
    :note,
    :displayJson,
    :createdAt,
    :updatedAt
  )
  RETURNING *
"""

@Language("SQL")
const val UPDATE_ACCOUNT = """
  UPDATE accounts
  SET name = :name,
      category = :category,
      balance_cents = :balanceCents,
      monthly_contribution_cents = :monthlyContributionCents,
      currency = :currency,
      note = :note,
      display_json = :displayJson,
      updated_at = :updatedAt
  WHERE id = :id
  RETURNING *
"""
