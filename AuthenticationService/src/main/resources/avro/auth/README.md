# Avro Specification

## User manipulation

### Key *{RAW}*

User Email **[STRING]**

### Value Type oneOf *{AVRO}*

1. UserCreated
2. UserUpdatedEmail
   1. previousEmail **[STRING]**
3. UserDeleted *gets sent twice for no particular reason*
