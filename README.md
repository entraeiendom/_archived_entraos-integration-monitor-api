# entraos-integration-monitor-api
Generic monitor observing that integration between services do work.

## Endpoints:

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

## Alerting:
Will send alert to Slack

## Configuration

Use environment variables or edit local_config.properties.
