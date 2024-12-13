To test the track issue functionality put this to your postman url bar:
```
curl --location 'http://localhost:8080/api/v1/track-issues' \
--header 'Content-Type: application/json' \
--data '{
 "request":"jom tu pase problem me pranu mesazhin per otp",
 "userId":"123"
}'
```

