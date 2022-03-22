# Avro Specification

## User manipulation

### Key *{RAW}*

User Email **[STRING]**

### Value *{AVRO}*

1. event **[ENUM]**
   - CREATED
   - UPDATED_EMAIL
   - UPDATED_USERNAME
   - DELETED

2. previousEmail **[STRING]** *default: ""*
3. previousUsername **[STRING]** *default: ""*
4. username **[STRING]**
