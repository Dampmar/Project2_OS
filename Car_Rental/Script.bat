@echo off
echo Compiling Java files... 
start cmd /k "cd c:\Users\Usuario\Desktop\Repos\Project2_OS\Car_Rental\ && javac -d bin -sourcepath src src/utils/*.java src/classes/*.java src/tests/*.java"
echo Compilation complete.
echo Starting multiple license plate generation processes...
start cmd /k "cd c:\Users\Usuario\Desktop\Repos\Project2_OS\Car_Rental\ && java -cp bin classes.LotManager --lot-name=testLot --add-sedan=10 --add-suv=10 --add-van=10"
start cmd /k "cd c:\Users\Usuario\Desktop\Repos\Project2_OS\Car_Rental\ && java -cp bin tests.LicensePlateTester Process1"
start cmd /k "cd c:\Users\Usuario\Desktop\Repos\Project2_OS\Car_Rental\ && java -cp bin tests.LicensePlateTester Process2" 
echo Check the command windows for output.