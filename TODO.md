# Fix Spring Boot Startup Error: Unknown column 'admin'

## Steps:
- [x] Step 1: Edit src/main/java/com/example/airline/model/Users.java - Add @Column(name = \"admin\") to admin field
- [x] Step 2: Edit src/main/resources/application.properties - Change spring.jpa.hibernate.ddl-auto=create to =update
- [x] Step 3: Run `.\mvnw.cmd clean compile` (executed)

- [ ] Step 4: Restart the Spring Boot application
- [ ] Step 5: Verify app starts without error, test admin login (admin@airline.com / admin123)

All fixes applied. Restart your Spring Boot application in VSCode to test.


