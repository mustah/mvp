import {toIdNamed, uuid} from '../../../../types/Types';
import {Address, City} from '../../location/locationModels';
import {mapSelectedIdToAddress, mapSelectedIdToCity} from '../selectionsApiActions';

describe('selectionsApiActions', () => {

  describe('map selected city id to city', () => {

    it('maps selected city', () => {
      const id: uuid = 'sverige,stockholm';
      const expected: City = {
        id,
        name: 'stockholm',
        country: {...toIdNamed('sverige')},
      };
      expect(mapSelectedIdToCity(id)).toEqual(expected);
    });

  });

  describe('map selected id to address', () => {

    it('maps selected address', () => {
      const id: uuid = 'sverige,stockholm,gata 1';
      const expected: Address = {
        id,
        name: 'gata 1',
        city: {...toIdNamed('stockholm')},
        country: {...toIdNamed('sverige')},
      };

      expect(mapSelectedIdToAddress(id)).toEqual(expected);
    });

  });
});
