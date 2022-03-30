#!/bin/bash
cd OrderService
./build-image.sh
cd ../NotificationService
./build-image.sh
cd ../BillingService
./build-image.sh
cd ../AuthenticationService
./build-image.sh
cd ../DeliveryService
./build-image.sh
cd ../ProductService
./build-image.sh
cd ..
