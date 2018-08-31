import {Period} from '../../../components/dates/dateModels';
import {momentWithTimeZone} from '../../../helpers/dateHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {EncodedUriParameters, IdNamed, Status, toIdNamed} from '../../../types/Types';
import {initialPaginationState, limit} from '../../ui/pagination/paginationReducer';
import {getPagination} from '../../ui/pagination/paginationSelectors';
import {ADD_PARAMETER_TO_SELECTION, SELECT_PERIOD} from '../userSelectionActions';
import {
  ParameterName,
  SelectionParameter,
  UriLookupStatePaginated,
  UserSelection,
  UserSelectionState,
} from '../userSelectionModels';
import {initialState, userSelection} from '../userSelectionReducer';
import {
  getPaginatedGatewayParameters,
  getPaginatedMeterParameters,
  getSelectedPeriod,
  getUserSelection,
} from '../userSelectionSelectors';

describe('userSelectionSelectors', () => {

  const stockholm: IdNamed = {name: 'stockholm', id: 'sweden,stockholm'};
  const gothenburg: IdNamed = {name: 'göteborg', id: 'sweden,göteborg'};

  const initialUserSelectionState: UserSelectionState = {...initialState};
  const now: Date = momentWithTimeZone('2018-02-02T00:00:00Z').toDate();
  const initialUriLookupState: UriLookupStatePaginated = {
    ...initialUserSelectionState,
    pagination: getPagination({
      entityType: 'meters',
      componentId: 'test',
      pagination: initialPaginationState,
    }),
    now,
  };

  const latestUrlParameters = 'after=2018-02-01T00%3A00%3A00.000Z&before=2018-02-02T00%3A00%3A00.000Z';

  const initialEncodedParameters = getPaginatedMeterParameters(initialUriLookupState);

  it('can find user selection in user selection state', () => {
    const userSelection: UserSelection = getUserSelection(initialUserSelectionState);
    expect(userSelection).toEqual(initialState.userSelection);
  });

  it('encode the initial, empty, selection', () => {
    expect(initialEncodedParameters).toEqual(`${latestUrlParameters}&size=${limit}&page=0`);
  });

  describe('parameters', () => {

    it('has selected city search parameter', () => {
      const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
      const state: UserSelectionState = userSelection(
        initialState,
        {type: ADD_PARAMETER_TO_SELECTION, payload},
      );

      const uriParameters: EncodedUriParameters = getPaginatedMeterParameters({
        userSelection: state.userSelection,
        pagination: getPagination({
          entityType: 'meters',
          componentId: 'test',
          pagination: initialPaginationState,
        }),
        now,
      });

      expect(uriParameters).toEqual(`city=sweden%2Cstockholm&${latestUrlParameters}&size=${limit}&page=0`);
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
        {type: ADD_PARAMETER_TO_SELECTION, payload: payloadGot},
      );
      const state: UserSelectionState = userSelection(
        prevState,
        {type: ADD_PARAMETER_TO_SELECTION, payload: payloadSto},
      );

      const uriParameters: EncodedUriParameters = getPaginatedMeterParameters({
        userSelection: state.userSelection,
        pagination: getPagination({
          entityType: 'meters',
          componentId: 'test',
          pagination: initialPaginationState,
        }),
        now,
      });

      expect(uriParameters).toEqual(
        `city=sweden%2Cg%C3%B6teborg&city=sweden%2Cstockholm` +
        `&${latestUrlParameters}&size=${limit}&page=0`,
      );
    });

    it('has wildcard search parameters when there is search query', () => {
      const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
      const state: UserSelectionState = userSelection(
        initialState,
        {type: ADD_PARAMETER_TO_SELECTION, payload},
      );

      const uriParameters: EncodedUriParameters = getPaginatedMeterParameters({
        query: 'bro',
        userSelection: state.userSelection,
        pagination: getPagination({
          entityType: 'meters',
          componentId: 'test',
          pagination: initialPaginationState,
        }),
        now,
      });

      expect(uriParameters).toEqual(`city=sweden%2Cstockholm&${latestUrlParameters}&size=20&page=0&w=bro`);
    });
  });

  describe('getPaginatedGatewayParameters', () => {

    it('has no wildcard search parameter without a query string', () => {
      const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
      const state: UserSelectionState = userSelection(
        initialState,
        {type: ADD_PARAMETER_TO_SELECTION, payload},
      );

      const uriParameters: EncodedUriParameters = getPaginatedGatewayParameters({
        userSelection: state.userSelection,
        pagination: getPagination({
          entityType: 'gateways',
          componentId: 'test',
          pagination: initialPaginationState,
        }),
        now,
      });

      expect(uriParameters).toEqual(`city=sweden%2Cstockholm&${latestUrlParameters}&size=20&page=0`);
    });

    it('has wildcard search parameter for gateways', () => {
      const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
      const state: UserSelectionState = userSelection(
        initialState,
        {type: ADD_PARAMETER_TO_SELECTION, payload},
      );

      const uriParameters: EncodedUriParameters = getPaginatedGatewayParameters({
        query: 'sto',
        userSelection: state.userSelection,
        pagination: getPagination({
          entityType: 'gateways',
          componentId: 'test',
          pagination: initialPaginationState,
        }),
        now,
      });

      expect(uriParameters).toEqual(`city=sweden%2Cstockholm&${latestUrlParameters}&size=20&page=0&w=sto`);
    });

    it('has gateway search parameters', () => {
      const payload: SelectionParameter = {
        item: {...toIdNamed(Status.ok)},
        parameter: ParameterName.gatewayStatuses,
      };
      const state: UserSelectionState = userSelection(
        initialState,
        {type: ADD_PARAMETER_TO_SELECTION, payload},
      );

      const uriParameters: EncodedUriParameters = getPaginatedGatewayParameters({
        query: 'sto',
        userSelection: state.userSelection,
        pagination: getPagination({
          entityType: 'gateways',
          componentId: 'test',
          pagination: initialPaginationState,
        }),
        now,
      });

      expect(uriParameters).toEqual(`status=ok&${latestUrlParameters}&size=20&page=0&w=sto`);
    });

  });

  describe('get selected period', () => {

    it('there is a default period', () => {
      expect(initialState.userSelection.selectionParameters.dateRange)
        .toEqual({period: Period.latest});
    });

    it('get selected period', () => {
      const state: UserSelectionState = userSelection(
        initialState,
        {type: SELECT_PERIOD, payload: Period.currentWeek},
      );

      expect(getSelectedPeriod(state.userSelection))
        .toEqual({period: Period.currentWeek, customDateRange: Maybe.nothing()});
    });
  });

});
