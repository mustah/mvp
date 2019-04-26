fn = function () {
  // default config
  config = {
    api_endpoint: 'http://localhost:8080'
  };

  var api_endpoint = karate.properties['api_endpoint'];
  if (api_endpoint) {
    config.api_endpoint = api_endpoint;
  }

  return config;
}
