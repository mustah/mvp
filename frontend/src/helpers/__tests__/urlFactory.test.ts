import {Period} from '../../components/dates/dateModels';
import {Quantity} from '../../state/ui/graph/measurement/measurementModels';
import {Pagination} from '../../state/ui/pagination/paginationModels';
import {RelationalOperator, SelectedParameters} from '../../state/user-selection/userSelectionModels';
import {EncodedUriParameters, IdNamed, toIdNamed} from '../../types/Types';
import {momentAtUtcPlusOneFrom, toPeriodApiParameters} from '../dateHelpers';
import {idGenerator} from '../idGenerator';
import {Maybe} from '../Maybe';
import {
  RequestParameters,
  encodeRequestParameters,
  encodedUriParametersFrom,
  requestParametersFrom,
  entityApiParametersGatewaysFactory,
  entityApiParametersMetersFactory,
  toGatewayIdsApiParameters,
  toMeterIdsApiParameters,
  toPaginationApiParameters,
} from '../urlFactory';

describe('urlFactory', () => {

  type SelectedParams = SelectedParameters & {somethingElse: IdNamed[]};

  const selectedParameters = (parameters: Partial<SelectedParams>): SelectedParams => parameters as SelectedParams;

  const cities: IdNamed[] = [toIdNamed('got'), toIdNamed('sto'), toIdNamed('mmx')];

  describe('requestParametersFrom', () => {

    it('transforms selected parameters url parameters', () => {
      const facilityId: string = idGenerator.uuid().toString();
      const selectedParameters: SelectedParameters = {
        dateRange: {
          period: Period.latest,
        },
        threshold: {
          quantity: Quantity.power,
          unit: 'kW',
          value: '3',
          relationalOperator: '<' as RelationalOperator,
        },
        media: [toIdNamed('District heating')],
        facilities: [toIdNamed(facilityId)]
      };

      const actualUrlParameters: RequestParameters = requestParametersFrom(selectedParameters);

      expect(actualUrlParameters).toHaveProperty('threshold', 'Power < 3 kW');
      expect(actualUrlParameters).toHaveProperty('medium', ['District heating']);
      expect(actualUrlParameters).toHaveProperty('facility', [facilityId]);
    });

    it('does not include parameters that does not have values', () => {
      const selectedParameters: SelectedParameters = {
        dateRange: {
          period: Period.latest,
        },
        facilities: []
      };

      const actualUrlParameters: RequestParameters = requestParametersFrom(selectedParameters);

      expect(actualUrlParameters).not.toHaveProperty('facility');
    });

  });

  describe('encodeRequestParameters', () => {

    it('encodes map to encoded key-value string', () => {
      const nowInApiFormat = momentAtUtcPlusOneFrom().format(`YYYY-MM-DDTHH:mm:ss.sss+01:00`);
      const facilityId: string = idGenerator.uuid().toString();

      const after = nowInApiFormat;
      const before = nowInApiFormat;
      const threshold = 'Power < 3 kW';
      const medium = ['District heating'];
      const facility = [facilityId];
      const queryParameters = {
        after,
        before,
        threshold,
        medium,
        facility,
      };

      const expected: EncodedUriParameters =
        `after=${encodeURIComponent(after)}&before=${encodeURIComponent(before)}` +
        `&threshold=${encodeURIComponent(threshold)}&medium=${encodeURIComponent(medium[0])}` +
        `&facility=${facilityId}`;

      const actualQueryString: EncodedUriParameters = encodeRequestParameters(queryParameters);
      expect(actualQueryString).toEqual(expected);
    });

  });

  describe('toEntityApiParameters', () => {
    it('returns empty parameters string when nothing is selected', () => {
      const selection = selectedParameters({cities: []});

      expect(entityApiParametersMetersFactory(selection))
        .toEqual([]);
      expect(entityApiParametersGatewaysFactory(selection))
        .toEqual([]);
    });

    it('returns selected city', () => {
      const selection = selectedParameters({cities: [toIdNamed('got')]});

      expect(entityApiParametersMetersFactory(selection)).toEqual(['city=got']);
      expect(entityApiParametersGatewaysFactory(selection)).toEqual(['city=got']);
    });

    it('returns selected cities', () => {
      const selection = selectedParameters({cities});

      expect(entityApiParametersMetersFactory(selection))
        .toEqual(['city=got', 'city=sto', 'city=mmx']);
      expect(entityApiParametersGatewaysFactory(selection))
        .toEqual(['city=got', 'city=sto', 'city=mmx']);
    });

    it('returns selected address', () => {
      const selection = selectedParameters({addresses: [toIdNamed('address 2')]});

      expect(entityApiParametersMetersFactory(selection))
        .toEqual(['address=address%202']);
      expect(entityApiParametersGatewaysFactory(selection))
        .toEqual(['address=address%202']);
    });

    it('returns selected addresses', () => {
      const selection = selectedParameters({
        addresses: [toIdNamed('address 2'), toIdNamed('storgatan 5')],
      });

      expect(entityApiParametersMetersFactory(selection))
        .toEqual(['address=address%202', 'address=storgatan%205']);
      expect(entityApiParametersGatewaysFactory(selection))
        .toEqual(['address=address%202', 'address=storgatan%205']);
    });

    it('filters out unknown parameter names', () => {
      const selection: SelectedParams = selectedParameters({
        facilities: [toIdNamed('123')],
        somethingElse: [toIdNamed('ok')],
      });

      expect(entityApiParametersGatewaysFactory(selection))
        .toEqual(['facility=123']);
    });
  });

  describe('toMeterIdsApiParameters', () => {

    it('maps single id', () => {
      expect(toMeterIdsApiParameters([1])).toEqual('id=1');
    });

    it('maps no id', () => {
      expect(toMeterIdsApiParameters([])).toBe('');
    });

    it('maps several ids', () => {
      expect(toMeterIdsApiParameters([1, 3])).toBe('id=1&id=3');
    });
  });

  describe('toGatewayIdsApiParameters', () => {

    it('creates parameter with the gateway id', () => {
      const meterIds = [1];
      expect(toGatewayIdsApiParameters(meterIds, 12)).toEqual('gatewayId=12');
    });

    it('ignores meter ids now', () => {
      const meterIds = [1, 3];
      expect(toGatewayIdsApiParameters(meterIds, 32)).toBe('gatewayId=32');
    });
  });

  it('returns all selected parameters', () => {
    const selection = selectedParameters({
      addresses: [toIdNamed('address 2'), toIdNamed('storgatan 5')],
      cities,
      reported: [toIdNamed('true')],
    });

    const space: string = '%20';

    const expectedParameters = [
      `address=address${space}2`,
      `address=storgatan${space}5`,
      'city=got', 'city=sto',
      'city=mmx',
      'reported=true',
    ];
    expect(entityApiParametersMetersFactory(selection)).toEqual(expectedParameters);
    expect(entityApiParametersGatewaysFactory(selection)).toEqual(expectedParameters);
  });

  describe('toPeriodApiParameters', () => {
    const now: Date = momentAtUtcPlusOneFrom('2018-02-02T00:00:00Z').toDate();

    it('know about the last 24h', () => {
      expect(toPeriodApiParameters({
        start: now,
        period: Period.latest,
        customDateRange: Maybe.nothing(),
      })).toEqual([
        'after=2018-02-01T00%3A00%3A00.000%2B01%3A00',
        'before=2018-02-02T00%3A00%3A00.000%2B01%3A00',
      ]);
    });

    it('knows about previous month', () => {
      const prevMonthRange = toPeriodApiParameters({
        start: now,
        period: Period.previousMonth,
        customDateRange: Maybe.nothing(),
      });
      expect(prevMonthRange).toEqual([
        'after=2018-01-01T00%3A00%3A00.000%2B01%3A00',
        'before=2018-01-31T00%3A00%3A00.000%2B01%3A00',
      ]);
    });

    it('knows about previous 7 days', () => {
      const prevWeek = toPeriodApiParameters({
        start: now,
        period: Period.previous7Days,
        customDateRange: Maybe.nothing(),
      });
      expect(prevWeek).toEqual([
        'after=2018-01-27T00%3A00%3A00.000%2B01%3A00',
        'before=2018-02-02T00%3A00%3A00.000%2B01%3A00',
      ]);
    });

    it('knows about current week', () => {
      const currentWeekApiParameters = toPeriodApiParameters({
        start: now,
        period: Period.currentWeek,
        customDateRange: Maybe.nothing(),
      });

      expect(currentWeekApiParameters).toEqual([
        'after=2018-01-29T00%3A00%3A00.000%2B01%3A00',
        'before=2018-02-05T00%3A00%3A00.000%2B01%3A00',
      ]);
    });

    it('knows about current month', () => {
      const currentMonthApiParameters = toPeriodApiParameters({
        start: now,
        period: Period.currentMonth,
        customDateRange: Maybe.nothing(),
      });
      expect(currentMonthApiParameters).toEqual([
        'after=2018-02-01T00%3A00%3A00.000%2B01%3A00',
        'before=2018-02-28T00%3A00%3A00.000%2B01%3A00',
      ]);
    });

    it('knows about a custom period', () => {
      const start: Date = momentAtUtcPlusOneFrom('2018-02-02T00:00:00Z').toDate();
      const end: Date = momentAtUtcPlusOneFrom('2018-02-10T00:00:00Z').toDate();

      const customPeriodApiParameters = toPeriodApiParameters({
        start: now,
        period: Period.custom,
        customDateRange: Maybe.just({start, end}),
      });
      expect(customPeriodApiParameters).toEqual([
        'after=2018-02-02T00%3A00%3A00.000%2B01%3A00',
        'before=2018-02-11T00%3A00%3A00.000%2B01%3A00',
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
      const entityApiParameters = ['city=sto', 'address=street'];
      const periodApiParameters = ['after=today', 'before=now'];

      expect(encodedUriParametersFrom([...entityApiParameters, ...periodApiParameters]))
        .toEqual('city=sto&address=street&after=today&before=now');
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
