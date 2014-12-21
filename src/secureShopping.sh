#!/bin/bash
javac *.java
echo "***************All java files compiled***************"

echo "Now Running all Ecommerce servers"

java ECOM1 &
java ECOM2 &
java ECOM3 &



