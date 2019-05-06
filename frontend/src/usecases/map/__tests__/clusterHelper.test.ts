import {Dictionary, Status} from '../../../types/Types';
import {isMapMarker, makeLeafletCompatibleMarkersFrom} from '../helper/clusterHelper';
import {MapMarker, Marker} from '../mapModels';

describe('clusterHelper', () => {

  const mapMarker1: MapMarker = {
    id: 1,
    status: Status.ok,
    latitude: 57.505402,
    longitude: 12.069364,
  };

  const mapMarker2: MapMarker = {
    id: 2,
    status: Status.warning,
    latitude: 57.505412,
    longitude: 12.069374,
  };

  const markers: Dictionary<MapMarker> = {
    1: mapMarker1,
    2: mapMarker2,
  };

  describe('makeLeafletCompatibleMarkersFrom', () => {

    it('should handle single marker', () => {
      const leafletMarkers: Marker[] = makeLeafletCompatibleMarkersFrom(mapMarker1);

      expect(leafletMarkers).toEqual([
        {
          position: [57.505402, 12.069364],
          options: {
            icon: {
              options: {
                iconAnchor: {x: 13, y: 41},
                iconSize: [25, 41],
                iconUrl: {}
              },
              _initHooksCalled: true,
            },
            mapMarkerItem: 1,
            status: Status.ok,
          },
        }
      ]);
    });

    it('can have no map markers', () => {
      const mapMarkers = undefined;
      const leafletMarkers: Marker[] = makeLeafletCompatibleMarkersFrom(mapMarkers || {});

      expect(leafletMarkers).toEqual([]);
    });

    it('should handle array of markers', () => {
      const leafletMarkers: Marker[] = makeLeafletCompatibleMarkersFrom(markers);

      expect(leafletMarkers).toEqual([
        {
          options: {
            icon: {
              _initHooksCalled: true,
              options: {
                iconAnchor: {x: 13, y: 41},
                iconSize: [25, 41],
                iconUrl: {},
              },
            },
            mapMarkerItem: 1,
            status: Status.ok,
          },
          position: [57.505402, 12.069364],
        },
        {
          options: {
            icon: {
              _initHooksCalled: true,
              options: {
                iconAnchor: {x: 13, y: 41},
                iconSize: [25, 41],
                iconUrl: {},
              },
            },
            mapMarkerItem: 2,
            status: Status.warning,
          },
          position: [57.505412, 12.069374],
        },
      ]);
    });
  });

  describe('isMapMarker', () => {
    it('is of type MapMarker', () => {
      const markers: MapMarker = {
        id: 1,
        status: Status.error,
        latitude: 1,
        longitude: 2,
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
        },
      };

      expect(isMapMarker(markers)).toBe(false);
    });
  });

});
