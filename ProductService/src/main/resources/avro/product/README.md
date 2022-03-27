# Avro Specification

## Products

### Key *{RAW}*

Product Name **[STRING]**

### Value one of *{AVRO}*

1. ProductAdded
    1. Price per unit **[INTEGER]**
    2. Added **[INTEGER]**
2. ProductSubtracted
    1. Subtracted **[INTEGER]**
3. ProductReserved
    1. Reserved **[INTEGER]**
4. ProductUnreserved
    1. Unreserved **[INTEGER]**
5. ProductTableValue
    1. price **[INTEGER]**
    2. quantity **[INTEGER]**
    3. reserved **[INTEGER]**

## Comments

### Key *{AVRO}*

1. Product Name **[STRING]**
2. Author email **[STRING]**

### Value *{AVRO}*

1. Contents **[STRING]**
2. Recommend **[BOOLEAN]**

