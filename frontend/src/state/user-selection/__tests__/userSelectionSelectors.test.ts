import {normalize} from 'normalizr';
import {testData} from '../../../__tests__/testDataFactory';
import {Period} from '../../../components/dates/dateModels';
import {momentWithTimeZone} from '../../../helpers/dateHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {EndPoints} from '../../../services/endPoints';
import {EncodedUriParameters, IdNamed} from '../../../types/Types';
import {DomainModelsState, Normalized, SelectionEntity} from '../../domain-models/domainModels';
import {getRequestOf} from '../../domain-models/domainModelsActions';
import {
  addresses,
  alarms,
  cities,
  gatewayStatuses,
  initialDomain,
  meterStatuses,
  users,
} from '../../domain-models/domainModelsReducer';
import {selectionsSchema} from '../../domain-models/selections/selectionsSchemas';
import {User} from '../../domain-models/user/userModels';
import {initialPaginationState, limit} from '../../ui/pagination/paginationReducer';
import {getPagination} from '../../ui/pagination/paginationSelectors';
import {ADD_PARAMETER_TO_SELECTION, SELECT_PERIOD} from '../userSelectionActions';
import {
  LookupState,
  ParameterName,
  SelectionListItem,
  SelectionParameter,
  UserSelection,
  UserSelectionState,
} from '../userSelectionModels';
import {initialState, userSelection} from '../userSelectionReducer';
import {
  getCities,
  getPaginatedMeterParameters,
  getSelectedPeriod,
  getSelection,
  UriLookupStatePaginated,
} from '../userSelectionSelectors';

describe('userSelectionSelectors', () => {

  const normalizedSelections = normalize(testData.selections, selectionsSchema);
  const {cities: cityEntities} = normalizedSelections.entities;
  const stockholm: IdNamed = cityEntities['sweden,stockholm'];
  const gothenburg: IdNamed = cityEntities['sweden,göteborg'];
  const vasa: IdNamed = cityEntities['finland,vasa'];

  const selectionsRequest = getRequestOf<Normalized<IdNamed>>(EndPoints.selections);
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

  const initialDomainModelState = initialDomain<SelectionEntity>();

  const domainModels = (domainModelPayload): Partial<DomainModelsState> => ({
    cities: cities(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
    addresses: addresses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
    alarms: alarms(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
    gatewayStatuses: gatewayStatuses(
      initialDomainModelState,
      selectionsRequest.success(domainModelPayload),
    ),
    meterStatuses: meterStatuses(
      initialDomainModelState,
      selectionsRequest.success(domainModelPayload),
    ),
    users: users(initialDomain<User>(), {type: 'none'}),
  });

  it('can find user selection in user selection state', () => {
    const userSelection: UserSelection = getSelection(initialUserSelectionState);
    expect(userSelection).toEqual(initialState.userSelection);
  });

  it('encode the initial, empty, selection', () => {
    expect(initialEncodedParameters).toEqual(`${latestUrlParameters}&size=${limit}&page=0`);
  });

  it('gets entities for type city', () => {

    const payload: SelectionParameter = {...stockholm, parameter: ParameterName.cities};

    const state: LookupState = {
      userSelection: userSelection(initialState, {type: ADD_PARAMETER_TO_SELECTION, payload}),
      domainModels: domainModels(normalizedSelections) as DomainModelsState,
    };

    const stockholmSelected: SelectionListItem[] = [
      {selected: true, ...stockholm},
      {selected: false, ...gothenburg},
      {selected: false, ...vasa},
    ];

    const actual: SelectionListItem[] = getCities(state);
    expect(actual).toEqual(stockholmSelected);
  });

  it('get entities for undefined entity type', () => {
    const domainModelPayload = normalize(testData.selections, selectionsSchema);

    const payload: SelectionParameter = {...stockholm, parameter: ParameterName.cities};
    const notCity: Partial<DomainModelsState> = domainModels(domainModelPayload);
    notCity.cities = cities(initialDomainModelState, {type: 'unknown'});

    const state: LookupState = {
      userSelection: userSelection(initialState, {type: ADD_PARAMETER_TO_SELECTION, payload}),
      domainModels: notCity as DomainModelsState,
    };

    expect(getCities(state)).toEqual([]);
  });

  it('can handle user selection with missing parameters', () => {
    const oldState: UserSelectionState = {
      userSelection: {
        id: -1,
        name: 'all',
        isChanged: false,
        selectionParameters: {
          addresses: [],
          alarms: [],
          // 'cities' is left out
          dateRange: {period: Period.latest},
          gatewayStatuses: [],
          manufacturers: [],
          media: [],
          meterStatuses: [],
          productModels: [],
        },
      },
    };

    const state: LookupState = {
      userSelection: oldState,
      domainModels: domainModels(normalizedSelections) as DomainModelsState,
    };

    const noneSelected: SelectionListItem[] = [
      {selected: false, ...gothenburg},
      {selected: false, ...stockholm},
      {selected: false, ...vasa},
    ];

    const actual: SelectionListItem[] = getCities(state);
    expect(actual).toEqual(noneSelected);
  });

  describe('encodedUriParameters', () => {

    it('has selected city search parameter', () => {
      const payload: SelectionParameter = {...stockholm, parameter: ParameterName.cities};
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
      const payloadGot: SelectionParameter = {...gothenburg, parameter: ParameterName.cities};
      const payloadSto: SelectionParameter = {...stockholm, parameter: ParameterName.cities};
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

  describe('get subset of cities', () => {

    it('can detect which the selected entities are', () => {
      const payload: SelectionParameter = {...stockholm, parameter: ParameterName.cities};

      const state: LookupState = {
        userSelection: userSelection(initialState, {type: ADD_PARAMETER_TO_SELECTION, payload}),
        domainModels: domainModels(normalizedSelections) as DomainModelsState,
      };

      const stockholmSelected: SelectionListItem[] = [
        {selected: true, ...stockholm},
        {selected: false, ...gothenburg},
        {selected: false, ...vasa},
      ];

      expect(getCities(state)).toEqual(stockholmSelected);
    });

  });

});
