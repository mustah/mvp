import {thresholdClassName} from '../thresholds';

describe('thresholds', () => {

  describe('thresholdClassName', () => {

    const errorTestCases: Array<[number, any, string]> = [
      [1, -1, 'error'],
      [2, 69, 'error'],
      [3, 70, 'error'],

      [4, 71, 'warning'],
      [5, 99, 'warning'],

      [6, 100, 'ok'],
      [7, 101, 'ok'],

      [8, undefined, 'info'],
      [9, 'foobar', 'info'],
    ];

    test.each(errorTestCases)(
      'test #%i: regex %p matches? %p',
      (id: number, percent: number | undefined, cssClassName: string) =>
        expect(thresholdClassName(percent!)).toEqual(cssClassName)
    );

  });
});
