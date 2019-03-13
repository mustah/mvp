import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {ObjectsById} from '../../domainModels';
import {Status, uuid} from '../../../../types/Types';
import {centerMap} from '../../../../usecases/map/mapActions';
import {MapMarker} from '../../../../usecases/map/mapModels';
import {centerMapOnMeter} from '../dashboardActions';

describe('dashboardActions', () => {

  describe('centerMapOnMeter', () => {

    const mockedStoreWithFixedGeoPosition = (meterIds: uuid[]) => {
      const entities: ObjectsById<MapMarker> = {};
      meterIds.forEach((meterId) =>
        entities[meterId] = {
          latitude: 2,
          longitude: 2,
          status: Status.ok,
          id: meterId,
        });

      const configureMockStore = configureStore([thunk]);
      return configureMockStore({
        domainModels: {
          meterMapMarkers: {
            entities,
          },
        },
      });
    };

    it('dispatches the geoposition of a meter that has a geoposition', () => {
      const store = mockedStoreWithFixedGeoPosition(['123']);

      store.dispatch(centerMapOnMeter('123'));

      expect(store.getActions()).toEqual([centerMap({latitude: 2, longitude: 2})]);
    });

    it('does not dispatch an action if a meter does not have a geoposition', () => {
      const store = mockedStoreWithFixedGeoPosition(['hello-there']);

      store.dispatch(centerMapOnMeter('123'));

      expect(store.getActions()).toHaveLength(0);
    });

  });

});
