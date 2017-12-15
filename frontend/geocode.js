const rp = require('request-promise');

const Service = {
  GeocodeXYZ: 'geocode.xyz',
  Mapbox: 'mapbox',
};

const MapboxAccessToken = 'API_KEY';
const encodeAddressInfo = (addressInfo) => {
  return joinAddressInfo(addressInfo, ' :: ' );
};

const joinAddressInfo = (addressInfo, separator) => {
  if (!addressInfo) {
    throw Error('Missing required parameter addressInfo!');
  }
  return [addressInfo.streetAddress, addressInfo.city, addressInfo.country]
    .filter((addrInfo) => addrInfo !== undefined)
    .join(separator);
};

const fetchGeocodeAddressMapbox = (addressInfo, accessToken) => {
  const url = 'https://api.mapbox.com/geocoding/v5/mapbox.places/'
    + encodeURIComponent(joinAddressInfo(addressInfo, ' ')) + '.json?limit=1&access_token=' + MapboxAccessToken;

  console.log('requesting: ' + url);
  return rp.get({
      url: url,
  }).then((response) => {
    const responseObj = JSON.parse(response);
    if (responseObj.features.length === 0) {
      throw Error('No features in response from Mapbox?');
    }

    const feature = responseObj.features[0];
    return {
      longitude: feature.geometry.coordinates[0],
      latitude: feature.geometry.coordinates[1],
      confidence: feature.relevance,
    };

  })
  .catch((err) => {
    return err;
  });
};

const fetchGeocodeAddressGeocodeXyz = (addressInfo) => {
  const locate = joinAddressInfo(addressInfo, ', ');
  let rpOptions = {
    url: 'https://geocode.xyz',
    form: {
      locate: locate,
      json: 1,
    },
  };
  return rp.post(rpOptions)
    .then((coords) => {
      const coordinates = JSON.parse(coords);
      if ('error' in coordinates) {
        throw Error('geocoding error: ' + coordinates.error.description);
      }
      return {
        longitude: coordinates.longt,
        latitude: coordinates.latt,
        confidence: Number.parseFloat(coordinates.standard.confidence),
      };
    })
    .catch((err) => {
      return err;
    });

};

const fetchGeocodeAddress = (addressInfo, service = null) => {
  if (!service) {
    service = Service.GeocodeXYZ;
  }

  switch (service) {
    case Service.GeocodeXYZ:
      return fetchGeocodeAddressGeocodeXyz(addressInfo);
    case Service.Mapbox:
      return fetchGeocodeAddressMapbox(addressInfo, MapboxAccessToken);
    default:
      throw Error('Unknown service!!');
  }
};
module.exports = {fetchGeocodeAddress, encodeAddressInfo, Service};

/*
Usage:
const handleResult = (r) =>  {
  if (r instanceof Error) {
    console.log('err: ', r.message);
  } else {
    console.log('longitude: ', r.longitude);
    console.log('latitude: ', r.latitude);
    console.log('confidence factor: ', r.confidence);
  }
}
let addresses = []
addresses.push(fetchGeocodeAddress({city: 'Kungsbacka', streetAddress: 'Kabelgatan 2T'}));
addresses.push(fetchGeocodeAddress({city: 'Göteborg', country: 'Sweden'}));
addresses.push(fetchGeocodeAddress({city: 'Onsala'}));
Promise.all(addresses).then((results) => {
  results.forEach((r) => {
    handleResult(r);
  });
});

Promise.all([fetchGeocodeAddress({city: 'Kungsbacka', streetAddress: 'Kabelgatan 2T'},
  Service.Mapbox)]).then((results) => {
  results.forEach((r) => {
    handleResult(r);
  });
});
*/
