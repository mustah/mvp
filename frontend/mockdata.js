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
      id: 3,
      systemOverview: {
        widgets: [
          {
            type: 'collection',
            total: 1697,
            status: 'warning',
            pending: 22,
          },
          {
            type: 'measurementQuality',
            total: 1697,
            status: 'critical',
            pending: 79,
          },
        ],
      },
    },
  ],
  profile: {
    name: 'typicode',
  },
};

const alarmsLookup = [
  'Låg batterinivå',
  'Flödessensorfel (luft)',
  'Flödessensorfel (generisk)',
  'Flödessensorfel (smutsig)',
  'Läckage',
  'Högt flöde',
  'Felvänt flöde',
  'Ingående temperatursensorfel',
  'Utgående temeratursensorfel',
  'Temperatursensorfel (generisk)',
  'Temperatursensor inverterad',
  'Tamperfel',
  'Matningsspänningsfel',
  'Behöver batteribyte',
  'Internt mätarfel',
];

const getRandomAlarm = () => {
  const randomNumber = Math.floor(Math.random() * 250);
  if (randomNumber < 15) {
    return alarmsLookup[randomNumber];
  }
  return ':Inget fel:';
};

const parseMeasurementSeedData = (path) => {
  const measurements = [];
  const statusChanges = {};
  const padZero = (aNumber) => {
    let str = aNumber + '';
    if (str.length > 1) {
      return str;
    }
    return '0' + str;
  };
  const dateString = (d) => {
    //tslint:disable-next-line
    return `${d.getFullYear()}-${padZero(d.getMonth() + 1)}-${padZero(d.getDate())} ${padZero(d.getHours())}:${padZero(d.getMinutes())}:${padZero(d.getSeconds())}`;
  };

  glob.sync(path).forEach((seedFile) => {
    /**
     * NOTE: This code could be much, much prettier but most of the ugly here is
     * required to keep performance at an acceptable level.
     *
     * The rest is incompetence.
     */
    const measurementData = fs.readFileSync(seedFile, 'utf-8');
    measurementData.split('\n').forEach((csv) => {
      if (csv.length === 0) {
        return;
      }
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
      const created = new Date(year, month - 1, day, hour, minute);
      const createdIso = created.toISOString();
      // this could be a complete lie, but hopefully the CSV is ordered by date in descending order
      // also, we don't know if the status changed, but at least there is 'a' timestamp (perhaps we should randomize it)
      statusChanges[meterId] = dateString(created);
      measurements.push({
        facility,
        meterId,
        created: createdIso,
        quantity: 'Energy',
        value: energy,
        unit: 'kWh',
      });
      measurements.push({
        facility,
        meterId,
        created: createdIso,
        quantity: 'Volume',
        value: volume,
        unit: 'm^3',
      });
      measurements.push({
        facility,
        meterId,
        created: createdIso,
        quantity: 'Forward Temp.',
        value: forwardTemp,
        unit: '°C',
      });
      measurements.push({
        facility,
        meterId,
        created: createdIso,
        quantity: 'Volume',
        value: returnTemp,
        unit: '°C',
      });
    });
  });
  return {measurements, statusChanges};
};

const parseMeterSeedData = (path, seedOptions = {geocodeCacheFile: null, doGeocoding: false, statusChanges: {}}) => {
  const {geocodeCacheFile, doGeocoding, statusChanges} = seedOptions;
  const r = {
    meters: [],
    gateways: [],
    selections: {
      meteringPoints: [],
      statuses: [],
      cities: [],
      addresses: [],
      alarms: [],
      manufacturers: [],
      productModels: [],
    },
  };
  let geocodeData = {};
  let limiter;
  if (geocodeCacheFile !== null) {
    limiter = new Bottleneck(1, 1000);
    try {
      geocodeData = Object.assign(geocodeData,
        JSON.parse(fs.readFileSync(geocodeCacheFile, 'utf-8').toString()));
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
  const alarms = new Set();
  const manufacturers = new Set();
  const productModels = new Set();

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
      } else if (doGeocoding) {
        const pos = await limiter.schedule(geocode.fetchGeocodeAddress, addressInfo);
        if (pos instanceof Error) {
          console.log(pos.message);
        } else {
          geocodeData[geoKey] = objPosition = pos;
          console.log('found position', pos, 'for', geoKey);
        }
      }

      const decorateStatus = (status) => status === 'OK' ? {name: status, id: 0} : {name: 'Fel', id: 3};
      const nullOr = (str) => str === 'NULL' ? null : str;

      row.gateway_flags = row.gateway_status === 'OK' ? [] : [{title: 'Åtgärd'}];
      row.meter_flags = row.meter_status === 'OK' ? [] : [{title: 'Åtgärd'}];

      row.meter_status = decorateStatus(row.meter_status);
      row.gateway_status = decorateStatus(row.gateway_status);

      const cityId = row.city;
      const addressId = row.address;
      const city = {id: cityId, name: row.city};
      const address = {id: addressId, name: row.address, cityId};

      let statusChanged;

      if (statusChanges.hasOwnProperty(row.meter_id)) {
        statusChanged = statusChanges[row.meter_id];
      }

      r.gateways.push({
        id: row.gateway_id,
        facility: row.facility,
        address,
        city,
        flags: row.gateway_flags,
        productModel: row.gateway_product_model,
        telephoneNumber: row.tel,
        ip: nullOr(row.ip),
        port: nullOr(row.port),
        status: row.gateway_status,
        statusChanged,
        meterIds: [row.meter_id],
        position: objPosition,
      });
      const alarm = getRandomAlarm();
      r.meters.push({
        id: row.meter_id,
        facility: row.facility,
        address,
        city,
        flags: row.meter_flags,
        productModel: row.gateway_product_model,
        medium: row.medium,
        manufacturer: row.meter_manufacturer,
        status: row.meter_status,
        statusChanged,
        gatewayId: row.gateway_id,
        position: objPosition,
        alarm,
      });
      if (!cities.has(cityId)) {
        r.selections.cities.push({id: cityId, name: row.city});
        cities.add(cityId);
      }
      if (!addresses.has(addressId)) {
        r.selections.addresses.push({id: addressId, name: row.address, cityId});
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
      if (!alarms.has(alarm)) {
        r.selections.alarms.push({id: alarm, name: alarm});
        alarms.add(alarm);
      }
      if (!manufacturers.has(row.meter_manufacturer)) {
        r.selections.manufacturers.push({id: row.meter_manufacturer, name: row.meter_manufacturer});
        manufacturers.add(row.meter_manufacturer);
      }
      if (!productModels.has(row.gateway_product_model)) {
        r.selections.productModels.push({id: row.gateway_product_model, name: row.gateway_product_model});
        productModels.add(row.gateway_product_model);
      }
    }));
  });
  Promise.all(promises).then(() => {
    if (doGeocoding && geocodeCacheFile) {
      fs.writeFileSync(geocodeCacheFile, JSON.stringify(geocodeData));
    }
  });
  return r;
};

module.exports = (doGeocoding = false) => {
  const {measurements, statusChanges} = parseMeasurementSeedData('data/seed_data/*measurements*.csv');
  const metersAndGateways = parseMeterSeedData('data/seed_data/*_meters.csv', {
    statusChanges,
    doGeocoding,
    geocodeCacheFile: 'data/geocoding.json',
  });
  return Object.assign(fromDbJson,
    {measurements},
    metersAndGateways,
  );
};
