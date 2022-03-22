# Notification Service

Basic Notification service

Consists of Fetch REST API and acts as Consumer for Order Acknowledgment Events

**Consumer actions**

```mermaid
sequenceDiagram
    participant Order Acknowledgment Topic
    participant Notification Service
    participant User's EMail
    participant MongoDB
    loop Long Pooling
    Notification Service ->>+ Order Acknowledgment Topic: Pool
    Order Acknowledgment Topic ->>+ Notification Service: New messages
    deactivate Order Acknowledgment Topic
    Notification Service ->> User's EMail: EMail with Status
    Notification Service ->> MongoDB: Parsed Messages 
    end

```

**API
Specification:** [Swagger Hub](https://app.swaggerhub.com/apis-docs/ReDestroyDeR/notification-service-fetch-api/1.0.0)

### Avro Schemas

**Key**

```avro schema
{
  "type": "record",
  "name": "KeyOrderAcknowledgment",
  "namespace": "streamprocessing.avro",
  "fields": [
    {
      "name": "user_address",
      "type": "string"
    }
  ]
}
```

**Value**

```avro schema
{
  "type": "record",
  "name": "ValueOrderAcknowledgment",
  "namespace": "streamprocessing.avro",
  "fields": [
    {
      "name": "event",
      "type": {
        "type": "enum",
        "name": "OrderAckStatus",
        "symbols": [
          "ACK",
          "NACK"
        ]
      }
    },
    {
      "name": "orderId",
      "type": "string",
      "default": "-"
    },
    {
      "name": "orderTotalPrice",
      "type": "int",
      "default": 0
    },
    {
      "name": "userBalance",
      "type": "int",
      "default": 0
    }
  ]
}
```