const seedrandom = require('seedrandom');
const fs = require('fs');
const csvjson = require('csvjson');
const glob = require('glob');
const fromDbJson = {
  authenticate: {
    id: 8,
    firstName: 'Eva',
    lastName: 'Nilsson',
    email: 'evanil@elvaco.se',
    company: 'Bostäder AB',
  },
  'todos': [
    'translate unhandled and handled collections in the right place',
  ],
  'collections': {
    'unhandled': {
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
      'product_model':
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
      'product_model':
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
  'dashboards': [
    {
      'id': '3',
      'author': 'Sven',
      'title': 'Sven\'s dashboard from the DashboardController',
      'systemOverview': {
        'title': 'Sven\'s system overview from the DashboardController',
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
  'reports': [
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
  'validations': [
    {
      'id': 1,
      'title': 'json-server',
      'author': 'typicode',
    },
    {
      'id': 2,
      'title': 'validation 2',
      'author': 'elvaco',
    },
    {
      'id': 3,
      'title': 'validation 3',
      'author': 'elvaco - home',
    },
  ],
  'profile': {
    'name': 'typicode',
  },
  'selections': {
    'cities': [
      {
        'id': 'got',
        'name': 'Göteborg',
      },
      {
        'id': 'sto',
        'name': 'Stockholm',
      },
      {
        'id': 'mmx',
        'name': 'Malmö',
      },
      {
        'id': 'kub',
        'name': 'Kungsbacka',
      },
    ],
    'addresses': [
      {
        'id': 1,
        'name': 'Stampgatan 46',
      },
      {
        'id': 2,
        'name': 'Stampgatan 33',
      },
      {
        'id': 3,
        'name': 'Kungsgatan 44',
      },
      {
        'id': 4,
        'name': 'Drottninggatan 1',
      },
      {
        'id': 5,
        'name': 'Åvägen 9',
      },
    ],
    'statuses': [
      {
        'id': 'ok',
        'name': 'Ok',
      },
      {
        'id': 'warning',
        'name': 'Varning',
      },
      {
        'id': 'info',
        'name': 'Info',
      },
      {
        'id': 'critical',
        'name': 'Kritisk',
      },
    ],
    'meteringPoints': [
      {
        'id': 'm1',
        'name': 'UNICOcoder',
      },
      {
        'id': 'm2',
        'name': '3100',
      },
      {
        'id': 'm3',
        'name': 'xxx2233',
      },
      {
        'id': 'm4',
        'name': '3100',
      },
      {
        'id': 'm5',
        'name': 'Test kit',
      },
    ],
  },
};

const gatewayStatuses = [
  {
    'code': 0,
    'text': 'OK',
  },
  {
    'code': 3,
    'text': 'Gateway kunde inte avläsas',
  },
];

const cities = [
  'Göteborg',
  'Kungsbacka',
  'Mölndal',
  'Stockholm',
  'Alvesta',
  'Höganäs',
  'Varberg',
  'Borås',
];

const position = [
  {lat: 57.715954, lng: 11.974855},
  {lat: 57.487614, lng: 12.076706},
  {lat: 57.650215, lng: 12.016228},
  {lat: 59.330270, lng: 18.069251},
  {lat: 56.899273, lng: 14.556001},
  {lat: 56.200461, lng: 12.555504},
  {lat: 57.107967, lng: 12.272229},
  {lat: 57.721190, lng: 12.940214},
];

const gatewayModels = [
  'CMe2100',
  'CMi2110',
  'CMe3100',
];

const meterModels = [
  'Kamstrup Multical 403',
  'Kamstrup Multical 603',
];

const actions = [
  '',
  'Inspektion väntar',
  'Ny produkt beställd',
  'Lagret kör ut ny gateway',
];

const mpDistrictHeatingErrors = [
  'Battery low',
  'Flow sensor error (air)',
  'Flow sensor error (generic)',
  'Flow sensor error (dirty)',
  'Leakage',
  'Overflow',
  'Backflow',
  'Forward temperature sensor error',
  'Return temperature sensor error',
  'Temperature sensor error (generic)',
  'Temperature sensor inverted',
  'Tamper error',
  'Supply voltage error',
  'Time for battery change',
  'Internal meter error',
];

const getPosition = (area) => {
  switch (area) {
    case   'Göteborg': {
      return randomizeLatLng(position[0]);
    }
    case 'Mölndal': {
      return randomizeLatLng(position[1]);
    }
    case 'Stockholm': {
      return randomizeLatLng(position[2]);
    }
    case 'Alvesta': {
      return randomizeLatLng(position[3]);
    }
    case 'Höganäs': {
      return randomizeLatLng(position[4]);
    }
    case 'Varberg': {
      return randomizeLatLng(position[5]);
    }
    case 'Borås': {
      return randomizeLatLng(position[6]);
    }
    default:
      return randomizeLatLng(position[6]);
  }
};

const randomizeLatLng = (a) => {
  return {
    lat: getRandomArbitrary(a.lat - 0.05, a.lat + 0.05),
    lng: getRandomArbitrary(a.lng - 0.05, a.lng + 0.05),
  };
};

const getRandomArbitrary = (min, max) => {
  return Math.random() * (max - min) + min;
};

const getWeightedRandomStatus = () => {
  const x = getRandomArbitrary(0, 100);
  if (x < 1) {
    return 2;
  } else if (x < 5) {
    return 1;
  } else {
    return 0;
  }
};

module.exports = () => {
  const returnValues = Object.assign({}, fromDbJson);
  returnValues.meters = [];
  returnValues.gateways = [];
  glob('data/seed_data/*.csv', {}, (er, files) => {
    if (er) {
      throw er;
    }
    files.forEach((seedFile) => {
      const meterData = fs.readFileSync(seedFile, 'utf-8').toString();
      const options = {
        delimiter: ';',
        headers: 'facility;address;city;medium;meter_id;meter_manufacturer;' +
                 'gateway_id;gateway_product_model;tel;ip;port;gateway_status;meter_status',
      };
      const obj = csvjson.toObject(meterData, options);
      obj.forEach((row) => {
        returnValues.meters.push({
          'id': row.gateway_id,
          'facility': row.facility,
          'address': row.address,
          'city': row.city,
          'product_model': row.gateway_product_model,
          'telephone_no': row.tel,
          'ip': row.ip,
          'port': row.port,
          'status': row.gateway_status,
          'position': getPosition(row.city),
        });
      });
      obj.forEach((row) => {
        returnValues.gateways.push({
          'id': row.meter_id,
          'facility': row.facility,
          'address': row.address,
          'city': row.city,
          'medium': row.medium,
          'manufacturer': row.meter_manufacturer,
          'status': row.meter_status,
          'gateway_id': row.gateway_id,
          'position': getPosition(row.city),
        });
      });
    });
  });

  // remove the entire endpoint from fromDbJson once we're done with the generation logic
  returnValues.random_gateways = [];
  returnValues.random_meters = [];

  const appRandom = new Math.seedrandom('this is a seed');

  for (let i = 0; i < 1000; i++) {
    const numberOfMeters = Math.floor(appRandom() * 7);
    const gwId = 23 + i;
    const gwStatus = gatewayStatuses[Math.floor(appRandom() * gatewayStatuses.length)];
    const city = cities[Math.floor(appRandom() * cities.length)];

    const gw = {
      'id': gwId,
      'city': city,
      'product_model': gatewayModels[Math.floor(appRandom() * gatewayModels.length)],
      'status': gwStatus,
      'connected_meters': numberOfMeters,
      'action': (gwStatus.code === 0 ? '' : actions[Math.floor(appRandom() * actions.length)]),
    };

    for (let j = 0; j < numberOfMeters; j++) {
      const meter = {
        'gateway_id': gwId,
        'id': Math.floor(appRandom() * 100000),
        'medium': 'Heat, Return temp',
        'status': getWeightedRandomStatus(),
        'city': city,
        'position': getPosition(city),
        'product_model': meterModels[Math.floor(appRandom() * meterModels.length)],
      };
      returnValues.random_meters.push(meter);
    }

    returnValues.random_gateways.push(gw);
  }
  return returnValues;
};
