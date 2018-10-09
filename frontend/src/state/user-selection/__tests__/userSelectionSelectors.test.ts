import {Period} from '../../../components/dates/dateModels';
import {momentFrom} from '../../../helpers/dateHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {EncodedUriParameters, IdNamed, toIdNamed} from '../../../types/Types';
import {initialPaginationState, limit} from '../../ui/pagination/paginationReducer';
import {getPagination} from '../../ui/pagination/paginationSelectors';
import {addParameterToSelection, selectPeriod} from '../userSelectionActions';
import {
  ParameterName,
  SelectionParameter,
  UriLookupStatePaginated,
  UserSelection,
  UserSelectionState,
} from '../userSelectionModels';
import {initialState, userSelection} from '../userSelectionReducer';
import {
  getGatewayParameters,
  getMeterParameters,
  getPaginatedGatewayParameters,
  getPaginatedMeterParameters,
  getSelectedPeriod,
  getUserSelection,
} from '../userSelectionSelectors';

describe('userSelectionSelectors', () => {

  const stockholm: IdNamed = {name: 'stockholm', id: 'sweden,stockholm'};
  const gothenburg: IdNamed = {name: 'göteborg', id: 'sweden,göteborg'};

  const initialUserSelectionState: UserSelectionState = {...initialState};
  const start: Date = momentFrom('2018-02-02T00:00:00Z').toDate();
  const initialUriLookupState: UriLookupStatePaginated = {
    ...initialUserSelectionState,
    pagination: getPagination({
      entityType: 'meters',
      componentId: 'test',
      pagination: initialPaginationState,
    }),
    start,
  };

  const latestUrlParameters =
    'after=2018-02-01T00%3A00%3A00.000%2B01%3A00&before=2018-02-02T00%3A00%3A00.000%2B01%3A00';

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
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getPaginatedMeterParameters({
        userSelection: state.userSelection,
        pagination: getPagination({
          entityType: 'meters',
          componentId: 'test',
          pagination: initialPaginationState,
        }),
        start,
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
        addParameterToSelection(payloadGot),
      );
      const state: UserSelectionState = userSelection(
        prevState,
        addParameterToSelection(payloadSto),
      );

      const uriParameters: EncodedUriParameters = getPaginatedMeterParameters({
        userSelection: state.userSelection,
        pagination: getPagination({
          entityType: 'meters',
          componentId: 'test',
          pagination: initialPaginationState,
        }),
        start,
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
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getPaginatedMeterParameters({
        query: 'bro',
        userSelection: state.userSelection,
        pagination: getPagination({
          entityType: 'meters',
          componentId: 'test',
          pagination: initialPaginationState,
        }),
        start,
      });

      expect(uriParameters).toEqual(`city=sweden%2Cstockholm&${latestUrlParameters}&size=20&page=0&w=bro`);
    });
  });

  describe('getPaginatedGatewayParameters', () => {

    it('has no wildcard search parameter without a query string', () => {
      const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getPaginatedGatewayParameters({
        userSelection: state.userSelection,
        pagination: getPagination({
          entityType: 'gateways',
          componentId: 'test',
          pagination: initialPaginationState,
        }),
        start,
      });

      expect(uriParameters).toEqual(`city=sweden%2Cstockholm&${latestUrlParameters}&size=20&page=0`);
    });

    it('has wildcard search parameter for gateways', () => {
      const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getPaginatedGatewayParameters({
        query: 'sto',
        userSelection: state.userSelection,
        pagination: getPagination({
          entityType: 'gateways',
          componentId: 'test',
          pagination: initialPaginationState,
        }),
        start,
      });

      expect(uriParameters).toEqual(`city=sweden%2Cstockholm&${latestUrlParameters}&size=20&page=0&w=sto`);
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

      const uriParameters: EncodedUriParameters = getPaginatedGatewayParameters({
        query: 'sto',
        userSelection: state.userSelection,
        pagination: getPagination({
          entityType: 'gateways',
          componentId: 'test',
          pagination: initialPaginationState,
        }),
        start,
      });

      expect(uriParameters).toEqual(`gatewaySerial=123abc&${latestUrlParameters}&size=20&page=0&w=sto`);
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

      expect(uriParameters).toEqual(`city=sweden%2Cstockholm&${latestUrlParameters}`);
    });

    it('has wildcard search parameter for meters', () => {
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

      expect(uriParameters).toEqual(`city=sweden%2Cstockholm&${latestUrlParameters}&w=sto`);
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
        query: 'sto',
        userSelection: state.userSelection,
        start,
      });

      expect(uriParameters).toEqual(`facility=112&${latestUrlParameters}&w=sto`);
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

      expect(uriParameters).toEqual(`alarm=yes&${latestUrlParameters}`);
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

      expect(uriParameters).toEqual(`alarm=no&${latestUrlParameters}`);
    });

  });

  describe('getGatewayParameters', () => {

    it('has no wildcard search parameter without a query string', () => {
      const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getGatewayParameters({
        userSelection: state.userSelection,
        start,
      });

      expect(uriParameters).toEqual(`city=sweden%2Cstockholm&${latestUrlParameters}`);
    });

    it('has wildcard search parameter for gateways', () => {
      const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
      const state: UserSelectionState = userSelection(
        initialState,
        addParameterToSelection(payload),
      );

      const uriParameters: EncodedUriParameters = getGatewayParameters({
        query: 'sto',
        userSelection: state.userSelection,
        start,
      });

      expect(uriParameters).toEqual(`city=sweden%2Cstockholm&${latestUrlParameters}&w=sto`);
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

      const uriParameters: EncodedUriParameters = getGatewayParameters({
        query: 'sto',
        userSelection: state.userSelection,
        start,
      });

      expect(uriParameters).toEqual(`gatewaySerial=666&${latestUrlParameters}&w=sto`);
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
        selectPeriod(Period.currentWeek),
      );

      expect(getSelectedPeriod(state.userSelection))
        .toEqual({period: Period.currentWeek, customDateRange: Maybe.nothing()});
    });
  });

});
