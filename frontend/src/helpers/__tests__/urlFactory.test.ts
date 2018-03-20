import {Period} from '../../components/dates/dateModels';
import {SelectedParameters} from '../../state/search/selection/selectionModels';
import {Pagination} from '../../state/ui/pagination/paginationModels';
import {Status} from '../../types/Types';
import {
  encodedUriParametersForAllMeters, encodedUriParametersForGateways,
  encodedUriParametersForMeters,
} from '../urlFactory';

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
      expect(encodedUriParametersForGateways(pagination, selection))
        .toEqual(`size=${pagination.size}&page=${pagination.page}`);
    });

    it('returns selected city', () => {
      const selection = selectedParameters({cities: ['got']});

      expect(encodedUriParametersForMeters(pagination, selection))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&city=got`);
      expect(encodedUriParametersForGateways(pagination, selection))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&city=got`);
    });

    it('returns selected cities', () => {
      const selection = selectedParameters({cities: ['got', 'sto', 'mmx']});

      expect(encodedUriParametersForMeters(pagination, selection))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&city=got&city=sto&city=mmx`);
      expect(encodedUriParametersForGateways(pagination, selection))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&city=got&city=sto&city=mmx`);
    });

    it('returns selected address', () => {
      const selection = selectedParameters({addresses: ['address 2']});

      expect(encodedUriParametersForMeters(pagination, selection))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&address=address%202`);
      expect(encodedUriParametersForGateways(pagination, selection))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&address=address%202`);
    });

    it('returns selected addresses', () => {
      const selection = selectedParameters({addresses: ['address 2', 'storgatan 5']});

      expect(encodedUriParametersForMeters(pagination, selection))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&address=address%202&address=storgatan%205`);
      expect(encodedUriParametersForGateways(pagination, selection))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&address=address%202&address=storgatan%205`);
    });

    it('returns selected statuses', () => {
      const selection = selectedParameters({meterStatuses: [Status.ok, Status.warning], gatewayStatuses: [Status.ok]});

      expect(encodedUriParametersForMeters(pagination, selection))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&status=ok&status=warning&gatewayStatus=ok`);
      expect(encodedUriParametersForGateways(pagination, selection))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&meterStatus=ok&meterStatus=warning&status=ok`);
    });

    it('returns all selected parameters', () => {
      const selection = selectedParameters({
        addresses: ['address 2', 'storgatan 5'],
        cities: ['got', 'sto', 'mmx'],
        meterStatuses: [Status.ok, Status.warning],
        gatewayStatuses: [Status.ok],
      });

      expect(encodedUriParametersForMeters(pagination, selection)).toEqual(`size=${pagination.size}` +
        `&page=${pagination.page}&address=address%202&address=storgatan%205` +
        '&city=got&city=sto&city=mmx&status=ok&status=warning&gatewayStatus=ok');
      expect(encodedUriParametersForGateways(pagination, selection)).toEqual(`size=${pagination.size}` +
        `&page=${pagination.page}&address=address%202&address=storgatan%205` +
        '&city=got&city=sto&city=mmx&meterStatus=ok&meterStatus=warning&status=ok');
    });
  });

  describe('calculate uri parameters from selected ids and no pagination', () => {
    const selectedParameters = (parameters: Partial<SelectedParameters>): SelectedParameters => {
      parameters.period = Period.latest;
      return parameters as SelectedParameters;
    };

    it('returns empty parameters string when nothing is selected', () => {
      const selection = selectedParameters({cities: []});

      expect(encodedUriParametersForAllMeters(selection))
        .toEqual('');
    });

    it('returns selected city', () => {
      const selection = selectedParameters({cities: ['got']});

      expect(encodedUriParametersForAllMeters(selection))
        .toEqual('city=got');
    });

    it('returns selected cities', () => {
      const selection = selectedParameters({cities: ['got', 'sto', 'mmx']});

      expect(encodedUriParametersForAllMeters(selection))
        .toEqual('city=got&city=sto&city=mmx');
    });

    it('returns all selected parameters', () => {
      const selection = selectedParameters({
        addresses: ['address 2', 'storgatan 5'],
        cities: ['got', 'sto', 'mmx'],
        meterStatuses: [Status.ok, Status.warning],
      });

      const expected =
        `address=address%202&address=storgatan%205` +
        '&city=got&city=sto&city=mmx&status=ok&status=warning';
      expect(encodedUriParametersForAllMeters(selection)).toEqual(expected);
    });
  });
});
