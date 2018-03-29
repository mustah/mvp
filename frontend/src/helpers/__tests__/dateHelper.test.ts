import {Period} from '../../components/dates/dateModels';
import {dateRange, momentWithTimeZone, toApiParameters, toFriendlyIso8601} from '../dateHelpers';

describe('dateHelper', () => {

  const pattern = 'YYYY-MM-DD HH:mm:ss';

  describe('relative time periods', () => {
    it('defaults to no limits if no start/end time is given', () => {
      const date = momentWithTimeZone('2018-03-23 11:00:00').toDate();
      const timePeriod = dateRange(date, Period.latest);

      expect(momentWithTimeZone(timePeriod.start).format(pattern)).toEqual('2018-03-22 00:00:00');
      expect(momentWithTimeZone(timePeriod.end).format(pattern)).toEqual('2018-03-22 23:59:59');
      expect(toApiParameters(timePeriod)).toEqual(['after=2018-03-21T23%3A00%3A00.000Z',
                                                   'before=2018-03-22T22%3A59%3A59.999Z']);
    });
  });

  describe('user friendliness', () => {

    it('can be expressed in a friendly looking way', () => {
      const endOfNovember = momentWithTimeZone('2013-11-24 11:00:00').toDate();
      const timePeriod = toFriendlyIso8601(dateRange(endOfNovember, Period.currentMonth));
      expect(timePeriod).toEqual('2013-11-01 - 2013-11-30');
    });

    it('can be consumed by the MVP REST API', () => {
      const apiFriendlyOutput = toApiParameters(dateRange(
        momentWithTimeZone('2012-02-04').toDate(),
        Period.currentMonth,
      ));
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
        const {start, end} = dateRange(now, Period.previousMonth);
        const previousMonthStart = momentWithTimeZone(start).format('MM');
        expect(previousMonthStart).toEqual('02');
        const previousMonthEnd = momentWithTimeZone(end).format('MM');
        expect(previousMonthEnd).toEqual('02');
      });

      it('knows about previous 7 days', () => {
        const march14 = momentWithTimeZone('2013-03-14').toDate();
        const aWeekEarlier = momentWithTimeZone('2013-03-08').toDate();
        const {start, end} = dateRange(march14, Period.previous7Days);
        expect(start).toEqual(aWeekEarlier);
        expect(end).toEqual(march14);
      });

      it('knows about current week', () => {
        const friday10thNovember = momentWithTimeZone('2017-11-10').toDate();
        const {start} = dateRange(friday10thNovember, Period.currentWeek);

        const monday = momentWithTimeZone('2017-11-06').toDate();
        expect(start.valueOf()).toBeLessThanOrEqual(monday.valueOf());

        const previousSunday = momentWithTimeZone('2017-11-05').toDate();
        expect(start.valueOf()).toBeGreaterThanOrEqual(previousSunday.valueOf());
      });

      it('knows about current month', () => {
        const date = momentWithTimeZone('2017-11-23').toDate();
        const {start, end} = dateRange(date, Period.currentMonth);
        expect(momentWithTimeZone(start).format('MM')).toEqual('11');
        expect(momentWithTimeZone(start).format('DD')).toEqual('01');
        expect(momentWithTimeZone(end).format('MM')).toEqual('11');
        expect(momentWithTimeZone(end).format('DD')).toEqual('30');
      });

      it('knows about last 24 h', () => {
        const {start, end} = dateRange(momentWithTimeZone('2013-03-13').toDate(), Period.latest);
        expect(momentWithTimeZone(start).format(pattern)).toEqual('2013-03-12 00:00:00');
        expect(momentWithTimeZone(end).format(pattern)).toEqual('2013-03-12 23:59:59');
      });
    });
  });
});
