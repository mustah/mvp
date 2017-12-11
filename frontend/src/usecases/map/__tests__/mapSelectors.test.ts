import {DomainModel} from '../../../state/domain-models/domainModels';
import {Gateway} from '../../../state/domain-models/gateway/gatewayModels';
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {isMapMarker} from '../containers/clusterHelper';
import {MapMarker} from '../mapModels';
import {MapState} from '../mapReducer';
import {getSelectedGatewayMarker, getSelectedMeterMarker} from '../mapSelectors';

describe('mapSelectors', () => {

  type MapStateType<T> = MapState | {selectedMarker?: Partial<T>};

  describe('getSelectedMeterMarkers', () => {

    it('has no selected meter marker', () => {
      const state: MapState = {isClusterDialogOpen: false};

      expect(getSelectedMeterMarker(state)).toBeUndefined();
    });

    it('has selected meter marker', () => {
      const meter: Partial<Meter> = {id: 1};
      const state: MapStateType<Meter> = {isClusterDialogOpen: true, selectedMarker: meter};

      expect(getSelectedMeterMarker(state as MapState)).toEqual({id: 1});
    });

  });

  describe('getSelectedGatewayMarkers', () => {
    it('has no selected gateway marker', () => {
      const state: MapState = {isClusterDialogOpen: false};

      expect(getSelectedGatewayMarker(state)).toBeUndefined();
    });

    it('has selected meter marker', () => {
      const gateway: Partial<Gateway> = {id: 1};
      const state: MapStateType<Gateway> = {isClusterDialogOpen: true, selectedMarker: gateway};

      expect(getSelectedGatewayMarker(state as MapState)).toEqual({id: 1});
    });
  });

  describe('test', () => {
    it('is of type MapMarker', () => {
      const markers: Partial<MapMarker> = {
        status: {id: 1, name: 'foo'},
        position: {
          latitude: 1,
          longitude: 2,
          confidence: 3,
        },
      };

      expect(isMapMarker(markers as MapMarker)).toBe(true);
    });

    it('is not of type MapMarker', () => {
      const markers: DomainModel<MapMarker> = {
        foo: {
          status: {id: 1, name: 'foo'},
          city: {id: 1, name: 'stockholm'},
          address: {
            cityId: 1,
            id: 2,
            name: 'stampgatan',
          },
          position: {
            latitude: 1,
            longitude: 2,
            confidence: 3,
          },
        },
      };

      expect(isMapMarker(markers as DomainModel<MapMarker>)).toBe(false);
    });
  });

});
