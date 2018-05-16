import {Dictionary, Status} from '../../../types/Types';
import {isMapMarker, makeLeafletCompatibleMarkersFrom} from '../helper/clusterHelper';
import {isGeoPositionWithinThreshold, metersWithinThreshold} from '../helper/mapHelper';
import {MapMarker, Marker} from '../mapModels';

describe('clusterHelper', () => {

  const mapMarker1: MapMarker = {
    id: 1,
    status: Status.ok,
    latitude: 57.505402,
    longitude: 12.069364,
    confidence: 1,
  };

  const mapMarker2: MapMarker = {
    id: 2,
    status: Status.warning,
    latitude: 57.505412,
    longitude: 12.069374,
    confidence: 0.76,
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
      latitude: 57.505412,
      longitude: 12.069374,
      confidence: 0.70,
    };

    it('it should accept confidence of 0.75 and above', () => {
      expect(isGeoPositionWithinThreshold(mapMarker1 as MapMarker)).toBe(true);
      expect(isGeoPositionWithinThreshold(mapMarker2 as MapMarker)).toBe(true);
    });

    it('it should not accept confidence less than 0.75', () => {
      expect(isGeoPositionWithinThreshold(mapMarker3 as MapMarker)).toBe(false);
    });
  });

  describe('isMapMarker', () => {
    it('is of type MapMarker', () => {
      const markers: MapMarker = {
        id: 1,
        status: Status.info,
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
          latitude: 1,
          longitude: 2,
          confidence: 3,
        },
      };

      expect(isMapMarker(markers)).toBe(false);
    });
  });

  describe('metersWithinThreshold', () => {

    it('can handle undefined input', () => {
      expect(metersWithinThreshold(undefined!)).toEqual([]);
    });

    it('filters out meters with low threshold, like 0.1', () => {
      const filteredMeters = metersWithinThreshold({
        asdf: {
          latitude: 1,
          longitude: 2,
          confidence: 1,
          status: Status.ok,
          id: 'asdf',
        },
        asdf2: {
          latitude: 1,
          longitude: 2,
          confidence: 0.1,
          status: Status.ok,
          id: 'asdf2',
        },
      });
      expect(filteredMeters.length).toEqual(1);
    });

  });

})
;
