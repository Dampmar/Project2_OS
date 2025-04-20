#!/bin/bash
# filepath: Car_Rental/Script.sh

# Function to pause execution until the user presses Enter
pause() {
    read -p "Press Enter to continue..."
}

# Main menu function
menu() {
    clear
    echo "==========================="
    echo "     Car Rental System"
    echo "==========================="
    echo ""
    echo "1. Compile the Project"
    echo "2. Lot Manager Script"
    echo "3. Rental Shop Script"
    echo "4. Run All Tests"
    echo "5. Exit"
    echo ""
    read -p "Select an option (1-5): " option

    case "$option" in
        1) compile ;;
        2) lotmanager ;;
        3) rentalshop ;;
        4) tests ;;
        5) exit 0 ;;
        *) echo "Invalid option, try again..."
           pause
           menu ;;
    esac
}

# Compile the project
compile() {
    echo "Compiling Java files..."
    javac -d bin -sourcepath src src/utils/*.java src/classes/*.java
    echo "Compilation complete."
    pause
    menu
}

# Lot Manager Script menu
lotmanager() {
    clear
    echo "Command Line Interface for Lot Manager"
    echo ""
    echo "1. Create a Lot or Add Vehicles to a Lot"
    echo "2. Remove a Vehicle from a Lot"
    echo "3. Exit"
    echo ""
    read -p "Select an option (1-3): " opt

    case "$opt" in
        1) create ;;
        2) remove ;;
        3) menu ;;
        *) echo "Invalid option, try again..."
           pause
           lotmanager ;;
    esac
}

# Create a Lot or Add Vehicles to a Lot
create() {
    echo "Conversation Interface for Adding Cars and Creating a Lot"
    read -p "Enter the lot name: " lotName
    read -p "Enter the number of sedans: " sedans
    read -p "Enter the number of SUVs: " suvs
    read -p "Enter the number of vans: " vans
    java -cp bin classes.LotManager --lot-name="$lotName" --add-sedan="$sedans" --add-suv="$suvs" --add-van="$vans"
    pause
    lotmanager
}

# Remove a Vehicle from a Lot
remove() {
    echo "Interface for Removing a Vehicle from a Lot"
    read -p "Enter the lot name: " lotName
    read -p "Enter the license plate of the vehicle to remove: " licensePlate
    java -cp bin classes.LotManager --lot-name="$lotName" --remove-vehicle="$licensePlate"
    pause
    lotmanager
}

# Rental Shop Script menu
rentalshop() {
    clear
    echo "Command Line Interface for Rental Shop"
    echo ""
    echo "1. Create a Rental Shop"
    echo "2. Access Rental Shop Interface"
    echo "3. Exit"
    echo ""
    read -p "Select an option (1-3): " cmd

    case "$cmd" in
        1) createRental ;;
        2) accessRental ;;
        3) menu ;;
        *) echo "Invalid option, try again..."
           pause
           rentalshop ;;
    esac
}

# Create a Rental Shop
createRental() {
    echo "Creating a Rental Shop..."
    read -p "Enter the location of the rental shop: " location
    read -p "Enter the number of available spaces: " spaces
    read -p "Enter the list of lots (comma-separated): " lots
    java -cp bin App --location="$location" --spaces-available="$spaces" --lots="$lots"
    pause
    rentalshop
}

# Access Rental Shop Interface
accessRental() {
    echo "Accessing Rental Shop Interface..."
    read -p "Enter the location of the rental shop: " location
    java -cp bin App --location="$location"
    pause
    rentalshop
}

# Run Tests
tests() {
    echo "Setting up test environment..."
    
    # Create directories if they don't exist
    mkdir -p lib
    mkdir -p bin
    
    # Download JUnit if not exists
    if [ ! -f "lib/junit-4.13.2.jar" ]; then
        echo "Downloading JUnit..."
        wget -q -O "lib/junit-4.13.2.jar" "https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar"
    fi
    
    # Download Hamcrest if not exists
    if [ ! -f "lib/hamcrest-core-1.3.jar" ]; then
        echo "Downloading Hamcrest..."
        wget -q -O "lib/hamcrest-core-1.3.jar" "https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"
    fi
    
    echo "Setup complete!"
    
    # Compile all code including tests
    echo "Compiling test files..."
    javac -d bin -cp "lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" -sourcepath src src/utils/*.java src/classes/*.java src/tests/*.java
    echo "Compilation complete."
    
    # Display test menu
    testmenu
}

# Test menu
testmenu() {
    clear
    echo "==========================="
    echo "     Test Runner"
    echo "==========================="
    echo ""
    echo "1. Run LotManagerTest"
    echo "2. Run RentalShopTest"
    echo "3. Return to Main Menu"
    echo ""
    read -p "Select a test to run (1-3): " testopt
    
    case "$testopt" in
        1) 
            java -cp "bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" org.junit.runner.JUnitCore tests.LotManagerTest
            pause
            testmenu
            ;;
        2)
            java -cp "bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" org.junit.runner.JUnitCore tests.RentalShopTest
            pause
            testmenu
            ;;
        3) menu ;;
        *) 
            echo "Invalid option, try again..."
            pause
            testmenu
            ;;
    esac
}

# Start the script by showing the main menu
menu