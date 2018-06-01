import axios from 'axios';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {Period} from '../../../../components/dates/dateModels';
import {Medium} from '../../../../components/indicators/indicatorWidgetModels';
import {now} from '../../../../helpers/dateHelpers';
import {makeApiParametersOf} from '../../../../helpers/urlFactory';
import {initTranslations} from '../../../../i18n/__tests__/i18nMock';
import {EndPoints} from '../../../../services/endPoints';
import {authenticate} from '../../../../services/restClient';
import {EncodedUriParameters, Status, uuid} from '../../../../types/Types';
import {SelectionInterval} from '../../../user-selection/userSelectionModels';
import {NormalizedState} from '../../domainModels';
import {domainModelsGetEntitySuccess, domainModelsRequest} from '../../domainModelsActions';
import {initialDomain} from '../../domainModelsReducer';
import {fetchMeterDetails} from '../meterDetailsApiActions';
import {MeterDetails} from '../meterDetailsModels';
import MockAdapter = require('axios-mock-adapter');

describe('meterDetailsApiActions', () => {

  let mockRestClient: MockAdapter;
  let store;

  const dateRange: SelectionInterval = {
    period: Period.custom,
    customDateRange: {
      start: now(),
      end: now(),
    },
  };

  const configureMockStoreWith = (meterDetailsState: NormalizedState<MeterDetails>) =>
    configureStore([thunk])(
      {
        domainModels: {
          meters: meterDetailsState,
        },
        userSelection: {
          userSelection: {
            selectionParameters: {
              dateRange: {...dateRange},
            },
            isChanged: false,
          },
        },
        language: {language: 'en'},
      },
    );

  beforeEach(() => {
    store = configureMockStoreWith({...initialDomain()});
    mockRestClient = new MockAdapter(axios);
    authenticate('test');

    initTranslations({
      code: 'en',
      translation: {},
    });

  });

  describe('fetchMeterDetails', () => {

    const meter: MeterDetails = {
      facility: 'fac123',
      medium: Medium.districtHeating,
      manufacturer: 'man123',
      measurements: [],
      statusChangelog: [],
      status: {
        id: Status.ok,
        name: Status.ok,
      },
      gatewaySerial: 'gatser123',
      gateway: {
        serial: 'ser123',
        productModel: 'pro123',
        status: {
          id: Status.ok,
          name: Status.ok,
        },
        id: 2,
      },
      organisationId: 'org123',
      id: 1,
      location: {
        address: {
          id: 'adr123',
          name: 'adr123',
        },
        city: {
          id: 'cit123',
          name: 'cit123',
        },
        position: {
          latitude: 0.0,
          longitude: 0.0,
        },
      },
    };

    const fetchMeterWithResponseOk = async (id: uuid, parameters?: EncodedUriParameters) => {
      mockRestClient
        .onGet()
        .reply(200, meter);
      return store.dispatch(fetchMeterDetails(id, parameters));
    };

    it('does not normalize response', async () => {
      await fetchMeterWithResponseOk(meter.id as uuid);

      expect(store.getActions()).toEqual([
        {type: domainModelsRequest(EndPoints.meters)},
        {type: domainModelsGetEntitySuccess(EndPoints.meters), payload: {...meter}},
      ]);
    });

    it('fetches meter if not yet fetched', async () => {
      const parameters: EncodedUriParameters = makeApiParametersOf(now(), dateRange);

      await fetchMeterWithResponseOk(meter.id as uuid, parameters);

      expect(store.getActions()).toEqual([
        {type: domainModelsRequest(EndPoints.meters)},
        {type: domainModelsGetEntitySuccess(EndPoints.meters), payload: {...meter}},
      ]);
    });

    it('does not fetch if meter details was already fetched, even if parameters change', async () => {
      // this is fine, because we throw away the cached data when the selection (time period) changes
      const alreadyFetchedMeter: NormalizedState<MeterDetails> = {
        ...initialDomain(),
        result: [meter.id as uuid],
        entities: {
          [meter.id!.toString()]: meter,
        },
      };
      store = configureMockStoreWith(alreadyFetchedMeter);

      const parameters: EncodedUriParameters = makeApiParametersOf(now(), {
        period: Period.custom,
        customDateRange: {
          start: new Date(0),
          end: new Date(100),
        },
      });

      await fetchMeterWithResponseOk(meter.id as uuid, parameters);

      expect(store.getActions()).toEqual([]);
    });

    it('does not fetch if already fetching entity', async () => {
      const metersFetchingState: NormalizedState<MeterDetails> = {
        ...initialDomain(),
        isFetching: true,
      };
      store = configureMockStoreWith(metersFetchingState);

      await fetchMeterWithResponseOk(meter.id as uuid);

      expect(store.getActions()).toEqual([]);
    });

  });
});
