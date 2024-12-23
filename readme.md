# Saving in the Democratic Republic of Congo (DRC)

- The idea is to have a system of accounts that people can join if they are properly KYC-ed.
- Typically these will be people from DRC as well as the diaspora.
- (Small - size 10) random groups of people can create and join Group Accounts.
- All group members (can) send money to this group account, they'd typically do so on a regular (for instance monthly) basis.
- All group members have access to the group account in the sense that they can also transfer the total funds of the group account to their personal account and use it as they see fit.
- The system assumes group members decide amongst each other when this is appropriate.  

### Notes
- We will build a technical solution, that is controlled by the central bank of DRC.
- 'Real' money will be send by Telpos and EU-based banks to a central bank account number held by the central bank of DRC.  
- Money leaves the system in the same way.  
- We should provide the tools to reconcile these funds.


## Commands for DRC Savings

**CREATE ACCOUNT** _ACCOUNT-HOLDER-NAME_ **[IN EUR]**

Returns: an _ACCOUNT-NUMBER_, referring to an account with a balance of € 0, that belongs to a person called _ACCOUNT-HOLDER-NAME_. The assumption is that this person is KYC-ed.
Another assumption is that the central bank will maintain accounts in EUR and USD as well as Congolese Francs.

**CREATE GROUP ACCOUNT** _GROUP-NAME_ **[IN EUR]**

Returns: an _ACCOUNT-NUMBER_, referring to an account with a balance of € 0. In isolation, nobody can transfer funds to this account, unless accounts join the group.

_ACCOUNT-HOLDER-NAME_ **JOIN GROUP** _GROUP-NAME_

Returns: a boolean True if averything was OK.
Assumption: authentication and autorisation is outside of our system

**DEPOSIT** _AMOUNT_ **TO** _ACCOUNT-HOLDER-NAME_ 

**DEPOSIT** _AMOUNT_ **TO** _ACCOUNT-NUMBER_ 

Returns: a boolean True if averything was OK, note that both the Account and Group Account should be in the same currency.
--> Amounts in cents?

**WITHDRAW** _AMOUNT_ **FROM** _ACCOUNT-HOLDER-NAME_

**WITHDRAW** _AMOUNT_ **FROM** _ACCOUNT-NUMBER_


dass

**TRANSFER** _AMOUNT_ **FROM** _ACCOUNT_ **TO** _ACCOUNT_  

Where:
- _ACCOUNT_ can be a [_ACCOUNT-HOLDER-NAME_ | _GROUP-NAME_ | _ACCOUNT-NUMBER_ ]

Restrictions:
- From a personal account to another personal account
- To a personal account to a group that person has joined.
- From a group that person joined to a personal account.







