import {Gateway} from '../../../state/domain-models-paginated/gateway/gatewayModels';
import {Meter} from '../../../state/domain-models-paginated/meter/meterModels';
import {MapState} from '../mapReducer';
import {getSelectedMapMarker} from '../mapSelectors';

describe('mapSelectors', () => {

  type MapStateType<T> = MapState | {selectedMarker?: Partial<T>};

  describe('getSelectedMeterMarkers', () => {

    it('has no selected meter marker', () => {
      const state: MapState = {isClusterDialogOpen: false};

      expect(getSelectedMapMarker(state).isNothing()).toBe(true);
    });

    it('has selected meter marker', () => {
      const meter: Partial<Meter> = {id: 1};
      const state: MapStateType<Meter> = {isClusterDialogOpen: true, selectedMarker: meter};

      expect(getSelectedMapMarker(state as MapState).get()).toEqual({id: 1});
    });

  });

  describe('getSelectedGatewayMarkers', () => {
    it('has no selected gateway marker', () => {
      const state: MapState = {isClusterDialogOpen: false};

      expect(getSelectedMapMarker(state).isNothing()).toBe(true);
    });

    it('has selected meter marker', () => {
      const gateway: Partial<Gateway> = {id: 1};
      const state: MapStateType<Gateway> = {isClusterDialogOpen: true, selectedMarker: gateway};

      expect(getSelectedMapMarker(state as MapState).get()).toEqual({id: 1});
    });
  });
});
