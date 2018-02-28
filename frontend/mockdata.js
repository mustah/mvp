const fs = require('fs');
const csvjson = require('csvjson');
const glob = require('glob');
const Bottleneck = require('bottleneck');
const geocode = require('./geocode');
const moment = require('moment');
const {v4: generateId} = require('uuid');

const fromDbJson = {
  dashboards: [
    {
      id: 3,
      widgets: [
        {
          type: 'collection',
          total: 1697,
          status: 'warning',
          pending: 22,
        },
        {
          type: 'measurementQuality',
          total: 1709,
          status: 'critical',
          pending: 6,
        },
      ],
    },
  ],
};

const statusChangelog = [
  {
    date: '2017-11-22 09:34',
    status: {
      id: 0,
      name: 'OK',
    },
  },
  {
    date: '2017-11-22 10:34',
    status: {
      id: 0,
      name: 'OK',
    },
  },
  {
    date: '2017-11-22 11:34',
    status: {
      id: 3,
      name: 'Fel',
    },
  },
  {
    date: '2017-11-22 12:34',
    status: {
      id: 0,
      name: 'OK',
    },
  },
];

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

const getRandomAlarm = (meterStatus) => {
  if (meterStatus === 0 || meterStatus === 4) {
    return ':Inget fel:';
  }
  return alarmsLookup[Math.floor(Math.random() * alarmsLookup.length)];
};

const parseMeasurementSeedData = (path) => {
  const measurements = [];
  const statusChanges = {};
  const padZero = (aNumber) => {
    return aNumber < 10 ? `0${aNumber}` : aNumber + '';
  };

  glob.sync(path).forEach((seedFile) => {
    fs.readFileSync(seedFile, 'utf-8')
      .split('\n')
      .forEach((csv) => {
        if (csv.length === 0) {
          return;
        }
        const [facility, meterId, dateString, energy, volume, forwardTemp, returnTemp] = csv.split(';');
        const year = dateString.substr(0, 4);
        const month = Number(dateString.substr(4, 2));
        const day = Number(dateString.substr(6, 2));
        // NOTE: We're only including about a week of data here, since serializing
        // any more will either take a *lot* of time or crash with an OOM error.
        if (month < 10 || (month === 10 && day < 28)) {
          return;
        }
        const hour = dateString.substr(8, 2);
        const minute = dateString.substr(10, 2);
        // this could be a complete lie, but hopefully the CSV is ordered by date in descending order
        // also, we don't know if the status changed, but at least there is 'a' timestamp.
        const createdAt = `${year}-${month}-${padZero(day)} ${hour}:${minute}`;
        const created = moment(createdAt).valueOf();

        statusChanges[meterId] = createdAt;

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
  return {measurements, statusChanges};
};

const parseMeterSeedData = (path, seedOptions = {
  geocodeCacheFile: null, doGeocoding: false, statusChanges: {}
}) => {
  const {geocodeCacheFile, doGeocoding, statusChanges} = seedOptions;
  const r = {
    meters: [],
    gateways: [],
    selections: {
      meteringPoints: [],
      meterStatuses: [],
      gatewayStatuses: [],
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
  const meterStatuses = new Set();
  const gatewayStatuses = new Set();
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

      const decorateGatewayStatus = (status) => status === 'OK'
        ? {name: status, id: 0}
        : {name: 'Fel', id: 3};

      const decorateMeterStatus = (gwStatus, status) =>
        gwStatus === 'OK' ? status === 'OK' ? {name: 'ok', id: 0} : {name: 'alarm', id: 3}
          : {name: 'unknown', id: 4};

      const nullOr = (str) => str === 'NULL' ? null : str;

      const meterStatus = decorateMeterStatus(row.gateway_status, row.meter_status);
      const gatewayStatus = decorateGatewayStatus(row.gateway_status);

      row.gateway_flags = row.gateway_status === 'OK' ? [] : [{title: 'Åtgärd'}];
      row.meter_flags = meterStatus.id === 0 ? [] : [];

      const cityId = row.city;
      const addressId = row.address;
      const city = {id: cityId, name: row.city};
      const address = {id: addressId, name: row.address, cityId};

      const gatewayId = row.gateway_id;
      const meterId = row.meter_id;
      const statusChanged = statusChanges[meterId];
      const alarm = getRandomAlarm(meterStatus.id);

      const gatewayStatusChangelog = statusChangelog
        .map(changelog => Object.assign({}, changelog, {id: generateId(), gatewayId}));

      const meterStatusChangelog = statusChangelog
        .map(changelog => Object.assign({}, changelog, {id: generateId(), meterId}));

      r.gateways.push({
        id: gatewayId,
        facility: row.facility,
        address,
        city,
        flags: row.gateway_flags,
        flagged: row.gateway_flags.length !== 0,
        productModel: row.gateway_product_model,
        status: gatewayStatus,
        statusChangelog: gatewayStatusChangelog,
        statusChanged,
        meterIds: [meterId],
        position: objPosition,
        meterStatus,
        meterAlarm: alarm,
        meterManufacturer: row.meter_manufacturer,
      });

      r.meters.push({
        id: meterId,
        facility: row.facility,
        address,
        city,
        flags: row.meter_flags,
        flagged: row.meter_flags.length !== 0,
        medium: row.medium,
        manufacturer: row.meter_manufacturer,
        status: meterStatus,
        statusChangelog: meterStatusChangelog,
        statusChanged,
        position: objPosition,
        alarm,
        gatewayId,
        gatewayStatus,
        gatewayProductModel: row.gateway_product_model,
      });
      if (!cities.has(cityId)) {
        r.selections.cities.push({id: cityId, name: row.city});
        cities.add(cityId);
      }
      if (!addresses.has(addressId)) {
        r.selections.addresses.push({id: addressId, name: row.address, cityId});
        addresses.add(addressId);
      }
      if (!meteringPoints.has(meterId)) {
        r.selections.meteringPoints.push({id: meterId, name: meterId});
        meteringPoints.add(meterId);
      }
      if (!meterStatuses.has(meterStatus.id)) {
        r.selections.meterStatuses.push(meterStatus);
        meterStatuses.add(meterStatus.id);
      }
      if (!gatewayStatuses.has(gatewayStatus.id)) {
        r.selections.gatewayStatuses.push(gatewayStatus);
        gatewayStatuses.add(gatewayStatus.id);
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
        r.selections.productModels.push({
          id: row.gateway_product_model, name: row.gateway_product_model
        });
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
