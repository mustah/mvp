import axios from 'axios';
import {default as MockAdapter} from 'axios-mock-adapter';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {Period} from '../../../../components/dates/dateModels';
import {momentAtUtcPlusOneFrom} from '../../../../helpers/dateHelpers';
import {initTranslations} from '../../../../i18n/__tests__/i18nMock';
import {EndPoints} from '../../../../services/endPoints';
import {authenticate} from '../../../../services/restClient';
import {Status, toIdNamed, uuid} from '../../../../types/Types';
import {Medium} from '../../../ui/graph/measurement/measurementModels';
import {SelectionInterval} from '../../../user-selection/userSelectionModels';
import {NormalizedState} from '../../domainModels';
import {domainModelsGetEntitySuccess, domainModelsRequest} from '../../domainModelsActions';
import {initialDomain} from '../../domainModelsReducer';
import {fetchMeter} from '../meterDetailsApiActions';
import {MeterDetails} from '../meterDetailsModels';

describe('meterDetailsApiActions', () => {

  let mockRestClient: MockAdapter;
  let store;

  const now = momentAtUtcPlusOneFrom().toDate();
  const dateRange: SelectionInterval = {
    period: Period.custom,
    customDateRange: {
      start: now,
      end: now,
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
    store = configureMockStoreWith({...initialDomain<MeterDetails>()});
    mockRestClient = new MockAdapter(axios);
    authenticate('test');

    initTranslations({
      code: 'en',
      translation: {},
    });

  });

  describe('fetchMeter', () => {

    const meter = {
      id: 1,
      facility: 'fac123',
      medium: Medium.districtHeating,
      manufacturer: 'man123',
      eventLog: [],
      gatewaySerial: 'gatser123',
      gateway: {
        serial: 'ser123',
        productModel: 'pro123',
        status: toIdNamed(Status.ok),
        id: 2,
        phoneNumber: '911',
        ip: '127.0.0.1',
      },
      organisationId: 'org123',
      location: {
        address: 'adr123',
        city: 'cit123',
        country: 'sverige',
        position: {
          latitude: 0.0,
          longitude: 0.0,
        },
      },
    };

    const fetchMeterWithResponseOk = async (id: uuid) => {
      mockRestClient.onGet().reply(200, meter);
      return store.dispatch(fetchMeter(id));
    };

    it('fetches meter if not yet fetched', async () => {
      await fetchMeterWithResponseOk(meter.id);

      const payload: MeterDetails = {...meter, gateway: {...meter.gateway}};

      expect(store.getActions()).toEqual([
        {type: domainModelsRequest(EndPoints.meters)},
        {type: domainModelsGetEntitySuccess(EndPoints.meters), payload},
      ]);
    });

    it('does not fetch if meter details was already fetched', async () => {
      const alreadyFetchedMeter: NormalizedState<MeterDetails> = {
        ...initialDomain(),
        result: [meter.id],
        entities: {
          [meter.id]: {...meter, gateway: {...meter.gateway}},
        },
      };
      store = configureMockStoreWith(alreadyFetchedMeter);

      await fetchMeterWithResponseOk(meter.id);

      expect(store.getActions()).toEqual([]);
    });

    it('does not fetch if already fetching entity', async () => {
      const metersFetchingState: NormalizedState<MeterDetails> = {
        ...initialDomain(),
        isFetching: true,
      };
      store = configureMockStoreWith(metersFetchingState);

      await fetchMeterWithResponseOk(meter.id);

      expect(store.getActions()).toEqual([]);
    });

  });
});
