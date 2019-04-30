fn = function () {
  // default config
  config = {
    api_endpoint: 'http://localhost:8080'
  };

  var api_endpoint = karate.properties['karate.api.endpoint'];
  if (api_endpoint) {
    config.api_endpoint = api_endpoint;
  }

  karate.log('Using API endpoint:', config.api_endpoint);
  return config;
};
