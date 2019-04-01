import {isOnMeterDetailsPage, routes} from '../routes';

describe('routes', () => {

  describe('detects meter details page', () => {
    const testCases: Array<[number, string, boolean]> = [
      [1, 'test', false],
      [2, routes.meters, false],
      [3, `${routes.meter}/1`, true],
    ];

    test.each(testCases)(
      'test #%i: regex %p matches? %p',
      (testId, route, isOnMeterDetails) => expect(isOnMeterDetailsPage(route)).toEqual(isOnMeterDetails)
    );

  });
});
