# API Rest 

## Authentication

Method|Path|Description|Status
------|-----|---------|---
POST | /auth/login | Logins. Create JWT and adds needed cookie | ✅
POST | /auth/logout | Expire session | ✅
GET | /auth/check | Returns if session is still active | ✅
GET | /auth/self | Logged in user profile | ✅
POST | /auth/password | Change password of the user | ✅
POST | /auth/recovery | Request for recovery code to change password | ✅
POST | /auth/recovery/{email}/check | Check if recoveyr code for email is OK | ✅
POST | /auth/recovery/{email} | Change the password of user with valid code | ✅

## Users

 Method | Path                 | Description                                        | Status 
--------|----------------------|----------------------------------------------------|-------- 
 GET    | /users               | Get all users (Only Administrators)                | ✅      
 GET    | /users/uuid/{uuid}   | View public profile of user using uuid             | ✅      
 GET    | /users/{email}       | View public profile of user using email            | ✅      
 POST   | /users/register      | Register user into website                         | ✅      
 PATCH  | /users               | Change settings of my user                         | ✅      
 PATCH  | /users/{id}          | Change settings of other user (Only Administrator) | ✅      
 GET    | /users/checker/admin | Check if the authenticades user is admin           | ✅      

## Subjects

Method|Path|Description|Status
------|-----|---------|--- 
GET | /subjects | View all subjects (Only Administrators) | ✅
GET | /subjects/{id} | View subject (Only for owner & Administrador) | ✅
POST | /subjects | Create a new subject (Only for Administrators) | ✅
PUT | /subjects/{id} | Update general properties of a subject (Only for owner & Administrators) | ✅
DELETE | /subjects/{id} | Delete subject (Only for owner & Administrators) | ✅

## Courses

Method|Path|Description|Status
------|-----|---------|---
GET | /courses | View enrolled owned courses (all for Administrators) | ✅
GET | /courses/{id} | View one course owned courses (all for Administrators) | ✅
PATCH | /courses/{id} | Edit one course owned courses (all for Administrators) | ✅
DELETE | /course/{id} | Delete one course owned (all for Administrators) | ✅
GET | /course/{id}/projects | Get projects of one project  (owners or Administrators) | ✅
POST | /course/{id}/projects | Create one project in course  (owners or Administrators) | ✅

## Projects

Method|Path|Description|Status
------|-----|---------|---
GET | /projects | View all projects (only for administrators) | ✅
GET | /projects/{id} | View one project (owners or administrators) | ✅
GET | /projects/{id}/qualification | Generate the qualifications of members (only for administrator) | ✅
PATCH | /projects/{id} | Edit one project | ✅
DEL | /projects/{id} | Delete one project  (only Administrators) | ✅
GET | /projects/{id}/tasks | Get all tasks of project | ✅
GET | /projects/{id}/sprints | Get all sprints in project | ✅
POST | /projects/{id}/tasks | Create one task in project | ✅
POST | /projects/{id}/sprints | Create one sprint in project | ✅

## Sprints

Method|Path|Description|Status
------|-----|---------|---
GET | /sprints | View all sprints| ✅
GET | /sprints/{id} | View one sprint | ✅
DEL | /sprints/{id} | Delete one sprint | ✅
PATCH | /sprints/{id} | Edit one project | ✅
GET | /sprints/{id} | Get logs of changes in sprint | ✅

** Only valid for project members & Administrator

## Tasks

Method|Path|Description|Status
------|-----|---------|---
GET | /tasks | View all tasks (only for Administrator)| ✅
GET | /tasks/status | View possible status of US and tasks | ✅
GET | /tasks/usstatus | View possible status of US | ✅
GET | /tasks/taskstatus | View possible status of tasks | ✅
GET | /tasks/types | View possible types of tasks | ✅
GET | /tasks/{id} | View one task | ✅
POST | /tasks/{id}/subtasks | Create one task in US | ✅
PATCH | /tasks/{id} | Edit one task | ✅
DEL | /tasks/{id} | Delete one task | ✅
GET | /tasks/{id}/comments | Get comments of task | ✅
GET | /tasks/{id}/history | Get logs of task | ✅


