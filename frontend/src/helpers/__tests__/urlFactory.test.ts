import {Period} from '../../components/dates/dateModels';
import {Pagination} from '../../state/ui/pagination/paginationModels';
import {SelectedParameters} from '../../state/user-selection/userSelectionModels';
import {IdNamed, Status, toIdNamed} from '../../types/Types';
import {momentWithTimeZone, toPeriodApiParameters} from '../dateHelpers';
import {Maybe} from '../Maybe';
import {
  encodedUriParametersFrom,
  toEntityApiParametersGateways,
  toEntityApiParametersMeters,
  toPaginationApiParameters,
} from '../urlFactory';

describe('urlFactory', () => {

  const selectedParameters = (parameters: Partial<SelectedParameters>): SelectedParameters =>
    parameters as SelectedParameters;

  const cities: IdNamed[] = [toIdNamed('got'), toIdNamed('sto'), toIdNamed('mmx')];

  describe('toEntityApiParameters', () => {
    it('returns empty parameters string when nothing is selected', () => {
      const selection = selectedParameters({cities: []});

      expect(toEntityApiParametersMeters(selection))
        .toEqual([]);
      expect(toEntityApiParametersGateways(selection))
        .toEqual([]);
    });

    it('returns selected city', () => {
      const selection = selectedParameters({cities: [toIdNamed('got')]});

      expect(toEntityApiParametersMeters(selection)).toEqual(['city=got']);
      expect(toEntityApiParametersGateways(selection)).toEqual(['city=got']);
    });

    it('returns selected cities', () => {
      const selection = selectedParameters({cities});

      expect(toEntityApiParametersMeters(selection))
        .toEqual(['city=got', 'city=sto', 'city=mmx']);
      expect(toEntityApiParametersGateways(selection))
        .toEqual(['city=got', 'city=sto', 'city=mmx']);
    });

    it('returns selected address', () => {
      const selection = selectedParameters({addresses: [toIdNamed('address 2')]});

      expect(toEntityApiParametersMeters(selection))
        .toEqual(['address=address%202']);
      expect(toEntityApiParametersGateways(selection))
        .toEqual(['address=address%202']);
    });

    it('returns selected addresses', () => {
      const selection = selectedParameters({
        addresses: [toIdNamed('address 2'), toIdNamed('storgatan 5')],
      });

      expect(toEntityApiParametersMeters(selection))
        .toEqual(['address=address%202', 'address=storgatan%205']);
      expect(toEntityApiParametersGateways(selection))
        .toEqual(['address=address%202', 'address=storgatan%205']);
    });

    it('returns selected statuses', () => {
      const selection = selectedParameters({
        meterStatuses: [toIdNamed(Status.ok), toIdNamed(Status.warning)],
        gatewayStatuses: [toIdNamed(Status.ok)],
      });

      expect(toEntityApiParametersMeters(selection))
        .toEqual(['status=ok', 'status=warning', 'gatewayStatus=ok']);

      expect(toEntityApiParametersGateways(selection))
        .toEqual(['meterStatus=ok', 'meterStatus=warning', 'status=ok']);
    });
  });

  it('returns all selected parameters', () => {
    const selection = selectedParameters({
      addresses: [toIdNamed('address 2'), toIdNamed('storgatan 5')],
      cities,
      meterStatuses: [toIdNamed(Status.ok), toIdNamed(Status.warning)],
      gatewayStatuses: [toIdNamed(Status.ok)],
    });

    expect(toEntityApiParametersMeters(selection))
      .toEqual([
        'address=address%202',
        'address=storgatan%205',
        'city=got', 'city=sto',
        'city=mmx',
        'status=ok',
        'status=warning',
        'gatewayStatus=ok',
      ]);
    expect(toEntityApiParametersGateways(selection))
      .toEqual([
        'address=address%202',
        'address=storgatan%205',
        'city=got', 'city=sto',
        'city=mmx',
        'meterStatus=ok',
        'meterStatus=warning',
        'status=ok',
      ]);
  });

  describe('toPeriodApiParameters', () => {
    const now: Date = momentWithTimeZone('2018-02-02T00:00:00Z').toDate();

    it('know about the last 24h', () => {
      expect(toPeriodApiParameters({
        now,
        period: Period.latest,
        customDateRange: Maybe.nothing(),
      })).toEqual(['after=2018-02-01T00%3A00%3A00.000Z', 'before=2018-02-02T00%3A00%3A00.000Z']);
    });

    it('knows about previous month', () => {
      const prevMonthRange = toPeriodApiParameters({
        now,
        period: Period.previousMonth,
        customDateRange: Maybe.nothing(),
      });
      expect(prevMonthRange).toEqual(['after=2018-01-01T00%3A00%3A00.000Z', 'before=2018-02-01T00%3A00%3A00.000Z']);
    });

    it('knows about previous 7 days', () => {
      const prevWeek = toPeriodApiParameters({
        now,
        period: Period.previous7Days,
        customDateRange: Maybe.nothing(),
      });
      expect(prevWeek).toEqual(['after=2018-01-27T00%3A00%3A00.000Z', 'before=2018-02-02T00%3A00%3A00.001Z']);
    });

    it('knows about current week', () => {
      const currentWeekApiParameters = toPeriodApiParameters({
        now,
        period: Period.currentWeek,
        customDateRange: Maybe.nothing(),
      });

      expect(currentWeekApiParameters).toEqual([
        'after=2018-01-29T00%3A00%3A00.000Z',
        'before=2018-02-05T00%3A00%3A00.000Z',
      ]);
    });

    it('knows about current month', () => {
      const currentMonthApiParameters = toPeriodApiParameters({
        now,
        period: Period.currentMonth,
        customDateRange: Maybe.nothing(),
      });
      expect(currentMonthApiParameters).toEqual([
        'after=2018-02-01T00%3A00%3A00.000Z',
        'before=2018-03-01T00%3A00%3A00.000Z',
      ]);
    });

    it('knows about a custom period', () => {
      const start: Date = momentWithTimeZone('2018-02-02T00:00:00Z').toDate();
      const end: Date = momentWithTimeZone('2018-02-10T00:00:00Z').toDate();

      const customPeriodApiParameters = toPeriodApiParameters({
        now,
        period: Period.custom,
        customDateRange: Maybe.just({start, end}),
      });
      expect(customPeriodApiParameters).toEqual([
        'after=2018-02-02T00%3A00%3A00.000Z',
        'before=2018-02-11T00%3A00%3A00.000Z',
      ]);
    });

  });

  describe('toPaginationApiParameters', () => {
    const pagination: Pagination = {
      page: 0,
      totalElements: 1000,
      totalPages: 100,
      size: 10,
    };

    it('calculates pagination api parameters', () => {
      expect(toPaginationApiParameters(pagination)).toEqual(['size=10', 'page=0']);
    });
  });

  describe('encodedUriParametersFrom', () => {
    it('concat Uri with out pagination', () => {
      const entityApiParameters = ['city=...', 'address=...'];
      const periodApiParameters = ['after=...', 'before=...'];

      expect(encodedUriParametersFrom([...entityApiParameters, ...periodApiParameters]))
        .toEqual('city=...&address=...&after=...&before=...');
    });
    it('concat Uri with pagination', () => {
      const periodApiParameters = ['after=...', 'before=...'];
      const entityApiParameters = ['city=...', 'address=...'];
      const paginationApiParameters = ['size=...', 'page=...'];

      expect(encodedUriParametersFrom([...entityApiParameters, ...periodApiParameters, ...paginationApiParameters]))
        .toEqual('city=...&address=...&after=...&before=...&size=...&page=...');
    });
  });
});
