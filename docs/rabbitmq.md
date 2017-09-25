# RabbitMQ setup & configuration
For delivery of metering values from external systems (such as Metering) to MVP, we use [RabbitMQ](https://www.rabbitmq.com/). This document briefly describes the deployment of RabbitMQ, and what configuration has been made.

The management UI can be accessed at http://elvsealiweb04.elvaco.local:15672.

## Installation
RabbitMQ is installed through the official RabbitMQ APT repositories. Read the [documentation](https://www.rabbitmq.com/install-debian.html) for up to date installation instructions. For posterity, these are the commands that were used to install RabbitMQ on `elvsealiweb04`:

```bash
echo 'deb http://www.rabbitmq.com/debian/ testing main' |
     sudo tee /etc/apt/sources.list.d/rabbitmq.list
wget -O- https://www.rabbitmq.com/rabbitmq-release-signing-key.asc |
     sudo apt-key add -
sudo apt-get update
sudo apt-get install rabbitmq-server
```

## Deployment
The RabbitMQ broker is currently running on `elvsealiweb04`, hosted by Candidator.

## Plugins
The following commands have been run to activate the  [LDAP plugin](http://www.rabbitmq.com/ldap.html) and [Management plugin](http://www.rabbitmq.com/management.html), respectively:

```
rabbitmq-plugins enable rabbitmq_auth_backend_ldap
rabbitmq-plugins enable rabbitmq_management
```

## Authentication & Authorisation
We use the LDAP plugin for RabbitMQ to provide authentication & authorisation.

This is configured as such:

`/etc/rabbitmq/rabbitmq.config:`
```
[
{rabbit,[{auth_backends, [rabbit_auth_backend_ldap, rabbit_auth_backend_internal]}]},
{rabbitmq_auth_backend_ldap,
        [ 
        {servers, ["ad1.elvaco.local"]},
        {dn_lookup_attribute, "sAMAccountName"},
        {dn_lookup_base, "dc=elvaco,dc=local"},
        {dn_lookup_bind, {"administrator","Password2"}},
        {use_ssl, false},
        {port, 389},
        {timeout, 5000},
        {log, true},
        {tag_queries, [
                {administrator, {in_group, "cn=systemDevelopers,ou=Elvaco Groups,DC=elvaco,DC=local"}},
                {management, {constant, true}}]}
        ]
}
].
```
This configuration gives everyone that succesfully authenticates against our LDAP server management access. In addition, it gives everyone who is a member of the `systemDevelopers` group administration rights.

## Management
The Management plugin provides an HTTP-based API for management and monitoring of RabbitMQ, along with a browser-based UI and a [command-line tool](http://www.rabbitmq.com/management-cli.html).

The management UI can be accessed at http://elvsealiweb04.elvaco.local:15672.

## Firewall configuration
The firewall on `elvsealiweb04` has been augmented with the following rules, to allow for AMQP traffic and access to the management UI, respectively:
```
sudo iptables -I INPUT 1 -p tcp --dport 5672 -j ACCEPT
sudo iptables -I INPUT 1 -p tcp --dport 15672 -j ACCEPT
```

Candidator has also opened up these ports on their end for `elvsealiweb04`.

Candidator has, in addition to the RabbitMQ ports (`5672`, `15672`), also opened up port `389` towards `ad1.elvaco.local`, in order for us to be able to authenticate via LDAP.

## Notes
Restarting RabbitMQ (for example, to let a configuration change take effect) is done by issuing
```
systemctl restart rabbitmq-server.service
```
as `root`.

For more information regarding RabbitMQ and AMQP concepts and terminology, a good resource can be found [here](https://www.rabbitmq.com/tutorials/amqp-concepts.html)
