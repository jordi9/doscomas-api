# Dos Comas API

Dos Comas API supports financial planning data for people managing personal or shared financial lives.

## Language

**User**:
A person who logs in to Dos Comas.
_Avoid_: Account, customer

**Space**:
A shared financial planning environment for one or more users.
_Avoid_: Household, workspace, team, family, home

**Account**:
A financial account or asset row inside a space, covering cash, investments, pension plans, property equity, and autónomo planning rows.
_Avoid_: Financial account, position, asset, holding, balance

**Balance**:
The current estimated value of an account for net-worth and planning calculations.
_Avoid_: Amount, total

**Monthly Contribution**:
The expected recurring monthly cash flow associated with an account. Positive values are inflows or savings contributions; negative values are outflows.
_Avoid_: Contribution when it only means investment deposits

**Account Category**:
A fixed planning group for accounts: cash, investment, private pension, real estate, social security, or other.
_Avoid_: Mixed-language category values, user-defined labels

**Account Display**:
Presentation details for how an account is shown in Dos Comas, separate from the account's financial meaning.
_Avoid_: Treating display preferences as core account data
