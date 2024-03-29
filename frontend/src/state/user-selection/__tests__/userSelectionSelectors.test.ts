import {makeThreshold} from '../../../__tests__/testDataFactory';
import {urlFromParameters} from '../../../__tests__/urlFromParameters';
import {momentAtUtcPlusOneFrom} from '../../../helpers/dateHelpers';
import {meterParameterNames, RequestParameter} from '../../../helpers/urlFactory';
import {EncodedUriParameters, IdNamed, toIdNamed} from '../../../types/Types';
import {initialState as initialPaginationState, paginationPageSize} from '../../ui/pagination/paginationReducer';
import {addParameterToSelection} from '../userSelectionActions';
import {
  ParameterName,
  SelectionParameter,
  UriLookupStatePaginated,
  UserSelection,
  UserSelectionState,
} from '../userSelectionModels';
import {initialState, userSelection} from '../userSelectionReducer';
import {
  getApiParameters,
  getMeterParameters,
  getPaginatedApiParameters,
  getPaginatedMeterParameters,
  getUserSelection,
} from '../userSelectionSelectors';

describe('userSelectionSelectors', () => {

  const stockholm: IdNamed = {name: 'stockholm', id: 'sweden,stockholm'};
  const gothenburg: IdNamed = {name: 'göteborg', id: 'sweden,göteborg'};

  const initialUserSelectionState: UserSelectionState = {...initialState};
  const start: Date = momentAtUtcPlusOneFrom('2018-02-02T00:00:00Z').toDate();
  const initialUriLookupState: UriLookupStatePaginated = {
    ...initialUserSelectionState,
    pagination: initialPaginationState.meters,
    start,
  };

  const initialEncodedParameters = getPaginatedMeterParameters(initialUriLookupState);

  it('can find user selection in user selection state', () => {
    const userSelection: UserSelection = getUserSelection(initialUserSelectionState);
    expect(userSelection).toEqual(initialState.userSelection);
  });

  it('encode the initial, empty, selection', () => {
    const parameters = urlFromParameters(initialEncodedParameters);
    expect(parameters.searchParams.get('size')).toEqual(paginationPageSize.toString());
    expect(parameters.searchParams.get('page')).toEqual('0');
    expect(parameters.searchParams.get('after')).toBeTruthy();
    expect(parameters.searchParams.get('before')).toBeTruthy();
  });

  describe('getPaginatedMeterParameters', () => {

    it('has selected city search parameter', () => {
      const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getPaginatedMeterParameters({
        userSelection: state.userSelection,
        pagination: initialPaginationState.meters,
        start,
      });

      expect(urlFromParameters(uriParameters).searchParams.get('city')).toEqual('sweden,stockholm');
    });

    it('has only wildcard and period parameters when query parameter is set', () => {
      const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );
      const query = 'KAM';

      const uriParameters: EncodedUriParameters = getPaginatedMeterParameters({
        userSelection: state.userSelection,
        pagination: initialPaginationState.meters,
        query,
        start,
      });

      expect(urlFromParameters(uriParameters).searchParams.get('w')).toEqual(query);
    });

    it('has two selected cities', () => {
      const payloadGot: SelectionParameter = {
        item: {...gothenburg},
        parameter: ParameterName.cities,
      };
      const payloadSto: SelectionParameter = {
        item: {...stockholm},
        parameter: ParameterName.cities,
      };
      const prevState: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payloadGot),
      );
      const state: UserSelectionState = userSelection(
        prevState,
        addParameterToSelection(payloadSto),
      );

      const uriParameters: EncodedUriParameters = getPaginatedMeterParameters({
        userSelection: state.userSelection,
        pagination: initialPaginationState.meters,
        start,
      });

      expect(urlFromParameters(uriParameters).searchParams.getAll('city'))
        .toEqual(['sweden,göteborg', 'sweden,stockholm']);
    });

    it('has wildcard search parameters when there is search query', () => {
      const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getPaginatedMeterParameters({
        userSelection: state.userSelection,
        pagination: initialPaginationState.meters,
        start,
      });

      expect(urlFromParameters(uriParameters).searchParams.get('city')).toEqual('sweden,stockholm');
    });

    it('includes organisations', () => {
      const anOrganisation: IdNamed = {id: 'hello', name: 'there'};
      const anotherOrganisation: IdNamed = {id: 'good', name: 'bye'};

      const firstPayload: SelectionParameter = {item: {...anOrganisation}, parameter: ParameterName.organisations};
      const secondPayload: SelectionParameter = {
        item: {...anotherOrganisation},
        parameter: ParameterName.organisations
      };

      const oneOrganisation: UserSelectionState = userSelection(initialState, addParameterToSelection(firstPayload));
      const twoOrganisations: UserSelectionState = userSelection(
        oneOrganisation,
        addParameterToSelection(secondPayload)
      );

      const stateWithOrganisation: UriLookupStatePaginated = {
        ...initialUriLookupState,
        userSelection: {
          ...twoOrganisations.userSelection,
          selectionParameters: {
            ...twoOrganisations.userSelection.selectionParameters,
            organisations: [{...anOrganisation}],
          },
        },
      };

      const parameters: EncodedUriParameters = getPaginatedMeterParameters(stateWithOrganisation);
      const url: URL = urlFromParameters(parameters);
      expect(url.searchParams.getAll(meterParameterNames.organisations)).toEqual([anOrganisation.id]);
    });

    it('includes a threshold query', () => {
      const state: UserSelectionState = {
        ...initialState,
        userSelection: {
          ...initialState.userSelection,
          selectionParameters: {
            ...initialState.userSelection.selectionParameters,
            threshold: makeThreshold()
          },
        },
      };

      const parameters: EncodedUriParameters = getPaginatedMeterParameters({
        ...initialUriLookupState,
        userSelection: state.userSelection,
        start,
      });

      const url: URL = urlFromParameters(parameters);

      expect(url.searchParams.get('threshold')).toEqual('Power >= 3 kW');
    });

    it('excludes threshold query when global meter query parameter is set', () => {
      const state: UserSelectionState = {
        ...initialState,
        userSelection: {
          ...initialState.userSelection,
          selectionParameters: {
            ...initialState.userSelection.selectionParameters,
            threshold: makeThreshold()
          },
        },
      };

      const query = '123123';
      const parameters: EncodedUriParameters = getPaginatedMeterParameters({
        ...initialUriLookupState,
        userSelection: state.userSelection,
        start,
        query,
      });

      const dateRange = 'after=2018-02-02T01%3A00%3A00.000%2B01%3A00&before=2018-02-02T01%3A00%3A00.000%2B01%3A00';

      expect(parameters).toEqual(`${dateRange}&size=${paginationPageSize}&page=0&w=${query}`);
      expect(urlFromParameters(parameters).searchParams.get('threshold')).toBeNull();
    });

    it('includes a threshold for duration query', () => {
      const state: UserSelectionState = {
        ...initialState,
        userSelection: {
          ...initialState.userSelection,
          selectionParameters: {
            ...initialState.userSelection.selectionParameters,
            threshold: {...makeThreshold(), duration: '3'}
          },
        },
      };

      const parameters: EncodedUriParameters = getPaginatedMeterParameters({
        ...initialUriLookupState,
        userSelection: state.userSelection,
        start,
      });

      const url: URL = urlFromParameters(parameters);

      expect(url.searchParams.get('threshold')).toEqual('Power >= 3 kW for 3 days');
    });

    it('does not include a duration in threshold query', () => {
      const state: UserSelectionState = {
        ...initialState,
        userSelection: {
          ...initialState.userSelection,
          selectionParameters: {
            ...initialState.userSelection.selectionParameters,
            threshold: makeThreshold()
          },
        },
      };

      const parameters: EncodedUriParameters = getPaginatedMeterParameters({
        ...initialUriLookupState,
        userSelection: state.userSelection,
        start,
      });

      const url: URL = urlFromParameters(parameters);

      expect(url.searchParams.get('threshold')).toEqual('Power >= 3 kW');
    });

    it('can sort by field', () => {
      const state: UriLookupStatePaginated = {
        ...initialUriLookupState,
        sort: [
          {dir: 'ASC', field: RequestParameter.city},
        ],
      };

      const parameters: EncodedUriParameters = getPaginatedMeterParameters(state);

      const url: URL = urlFromParameters(parameters);

      expect(url.searchParams.get(RequestParameter.sort)).toEqual('city,ASC');
    });

  });

  describe('getPaginatedApiParameters', () => {

    it('has no wildcard search parameter without a query string', () => {
      const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getPaginatedApiParameters({
        userSelection: state.userSelection,
        pagination: initialPaginationState.gateways,
        start,
      });

      expect(urlFromParameters(uriParameters).searchParams.get('w')).toEqual(null);
    });

    it('has wildcard search parameter for gateways', () => {
      const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getPaginatedApiParameters({
        userSelection: state.userSelection,
        pagination: initialPaginationState.gateways,
        start,
      });

      expect(urlFromParameters(uriParameters).searchParams.get('city')).toEqual('sweden,stockholm');
    });

    it('has gateway search parameters', () => {
      const payload: SelectionParameter = {
        item: {...toIdNamed('123abc')},
        parameter: ParameterName.gatewaySerials,
      };
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getPaginatedApiParameters({
        userSelection: state.userSelection,
        pagination: initialPaginationState.gateways,
        start,
      });

      expect(urlFromParameters(uriParameters).searchParams.get('gatewaySerial')).toEqual('123abc');
    });

  });

  describe('getMeterParameters', () => {

    it('has no wildcard search parameter without a query string', () => {
      const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getMeterParameters({
        userSelection: state.userSelection,
        start,
      });

      expect(urlFromParameters(uriParameters).searchParams.get('w')).toEqual(null);
    });

    it('has wildcard and period search parameter for meters and no other parameters', () => {
      const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getMeterParameters({
        query: 'sto',
        userSelection: state.userSelection,
        start,
      });

      expect(urlFromParameters(uriParameters).searchParams.get('w')).toEqual('sto');
    });

    it('has meter search parameters', () => {
      const payload: SelectionParameter = {
        item: {...toIdNamed('112')},
        parameter: ParameterName.facilities,
      };
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getMeterParameters({
        userSelection: state.userSelection,
        start,
      });

      expect(urlFromParameters(uriParameters).searchParams.get('facility')).toEqual('112');
    });

    it('search meters with alarms', () => {
      const payload: SelectionParameter = {
        item: {...toIdNamed('yes')},
        parameter: ParameterName.alarms,
      };
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getMeterParameters({
        userSelection: state.userSelection,
        start,
      });

      expect(urlFromParameters(uriParameters).searchParams.get('alarm')).toEqual('yes');
    });

    it('has limit parameter', () => {
      const payload: SelectionParameter = {
        item: {...toIdNamed('yes')},
        parameter: ParameterName.alarms,
      };
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getMeterParameters({userSelection: state.userSelection, limit: 42});

      const url = urlFromParameters(uriParameters);

      expect(url.searchParams.get('limit')).toEqual('42');
    });

    it('search meters with no alarms', () => {
      const payload: SelectionParameter = {
        item: {...toIdNamed('no')},
        parameter: ParameterName.alarms,
      };
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getMeterParameters({
        userSelection: state.userSelection,
        start,
      });

      expect(urlFromParameters(uriParameters).searchParams.get('alarm')).toEqual('no');
    });

    it('includes a threshold query', () => {
      const state: UserSelectionState = {
        ...initialState,
        userSelection: {
          ...initialState.userSelection,
          selectionParameters: {
            ...initialState.userSelection.selectionParameters,
            threshold: makeThreshold()
          },
        },
      };

      const parameters: EncodedUriParameters = getMeterParameters({
        userSelection: state.userSelection,
        start,
      });

      const url: URL = urlFromParameters(parameters);

      expect(url.searchParams.get('threshold')).toEqual('Power >= 3 kW');
    });

  });

  describe('getGatewayParameters', () => {

    it('has no wildcard search parameter without a query string', () => {
      const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getApiParameters({
        userSelection: state.userSelection,
        start,
      });

      expect(urlFromParameters(uriParameters).searchParams.get('w')).toEqual(null);
    });

    it('has wildcard and period search parameter for gateways', () => {
      const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getApiParameters({
        query: 'sto',
        userSelection: state.userSelection,
        start,
      });

      expect(urlFromParameters(uriParameters).searchParams.get('w')).toEqual('sto');
    });

    it('has gateways search parameters', () => {
      const payload: SelectionParameter = {
        item: {...toIdNamed('666')},
        parameter: ParameterName.gatewaySerials,
      };
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getApiParameters({
        userSelection: state.userSelection,
        start,
      });

      expect(urlFromParameters(uriParameters).searchParams.get('gatewaySerial')).toEqual('666');
    });

  });
});
