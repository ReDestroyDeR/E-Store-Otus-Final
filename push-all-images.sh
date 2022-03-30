#!/bin/bash
docker push redestroyder/order-service -a
docker push redestroyder/notification-service -a
docker push redestroyder/billing-service -a
docker push redestroyder/authentication-service -a
docker push redestroyder/product-service -a
docker push redestroyder/delivery-service -a

