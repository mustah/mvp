import {Period} from '../../components/dates/dateModels';
import {dateRange, toApiParameters, toFriendlyIso8601} from '../dateHelpers';
import moment = require('moment');

describe('periodSelection', () => {

  describe('relative time periods', () => {
    it('defaults to no limits if no start/end time is given', () => {
      const date = new Date(2018, 3, 23, 10, 33, 12);
      const timePeriod = dateRange(date, Period.latest);
      const pattern = 'YYYY-MM-DD HH:MM:ss';
      expect(moment(timePeriod.start, pattern).format()).toEqual('2018-04-22T00:00:00+02:00');
      expect(moment(timePeriod.end, pattern).format()).toEqual('2018-04-22T23:59:59+02:00');
      expect(toApiParameters(timePeriod)).toEqual(['after=2018-04-21T22%3A00%3A00.000Z',
                                                   'before=2018-04-22T21%3A59%3A59.999Z']);
    });
  });

  describe('user friendliness', () => {
    it('can be expressed in a friendly looking way', () => {
      const endOfNovember = new Date(2013, 10, 24);
      const timePeriod = toFriendlyIso8601(dateRange(endOfNovember, Period.currentMonth));
      expect(timePeriod).toEqual('2013-11-01 - 2013-11-30');
    });

    it('can be consumed by the MVP REST API', () => {
      const apiFriendlyOutput = toApiParameters(dateRange(
        new Date(2012, 3, 4),
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
        const now = new Date(2013, 2, 4);
        const {start} = dateRange(now, Period.previousMonth);
        expect(start.getMonth()).toEqual(1);
      });

      it('knows about previous 7 days', () => {
        const march14 = new Date(2013, 2, 14);
        const aWeekEarlier = new Date(2013, 2, 8);
        const {start} = dateRange(march14, Period.previous7Days);
        expect(start).toEqual(aWeekEarlier);
      });

      it('knows about current week', () => {
        const friday10thNovember = new Date(2017, 10, 10);
        const {start} = dateRange(friday10thNovember, Period.currentWeek);

        const monday = new Date(2017, 10, 6);
        expect(start.valueOf()).toBeLessThanOrEqual(monday.valueOf());

        const previousSunday = new Date(2017, 10, 5);
        expect(start.valueOf()).toBeGreaterThanOrEqual(previousSunday.valueOf());
      });

      it('knows about current month', () => {
        const firstOfMonth = new Date(2013, 2, 13);
        const {start, end} = dateRange(firstOfMonth, Period.currentMonth);
        expect(start.getMonth()).toEqual(2);
        expect(start.getDate()).toEqual(1);

        expect(end.getMonth()).toEqual(2);
        expect(end.getDate()).toEqual(31);
      });

      it('knows about latest', () => {
        const {start, end} = dateRange(new Date(2013, 2, 13), Period.latest);
        expect(moment(start, 'YYYY-MM-DD HH:MM:ss').format()).toEqual('2013-03-12T00:00:00+01:00');
        expect(moment(end, 'YYYY-MM-DD HH:MM:ss').format()).toEqual('2013-03-12T23:59:59+01:00');
      });
    });
  });
});
