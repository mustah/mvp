import {SelectedParameters} from '../../state/search/selection/selectionModels';
import {Status} from '../../types/Types';
import {encodedUriParametersFrom} from '../urlFactory';

describe('urlFactory', () => {

  describe('parameters from selected ids', () => {

    it('returns empty parameters string when nothing is selected', () => {
      const selection: SelectedParameters = {cities: []};

      expect(encodedUriParametersFrom(selection)).toEqual('');
    });

    it('returns selected city', () => {
      const selection: SelectedParameters = {cities: ['got']};

      expect(encodedUriParametersFrom(selection)).toEqual('city=got');
    });

    it('returns selected cities', () => {
      const selection: SelectedParameters = {cities: ['got', 'sto', 'mmx']};

      expect(encodedUriParametersFrom(selection)).toEqual('city=got&city=sto&city=mmx');
    });

    it('returns selected address', () => {
      const selection: SelectedParameters = {addresses: ['address 2']};

      expect(encodedUriParametersFrom(selection)).toEqual('address=address%202');
    });

    it('returns selected addresses', () => {
      const selection: SelectedParameters = {addresses: ['address 2', 'storgatan 5']};

      expect(encodedUriParametersFrom(selection)).toEqual('address=address%202&address=storgatan%205');
    });

    it('returns selected statuses', () => {
      const selection: SelectedParameters = {statuses: [Status.ok, Status.warning]};

      expect(encodedUriParametersFrom(selection)).toEqual('status=ok&status=warning');
    });

    it('returns all selected parameters', () => {
      const selection: SelectedParameters = {
        addresses: ['address 2', 'storgatan 5'],
        cities: ['got', 'sto', 'mmx'],
        statuses: [Status.ok, Status.warning],
      };

      const expected = 'address=address%202&address=storgatan%205&city=got&city=sto&city=mmx&status=ok&status=warning';
      expect(encodedUriParametersFrom(selection)).toEqual(expected);
    });

  });
});
