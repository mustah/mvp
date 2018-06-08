import {Meter, MeterStatusChangelog} from '../state/domain-models-paginated/meter/meterModels';
import {Flag} from '../state/domain-models/flag/flagModels';
import {LocationHolder} from '../state/domain-models/location/locationModels';
import {Identifiable, IdNamed, Status, uuid} from '../types/Types';

const selections = {
  locations: {
    countries: [
      {
        name: 'sweden',
        cities: [
          {
            name: 'göteborg',
            addresses: [
              {name: 'kungsgatan'},
            ],
          },
          {
            name: 'stockholm',
            addresses: [
              {name: 'kungsgatan'},
              {name: 'drottninggatan'},
            ],
          },
        ],
      },
      {
        name: 'finland',
        cities: [
          {
            name: 'vasa',
            addresses: [
              {name: 'kungsgatan'},
            ],
          },
        ],
      },
    ],
  },
  alarms: [],
  manufacturers: [],
  productModels: [],
  meterStatuses: [],
  gatewayStatuses: [],
  users: [],
  facilities: [
    {id: 'a', name: '1'},
    {id: 'b', name: '2'},
    {id: 'c', name: '3'},
  ],
  secondaryAddresses: [
    {id: 'aa', name: '11'},
    {id: 'ab', name: '12'},
    {id: 'ac', name: '13'},
  ],
  gatewaySerials: [
    {id: 'ba', name: '21'},
    {id: 'bb', name: '22'},
    {id: 'bc', name: '23'}],
};

const meters = [
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
];

const gateways = {
  content: [
    {
      id: 'g1',
      name: 'UNICOcoder',
    },
    {
      id: 'g2',
      name: '3100',
    },
    {
      id: 'g3',
      name: 'xxx2233',
    },
    {
      id: 'g4',
      name: '3100',
    },
    {
      id: 'g5',
      name: 'Test kit',
    },
  ],
  totalElements: 5,
  last: true,
  totalPages: 1,
  size: 20,
  number: 0,
  first: true,
  sort: null,
  numberOfElements: 5,
};

const meterStatuses = [
  {
    id: 'ok',
    name: 'ok',
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
];

const facilities = [
  {id: 'a', name: '1'},
  {id: 'b', name: '2'},
  {id: 'c', name: '3'},
];

const secondaryAddresses = [
  {id: 'aa', name: '11'},
  {id: 'ab', name: '12'},
  {id: 'ac', name: '13'},
];

const gatewaySerials = [
  {id: 'ba', name: '21'},
  {id: 'bb', name: '22'},
  {id: 'bc', name: '23'},
];

export const testData = {
  selections,
  meterStatuses,
  gatewayStatues: meterStatuses,
  meters,
  gateways,
  facilities,
  secondaryAddresses,
  gatewaySerials,
};

const okStatus: IdNamed = {id: Status.ok, name: Status.ok};

export const makeMeter = (id: number, city: IdNamed, address: IdNamed): Meter => (
  {
    id,
    facility: '1',
    medium: 'asdf',
    manufacturer: 'asdf',
    status: okStatus,
    gatewaySerial: '123',
    location: {
      address,
      city,
      position: {
        latitude: 1,
        longitude: 1,
      },
    },
    readIntervalMinutes: 60,
    organisationId: '',
  }
);

export interface MeterDto extends Identifiable, LocationHolder {
  status: Status;
  facility: uuid;
  flags: Flag[];
  flagged: boolean;
  statusChangelog: MeterStatusChangelog[];
}

export const makeMeterDto = (id: number, city: IdNamed, address: IdNamed): MeterDto => {
  const meter: any = makeMeter(id, city, address);
  return {
    ...meter,
    status: Status.ok,
  };
};
