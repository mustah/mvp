import {
  FORMAT_DATE_DAY_MONTH,
  FORMAT_DATE_FULL_MINUTE,
  formatDate,
  roundMeasurement,
} from '../formatters';

describe('formatters', () => {

  describe('roundMeasurement', () => {

    test('Measurement is rounded to three decimals', () => {
      expect(roundMeasurement(304.4410237975271)).toEqual('304.441');
    });

    test('Measurement is rounded, not truncated', () => {
      expect(roundMeasurement(304.4419237975271)).toEqual('304.442');
    });

    test('String measurement is almost always returned as is', () => {
      expect(roundMeasurement('A string value')).toEqual('A string value');
      expect(roundMeasurement('')).toEqual('0.000');
    });

  });

  describe('Format dates', () => {
    test('Without specifying format, result defaults to FORMAT_DATE_DAY_MONTH', () => {
      const now = new Date();

      expect(formatDate(now)).toEqual(formatDate(now, FORMAT_DATE_DAY_MONTH));
    });
    test('Custom format', () => {
      const date = new Date('21 march 2017');

      expect(formatDate(date, 'DD/MM/YYYY')).toEqual('21/03/2017');
    });
    test('UNIX timestamp in millisecond precision, to formatted date string', () => {
      const date = new Date('21 march 2017');

      expect(formatDate(date, FORMAT_DATE_FULL_MINUTE)).toEqual('17-03-21 12:00');
    });
  });

});
