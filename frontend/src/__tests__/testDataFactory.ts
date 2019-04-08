import {EventLog, Meter} from '../state/domain-models-paginated/meter/meterModels';
import {LocationHolder} from '../state/domain-models/location/locationModels';
import {Role, User} from '../state/domain-models/user/userModels';
import {LegendItem, SavedReportsState} from '../state/report/reportModels';
import {makeInitialLegendViewOptions} from '../state/report/reportReducer';
import {Identifiable, uuid} from '../types/Types';

const meters = [
  {id: 'm1', name: 'UNICOcoder'},
  {id: 'm2', name: '3100'},
  {id: 'm3', name: 'xxx2233'},
  {id: 'm4', name: '3100'},
  {id: 'm5', name: 'Test kit'},
];

const gateways = {
  content: [
    {id: 'g1', name: 'UNICOcoder'},
    {id: 'g2', name: '3100'},
    {id: 'g3', name: 'xxx2233'},
    {id: 'g4', name: '3100'},
    {id: 'g5', name: 'Test kit'},
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

export const testData = {
  meters,
  gateways,
};

export const makeMeter = (id: number, city: string, address: string, medium: string = 'Gas'): Meter => (
  {
    id,
    facility: '1',
    medium,
    manufacturer: 'asdf',
    gatewaySerial: '123',
    location: {
      address,
      city,
      country: 'sverige',
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
  facility: uuid;
  isReported?: boolean;
  statusChangelog: EventLog[];
}

export const makeMeterDto = (id: number, city: string, address: string): MeterDto => {
  const meter: any = makeMeter(id, city, address);
  return {
    ...meter,
  };
};

export const savedReportsWith = (legendItems: LegendItem[]): SavedReportsState =>
  ({
    meterPage: {
      id: 'meterPage',
      legendItems,
      legendViewOptions: makeInitialLegendViewOptions(),
      shouldShowAverage: false,
    }
  });

export const makeUser = (): User => ({
  id: 1,
  name: 'clark',
  email: 'ck@dailyplanet.net',
  language: 'en',
  organisation: {id: 'daily planet', name: 'daily planet', slug: 'daily-planet'},
  roles: [Role.USER],
});
