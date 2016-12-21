A sample REST web service application for registering users.

Built on Play Framework 2.5 / Scala 2.11.7 / Slick 2.0.0 / MySQL 5.7.16

Future improvements include:
- Implement user states (Registered/Verified/Suspended)
- Add password, registration date
- Implement password encryption
- Make RegisterUser method fire-and-forget
- Send email to user on registration requiring verification step
- Improve error handling - distinguish between validation errors and system errors
