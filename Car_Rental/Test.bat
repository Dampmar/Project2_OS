@echo off
echo Fixing serialization issues...

echo Compiling Vehicle class...
javac -d bin src/classes/Vehicle.java

echo Compiling and running test...
javac -d bin src/tests/VehicleSerializationTest.java src/classes/Vehicle.java
java -cp bin tests.VehicleSerializationTest

echo If successful, recompile and run RentalShopInit...
javac -d bin src/utils/*.java src/classes/*.java src/tests/RentalShopInit.java
java -cp bin tests.RentalShopInit

echo Done!
pause