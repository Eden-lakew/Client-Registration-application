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

Sprint 2 – Completed Features
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
