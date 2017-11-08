const fs = require('fs');
const csvjson = require('csvjson');
const glob = require('glob');
const Bottleneck = require('bottleneck');
const geocode = require('./geocode');

const fromDbJson = {
  authenticate: {
    id: 8,
    firstName: 'Eva',
    lastName: 'Nilsson',
    email: 'evanil@elvaco.se',
    company: 'Bostäder AB',
  },
  todos: [
    'translate unhandled and handled collections in the right place',
  ],
  collections: {
    unhandled: {
      'total': 2983,
      'city': {
        'count': 7,
        'entities':
          [
            {'id': 'Göteborg', 'count': 256},
            {'id': 'Kungsbacka', 'count': 2000},
            {'id': 'Mölndal', 'count': 33},
            {'id': 'Stockholm', 'count': 44},
            {'id': 'Alvesta', 'count': 55},
            {'id': 'Höganäs', 'count': 345},
            {'id': 'Borås', 'count': 250},
          ],
      },
      productModel:
        {
          'count': 3,
          'entities': [
            {'id': 'CMe2100', 'count': 273},
            {'id': 'CMi2100', 'count': 1576},
            {'id': 'CMe3100', 'count': 1134},
          ],
        },
    },
    'handled': {
      'total': 2627,
      'city':
        {
          'count': 7,
          'entities': [
            {'id': 'Göteborg', 'count': 113},
            {'id': 'Kungsbacka', 'count': 756},
            {'id': 'Mölndal', 'count': 123},
            {'id': 'Stockholm', 'count': 423},
            {'id': 'Alvesta', 'count': 916},
            {'id': 'Höganäs', 'count': 44},
            {'id': 'Borås', 'count': 252},
          ],
        },
      productModel:
        {
          'count': 3,
          'entities': [
            {'id': 'CMe2100', 'count': 573},
            {'id': 'CMi2100', 'count': 176},
            {'id': 'CMe3100', 'count': 1878},
          ],
        },
    },
  },
  dashboards: [
    {
      'id': '3',
      'systemOverview': {
        'indicators': [
          {
            'title': 'Insamling',
            'type': 'collection',
            'subtitle': '3567 punkter',
            'state': 'warning',
            'value': '95.8',
            'unit': '%',
          },
          {
            'title': 'Mätvärdeskvalitet',
            'type': 'measurementQuality',
            'subtitle': '3481 punkter',
            'state': 'critical',
            'value': '93.5',
            'unit': '%',
          },
        ],
      },
    },
  ],
  reports: [
    {
      'id': 1,
      'title': 'json-server',
      'author': 'typicode',
    },
    {
      'id': 2,
      'title': 'report 2',
      'author': 'elvaco',
    },
    {
      'id': 3,
      'title': 'report 3',
      'author': 'elvaco - home',
    },
  ],
  profile: {
    name: 'typicode',
  },
};

const parseMeasurementSeedData = (path) => {
  const measurements = [];
  glob.sync(path).forEach((seedFile) => {
    /**
     * NOTE: This code could be much, much prettier but most of the ugly here is
     * required to keep performance at an acceptable level.
     *
     * The rest is incompetence.
     */
    const measurementData = fs.readFileSync(seedFile, 'utf-8');
    measurementData.split('\n').forEach((csv) => {
      if (csv.length === 0) { return; }
      const [facility, meterId, datestring, energy, volume, forwardTemp, returnTemp] = csv.split(';');
      const year = datestring.substr(0, 4);
      const month = Number(datestring.substr(4, 2));
      const day = Number(datestring.substr(6, 2));
      // NOTE: We're only including about a week of data here, since serializing
      // any more will either take a *lot* of time or crash with an OOM error.
      if (month < 10 || (month === 10 && day < 28)) {
        return;
      }
      const hour = datestring.substr(8, 2);
      const minute = datestring.substr(10, 2);
      const created = new Date(year, month-1, day, hour, minute).toISOString();
      measurements.push({
        facility,
        meterId,
        created,
        quantity: 'Energy',
        value: energy,
        unit: 'kWh',
      });
      measurements.push({
        facility,
        meterId,
        created,
        quantity: 'Volume',
        value: volume,
        unit: 'm^3',
      });
      measurements.push({
        facility,
        meterId,
        created,
        quantity: 'Forward Temp.',
        value: forwardTemp,
        unit: '°C',
      });
      measurements.push({
        facility,
        meterId,
        created,
        quantity: 'Volume',
        value: returnTemp,
        unit: '°C',
      });
    });
  });
  return measurements;
};

const parseMeterSeedData = (path, geocodeOptions = {geocodeCacheFile: null, doGeocoding: false}) => {
  const r = {
    meters: [],
    gateways: [],
    selections: {meteringPoints: [], statuses: [], cities: [], addresses: []},
  };
  let geocodeData = {};
  let limiter;
  if (geocodeOptions.geocodeCacheFile !== null) {
    limiter = new Bottleneck(1, 1000);
    try {
      geocodeData = Object.assign(geocodeData,
        JSON.parse(fs.readFileSync(geocodeOptions.geocodeCacheFile, 'utf-8').toString()));
    } catch (err) {
      if (err.code !== 'ENOENT') {
        throw err;
      }
    }
  }

  const cities = new Set();
  const addresses = new Set();
  const meteringPoints = new Set();
  const statuses = new Set();

  const promises = glob.sync(path).map((seedFile) => {

    const meterData = fs.readFileSync(seedFile, 'utf-8').toString();
    const options = {
      delimiter: ';',
      headers: 'facility;address;city;medium;meter_id;meter_manufacturer;' +
      'gateway_id;gateway_product_model;tel;ip;port;gateway_status;meter_status',
    };
    const obj = csvjson.toObject(meterData, options);
    return Promise.all(obj.map(async (row) => {
      let objPosition = {};
      const addressInfo = {
        city: row.city,
        streetAddress: row.address,
        country: 'Sweden',
      };
      const geoKey = geocode.encodeAddressInfo(addressInfo);
      if (geoKey in geocodeData) {
        objPosition = geocodeData[geoKey];
      } else if (geocodeOptions.doGeocoding) {
        const pos = await limiter.schedule(geocode.fetchGeocodeAddress, addressInfo);
        if (pos instanceof Error) {
          console.log(pos.message);
        } else {
          geocodeData[geoKey] = objPosition = pos;
          console.log('found position', pos, 'for', geoKey);
        }
      }

      const decorateStatus = (s) => s === 'OK' ? {text: s, code: 0} : {text: s, code: 2};
      const nullOr = (s) => s === 'NULL' ? null : s;

      row.meter_status = decorateStatus(row.meter_status);
      row.gateway_status = decorateStatus(row.gateway_status);

      r.gateways.push({
        id: row.gateway_id,
        facility: row.facility,
        address: row.address,
        city: row.city,
        productModel: row.gateway_product_model,
        telephoneNumber: row.tel,
        ip: nullOr(row.ip),
        port: nullOr(row.port),
        status: row.gateway_status,
        position: objPosition,
      });
      r.meters.push({
        id: row.meter_id,
        facility: row.facility,
        address: row.address,
        city: row.city,
        medium: row.medium,
        manufacturer: row.meter_manufacturer,
        status: row.meter_status,
        gatewayId: row.gateway_id,
        position: objPosition,
      });
      if (!cities.has(row.city)) {
        r.selections.cities.push({id: row.city, name: row.city});
        cities.add(row.city);
      }
      const addressId = row.address;
      if (!addresses.has(addressId)) {
        r.selections.addresses.push({id: addressId, name: row.address});
        addresses.add(addressId);
      }
      if (!meteringPoints.has(row.meter_id)) {
        r.selections.meteringPoints.push({id: row.meter_id, name: row.meter_id});
        meteringPoints.add(row.meter_id);
      }
      if (!statuses.has(row.meter_status)) {
        r.selections.statuses.push({id: row.meter_status, name: row.meter_status});
        statuses.add(row.meter_status);
      }
    }));
  });
  Promise.all(promises).then(() => {
    if (geocodeOptions.doGeocoding && geocodeOptions.geocodeCacheFile) {
      fs.writeFileSync(geocodeOptions.geocodeCacheFile,
        JSON.stringify(geocodeData));
    }
  });
  return r;
};

module.exports = (doGeocoding = false) => {
  const measurements = parseMeasurementSeedData('data/seed_data/*measurements*.csv');
  const metersAndGateways = parseMeterSeedData('data/seed_data/*_meters.csv', {
        doGeocoding: doGeocoding,
        geocodeCacheFile: 'data/geocoding.json',
  });
  return Object.assign(fromDbJson,
    {measurements},
    metersAndGateways,
  );
};
