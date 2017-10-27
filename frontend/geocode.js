const rp = require('request-promise');

const encodeAddressInfo = (addressInfo) => {
  return [addressInfo.streetAddress, addressInfo.city, addressInfo.country]
    .filter((addrInfo) => addrInfo !== undefined)
    .join(' :: ');
};

const fetchGeocodeAddress = (addressInfo) => {

  const locate = [addressInfo.streetAddress, addressInfo.city, addressInfo.country]
    .filter((addrInfo) => addrInfo !== undefined)
    .join(', ');

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
module.exports = {fetchGeocodeAddress, encodeAddressInfo};
/*
Usage:
let addresses = []
addresses.push(fetchGeocodeAddress({city: 'Kungsbacka', streetAddress: 'Kabelgatan 2T'}));
addresses.push(fetchGeocodeAddress({city: 'Göteborg', country: 'Sweden'}));
addresses.push(fetchGeocodeAddress({city: 'Onsala'}));
Promise.all(addresses).then((results) => {
  results.forEach((r) => {
    if (r instanceof Error) {
      console.log('err: ', r.message);
    } else {
      console.log('longitude: ', r.longitude);
      console.log('latitude: ', r.latitude);
      console.log('confidence factor: ', r.confidence);
    }
  });
});*/
