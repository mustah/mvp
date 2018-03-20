import {Dictionary, Status} from '../../../../types/Types';
import {MapMarker, Marker} from '../../mapModels';
import {isGeoPositionWithinThreshold, isMapMarker, makeLeafletCompatibleMarkersFrom} from '../clusterHelper';

describe('clusterHelper', () => {

  const mapMarker1: MapMarker = {
    id: 1,
    status: Status.ok,
    mapMarkerType: 'Meter',
    latitude: 57.505402,
    longitude: 12.069364,
    confidence: 1,
  };

  const mapMarker2: MapMarker = {
    id: 2,
    status: Status.warning,
    mapMarkerType: 'Meter',
    latitude: 57.505412,
    longitude: 12.069374,
    confidence: 0.7,
  };

  const markers: Dictionary<MapMarker> = {
    1: mapMarker1,
    2: mapMarker2,
  };

  describe('makeLeafletCompatibleMarkersFrom', () => {
    it('should handle single marker', () => {
      const leafletMarkers: Marker[] = makeLeafletCompatibleMarkersFrom(mapMarker1);

      expect(leafletMarkers).toEqual([{
        position: [57.505402, 12.069364],
        options: {
          icon: {
            options: {iconUrl: 'assets/images/marker-icon-ok.png'},
            _initHooksCalled: true,
          },
          mapMarkerItem: 1,
        },
      }]);
    });

    it('should handle array of markers', () => {
      const leafletMarkers: Marker[] = makeLeafletCompatibleMarkersFrom(markers);

      expect(leafletMarkers).toEqual([
        {
          options: {
            icon: {
              _initHooksCalled: true,
              options: {iconUrl: 'assets/images/marker-icon-ok.png'},
            },
            mapMarkerItem: 1,
          },
          position: [57.505402, 12.069364],
        },
        {
          options: {
            icon: {
              _initHooksCalled: true,
              options: {iconUrl: 'assets/images/marker-icon-warning.png'},
            },
            mapMarkerItem: 2,
          },
          position: [57.505412, 12.069374],
        },
      ]);
    });
  });

  describe('isGeoPositionWithinThreshold', () => {

    const mapMarker3: MapMarker = {
      id: 1,
      status: Status.warning,
      mapMarkerType: 'Meter',
      latitude: 57.505412,
      longitude: 12.069374,
      confidence: 0.6,
    };

    it('it should accept confidence of 0.7 and above', () => {
      expect(isGeoPositionWithinThreshold(mapMarker1 as MapMarker)).toBe(true);
      expect(isGeoPositionWithinThreshold(mapMarker2 as MapMarker)).toBe(true);
      expect(isGeoPositionWithinThreshold(mapMarker3 as MapMarker)).toBe(false);
    });

    it('it should not accept confidence less than 0.7', () => {
      expect(isGeoPositionWithinThreshold(mapMarker3 as MapMarker)).toBe(false);
    });
  });

  describe('isMapMarker', () => {
    it('is of type MapMarker', () => {
      const markers: MapMarker = {
        id: 1,
        status: Status.info,
        mapMarkerType: 'Meter',
        latitude: 1,
        longitude: 2,
        confidence: 3,
      };

      expect(isMapMarker(markers as MapMarker)).toBe(true);
    });

    it('is not of type MapMarker', () => {
      const markers: Dictionary<MapMarker> = {
        foo: {
          id: 'foo',
          status: Status.ok,
          mapMarkerType: 'Meter',
          latitude: 1,
          longitude: 2,
          confidence: 3,
        },
      };

      expect(isMapMarker(markers)).toBe(false);
    });
  });
})
;
