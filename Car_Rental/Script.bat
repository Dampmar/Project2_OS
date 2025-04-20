:: filepath: Car_Rental/Script.bat
@echo off 
:menu 
cls 
echo ===========================
echo     Car Rental System
echo ===========================
echo.
echo 1. Compile the Project
echo 2. Lot Manager Script
echo 3. Rental Shop Script
echo 4. Run All Tests
echo 5. Exit
echo.
set /p option=Select an option(1-4):
if "%option%"=="1" goto compile
if "%option%"=="2" goto lotmanager
if "%option%"=="3" goto rentalshop
if "%option%"=="4" goto tests
if "%option%"=="5" exit
echo Invalid option, try again...
pause
goto menu

:: Compile the Project
:compile
echo Compiling Java files...
javac -d bin -sourcepath src src/utils/*.java src/classes/*.java src/App.java
echo Compilation complete.
pause 
goto menu 

:: Lot Manager Script
:lotmanager 
echo Command Line Interface for Lot Manager 
echo. 
echo 1. Create a Lot or Add Vehicles to a Lot 
echo 2. Remove a Vehicle from a Lot
echo 3. Exit
echo. 
set /p opt=Select an option(1-3):
if "%opt%"=="1" goto create
if "%opt%"=="2" goto remove
if "%opt%"=="3" goto menu 
echo Invalid option, try again...
pause
goto lotmanager

:: Create a Lot or Add Vehicles to a Lot
:create 
echo Conversation Interface for Adding Cars and Creating a Lot 
set /p lotName=Enter the lot name:
set /p sedans=Enter the number of sedans:
set /p suvs=Enter the number of SUVs:
set /p vans=Enter the number of vans:
java -cp bin classes.LotManager --lot-name=%lotName% --add-sedan=%sedans% --add-suv=%suvs% --add-van=%vans%
pause
goto lotmanager

:: Remove a Vehicle from a Lot 
:remove
echo Interface for Removing a Vehicle from a Lot
set /p lotName=Enter the lot name:
set /p licensePlate=Enter the license plate of the vehicle to remove:
java -cp bin classes.LotManager --lot-name=%lotName% --remove-vehicle=%licensePlate%
pause
goto lotmanager

:: Rental Shop Script
:rentalshop
echo Command Line Interface for Rental Shop
echo.
echo 1. Create a Rental Shop
echo 2. Access Rental Shop Interface
echo 3. Exit 
set /p cmd=Select an option(1-3):
if "%cmd%"=="1" goto createRental
if "%cmd%"=="2" goto accessRental
if "%cmd%"=="3" goto menu
echo Invalid option, try again...
pause
goto rentalshop

:: Create a Rental Shop
:createRental
echo Creating a Rental Shop...
set /p location=Enter the location of the rental shop:
set /p spaces=Enter the number of available spaces:
set /p lots=Enter the list of lots (comma-separated):
java -cp bin App --location=%location% --spaces-available=%spaces% --lots=%lots%
pause 
goto rentalshop

:accessRental
echo Accessing Rental Shop Interface...
set /p location=Enter the location of the rental shop:
java -cp bin App --location=%location%
pause 
goto rentalshop

:tests 
echo Setting up test environment...
if not exist "lib" mkdir lib 
if not exist "bin" mkdir bin 

:: Download JUnit if not exists
if not exist "lib\junit-4.13.2.jar" (
    echo Downloading JUnit...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar' -OutFile 'lib\junit-4.13.2.jar'"
)

:: Download Hamcrest if not exists
if not exist "lib\hamcrest-core-1.3.jar" (
    echo Downloading Hamcrest...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar' -OutFile 'lib\hamcrest-core-1.3.jar'"
)

echo Setup complete!

:: Compile all code including tests
echo Compiling test files...
javac -d bin -cp "lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar" -sourcepath src src\utils\*.java src\classes\*.java src\tests\*.java
echo Compilation complete.

:: Test menu
:testmenu
cls
echo ===========================
echo     Test Runner
echo ===========================
echo.
echo 1. Run LotManagerTest
echo 2. Run RentalShopTest
echo 3. Return to Main Menu
echo.
set /p testopt=Select a test to run (1-3):
if "%testopt%"=="1" (
    java -cp "bin;lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar" org.junit.runner.JUnitCore tests.LotManagerTest
    pause
    goto testmenu
)
if "%testopt%"=="2" (
    java -cp "bin;lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar" org.junit.runner.JUnitCore tests.RentalShopTest
    pause
    goto testmenu
)
if "%testopt%"=="3" goto menu
echo Invalid option, try again...
pause
goto testmenu


:exit 
echo Exiting...
pause