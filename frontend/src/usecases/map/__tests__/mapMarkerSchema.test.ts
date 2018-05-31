import {gatewayMapMarkersDataFormatter} from '../mapMarkerSchema';
import {IdentifiablePosition, MapMarkerApiResponse} from '../mapModels';

describe('gatewayMapMarkerSchema', () => {

  describe('empty response', () => {

    it('will have empty map markers in the result', () => {
      const responseFromApi: MapMarkerApiResponse = {markers: {}};

      expect(gatewayMapMarkersDataFormatter(responseFromApi)).toEqual({entities: {}, result: []});
    });
  });

  describe('single response', () => {

    it('flattens markers before normalizing', () => {
      const position1: IdentifiablePosition = {
        id: 1,
        latitude: 1.22,
        longitude: 2.33,
      };
      const position2: IdentifiablePosition = {
        id: 2,
        latitude: 1.22,
        longitude: 2.33,
      };
      const position3: IdentifiablePosition = {
        id: 3,
        latitude: 1.22,
        longitude: 2.33,
      };

      const responseFromApi: MapMarkerApiResponse = {
        markers: {
          ok: [position1, position2],
          warning: [position3],
        },
      };

      expect(gatewayMapMarkersDataFormatter(responseFromApi)).toEqual({
        entities: {
          gatewayMapMarkers: {
            1: {...position1, status: 'ok'},
            2: {...position2, status: 'ok'},
            3: {...position3, status: 'warning'},
          },
        },
        result: [1, 2, 3],
      });
    });
  });

});
