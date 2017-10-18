const seedrandom = require('seedrandom');

const fromDbJson = {
  "todos": [
    "translate unhandled and handled collections in the right place"
  ],
  "collections": {
    "unhandled": {
      "total": 2983,
      "area": {
        "count": 7,
        "entities":
          [
            {"id": "Göteborg", "count": 256},
            {"id": "Kungsbacka", "count": 2000},
            {"id": "Mölndal", "count": 33},
            {"id": "Stockholm", "count": 44},
            {"id": "Alvesta", "count": 55},
            {"id": "Höganäs", "count": 345},
            {"id": "Borås", "count": 250}
          ]
      },
      "product_model":
        {
          "count": 3,
          "entities": [
            {"id": "CMe2100", "count": 273},
            {"id": "CMi2100", "count": 1576},
            {"id": "CMe3100", "count": 1134}
            ]
        }
    },
    "handled": {
      "total": 2627,
      "area":
        {
          "count": 7,
          "entities": [
            {"id": "Göteborg", "count": 113},
            {"id": "Kungsbacka", "count": 756},
            {"id": "Mölndal", "count": 123},
            {"id": "Stockholm", "count": 423},
            {"id": "Alvesta", "count": 916},
            {"id": "Höganäs", "count": 44},
            {"id": "Borås", "count": 252}
          ]
        },
      "product_model":
        {
          "count": 3,
          "entities": [
            {"id": "CMe2100", "count": 573},
            {"id": "CMi2100", "count": 176},
            {"id": "CMe3100", "count": 1878}
          ]
        }
    },
  },
  "dashboards": [
    {
      "id": "3",
      "author": "Sven",
      "title": "Sven's dashboard from the DashboardController",
      "systemOverview": {
        "title": "Sven's system overview from the DashboardController",
        "indicators": [
          {
            "title": "Insamling",
            "type": "collection",
            "subtitle": "3567 punkter",
            "state": "warning",
            "value": "95.8",
            "unit": "%"
          },
          {
            "title": "Mätvärdeskvalitet",
            "type": "measurementQuality",
            "subtitle": "3481 punkter",
            "state": "critical",
            "value": "93.5",
            "unit": "%"
          }
        ]
      }
    }
  ],
  "reports": [
    {
      "id": 1,
      "title": "json-server",
      "author": "typicode"
    },
    {
      "id": 2,
      "title": "report 2",
      "author": "elvaco"
    },
    {
      "id": 3,
      "title": "report 3",
      "author": "elvaco - home"
    }
  ],
  "validations": [
    {
      "id": 1,
      "title": "json-server",
      "author": "typicode"
    },
    {
      "id": 2,
      "title": "validation 2",
      "author": "elvaco"
    },
    {
      "id": 3,
      "title": "validation 3",
      "author": "elvaco - home"
    }
  ],
  "profile": {
    "name": "typicode"
  },
  "search-options": {
    "cities": [
      {
        "id": "got",
        "name": "Göteborg"
      },
      {
        "id": "sto",
        "name": "Stockholm"
      },
      {
        "id": "mmx",
        "name": "Malmö"
      },
      {
        "id": "kub",
        "name": "Kungsbacka"
      }
    ],
    "addresses": [
      {
        "id": 1,
        "name": "Stampgatan 46"
      },
      {
        "id": 2,
        "name": "Stampgatan 33"
      },
      {
        "id": 3,
        "name": "Kungsgatan 44"
      },
      {
        "id": 4,
        "name": "Drottninggatan 1"
      },
      {
        "id": 5,
        "name": "Åvägen 9"
      }
    ],
    "statuses": [
      {
        "id": "ok",
        "name": "Ok"
      },
      {
        "id": "warning",
        "name": "Varning"
      },
      {
        "id": "info",
        "name": "Info"
      },
      {
        "id": "critical",
        "name": "Kritisk"
      }
    ],
    "meteringPoints": [
      {
        "id": "m1",
        "name": "UNICOcoder"
      },
      {
        "id": "m2",
        "name": "3100"
      },
      {
        "id": "m3",
        "name": "xxx2233"
      },
      {
        "id": "m4",
        "name": "3100"
      },
      {
        "id": "m5",
        "name": "Test kit"
      }
    ]
  }
};

const statuses = [
  {
    "code": 0,
    "text": "OK"
  },
  {
    "code": 2,
    "text": "Något är fel"
  },
  {
    "code": 3,
    "text": "Mayday"
  },
];

const areas = [
  "Göteborg",
  "Kungsbacka",
  "Mölndal",
  "Stockholm",
  "Alvesta",
  "Höganäs",
  "Borås",
];

const position = [
  {lat: 49.8397, lng: 24.0297}
];

const gatewayModels = [
  "CMe2100",
  "CMi2110",
  "CMe3100",
];

const meterModels = [
  "Kamstrup Multical 403",
  "Kamstrup Multical 603",
];

const actions = [
  "",
  "Inspektion väntar",
  "Ny produkt beställd",
  "Lagret kör ut ny gateway",
];

module.exports = () => {
  const returnValues = Object.assign({}, fromDbJson);

  // remove the entire endpoint from fromDbJson once we're done with the generation logic
  returnValues['gateways'] = [];
  returnValues['mps'] = [];

  const appRandom = new Math.seedrandom("this is a seed");

  for (let i = 0; i < 10; i++) {
    const numberOfMeters = Math.floor(appRandom() * 7);
    const gwId = 23 + i;
    const gwStatus = statuses[Math.floor(appRandom() * statuses.length)];
    const area = areas[Math.floor(appRandom() * areas.length)];

    const gw = {
      "id": gwId,
      "area": area,
      "product_model": gatewayModels[Math.floor(appRandom() * gatewayModels.length)],
      "status": gwStatus,
      "connected_meters": numberOfMeters,
      "action": (gwStatus['code'] === 0 ? "" : actions[Math.floor(appRandom() * actions.length)]),
    };

    for (let j = 0; j < numberOfMeters; j++) {
      const meter = {
        "gateway_id": gwId,
        "id": Math.floor(appRandom() * 100000),
        "medium": "Heat, Return temp",
        "status": Math.floor(appRandom() * 3),
        "area": area,
        "position": position[0],
        "product_model": meterModels[Math.floor(appRandom() * meterModels.length)],
      };
      returnValues['mps'].push(meter);
    }

    returnValues['gateways'].push(gw);
  }
  return returnValues;
};
