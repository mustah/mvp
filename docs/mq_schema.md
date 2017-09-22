# MVP MQ Message Protocol Specification
The MVP MQ Message Protocol describes the format of messages that contain metering values, delivered from an external system to MVP via a message broker (specifically RabbitMQ, in our case). It is a JSON-based protocol. It is defined by the [mq_json_message_protocol.json](schema/mq_json_message_protocol.json) json-schema.

## Purpose
Since we want to be able to accept meter values from various different systems, it is important that we are able to decide on a common language. This protocol is intended to serve as that language.

## Rationale
JSON is chosen because:
- it is human readable & and writable (no decoder/encoder necessary)
- it has extensive programming language support, either in the form of libraries, or natively
- its limitations are well understood

Additionally, specifying a json-schema allows us to easily generate data container classes, as well as automatically validate messages against the schema.

## Examples:
A message might, for example look like [this](schema/mq_json_message_protocol_example.json):

```JSON
{
  "message_type" : "Elvaco MVP MQ Message 1.0",
  "gateway_id": "GW-CME3100-XXYYZZ",
  "meter_id": "ABC-123-XYZ",
  "organisation_id": "Elvaco AB",
  "medium": "Electricity meter",
  "source_system_id": "Elvaco Metering",
  "values": [
    {
      "timestamp": 1506069947,
      "value": 0.659,
      "unit": "wH",
      "quantity": "power"
    }
  ]
}
```

Another non-sensical, although technically valid message might look like this (courtesy of [Json Schema Faker](http://json-schema-faker.js.org/)):
```JSON
{
  "message_type": "Elvaco MVP MQ Message 1.0",
  "gateway_id": "non laboris",
  "meter_id": "aliquip consectetur",
  "organisation_id": "Lorem",
  "medium": "exercitation ipsum irure a",
  "source_system_id": "ad dolor reprehenderit occaecat",
  "values": [
    {
      "timestamp": 25465315,
      "value": "1953-07-10T11:47:18.029Z",
      "unit": "Duis eiusmod dolore",
      "quantity": "voluptate ea exercitation amet"
    },
    {
      "timestamp": 75096078,
      "value": 8634299,
      "unit": "sint nulla in minim",
      "quantity": "occaecat",
      "accumulated": false
    },
    {
      "timestamp": 14087656,
      "value": -35226208.01811245,
      "unit": "labore fugiat",
      "quantity": "sint cillum",
      "accumulated": true
    },
    {
      "timestamp": 28214906,
      "value": "2000-06-07T18:25:55.809Z",
      "unit": "cupidatat reprehenderit commodo mollit",
      "quantity": "aliqua sint"
    }
  ]
}
```
