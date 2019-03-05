import {DateRange, Period} from '../../components/dates/dateModels';
import {
  displayDate,
  makeCompareCustomDateRange,
  makeCompareDateRange,
  momentAtUtcPlusOneFrom,
  prettyRange,
  toPeriodApiParameters
} from '../dateHelpers';
import {Maybe} from '../Maybe';

describe('dateHelper', () => {

  describe('relative time periods', () => {

    it('defaults to no limits if no start/end time is given', () => {
      expect(toPeriodApiParameters({
        start: momentAtUtcPlusOneFrom('2018-03-23 11:00:00').toDate(),
        period: Period.latest,
        customDateRange: Maybe.nothing(),
      })).toEqual([
        'after=2018-03-22T00%3A00%3A00.000%2B01%3A00',
        'before=2018-03-23T00%3A00%3A00.000%2B01%3A00',
      ]);
    });
  });

  describe('user friendliness', () => {

    it('can be expressed in a friendly looking way', () => {
      const endOfNovember = momentAtUtcPlusOneFrom('2013-11-24 11:00:00').toDate();
      const timePeriod = prettyRange({
        start: endOfNovember,
        period: Period.currentMonth,
        customDateRange: Maybe.nothing(),
      });
      expect(timePeriod).toEqual('2013-11-01 - 2013-11-30');
    });

    it('can be consumed by the MVP REST API', () => {
      const apiFriendlyOutput = toPeriodApiParameters({
        start: momentAtUtcPlusOneFrom('2012-02-04').toDate(),
        period: Period.currentMonth,
        customDateRange: Maybe.nothing(),
      });
      expect(apiFriendlyOutput.length).toEqual(2);

      // the API wants ISO-8601 "instant":
      // https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#ISO_INSTANT
      // JS regex lazily stolen from https://stackoverflow.com/a/3143231/49879 which has more variants if needed
      const iso8601Instant = new RegExp(
        /\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d\.\d+([+-][0-2]\d:[0-5]\d|Z)/);
      expect(apiFriendlyOutput[0]).toMatch(/after=.+/);
      expect(apiFriendlyOutput[1]).toMatch(/before=.+/);

      const after = decodeURIComponent(apiFriendlyOutput[0].split('=')[1]);
      expect(after).toMatch(iso8601Instant);

      const before = decodeURIComponent(apiFriendlyOutput[1].split('=')[1]);
      expect(before).toMatch(iso8601Instant);
    });

    describe('can be constructed from relative terms', () => {
      it('knows about previous month', () => {
        const prevMonthRange = prettyRange({
          start: momentAtUtcPlusOneFrom('2013-03-25').toDate(),
          period: Period.previousMonth,
          customDateRange: Maybe.nothing(),
        });
        expect(prevMonthRange).toEqual('2013-02-01 - 2013-02-28');
      });

      it('knows about previous 7 days', () => {
        const prevWeek = prettyRange({
          start: momentAtUtcPlusOneFrom('2013-03-14T00:00:00Z').toDate(),
          period: Period.previous7Days,
          customDateRange: Maybe.nothing(),
        });
        expect(prevWeek).toEqual('2013-03-08 - 2013-03-14');
      });

      it('knows about current week', () => {
        const currentWeekApiParameters = prettyRange({
          start: momentAtUtcPlusOneFrom('2017-11-10T00:00:00Z').toDate(),
          period: Period.currentWeek,
          customDateRange: Maybe.nothing(),
        });

        expect(currentWeekApiParameters).toEqual('2017-11-06 - 2017-11-13');
      });

      it('knows about current month', () => {
        const currentMonthApiParameters = prettyRange({
          start: momentAtUtcPlusOneFrom('2017-11-23T00:00:00Z').toDate(),
          period: Period.currentMonth,
          customDateRange: Maybe.nothing(),
        });
        expect(currentMonthApiParameters).toEqual('2017-11-01 - 2017-11-30');
      });

      it('knows about last 24 h', () => {
        const currentDayApiParameters = prettyRange({
          start: momentAtUtcPlusOneFrom('2013-03-13T00:00:00Z').toDate(),
          period: Period.latest,
          customDateRange: Maybe.nothing(),
        });
        expect(currentDayApiParameters).toEqual('2013-03-12 - 2013-03-13');
      });

      it('knows about a custom time period', () => {
        const start = momentAtUtcPlusOneFrom('2013-03-13T00:00:00Z').toDate();
        const end = momentAtUtcPlusOneFrom('2013-03-13T00:00:00Z').toDate();

        const currentDayApiParameters = prettyRange({
          start: momentAtUtcPlusOneFrom('2013-03-25T00:00:00Z').toDate(),
          period: Period.custom,
          customDateRange: Maybe.just({start, end}),
        });
        expect(currentDayApiParameters).toEqual('2013-03-13 - 2013-03-14');
      });
    });
  });

  describe('displayDate', () => {

    it('formats in timezone CET', () => {
      expect(displayDate('2018-01-21T00:00:00Z')).toBe('2018-01-21 01:00');
      expect(displayDate('2018-04-21T08:00:00Z')).toBe('2018-04-21 09:00');
    });

    it('formats in timezone with offset +1 from UTC', () => {
      expect(displayDate('2018-01-21T08:00:00+01:00')).toBe('2018-01-21 08:00');
    });

    it('formats in timezone in Stockholm (offset +2 from UTC)', () => {
      expect(displayDate('2018-01-21T08:00:00+02:00')).toBe('2018-01-21 07:00');
    });

  });

  describe('makeCompareDateRange', () => {

    describe('compare period is yesterday', () => {

      it('has date range for current month period ', () => {
        const period = Period.currentMonth;
        const start = momentAtUtcPlusOneFrom('2019-03-05 10:00:00').toDate();
        const actual: DateRange = makeCompareDateRange(period, start);

        expect(displayDate(actual.start)).toBe('2019-02-01 00:00');
        expect(displayDate(actual.end)).toBe('2019-02-28 00:00');
      });

      it('has date range for previous month period ', () => {
        const period = Period.previousMonth;
        const start = momentAtUtcPlusOneFrom('2019-03-05 10:00:00').toDate();
        const actual: DateRange = makeCompareDateRange(period, start);

        expect(displayDate(actual.start)).toBe('2019-01-01 00:00');
        expect(displayDate(actual.end)).toBe('2019-01-31 00:00');
      });

      it('has date range for current week period ', () => {
        const period = Period.currentWeek;
        const start = momentAtUtcPlusOneFrom('2019-03-05 10:00:00').toDate();
        const actual: DateRange = makeCompareDateRange(period, start);

        expect(displayDate(actual.start)).toBe('2019-02-25 00:00');
        expect(displayDate(actual.end)).toBe('2019-03-04 00:00');
      });

      it('has date range for latest period ', () => {
        const period = Period.latest;
        const start = momentAtUtcPlusOneFrom('2019-03-05 10:00:00').toDate();
        const actual: DateRange = makeCompareDateRange(period, start);

        expect(displayDate(actual.start)).toBe('2019-03-02 00:00');
        expect(displayDate(actual.end)).toBe('2019-03-03 00:00');
      });
    });

    describe('custom date range', () => {

      it('compares custom date range', () => {
        const start = momentAtUtcPlusOneFrom('2019-03-18 11:00:00').toDate();
        const end = momentAtUtcPlusOneFrom('2019-03-20 11:00:00').toDate();
        const actual: DateRange = makeCompareCustomDateRange({start, end});

        expect(displayDate(actual.start)).toBe('2019-03-16 00:00');
        expect(displayDate(actual.end)).toBe('2019-03-18 00:00');
      });

      it('compares custom date range over long period', () => {
        const start = momentAtUtcPlusOneFrom('2019-03-20 11:00:00').toDate();
        const end = momentAtUtcPlusOneFrom('2019-03-30 11:00:00').toDate();
        const actual: DateRange = makeCompareCustomDateRange({start, end});

        expect(displayDate(actual.start)).toBe('2019-03-10 00:00');
        expect(displayDate(actual.end)).toBe('2019-03-20 00:00');
      });

      it('compares custom date range for same day', () => {
        const start = momentAtUtcPlusOneFrom('2019-03-20 11:00:00').toDate();
        const end = momentAtUtcPlusOneFrom('2019-03-20 11:00:00').toDate();
        const actual: DateRange = makeCompareCustomDateRange({start, end});

        expect(displayDate(actual.start)).toBe('2019-03-19 00:00');
        expect(displayDate(actual.end)).toBe('2019-03-20 00:00');
      });

    });

  });
});
