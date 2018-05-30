import {EndPoints} from '../../../../services/endPoints';
import {MeterDetails} from '../meterDetailsModels';
import {uuid} from '../../../../types/Types';
import {fetchMeterDetails} from '../meterDetailsApiActions';
import thunk from 'redux-thunk';
import {authenticate} from '../../../../services/restClient';
import {initialDomain} from '../../domainModelsReducer';
import {NormalizedState} from '../../domainModels';
import axios from 'axios';
import configureStore from 'redux-mock-store';
import {getEntityRequestOf} from '../../domainModelsActions';
import MockAdapter = require('axios-mock-adapter');

describe('meterDetailsApiActions', () => {
    let mockRestClient: MockAdapter;
    let store;
    const configureMockStore = (meterDetailsState: NormalizedState<MeterDetails>) => configureStore([thunk])(
      {
        domainModels: {
          meters: meterDetailsState,
        },
        language: {language: 'en'},
      },
    );

    beforeEach(() => {
      store = configureMockStore({...initialDomain()});
      mockRestClient = new MockAdapter(axios);
      authenticate('test');
    });
    describe('fetchMeterDetails', () => {
      const getMeterRequest = getEntityRequestOf<MeterDetails[]>(EndPoints.meters);
      const meter: Partial<MeterDetails> = {
        id: 1,
      };

      const fetchMeterWithResponseOk = async (id: uuid) => {
        mockRestClient.onGet(`${EndPoints.meters}/${id.toString()}`).reply(201, meter);
        return store.dispatch(fetchMeterDetails(id));
      };

      it('does not normalize response', async () => {
        await fetchMeterWithResponseOk(meter.id as uuid);

        expect(store.getActions()).toEqual([
          getMeterRequest.request(),
          getMeterRequest.success(meter as MeterDetails[]),
        ]);
      });

      it('does not fetch if already fetching entity', async () => {
        const metersFetchingState: NormalizedState<MeterDetails> = {
          ...initialDomain(),
          isFetching: true,
        };
        store = configureMockStore(metersFetchingState);

        await fetchMeterWithResponseOk(meter.id as uuid);

        expect(store.getActions()).toEqual([]);
      });

      it('does not fetch is entity already exist in state', async () => {
        const stateWithMeterDetails: NormalizedState<MeterDetails> = {
          ...initialDomain(),
          entities: {1: meter as MeterDetails},
        };
        store = configureMockStore(stateWithMeterDetails);

        await fetchMeterWithResponseOk(meter.id as uuid);

        expect(store.getActions()).toEqual([]);
      });

      it('does not fetch if entity have been attempted to be fetched but failed', async () => {
        const entityFetchFailedState: NormalizedState<MeterDetails> = {
          ...initialDomain(),
          entities: {1: meter as MeterDetails},
        };
        store = configureMockStore(entityFetchFailedState);

        await
          fetchMeterWithResponseOk(meter.id as uuid);

        expect(store.getActions()).toEqual([]);
      });
    });
  },
)
;
