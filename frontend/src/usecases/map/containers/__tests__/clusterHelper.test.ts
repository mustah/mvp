import {MapMarker, Marker} from '../../mapModels';
import {isMapMarker, makeLeafletCompatibleMarkersFrom, isWithinThreshold} from '../clusterHelper';

describe('test clusterHelper', () => {

  const mapMarker1: MapMarker = {
    id: 1,
    status: {id: 1, name: 'Ok'},
    address: {cityId: 1, id: 1, name: 'vägen 1'},
    city: {id: 1, name: 'Kungsbacka'},
    position: {latitude: 57.505402, longitude: 12.069364, confidence: 1}
  } as MapMarker;

  const mapMarker2: MapMarker = {
    id: 2,
    status: {id: 2, name: 'Warning'},
    address: {cityId: 1, id: 1, name: 'vägen 2'},
    city: {id: 1, name: 'Kungsbacka'},
    position: {latitude: 57.505412, longitude: 12.069374, confidence: 0.7}
  } as MapMarker;

  const mapMarker3: MapMarker = {
    id: 2,
    status: {id: 2, name: 'Warning'},
    address: {cityId: 1, id: 1, name: 'vägen 2'},
    city: {id: 1, name: 'Kungsbacka'},
    position: {latitude: 57.505412, longitude: 12.069374, confidence: 0.6}
  } as MapMarker;

  const markers: { [key: string]: MapMarker } = {
    '1': mapMarker1,
    '2': mapMarker2,
  };

  describe('test makeLeafletCompatibleMarkersFrom', () => {
    it('should handle single marker', () => {
      const leafletMarkers: Marker[] = makeLeafletCompatibleMarkersFrom(mapMarker1);

      const expectedResult = [{
        "options": {
          "icon": {
            "_initHooksCalled": true,
            "options": {"iconUrl": "assets/images/marker-icon-ok.png"}
          },
          "mapMarkerItem": {
            "address": {"cityId": 1, "id": 1, "name": "vägen 1"},
            "city": {"id": 1, "name": "Kungsbacka"},
            "id": 1,
            "position": {"confidence": 1, "latitude": 57.505402, "longitude": 12.069364},
            "status": {"id": 1, "name": "Ok"}
          }
        }, "position": [57.505402, 12.069364]
      }];

      expect(leafletMarkers).toEqual(expectedResult);
    });

    it('should handle array of markers', () => {

      const leafletMarkers: Marker[] = makeLeafletCompatibleMarkersFrom(markers);

      const expectedResult = [{
        "options": {
          "icon": {
            "_initHooksCalled": true,
            "options": {"iconUrl": "assets/images/marker-icon-ok.png"}
          },
          "mapMarkerItem": {
            "address": {"cityId": 1, "id": 1, "name": "vägen 1"},
            "city": {"id": 1, "name": "Kungsbacka"},
            "id": 1,
            "position": {"confidence": 1, "latitude": 57.505402, "longitude": 12.069364},
            "status": {"id": 1, "name": "Ok"}
          }
        }, "position": [57.505402, 12.069364]
      }, {
        "options": {
          "icon": {
            "_initHooksCalled": true,
            "options": {"iconUrl": "assets/images/marker-icon-warning.png"}
          },
          "mapMarkerItem": {
            "address": {"cityId": 1, "id": 1, "name": "vägen 2"},
            "city": {"id": 1, "name": "Kungsbacka"},
            "id": 2,
            "position": {"confidence": 0.7, "latitude": 57.505412, "longitude": 12.069374},
            "status": {"id": 2, "name": "Warning"}
          }
        }, "position": [57.505412, 12.069374]
      }];

      expect(leafletMarkers).toEqual(expectedResult);
    });
  });

  describe('test isWithinThreshold', () => {
    it('it should accept confidence of 0.7 and above', () => {
      expect(isWithinThreshold(mapMarker1)).toBe(true);
      expect(isWithinThreshold(mapMarker2)).toBe(true);
      expect(isWithinThreshold(mapMarker3)).toBe(false);
    });

    it('it should not accept confidence less than 0.7', () => {
      expect(isWithinThreshold(mapMarker3)).toBe(false);
    });
  });

  describe('test isMapMarker', () => {
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
