import {Period} from '../../components/dates/dateModels';
import {momentWithTimeZone, prettyRange, toPeriodApiParameters} from '../dateHelpers';
import {Maybe} from '../Maybe';

describe('dateHelper', () => {

  describe('relative time periods', () => {
    it('defaults to no limits if no start/end time is given', () => {
      const date = momentWithTimeZone('2018-03-23 11:00:00').toDate();

      expect(toPeriodApiParameters({
        now: date,
        period: Period.latest,
        customDateRange: Maybe.nothing(),
      })).toEqual(['after=2018-03-22T00%3A00%3A00.000Z',
        'before=2018-03-23T00%3A00%3A00.000Z']);
    });
  });

  describe('user friendliness', () => {

    it('can be expressed in a friendly looking way', () => {
      const endOfNovember = momentWithTimeZone('2013-11-24 11:00:00').toDate();
      const timePeriod = prettyRange({
        now: endOfNovember,
        period: Period.currentMonth,
        customDateRange: Maybe.nothing(),
      });
      expect(timePeriod).toEqual('2013-11-01 - 2013-11-30');
    });

    it('can be consumed by the MVP REST API', () => {
      const apiFriendlyOutput = toPeriodApiParameters({
        now: momentWithTimeZone('2012-02-04').toDate(),
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
        const now = momentWithTimeZone('2013-03-25').toDate();
        const prevMonthRange = prettyRange({
          now,
          period: Period.previousMonth,
          customDateRange: Maybe.nothing(),
        });
        expect(prevMonthRange).toEqual('2013-02-01 - 2013-02-28');
      });

      it('knows about previous 7 days', () => {
        const march14 = momentWithTimeZone('2013-03-14T00:00:00Z').toDate();
        const prevWeek = prettyRange({
          now: march14,
          period: Period.previous7Days,
          customDateRange: Maybe.nothing(),
        });
        expect(prevWeek).toEqual('2013-03-08 - 2013-03-14');
      });

      it('knows about current week', () => {
        const friday10thNovember = momentWithTimeZone('2017-11-10T00:00:00Z').toDate();
        const currentWeekApiParameters = prettyRange({
          now: friday10thNovember,
          period: Period.currentWeek,
          customDateRange: Maybe.nothing(),
        });

        expect(currentWeekApiParameters).toEqual('2017-11-06 - 2017-11-12');
      });

      it('knows about current month', () => {
        const date = momentWithTimeZone('2017-11-23T00:00:00Z').toDate();
        const currentMonthApiParameters = prettyRange({
          now: date,
          period: Period.currentMonth,
          customDateRange: Maybe.nothing(),
        });
        expect(currentMonthApiParameters).toEqual('2017-11-01 - 2017-11-30');
      });

      it('knows about last 24 h', () => {
        const currentDayApiParameters = prettyRange({
          now: momentWithTimeZone('2013-03-13T00:00:00Z').toDate(),
          period: Period.latest,
          customDateRange: Maybe.nothing(),
        });
        expect(currentDayApiParameters).toEqual('2013-03-12 - 2013-03-12');
      });

      it('knows about a custom time period', () => {
        const start =  momentWithTimeZone('2013-03-13T00:00:00Z').toDate();
        const end =  momentWithTimeZone('2013-03-13T00:00:00Z').toDate();

        const currentDayApiParameters = prettyRange({
          now: momentWithTimeZone('2013-03-25T00:00:00Z').toDate(),
          period: Period.custom,
          customDateRange: Maybe.just({start, end}),
        });
        expect(currentDayApiParameters).toEqual('2013-03-13 - 2013-03-13');
      });
    });
  });
});
