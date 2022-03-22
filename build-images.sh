#!/bin/bash
cd OrderService
./build-image.sh
cd ../NotificationService
./build-image.sh
cd ../BillingService
./build-image.sh
