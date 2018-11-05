import {Dictionary, Status} from '../../../types/Types';
import {boundsFromMarkers, flatMapMarkers, flattenMapMarkers} from '../helper/mapHelper';
import {Bounds, IdentifiablePosition, MapMarker, MapMarkerApiResponse} from '../mapModels';

describe('mapHelper', () => {

  describe('flattenMapMarkers', () => {

    it('can handle undefined input', () => {
      expect(flattenMapMarkers(undefined!)).toEqual([]);
    });

    it('filters out meters with low threshold, like 0.1', () => {
      const location1: MapMarker = {
        latitude: 1,
        longitude: 2,
        status: Status.ok,
        id: 1234,
      };
      const location2: MapMarker = {
        latitude: 1,
        longitude: 2,
        status: Status.ok,
        id: 4321,
      };

      const actual: MapMarker[] = flattenMapMarkers({1234: location1, 4321: location2});

      expect(actual).toEqual([location1, location2]);
    });
  });

  describe('flatMapMarkers', () => {
    const position1: IdentifiablePosition = {id: 1, longitude: 1.2, latitude: 2.3};
    const position2: IdentifiablePosition = {id: 2, longitude: 1.4, latitude: 2.5};
    const position3: IdentifiablePosition = {id: 3, longitude: 1.4, latitude: 2.5};
    const position4: IdentifiablePosition = {id: 4, longitude: 1.4, latitude: 2.5};

    it('return empty array of empty map markers', () => {
      const responseFromApi: MapMarkerApiResponse = {markers: {}};

      const mapMarkers: MapMarker[] = flatMapMarkers(responseFromApi);

      expect(mapMarkers).toEqual([]);
    });

    it('flattens map markers by status type', () => {
      const responseFromApi: MapMarkerApiResponse = {
        markers: {ok: [position1]},
      };

      const mapMarkers: MapMarker[] = flatMapMarkers(responseFromApi);
      const expected: MapMarker[] = [{...position1, status: Status.ok}];

      expect(mapMarkers).toEqual(expected);
    });

    it('flattens map markers by status type', () => {
      const responseFromApi: MapMarkerApiResponse = {
        markers: {
          ok: [position1, position2],
        },
      };

      const mapMarkers: MapMarker[] = flatMapMarkers(responseFromApi);

      const expected: MapMarker[] = [
        {...position1, status: Status.ok},
        {...position2, status: Status.ok},
      ];

      expect(mapMarkers).toEqual(expected);
    });

    it('flattens map markers with different status types', () => {
      const responseFromApi: MapMarkerApiResponse = {
        markers: {
          ok: [position1, position2],
          error: [position3, position4],
        },
      };

      const mapMarkers: MapMarker[] = flatMapMarkers(responseFromApi);

      const expected: MapMarker[] = [
        {...position1, status: Status.ok},
        {...position2, status: Status.ok},
        {...position3, status: Status.error},
        {...position4, status: Status.error},
      ];

      expect(mapMarkers).toEqual(expected);
    });
  });

  describe('boundsFromMarkers', () => {

    it('finds bounds of single meter with latitude and longitude', () => {
      const latitude = 66.3091086409441;
      const longitude = 14.1472317996851;
      const mapMarkers: Dictionary<MapMarker> = {
        '0030734d-743a-4915-ae9f-5e07b5866e22': {
          id: '0030734d-743a-4915-ae9f-5e07b5866e22',
          latitude,
          longitude,
          status: Status.ok,
        }
      };

      const actual: Bounds = boundsFromMarkers(mapMarkers);

      const expected: Bounds = [[latitude, longitude], [latitude, longitude]];

      expect(actual).toEqual(expected);
    });

  });

});
