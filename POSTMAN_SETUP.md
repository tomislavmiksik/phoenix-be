# Postman Setup Guide

This guide will help you set up and use the Postman collection for testing the Phoenix BE API.

## Files

- `postman_collection.json` - The main collection with all API endpoints
- `postman_environment.json` - Environment variables for local development

## Import Instructions

### 1. Import the Collection

1. Open Postman
2. Click "Import" button (top left)
3. Select `postman_collection.json`
4. Click "Import"

### 2. Import the Environment

1. Click the gear icon (⚙️) in the top right or go to "Environments"
2. Click "Import"
3. Select `postman_environment.json`
4. Select "Phoenix BE - Local" from the environment dropdown

## API Endpoints

### Authentication Endpoints

#### Register
- **POST** `/api/auth/register`
- Creates a new user account
- Auto-saves JWT token to environment
- Example body:
```json
{
  "username": "johndoe",
  "email": "john.doe@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Login
- **POST** `/api/auth/login`
- Authenticates existing user
- Auto-saves JWT token to environment
- Example body:
```json
{
  "username": "johndoe",
  "password": "password123"
}
```

### Measurement Endpoints

All measurement endpoints require authentication (JWT token).

#### Create Measurement
- **POST** `/api/measurements`
- Example body:
```json
{
  "weight": 75.5,
  "height": 180.0,
  "chestCircumference": 95.5,
  "armCircumference": 32.0,
  "legCircumference": 58.0,
  "waistCircumference": 85.0,
  "measurementDate": "2025-12-03T10:30:00"
}
```

#### Get All Measurements
- **GET** `/api/measurements`
- Returns all measurements for the authenticated user

#### Get Recent Measurements
- **GET** `/api/measurements/recent?limit=10`
- Returns the most recent measurements
- Query parameter: `limit` (default: 10)

#### Get Measurement by ID
- **GET** `/api/measurements/{id}`
- Returns a specific measurement
- Update `{{measurement_id}}` variable or replace in URL

#### Update Measurement
- **PUT** `/api/measurements/{id}`
- Updates an existing measurement
- Same body structure as Create Measurement

#### Delete Measurement
- **DELETE** `/api/measurements/{id}`
- Deletes a measurement

### Admin Endpoints

Requires ADMIN role.

#### Generate API Key
- **POST** `/api/admin/keygen`
- Creates a new API key
- Example body:
```json
{
  "description": "API key description"
}
```

## Authentication Setup

The collection uses JWT Bearer token authentication by default.

### Option 1: JWT Token (Automatic)

1. Run the "Register" or "Login" request
2. The JWT token will be automatically saved to the `jwt_token` environment variable
3. All subsequent requests will use this token

### Option 2: API Key

If you have an API key:

1. Go to the Collection settings or individual request
2. Change Auth type from "Bearer Token" to "API Key"
3. Add header: `X-API-Key` with value `{{api_key}}`
4. Set the `api_key` environment variable

## Environment Variables

- `base_url` - API base URL (default: `http://localhost:8080`)
- `jwt_token` - JWT authentication token (auto-populated after login/register)
- `api_key` - API key for alternative authentication
- `measurement_id` - ID for testing single measurement endpoints

## Testing Workflow

1. **Start the application** on port 8080
2. **Register a new user** using the Register endpoint
3. **JWT token is automatically saved** to environment
4. **Test measurement endpoints** (all authenticated requests will now work)
5. **Update `measurement_id`** variable when testing specific measurements

## Notes

- All endpoints except Register and Login require authentication
- Admin endpoints require ADMIN role
- Measurement date is optional and will default to current time if not provided
- Weight and height are required fields for measurements
- The collection-level auth is set to Bearer token using `{{jwt_token}}`

## Troubleshooting

### 401 Unauthorized
- Ensure you've logged in/registered first
- Check that `jwt_token` environment variable is set
- JWT tokens expire after 24 hours (86400000ms)

### 403 Forbidden
- Admin endpoints require ADMIN role
- Check user permissions in database

### 400 Bad Request
- Validate request body against the examples
- Check required fields are present
- Ensure data types match (numbers for weight/height, valid email format, etc.)
