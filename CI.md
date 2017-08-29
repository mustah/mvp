# Continuous Integration Notes
CI is accomplished through the use of gitlab-ci (at http://gitlab.elvaco.local).

## Dynamic review environments setup (review apps)
To achieve dynamic review environments (called [Review Apps](https://about.gitlab.com/features/review-apps/) in GitLab lingo), our gitlab server has been configured to include a custom nginx configuration file:


`/etc/gitlab/gitlab.rb:`
```ruby
--- 8< ---
nginx['custom_nginx_config'] = "include /etc/nginx/conf.d/review_proxy.conf;"
--- >8 ---
```

The included file looks as follows:

`/etc/nginx/conf.d/review_proxy.conf:`
```
server {
        listen 80
        server_name     ~^rv.+\.gitlab\.elvaco\.local$;
        location / {
                proxy_pass http://localhost:9080;
                proxy_redirect off;
                proxy_set_header        Host    $host;
        }
}
```

This says that we want to redirect any requests to `rv*.gitlab.elvaco.local` to port `9080`.

> Note that we've configured our DNS to route requests to the wildcard subdomain `rv*.gitlab.elvaco.local` to `gitlab.elvaco.local`.

At port `9080` we have an instance of [nginx-proxy](https://github.com/jwilder/nginx-proxy) running in a docker container. `nginx-proxy` is responsible for proxying requests to the `rv*.gitlab.elvaco.local` subdomains to the correct docker container. It finds the correct container based on the `VIRTUAL_HOST` environment variable being set for that container.

We start `nginx-proxy` through [systemd](https://www.freedesktop.org/wiki/Software/systemd/) (which is the init system used by default in Ubuntu 15.04 and upwards) - the following service file is used:

`/etc/systemd/system/nginx-proxy.service:`
```
[Unit]
Description=Nginx proxy container (for dynamic GitLab review environments)
Requires=docker.service

[Service]
ExecStart=/usr/bin/docker run -p 9080:80 -v /var/run/docker.sock:/tmp/docker.sock --name systemd-nginx-proxy jwilder/nginx-proxy
ExecStop=/usr/bin/docker stop systemd-nginx-proxy
ExecStopPost=/usr/bin/docker rm -f systemd-nginx-proxy
ExecReload=/usr/bin/docker restart systemd-nginx-proxy

[Install]
WantedBy=multi-user-target
```

For example, given a request to rv-new-feature.gitlab.elvaco.local and a docker container started with `docker run -d -v $(pwd)/app-root:/app --expose 8080 -e VIRTUAL_HOST=rv-new-feature.gitlab.elvaco.local sgrio/java-oracle:server_jre_8 /app/bin/mvp`, GitLab's internal nginx would first proxy the request to nginx-proxy, which in turn would look for a docker container with `VIRTUAL_HOST=rv-new-feature.gitlab.elvaco.local`, find the container and redirect the request to that container's exposed port (`8080`, in this example).
