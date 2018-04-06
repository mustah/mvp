import {Period} from '../../components/dates/dateModels';
import {SelectedParameters} from '../../state/search/selection/selectionModels';
import {Pagination} from '../../state/ui/pagination/paginationModels';
import {EncodedUriParameters, Status} from '../../types/Types';
import {dateRange, momentWithTimeZone, toApiParameters} from '../dateHelpers';
import {
  encodedUriParametersForAllGateways,
  encodedUriParametersForAllMeters,
  encodedUriParametersForGateways,
  encodedUriParametersForMeters,
  ParameterCallbacks,
} from '../urlFactory';

describe('urlFactory', () => {

  const mockParameterCallbacks: ParameterCallbacks = {
    period: (parameter: EncodedUriParameters) => toApiParameters(dateRange(
      momentWithTimeZone('2018-04-27').toDate(),
      parameter as Period,
    )),
  };

  const latestUrlParameters = 'after=2018-04-25T22%3A00%3A00.000Z&before=2018-04-26T22%3A00%3A00.000Z';

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

      expect(encodedUriParametersForMeters(pagination, selection, mockParameterCallbacks))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&${latestUrlParameters}`);
      expect(encodedUriParametersForGateways(pagination, selection, mockParameterCallbacks))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&${latestUrlParameters}`);
    });

    it('returns selected city', () => {
      const selection = selectedParameters({cities: ['got']});

      expect(encodedUriParametersForMeters(pagination, selection, mockParameterCallbacks))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&city=got&${latestUrlParameters}`);
      expect(encodedUriParametersForGateways(pagination, selection, mockParameterCallbacks))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&city=got&${latestUrlParameters}`);
    });

    it('returns selected cities', () => {
      const selection = selectedParameters({cities: ['got', 'sto', 'mmx']});

      expect(encodedUriParametersForMeters(pagination, selection, mockParameterCallbacks))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&city=got&city=sto&city=mmx` +
                 `&${latestUrlParameters}`);
      expect(encodedUriParametersForGateways(pagination, selection, mockParameterCallbacks))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&city=got&city=sto&city=mmx` +
                 `&${latestUrlParameters}`);
    });

    it('returns selected address', () => {
      const selection = selectedParameters({addresses: ['address 2']});

      expect(encodedUriParametersForMeters(pagination, selection, mockParameterCallbacks))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&address=address%202` +
                 `&${latestUrlParameters}`);
      expect(encodedUriParametersForGateways(pagination, selection, mockParameterCallbacks))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&address=address%202` +
                 `&${latestUrlParameters}`);
    });

    it('returns selected addresses', () => {
      const selection = selectedParameters({addresses: ['address 2', 'storgatan 5']});

      expect(encodedUriParametersForMeters(pagination, selection, mockParameterCallbacks))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&address=address%202&address=storgatan%205` +
                 `&${latestUrlParameters}`);
      expect(encodedUriParametersForGateways(pagination, selection, mockParameterCallbacks))
        .toEqual(`size=${pagination.size}&page=${pagination.page}&address=address%202&address=storgatan%205` +
                 `&${latestUrlParameters}`);
    });

    it('returns selected statuses', () => {
      const selection = selectedParameters({
        meterStatuses: [Status.ok, Status.warning],
        gatewayStatuses: [Status.ok],
      });

      const expectedMeterParameters =
        `size=${pagination.size}&page=${pagination.page}` +
        `&status=ok&status=warning&gatewayStatus=ok&${latestUrlParameters}`;

      expect(encodedUriParametersForMeters(pagination, selection, mockParameterCallbacks))
        .toEqual(expectedMeterParameters);

      const expectedGatewayParameters =
        `size=${pagination.size}&page=${pagination.page}` +
        `&meterStatus=ok&meterStatus=warning&status=ok&${latestUrlParameters}`;

      expect(encodedUriParametersForGateways(pagination, selection, mockParameterCallbacks))
        .toEqual(expectedGatewayParameters);
    });

    it('returns all selected parameters', () => {
      const selection = selectedParameters({
        addresses: ['address 2', 'storgatan 5'],
        cities: ['got', 'sto', 'mmx'],
        meterStatuses: [Status.ok, Status.warning],
        gatewayStatuses: [Status.ok],
      });

      expect(encodedUriParametersForMeters(
        pagination,
        selection,
        mockParameterCallbacks,
      )).toEqual(`size=${pagination.size}` +
                 `&page=${pagination.page}&address=address%202&address=storgatan%205` +
                 `&city=got&city=sto&city=mmx&status=ok&status=warning&gatewayStatus=ok` +
                 `&${latestUrlParameters}`);
      expect(encodedUriParametersForGateways(
        pagination,
        selection,
        mockParameterCallbacks,
      )).toEqual(`size=${pagination.size}` +
                 `&page=${pagination.page}&address=address%202&address=storgatan%205` +
                 '&city=got&city=sto&city=mmx&meterStatus=ok&meterStatus=warning&status=ok'
                 + `&${latestUrlParameters}`);

      expect(encodedUriParametersForGateways(
        pagination,
        selection,
        mockParameterCallbacks,
      )).toEqual(`size=${pagination.size}` +
                 `&page=${pagination.page}&address=address%202&address=storgatan%205` +
                 '&city=got&city=sto&city=mmx&meterStatus=ok&meterStatus=warning&status=ok' +
                 `&${latestUrlParameters}`);
    });
  });

  describe('calculate uri parameters from selected ids and no pagination', () => {
    const selectedParameters = (parameters: Partial<SelectedParameters>): SelectedParameters => {
      parameters.period = Period.latest;
      return parameters as SelectedParameters;
    };

    it('returns empty parameters string when nothing is selected', () => {
      const selection = selectedParameters({cities: []});

      expect(encodedUriParametersForAllMeters(selection, mockParameterCallbacks))
        .toEqual(latestUrlParameters);
      expect(encodedUriParametersForAllGateways(selection, mockParameterCallbacks))
        .toEqual(latestUrlParameters);
    });

    it('returns selected city', () => {
      const selection = selectedParameters({cities: ['got']});

      expect(encodedUriParametersForAllMeters(selection, mockParameterCallbacks))
        .toEqual(`city=got&${latestUrlParameters}`);
      expect(encodedUriParametersForAllGateways(selection, mockParameterCallbacks))
        .toEqual(`city=got&${latestUrlParameters}`);
    });

    it('returns selected cities', () => {
      const selection = selectedParameters({cities: ['got', 'sto', 'mmx']});

      expect(encodedUriParametersForAllMeters(selection, mockParameterCallbacks))
        .toEqual(`city=got&city=sto&city=mmx&${latestUrlParameters}`);
      expect(encodedUriParametersForAllGateways(selection, mockParameterCallbacks))
        .toEqual(`city=got&city=sto&city=mmx&${latestUrlParameters}`);
    });

    it('returns all selected meter parameters', () => {
      const selection = selectedParameters({
        addresses: ['address 2', 'storgatan 5'],
        cities: ['got', 'sto', 'mmx'],
        meterStatuses: [Status.ok, Status.warning],
      });

      const expected =
        `address=address%202&address=storgatan%205` +
        `&city=got&city=sto&city=mmx&status=ok&status=warning&${latestUrlParameters}`;
      expect(encodedUriParametersForAllMeters(selection, mockParameterCallbacks)).toEqual(expected);
    });

    it('returns all selected gateway parameters', () => {
      const selection = selectedParameters({
        addresses: ['address 2', 'storgatan 5'],
        cities: ['got', 'sto', 'mmx'],
        meterStatuses: [Status.ok, Status.warning],
        gatewayStatuses: [Status.ok],
      });

      const expected =
        `address=address%202&address=storgatan%205` +
        `&city=got&city=sto&city=mmx&meterStatus=ok&meterStatus=warning&status=ok&${latestUrlParameters}`;

      expect(encodedUriParametersForAllGateways(
        selection,
        mockParameterCallbacks,
      )).toEqual(expected);
    });
  });
});
