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
GET | /courses | View courses created by the user |
GET | /courses/{id} | View course |
POST | /courses | Create a new course |
PUT | /courses/{id} | Update general properties of a course |
DELETE | /courses/{id} | Delete course. Note: only works for courses without groups. |

Comments:
- Only professors can create courses
- Only creators can manage them

## Course year

Method|Path|Description|Status
------|-----|---------|---
POST | /courses/{courseId}/years | Create new academic year for a course |
DELETE | /courses/years/{yearId} | Delete an academic year of a course |

### Invites to course year

Method|Path|Description|Status
------|-----|---------|---
POST | /courses/years/{yearId}/invites | Invite user by email to a course year | 

### Students
Method|Path|Description|Status
------|-----|---------|---
GET | /courses/years/{yearId}/students | View enrolled students of a course year | ✨ New
DELETE | /courses/years/{yearId}/students/{username} | Remove a student from a course year | ✨ New

### Groups
Method|Path|Description|Status
------|-----|---------|---
GET | /courses/years/{yearId}/groups | View groups of a course year | ✨ New
POST | /courses/years/{yearId}/groups | Create new group inside a course year | ✨ New
GET | /groups/{groupId} | View a group. | 💡 Upcoming
PUT | /groups/{groupId} | Modify general data of a group | 💡 Upcoming
DELETE | /groups/{groupId} | Delete a group | 💡 Upcoming