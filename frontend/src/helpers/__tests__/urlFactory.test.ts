import {Period} from '../../components/dates/dateModels';
import {SelectedParameters} from '../../state/search/selection/selectionModels';
import {Status} from '../../types/Types';
import {encodedUriParametersForMeters} from '../urlFactory';

describe('urlFactory', () => {

  describe('parameters from selected ids', () => {
    const selectedParameters = (parameters: Partial<SelectedParameters>): SelectedParameters => {
      parameters.period = Period.latest;
      return parameters as SelectedParameters;
    };

    it('returns empty parameters string when nothing is selected', () => {
      const selection = selectedParameters({cities: []});

      expect(encodedUriParametersForMeters(selection)).toEqual('');
    });

    it('returns selected city', () => {
      const selection = selectedParameters({cities: ['got']});

      expect(encodedUriParametersForMeters(selection)).toEqual('city.id=got');
    });

    it('returns selected cities', () => {
      const selection = selectedParameters({cities: ['got', 'sto', 'mmx']});

      expect(encodedUriParametersForMeters(selection)).toEqual('city.id=got&city.id=sto&city.id=mmx');
    });

    it('returns selected address', () => {
      const selection = selectedParameters({addresses: ['address 2']});

      expect(encodedUriParametersForMeters(selection)).toEqual('address.id=address%202');
    });

    it('returns selected addresses', () => {
      const selection = selectedParameters({addresses: ['address 2', 'storgatan 5']});

      expect(encodedUriParametersForMeters(selection)).toEqual('address.id=address%202&address.id=storgatan%205');
    });

    it('returns selected statuses', () => {
      const selection = selectedParameters({meterStatuses: [Status.ok, Status.warning]});

      expect(encodedUriParametersForMeters(selection)).toEqual('status.id=ok&status.id=warning');
    });

    it('returns all selected parameters', () => {
      const selection = selectedParameters({
        addresses: ['address 2', 'storgatan 5'],
        cities: ['got', 'sto', 'mmx'],
        meterStatuses: [Status.ok, Status.warning],
      });

      const expected =
        'address.id=address%202&address.id=storgatan%205&city.id=got&city.id=sto&city.id=mmx&' +
        'status.id=ok&status.id=warning';
      expect(encodedUriParametersForMeters(selection)).toEqual(expected);
    });

  });
});
