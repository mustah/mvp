import {Meter, MeterStatusChangelog} from '../state/domain-models-paginated/meter/meterModels';
import {Flag} from '../state/domain-models/flag/flagModels';
import {LocationHolder} from '../state/domain-models/location/locationModels';
import {Identifiable, IdNamed, Status, uuid} from '../types/Types';

const selections = {
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
  alarms: [],
  manufacturers: [],
  productModels: [],
  meterStatuses: [],
  gatewayStatuses: [],
  users: [],
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

const gateways = [
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
];

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

export const testData = {
  selections,
  meterStatuses,
  gatewayStatues: meterStatuses,
  meters,
  gateways,
};

const okStatus: IdNamed = {id: Status.ok, name: Status.ok};

export const makeMeter = (id: number, city: IdNamed, address: IdNamed): Meter => (
  {
    id,
    facility: '1',
    alarm: '1',
    flags: [],
    flagged: false,
    medium: 'asdf',
    manufacturer: 'asdf',
    status: okStatus,
    gateway: {id: 'a', productModel: 'a', serial: '123', status: okStatus},
    location: {
      address,
      city,
      position: {
        latitude: 1,
        longitude: 1,
        confidence: 1,
      },
    },
    statusChangelog: [],
  }
);

export interface MeterDto extends Identifiable, LocationHolder {
  status: Status;
  facility: uuid;
  alarm: string;
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
