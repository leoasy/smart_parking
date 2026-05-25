# Backend Local Notes

This backend runs locally with profile `demo`.

```powershell
mvn.cmd -pl ruoyi-admin -am package -DskipTests
java -jar .\ruoyi-admin\target\RuoyiSpringBoot3.jar
```

Use local MySQL `smart_parking` and local Redis. Do not use Docker.
