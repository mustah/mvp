import {initTranslations} from '../../../i18n/__tests__/i18nMock';
import {RootState} from '../../../reducers/rootReducer';
import {DomainModelsState} from '../../../state/domain-models/domainModels';
import {initialDomain} from '../../../state/domain-models/domainModelsReducer';
import {initialState as initialSearchState} from '../../../state/search/searchReducer';
import {PaginationState} from '../../../state/ui/pagination/paginationModels';
import {initialPagination} from '../../../state/ui/pagination/paginationReducer';
import {MapMarker, MapZoomSettings} from '../mapModels';
import {MapState} from '../mapReducer';
import {getGatewayLowConfidenceTextInfo, getMapZoomSettings, getMeterLowConfidenceTextInfo} from '../mapSelectors';

describe('mapSelectors', () => {

  initTranslations({
    code: 'en',
    translation: {
      test: 'no translations will default to key',
    },
  });

  type MeterPaginationState = Pick<PaginationState, 'meters'>;

  const pagination: MeterPaginationState = {
    meters: initialPagination,
  };

  const paginationWithNumMeters = (totalElements: number): MeterPaginationState =>
    ({meters: {...initialPagination, totalElements}});

  describe('getTotalMeterMapMarkers', () => {

    it('shows no low confidence info text', () => {
      const domainModels: Partial<DomainModelsState> = {
        meterMapMarkers: {...initialDomain<MapMarker>()},
      };

      const state = {
        domainModels,
        search: initialSearchState,
        ui: {pagination},
      };

      expect(getMeterLowConfidenceTextInfo(state as RootState)).toBeUndefined();
    });

    it('shows no low confidence info text when meters are markers have same total', () => {
      const domainModels: Partial<DomainModelsState> = {
        meterMapMarkers: {...initialDomain<MapMarker>(), total: 10},
      };

      const state = {
        domainModels,
        search: initialSearchState,
        ui: {pagination: paginationWithNumMeters(12)},
      };

      const text = getMeterLowConfidenceTextInfo(state as RootState);

      expect(text).toBe('2 meter are not displayed in the map due to low accuracy');
    });

    it('shows low confidence info with regard to selection when a filtering is performed', () => {
      const domainModels: Partial<DomainModelsState> = {
        meterMapMarkers: {...initialDomain<MapMarker>(), total: 10},
      };

      const state = {
        domainModels,
        search: {...initialSearchState, validation: {query: 'bro'}},
        ui: {pagination},
      };

      expect(getMeterLowConfidenceTextInfo(state as RootState)).toBeUndefined();
    });

    it('shows low confidence info text', () => {
      const domainModels: Partial<DomainModelsState> = {
        meterMapMarkers: {...initialDomain<MapMarker>(), total: 2},
      };

      const state = {
        domainModels,
        search: initialSearchState,
        ui: {pagination: paginationWithNumMeters(10)},
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
        search: initialSearchState,
        ui: {pagination},
      };

      expect(getGatewayLowConfidenceTextInfo(state as RootState)).toBeUndefined();
    });

    it('shows no low confidence info text when meters are markers have same total', () => {
      const domainModels: Partial<DomainModelsState> = {
        gatewayMapMarkers: {...initialDomain<MapMarker>(), total: 10},
      };

      const state = {
        domainModels,
        search: initialSearchState,
        ui: {pagination},
      };

      expect(getGatewayLowConfidenceTextInfo(state as RootState)).toBeUndefined();
    });

    it('shows low confidence info text', () => {
      const domainModels: Partial<DomainModelsState> = {
        gatewayMapMarkers: {...initialDomain<MapMarker>(), total: 9},
      };

      const state = {
        domainModels,
        search: initialSearchState,
        ui: {pagination: paginationWithNumMeters(10)},
      };

      expect(getGatewayLowConfidenceTextInfo(state as RootState))
        .toEqual('1 gateway are not displayed in the map due to low accuracy');
    });

  });

  describe('getMapZoomSettings', () => {

    it('has no map settings from empty initial state', () => {
      const mapState: MapState = {};

      expect(getMapZoomSettings(-99)(mapState)).toEqual({});
    });

    it('has can get map state from existing state', () => {
      const zoomSettings: MapZoomSettings = {zoom: 2, center: {longitude: 12, latitude: 34}};
      const mapState: MapState = {1: zoomSettings};

      expect(getMapZoomSettings(1)(mapState)).toEqual(zoomSettings);
    });

    it('has no map settings when id is not found', () => {
      const zoomSettings: MapZoomSettings = {zoom: 2, center: {longitude: 12, latitude: 34}};
      const mapState: MapState = {1: zoomSettings};

      expect(getMapZoomSettings(-11)(mapState)).toEqual({});
    });
  });
});
