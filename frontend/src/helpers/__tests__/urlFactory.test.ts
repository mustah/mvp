import {Period} from '../../components/dates/dateModels';
import {SelectedParameters} from '../../state/search/selection/selectionModels';
import {Pagination} from '../../state/ui/pagination/paginationModels';
import {Status} from '../../types/Types';
import {encodedUriParametersForGateways, encodedUriParametersForMeters} from '../urlFactory';

describe('urlFactory', () => {

  describe('calculate uri parameters from selected ids and pagination', () => {
    const selectedParameters = (parameters: Partial<SelectedParameters>): SelectedParameters => {
      parameters.period = Period.latest;
      return parameters as SelectedParameters;
    };
    const pagination: Pagination = {
      page: 0,
      totalElements: 1000,
      totalPages: 100,
      size: 10,
    };

    it('returns empty parameters string when nothing is selected', () => {
      const selection = selectedParameters({cities: []});

      expect(encodedUriParametersForMeters(pagination, selection))
        .toEqual(`size=${pagination.size}&page=${pagination.page}`);
    });

    it('returns selected city', () => {
      const selection = selectedParameters({cities: ['got']});

      expect(encodedUriParametersForMeters(pagination, selection))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&city.id=got`);
    });

    it('returns selected cities', () => {
      const selection = selectedParameters({cities: ['got', 'sto', 'mmx']});

      expect(encodedUriParametersForMeters(pagination, selection))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&city.id=got&city.id=sto&city.id=mmx`);
    });

    it('returns selected address', () => {
      const selection = selectedParameters({addresses: ['address 2']});

      expect(encodedUriParametersForMeters(pagination, selection))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&address.id=address%202`);
    });

    it('returns selected addresses', () => {
      const selection = selectedParameters({addresses: ['address 2', 'storgatan 5']});

      expect(encodedUriParametersForMeters(pagination, selection))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&address.id=address%202&address.id=storgatan%205`);
    });

    it('returns selected statuses', () => {
      const selection = selectedParameters({meterStatuses: [Status.ok, Status.warning]});

      expect(encodedUriParametersForMeters(pagination, selection))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&status.id=ok&status.id=warning`);
    });

    it('returns all selected parameters', () => {
      const selection = selectedParameters({
        addresses: ['address 2', 'storgatan 5'],
        cities: ['got', 'sto', 'mmx'],
        meterStatuses: [Status.ok, Status.warning],
      });

      const expected =
        `size=${pagination.size}&page=${pagination.page}&address.id=address%202&address.id=storgatan%205` +
        '&city.id=got&city.id=sto&city.id=mmx&status.id=ok&status.id=warning';
      expect(encodedUriParametersForMeters(pagination, selection)).toEqual(expected);
    });
  });

  describe('calculate uri parameters from selected ids and no pagination', () => {
    const selectedParameters = (parameters: Partial<SelectedParameters>): SelectedParameters => {
      parameters.period = Period.latest;
      return parameters as SelectedParameters;
    };

    it('returns empty parameters string when nothing is selected', () => {
      const selection = selectedParameters({cities: []});

      expect(encodedUriParametersForGateways(selection))
        .toEqual('');
    });

    it('returns selected city', () => {
      const selection = selectedParameters({cities: ['got']});

      expect(encodedUriParametersForGateways(selection))
        .toEqual('city.id=got');
    });

    it('returns selected cities', () => {
      const selection = selectedParameters({cities: ['got', 'sto', 'mmx']});

      expect(encodedUriParametersForGateways(selection))
        .toEqual('city.id=got&city.id=sto&city.id=mmx');
    });

    it('returns all selected parameters', () => {
      const selection = selectedParameters({
        addresses: ['address 2', 'storgatan 5'],
        cities: ['got', 'sto', 'mmx'],
        meterStatuses: [Status.ok, Status.warning],
      });

      const expected =
        `address.id=address%202&address.id=storgatan%205` +
        '&city.id=got&city.id=sto&city.id=mmx&meterStatus=ok&meterStatus=warning';
      expect(encodedUriParametersForGateways(selection)).toEqual(expected);
    });
  });
});
