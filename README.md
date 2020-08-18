# entraos-integration-monitor-api
Generic monitor observing that integration between services do work.

## Endpoints:

Default context
http://localhost:8080/monitor

### /status
List of integrations currently supporting:
* Logon
* Host is found
* Last seen ok
Will always respond with http 200

### /health
Target for external monitoring application
Will respond http 200 if all is well
Will respond http 412 precondition failed if any of the validations failed.

## Configuration

Use environment variables or edit local_config.properties.

```
environment=dev
service_name=local-test
server_port=8080

logon_uri=http://logon.example.com:8080/logon
logon_grant_type=password
logon_username=someone
logon_password=anyting
```

## Alerting:
1. Create app https://api.slack.com/apps
2. Select OAuth and Permissions
3. Copy token
4. Configure in local_config.properties
```
slack_alerting_enabled=true
slack_token=<token from 3.>
slack_channel=#random
```