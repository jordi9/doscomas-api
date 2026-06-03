# Store account money as cents and expose decimal strings

Dos Comas stores account balances and monthly contributions as integer cents internally, while the public API accepts and returns decimal string euro amounts such as `"8420.00"`. This keeps storage and calculations exact without making the user-facing API speak in minor units; API inputs with more than two decimal places are rejected until provider sync introduces a separate raw snapshot model.
