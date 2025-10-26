# CloudEagle - Dropbox Business Integration

A Spring Boot application that integrates with Dropbox Business API to provide SaaS management capabilities. This implementation demonstrates OAuth2 authentication, API integration, and production-ready backend development.

## API Endpoints

### Authentication Flow
1. **GET** `/api/oauth/authorize` - Get Dropbox authorization URL
2. **GET** `/api/oauth/callback` - OAuth callback (handles token exchange)

### Core APIs
- **GET** `/api/team-info` - Get team/organization details
- **GET** `/api/members` - Get list of all team members

