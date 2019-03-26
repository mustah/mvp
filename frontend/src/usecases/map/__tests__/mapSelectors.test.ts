import {initTranslations} from '../../../i18n/__tests__/i18nMock';
import {RootState} from '../../../reducers/rootReducer';
import {Gateway} from '../../../state/domain-models-paginated/gateway/gatewayModels';
import {Meter} from '../../../state/domain-models-paginated/meter/meterModels';
import {DomainModelsState} from '../../../state/domain-models/domainModels';
import {initialDomain} from '../../../state/domain-models/domainModelsReducer';
import {initialState} from '../../../state/summary/summaryReducer';
import {initialState as initialSearchState} from '../../../state/search/searchReducer';
import {MapMarker} from '../mapModels';
import {MapState} from '../mapReducer';
import {getGatewayLowConfidenceTextInfo, getMeterLowConfidenceTextInfo, getSelectedMapMarker} from '../mapSelectors';

describe('mapSelectors', () => {

  initTranslations({
    code: 'en',
    translation: {
      test: 'no translations will default to key',
    },
  });

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

  describe('getTotalMeterMapMarkers', () => {

    it('shows no low confidence info text', () => {
      const domainModels: Partial<DomainModelsState> = {
        meterMapMarkers: {...initialDomain<MapMarker>()},
      };

      const state = {
        summary: initialState,
        domainModels,
        search: initialSearchState,
      };

      expect(getMeterLowConfidenceTextInfo(state as RootState)).toBeUndefined();
    });

    it('shows no low confidence info text when meters are markers have same total', () => {
      const domainModels: Partial<DomainModelsState> = {
        meterMapMarkers: {...initialDomain<MapMarker>(), total: 10},
      };

      const state = {
        summary: {...initialState, payload: {...initialState.payload, numMeters: 12}},
        domainModels,
        search: initialSearchState,
      };

      const text = getMeterLowConfidenceTextInfo(state as RootState);

      expect(text).toBe('2 meter are not displayed in the map due to low accuracy');
    });

    it('shows low confidence info with regard to selection when a filtering is performed', () => {
      const domainModels: Partial<DomainModelsState> = {
        meterMapMarkers: {...initialDomain<MapMarker>(), total: 10},
      };

      const state = {
        summary: {...initialState, payload: {...initialState.payload, numMeters: 10}},
        domainModels,
        search: {...initialSearchState, validation: {query: 'bro'}},
      };

      expect(getMeterLowConfidenceTextInfo(state as RootState)).toBeUndefined();
    });

    it('shows low confidence info text', () => {
      const domainModels: Partial<DomainModelsState> = {
        meterMapMarkers: {...initialDomain<MapMarker>(), total: 2},
      };

      const state = {
        summary: {...initialState, payload: {...initialState.payload, numMeters: 10}},
        domainModels,
        search: initialSearchState,
      };

      expect(getMeterLowConfidenceTextInfo(state as RootState))
        .toEqual('8 meter are not displayed in the map due to low accuracy');
    });

  });

  describe('getTotalGatewayMapMarkers', () => {

    it('shows no low confidence info text', () => {
      const domainModels: Partial<DomainModelsState> = {
        gatewayMapMarkers: {...initialDomain<MapMarker>()},
      };

      const state = {
        domainModels,
        summary: initialState,
        search: initialSearchState,
      };

      expect(getGatewayLowConfidenceTextInfo(state as RootState)).toBeUndefined();
    });

    it('shows no low confidence info text when meters are markers have same total', () => {
      const domainModels: Partial<DomainModelsState> = {
        gatewayMapMarkers: {...initialDomain<MapMarker>(), total: 10},
      };

      const state = {
        domainModels,
        summary: {...initialState, payload: {...initialState.payload, numMeters: 10}},
        search: initialSearchState,
      };

      expect(getGatewayLowConfidenceTextInfo(state as RootState)).toBeUndefined();
    });

    it('shows low confidence info text', () => {
      const domainModels: Partial<DomainModelsState> = {
        gatewayMapMarkers: {...initialDomain<MapMarker>(), total: 9},
      };

      const state = {
        domainModels,
        summary: {...initialState, payload: {...initialState.payload, numMeters: 10}},
        search: initialSearchState,
      };

      expect(getGatewayLowConfidenceTextInfo(state as RootState))
        .toEqual('1 gateway are not displayed in the map due to low accuracy');
    });

  });

});
