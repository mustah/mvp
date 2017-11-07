
const geoData = {
  cities: [
    {
      id: 'got',
      name: 'Göteborg',
    },
    {
      id: 'sto',
      name: 'Stockholm',
    },
    {
      id: 'mmx',
      name: 'Malmö',
    },
    {
      id: 'kub',
      name: 'Kungsbacka',
    },
  ],
  addresses: [
    {
      id: 1,
      name: 'Stampgatan 46',
      cityId: 'got',
    },
    {
      id: 2,
      name: 'Stampgatan 33',
      cityId: 'got',
    },
    {
      id: 3,
      name: 'Kungsgatan 44',
      cityId: 'sto',
    },
    {
      id: 4,
      name: 'Drottninggatan 1',
      cityId: 'mmx',
    },
    {
      id: 5,
      name: 'Åvägen 9',
      cityId: 'kub',
    },
  ],
};

const meters = {
  meters: [
    {
      id: 'm1',
      name: 'UNICOcoder',
    },
    {
      id: 'm2',
      name: '3100',
    },
    {
      id: 'm3',
      name: 'xxx2233',
    },
    {
      id: 'm4',
      name: '3100',
    },
    {
      id: 'm5',
      name: 'Test kit',
    },
  ],
};

const statuses = {
  statuses: [
    {
      id: 'ok',
      name: 'Ok',
    },
    {
      id: 'warning',
      name: 'Varning',
    },
    {
      id: 'info',
      name: 'Info',
    },
    {
      id: 'critical',
      name: 'Kritisk',
    },
  ],
};

export const testData = {
  geoData,
  statuses,
  meters,
};
