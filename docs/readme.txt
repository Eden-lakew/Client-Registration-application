# Coding Style Guide
This project follows the Google Java Style Guide:
https://google.github.io/styleguide/javaguide.html

## Naming Conventions
- Classes: PascalCase
- Methods: camelCase
- Variables: camelCase
- Constants: UPPER_SNAKE_CASE

## Formatting
- Braces on same line
- Comments required at top of each class to describe purpose

## Commit Message Style
Use short, descriptive messages:
- "Add login success navigation"
- "Fix main menu button styling"
- "Update gradient background drawable"

## Branch Naming Convention
- feature/<name>

Examples:
feature/login


## Sprint 1 Features Completed
- Login page
- Main Dashboard with 3 main actions:
  • Client Registration
  • Client Information
  • Volunteer Management
- Styling + theme (buttons, gradient, rounded inputs)

## Default Login (Sprint 1)
username: admin
password: admin

-----------------------------
Sprint 2 – Completed Features
-----------------------------
## Edit Client
  -  New screen: EditClientActivity
  -  Pre-fills current client data
  -  Updates stored using Room Database
  -  LiveData automatically refreshes UI after saving
  -  Connected through ClientRepository + ClientDao

## Delete Client
   - Delete button added in ClientInfoActivity
   - Confirmation dialog to prevent accidental deletion
   - Deletes client record using ClientRepository.deleteClient()
   - Removes item from the Client List instantly via LiveData

## Unique Client ID Generator
   - Uses ClientIdGenerator to create a unique ID per site
   - ID shown in both Client List and Client Info screens
   - Ensures consistent and non-duplicate client identifiers

## Client List Improvements
   - Removed old/broken filter code
   - Cleaned and simplified RecyclerView adapter
   - Click listener now opens Client Info reliably
   - LiveData ensures list updates automatically after edits/deletes

## Database Alignment

# Updated ClientEntity fields to match registration form:
   - Address
   - First Language
   - Household (Adults + Children)
   - Qualifications (Low Income / Newcomer / Senior)
   - Dietary Restrictions
   - Authorized Pickup
   - Date Applied
   - Site Code

# DAO now fully supports CRUD operations:
   - insertClient()
   - getAllClients()
   - getClientById()
   - updateClient()
   - deleteClient()

## Development Environment
- Android Studio Ladybug Feature Drop | 2025.2.1 Patch 1
- Android 12+ target
- Java language

## Testing Instructions
1. Clone repo from GitLab
2. Open project in Android Studio
3. Build & Run on emulator or physical device (Android 12+)
4. Log in using default credentials above
5. Verify dashboard options load without crashing test

-------------------------------
Sprint 3 – Completed Features
-------------------------------
## Firebase Integration

    - Authentication: Users can now log in with their roles in Firebase accounts.
    - Role-based access:
        Admins and Volunteers have different dashboards.
        Firebase roles stored in Firestore users collection.

    - Check-in Event Recording:
        Volunteers can record client check-ins.
        Data stored in Firestore checkins collection.
        Includes timestamp, client ID, and volunteer ID.

## QR Code Features
    - QR Code Generator:
        Generates unique QR codes for clients.
        QR code displays client ID and basic info.
        Can be printed.

    - QR Code Scanner:
        Scans QR codes to retrieve client information.
        Works seamlessly with Firebase database.
        Supports real time check-in for volunteers.
        Can enter phone number manually to check status of pick-ups.

## Volunteer Management
    - Add, edit and view volunteer profiles.
    - Assign volunteers to check-in clients.
    - Role based dashboard ensures volunteers cannot access admin only features.
    - Integrated with Firebase Firestore to store volunteer info and assignments.

## Login & Logout Improvements
    - Login now validates against Firebase accounts.
    - Logout properly clears user session.

## Navigation & UI
    - MainActivity updates dynamically based on user role.
    - Volunteers and Admins see only the relevant options.
    - Logout button redirects to SplashActivity, which routes to LoginActivity if not authenticated.
    - Added filter by diet restriction feature
    - Added site code option box to write

## Testing Accounts

    Admin account:
        username: admin@mealconnect.org
        password: admin123

    Volunteer account:
        username: abhay@gmail.com
        password: abhay123
        - (This is Existing account you can use or You can make new one to test, new one should always include @gmail.com only)



## Firestore & Configuration
    - All necessary Firestore configuration files included (google-services.json).

    - Collections:
        users – stores user UID and role
        clients – stores client records
        checkins – stores volunteer check-in events

## Testing Instructions

1. Clone repo from GitLab.
2. Open project in Android Studio.
3. Build & run on emulator or physical device (Android 12+).
4. Log in using credentials above.


