# Admin Config Client

The properties can be access without vault profile, since the values in the properties are resolved
with vault secret values, it should a proper vault server to be enabled. 
```shell
curl http://localhost:8888/spring-config-client/dev/master/spring-config-client.properties
```