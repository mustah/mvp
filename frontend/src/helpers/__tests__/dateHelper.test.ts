import {Maybe} from '../Maybe';
import {Period} from '../../components/dates/dateModels';
import {dateRange, toApiParameters, toFriendlyIso8601} from '../dateHelpers';

describe('periodSelection', () => {
  describe('relative time periods', () => {
    it('defaults to no limits if no start/end time is given', () => {
      const timePeriod = dateRange(new Date(), Period.latest);
      expect(timePeriod.start).toEqual(Maybe.nothing());
      expect(timePeriod.end).toEqual(Maybe.nothing());

      const formattedForApi = toApiParameters(timePeriod);
      expect(formattedForApi).toEqual([]);
    });
  });

  describe('user friendliness', () => {
    it('can be expressed in a friendly looking way', () => {
      const endOfNovember = new Date(2013, 10, 24);
      const timePeriod = toFriendlyIso8601(dateRange(endOfNovember, Period.currentMonth));
      expect(timePeriod).toEqual('2013-11-01 - 2013-11-30');
    });

    it('can be consumed by the MVP REST API', () => {
      const apiFriendlyOutput = toApiParameters(dateRange(new Date(2012, 3, 4), Period.currentMonth));
      expect(apiFriendlyOutput.length).toEqual(2);

      // the API wants ISO-8601 "instant":
      // https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#ISO_INSTANT
      // JS regex lazily stolen from https://stackoverflow.com/a/3143231/49879 which has more variants if needed
      const iso8601Instant = new RegExp(/\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d\.\d+([+-][0-2]\d:[0-5]\d|Z)/);
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
        expect(start.get().getMonth()).toEqual(1);
      });

      it('knows about previous 7 days', () => {
        const march14 = new Date(2013, 2, 14);
        const aWeekEarlier = new Date(2013, 2, 8);
        const {start} = dateRange(march14, Period.previous7Days);
        expect(start.get()).toEqual(aWeekEarlier);
      });

      it('knows about current week', () => {
        const friday10thNovember = new Date(2017, 10, 10);
        const {start} = dateRange(friday10thNovember, Period.currentWeek);

        const monday = new Date(2017, 10, 6);
        expect(start.get().valueOf()).toBeLessThanOrEqual(monday.valueOf());

        const previousSunday = new Date(2017, 10, 5);
        expect(start.get().valueOf()).toBeGreaterThanOrEqual(previousSunday.valueOf());
      });

      it('knows about current month', () => {
        const firstOfMonth = new Date(2013, 2, 13);
        const {start, end} = dateRange(firstOfMonth, Period.currentMonth);
        expect(start.get().getMonth()).toEqual(2);
        expect(start.get().getDate()).toEqual(1);

        expect(end.get().getMonth()).toEqual(2);
        expect(end.get().getDate()).toEqual(31);
      });

      it('knows about latest', () => {
        const {start, end} = dateRange(new Date(), Period.latest);
        expect(start.isNothing()).toBe(true);
        expect(end.isNothing()).toBe(true);
      });
    });
  });
});
