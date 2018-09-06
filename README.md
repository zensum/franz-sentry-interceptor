# franz-sentry-interceptor

Interceptor for capturing unhandled exception in Franz
and sending them to a sentry.

Requires the following environmental variables
* SENTRY_DSN - a valid sentry DSN

Can read the following enviromental variables
* HOSTNAME 
* COMPUTERNAME