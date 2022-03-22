# Idempotency Questions

### Order Create

1. Single operation succeeds, response fails, Consumer tries again, what happens?

&ndash; Consumer gets response stored in a database

2. Operation fails, response fails, Consumer tries again, what happens?

&ndash; New order is published

3. Operation succeeds, response succeeds, but Consumer fails after the response, what happens?

&ndash; It doesn't matter, since consumer doesn't care this much

### Idempotency Key

Header `Idempotency-Key`

It can be anything you want, you can think about it as a correlation id

Only method that is parsing it is `POST /api/orders/`
