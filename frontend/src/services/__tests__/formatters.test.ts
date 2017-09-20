import {FORMAT_DATE_DAY_MONTH, FORMAT_NUMBER_DEFAULT, formatDate, formatNumber} from '../formatters';

describe('Formatters', () => {
  describe('Format numbers', () => {

    test('Without specifying format, result defaults to FORMAT_NUMBER_DEFAULT', () => {
      const num = 1000000.1234;
      expect(formatNumber(num)).toEqual(formatNumber(num, FORMAT_NUMBER_DEFAULT));
    });

    test('Rounding number', () => {
      const num = 1000000.1234567;
      expect(formatNumber(num)).toEqual('1,000,000.1235');
    });

    test('Custom format', () => {
      const num = 1000000.19;
      expect(formatNumber(num, '0,0.[0]')).toEqual('1,000,000.2');
    });

    test('No rounding within specified format precision\'', () => {
      const num = 1000000.1234;
      expect(formatNumber(num)).toEqual('1,000,000.1234');
    });
  });

  describe('Format dates', () => {
    test('Without specifying format, result defaults to FORMAT_DATE_DAY_MONTH', () => {
      const date = new Date();
      expect(formatDate(date)).toEqual(formatDate(date, FORMAT_DATE_DAY_MONTH));
    });
    test('Custom format', () => {
      const date = new Date('21 march 2017'); // A "long" number
      expect(formatDate(date, 'DD/MM/YYYY')).toEqual('21/03/2017');
    });
  });

});
