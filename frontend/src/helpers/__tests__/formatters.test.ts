import {
  FORMAT_DATE_DAY_MONTH,
  FORMAT_DATE_FULL_MINUTE,
  FORMAT_NUMBER_DEFAULT,
  formatDate,
  formatNumber,
} from '../formatters';

describe('formatters', () => {

  describe('Format numbers', () => {

    test('Without specifying format, result defaults to FORMAT_NUMBER_DEFAULT', () => {
      expect(formatNumber(1000000.1234)).toEqual(formatNumber(1000000.1234, FORMAT_NUMBER_DEFAULT));
    });

    test('Rounding number', () => {
      expect(formatNumber(1000000.1234567)).toEqual('1,000,000.1235');
    });

    test('Custom format', () => {
      expect(formatNumber(1000000.19, '0,0.[0]')).toEqual('1,000,000.2');
    });

    test('No rounding within specified format precision\'', () => {
      expect(formatNumber(1000000.1234)).toEqual('1,000,000.1234');
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
