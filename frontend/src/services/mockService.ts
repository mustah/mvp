import {Meter} from '../state/domain-models/meter/meterModels';

export const makeMeter = (id: number, cityId: number, city: string, addressId: number, address: string): Meter => ({
  id,
  moid: String(id),
  facility: '1',
  alarm: '1',
  flags: [],
  flagged: false,
  medium: 'asdf',
  manufacturer: 'asdf',
  status: {id: 0, name: 'ok'},
  gatewayId: 'a',
  gatewayProductModel: 'a',
  gatewayStatus: {id: 0, name: 'ok'},
  address: {
    cityId,
    id: addressId,
    name: address,
  },
  city: {
    id: cityId,
    name: city,
  },
  position: {
    latitude: 1,
    longitude: 1,
    confidence: 1,
  },
  statusChangelog: [],
});
