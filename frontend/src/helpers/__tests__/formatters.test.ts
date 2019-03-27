import {momentAtUtcPlusOneFrom} from '../dateHelpers';
import {
  cityWithoutCountry,
  formatCollectionPercentage,
  formatDate,
  formatAndFloorPercentage,
  round,
  roundMeasurement
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

  describe('round', () => {

    test('Round to three decimals', () => {
      expect(round(304.4410237975271, '0.000')).toEqual('304.441');
    });

    test('Round to two decimals', () => {
      expect(round(304.4410237975271, '0.00')).toEqual('304.44');
    });

    test('Round does not truncate', () => {
      expect(round(4.05, '0.0')).toEqual('4.1');
    });

    test('String measurement is almost always returned as is', () => {
      expect(round('A string value', '0.0')).toEqual('A string value');
      expect(round('', '0.0')).toEqual('0.0');
    });

  });

  describe('Format dates', () => {

    test('Without specifying format, result defaults to FORMAT_DATE_DAY_MONTH', () => {
      const date = momentAtUtcPlusOneFrom('2017-03-21T22:00:00Z').toDate();

      expect(formatDate(date)).toEqual('21/03');
    });

    test('Custom format', () => {
      const date = momentAtUtcPlusOneFrom('2017-03-21T00:00:00Z').toDate();

      expect(formatDate(date, 'DD/MM/YYYY')).toEqual('21/03/2017');
    });

    test('UNIX timestamp in millisecond precision, to formatted date string', () => {
      const date = momentAtUtcPlusOneFrom('2017-03-21T11:00:00Z').toDate();

      expect(formatDate(date, 'YY-MM-DD HH:mm')).toEqual('17-03-21 12:00');
    });
  });

  describe('formatCollectionPercentage', () => {

    it('formats undefined as dash', () => {
      expect(formatCollectionPercentage(undefined, 0)).toBe('-');
    });

    it('formats undefined as dash when interval is non-zero', () => {
      expect(formatCollectionPercentage(undefined, 15)).toBe('-');
    });

    it('formats 100% collection percentage as dash when interval is zero', () => {
      expect(formatCollectionPercentage(100.0, 0)).toBe('-');
    });

    it('formats as dash when called without parameters', () => {
      expect(formatCollectionPercentage()).toBe('-');
    });

    it('formats as dash when interval is undefined', () => {
      expect(formatCollectionPercentage(15.88)).toBe('-');
    });

    it('formats 100% collection percentage as 100% when interval is non-zero', () => {
      expect(formatCollectionPercentage(100.0, 15)).toBe('100%');
    });

    it('formats 99.7% collection percentage as 99.7% when interval is non-zero', () => {
      expect(formatCollectionPercentage(99.7, 15)).toBe('99.7%');
    });

    it('it floors the second decimal', () => {
      expect(formatCollectionPercentage(99.97, 15)).toBe('99.9%');
    });

    it('user should not see an indicator when percentage > 100', () => {
      expect(formatCollectionPercentage(104.2, 15, false)).toBe('100%');
    });

    it('super admin should see an indicator when percentage > 100', () => {
      expect(formatCollectionPercentage(104.2, 15, true)).toBe('100% *');
    });

    it('super admin should not see an indicator when percentage <= 100', () => {
      expect(formatCollectionPercentage(84.2, 15, true)).toBe('84.2%');
    });
  });

  describe('formatPercentage', () => {
    it('it floors the second decimal', () => {
      expect(formatAndFloorPercentage(99.97)).toBe('99.9%');
    });
  });

  describe('cityWithoutCountry', () => {

    it('uses the input value if it does not look like a country,city', () => {
      expect(cityWithoutCountry('stockholm')).toEqual('Stockholm');
    });

    it('removes the country if the country comes first', () => {
      expect(cityWithoutCountry('sweden,stockholm')).toEqual('Stockholm');
    });

  });

});
