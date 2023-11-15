# API Rest 

## Authentication
Methods to handle authentication

Method|Path|Description|Status
------|-----|---------|---
POST | /auth/login | Logins. Create JWT and adds needed cookie | 
POST | /auth/logout | Expire session | 
GET | /auth/check | Returns if session is still active | 
GET | /auth/self | Logged in user profile | 💡 Move to /users/self?

## Invites

Method|Path|Description|Status
------|-----|---------|---
POST | /invites | Creates an invite to register to the application with specific roles | 
GET | /invites | List invites created by the user | 
DELETE | /invites/{inviteId} | Delete created invite | 

### Invites to logged in user

Method|Path|Description|Status
------|-----|---------|---
GET   | /users/self/invites | List invites sent to the user | 
PATCH  | /users/self/invites/{inviteId} | Update invite by accepting it | 

## Users

Method|Path|Description|Status
------|-----|---------|--- 
GET | /users/{username} | View public profile of user |
POST | /register | Sign up into the application using an invite | 

## Courses

Method|Path|Description|Status
------|-----|---------|--- 
GET | /course | View course created by the user |
GET | /course/{id} | View subject |
POST | /course | Create a new subject |
PUT | /course/{id} | Update general properties of a subject |
DELETE | /course/{id} | Delete subject. Note: only works for course without projects. |

Comments:
- Only professors can create course
- Only creators can manage them

## Course year

Method|Path|Description|Status
------|-----|---------|---
GET | /course/years | View enrolled subject years as student | ✨ New
GET | /course/years/{yearId} | View one subject year | ✨ New
POST | /course/{courseId}/years | Create new academic year for a subject |
DELETE | /course/years/{yearId} | Delete an academic year of a subject |

### Invites to subject year

Method|Path|Description|Status
------|-----|---------|---
POST | /course/years/{yearId}/invites | Invite user by email to a subject year | 

### Students
Method|Path|Description|Status
------|-----|---------|---
GET | /course/years/{yearId}/students | View enrolled students of a subject year |
DELETE | /course/years/{yearId}/students/{username} | Remove a student from a subject year |

### Groups
Method|Path|Description|Status
------|-----|---------|---
GET | /course/years/{yearId}/projects | View projects of a subject year | 
POST | /course/years/{yearId}/projects | Create new project inside a subject year | 
GET | /projects/{groupId} | View a project. | ✨ New
PATCH | /projects/{groupId} | Modify general data of a project | ✨ New
DELETE | /projects/{groupId} | Delete a project | ✨ New