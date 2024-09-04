package com.example.cinema.show;

import akka.javasdk.annotations.Acl;

@akka.javasdk.annotations.Setup
// NOTE: This default ACL settings is very permissive as it allows any traffic from the internet.
// Our samples default to this permissive configuration to allow users to easily try it out.
// However, this configuration is not intended to be reproduced in production environments.
// Documentation at https://docs.kalix.io/java/access-control.html
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class CinemaShowSetup { }