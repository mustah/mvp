import {Period} from '../../../components/dates/dateModels';
import {momentFrom, newDateRange} from '../../../helpers/dateHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {Gateway} from '../../../state/domain-models-paginated/gateway/gatewayModels';
import {statusChangelogDataFormatter} from '../../../state/domain-models-paginated/gateway/gatewaySchema';
import {NormalizedPaginated} from '../../../state/domain-models-paginated/paginatedDomainModels';
import {
  ExistingReadings,
  Measurement,
  MeasurementsByQuantity,
  Medium,
  Quantity,
  Readings
} from '../../../state/ui/graph/measurement/measurementModels';
import {measurementDataFormatter} from '../../../state/ui/graph/measurement/measurementSchema';
import {UnixTimestamp} from '../../../types/Types';
import {fillMissingMeasurements, groupMeasurementsByDate, MeasurementTableData} from '../dialogHelper';

describe('dialogHelper', () => {

  describe('statusChangelogDataFormatter', () => {

    const gateway: Gateway = {
      id: '12032010',
      serial: '005',
      location: {
        address: 'Stockholmsv 33',
        city: 'Perstorp',
        country: 'sverige',
        position: {longitude: 14.205929, latitude: 59.666749},
      },
      productModel: 'CMi2110',
      status: {name: 'OK', id: 0},
      statusChangelog: [
        {
          date: '2017-11-22 09:34',
          status: {id: 0, name: 'OK'},
          id: '967af275-0026-43b9-a0ef-123dfb05612a',
          gatewayId: '12032010',
        }, {
          date: '2017-11-22 10:34',
          status: {id: 0, name: 'OK'},
          id: '6e4daf1f-a611-42e4-8ebf-ed9e10a7b4fb',
          gatewayId: '12032010',
        }, {
          date: '2017-11-22 11:34',
          status: {id: 3, name: 'Fel'},
          id: 'ac359487-0e7b-4ed2-85bb-d0f75e9d7a27',
          gatewayId: '12032010',
        }, {
          date: '2017-11-22 12:34',
          status: {id: 0, name: 'OK'},
          id: '3e4a4295-2d1a-4118-b303-16fbb3ddfa49',
          gatewayId: '12032010',
        }
      ],
      statusChanged: '2017-11-05 23:00',
      meterIds: ['67606228'],
      organisationId: '',
    };

    it('normalizes and uses only statusChangelog property', () => {
      expect(statusChangelogDataFormatter(gateway)).toEqual({
        entities:
          {
            '967af275-0026-43b9-a0ef-123dfb05612a':
              {
                date: '2017-11-22 09:34',
                status: {id: 0, name: 'OK'},
                id: '967af275-0026-43b9-a0ef-123dfb05612a',
                gatewayId: '12032010',
              },
            '6e4daf1f-a611-42e4-8ebf-ed9e10a7b4fb':
              {
                date: '2017-11-22 10:34',
                status: {id: 0, name: 'OK'},
                id: '6e4daf1f-a611-42e4-8ebf-ed9e10a7b4fb',
                gatewayId: '12032010',
              },
            'ac359487-0e7b-4ed2-85bb-d0f75e9d7a27':
              {
                date: '2017-11-22 11:34',
                status: {id: 3, name: 'Fel'},
                id: 'ac359487-0e7b-4ed2-85bb-d0f75e9d7a27',
                gatewayId: '12032010',
              },
            '3e4a4295-2d1a-4118-b303-16fbb3ddfa49':
              {
                date: '2017-11-22 12:34',
                status: {id: 0, name: 'OK'},
                id: '3e4a4295-2d1a-4118-b303-16fbb3ddfa49',
                gatewayId: '12032010',
              },
          },
        result: [
          '967af275-0026-43b9-a0ef-123dfb05612a',
          '6e4daf1f-a611-42e4-8ebf-ed9e10a7b4fb',
          'ac359487-0e7b-4ed2-85bb-d0f75e9d7a27',
          '3e4a4295-2d1a-4118-b303-16fbb3ddfa49',
        ],
      });
    });
  });

  describe('groupMeasurementsByDate', () => {

    it('can handle empty input', () => {
      const normalizedMeasurements: NormalizedPaginated<Measurement> = {
        entities: {},
        result: {
          content: [],
          totalElements: 0,
          totalPages: 0,
        },
        page: 0,
      };

      const actual: MeasurementTableData = groupMeasurementsByDate(normalizedMeasurements, Medium.unknown);
      const expected: MeasurementTableData = {
        readings: {},
        quantities: [],
      };

      expect(actual).toEqual(expected);
    });

    it('gracefully handles measurements not including all quantities', () => {
      const apiResponse = {
        content: [
          {
            id: 'Difference temperature_2018-09-28T06:00:00Z',
            quantity: 'Difference temperature',
            value: 4.71,
            unit: 'K',
            created: 1538114400.000000000,
          },
        ],
        totalElements: 350,
        totalPages: 1,
      };

      const normalizedMeasurements: NormalizedPaginated<Measurement> = measurementDataFormatter(apiResponse);

      const actual: MeasurementTableData = groupMeasurementsByDate(normalizedMeasurements, Medium.districtHeating);
      const readings: ExistingReadings = {
        1538114400: {
          id: 1538114400,
          measurements: {
            ['Difference temperature' as Quantity]: {
              created: 1538114400,
              id: 'Difference temperature_2018-09-28T06:00:00Z',
              quantity: 'Difference temperature',
              unit: 'K',
              value: 4.71,
            },
          },
        }
      };
      const expected: MeasurementTableData = {
        readings,
        quantities: [
          Quantity.differenceTemperature,
        ],
      };

      expect(actual).toEqual(expected);
    });

    it('extracts ordered list of quantities for found measurements', () => {
      const apiResponse = {
        content: [
          {
            id: 'Difference temperature_2018-09-28T06:00:00Z',
            quantity: 'Difference temperature',
            value: 4.71,
            unit: 'K',
            created: 1538114400.000000000,
          },
          {
            id: 'Power_2018-09-28T06:00:00Z',
            quantity: 'Power',
            value: 1200.0,
            unit: 'W',
            created: 1538114400.000000000,
          },
        ],
        totalElements: 350,
        totalPages: 1,
      };

      const normalizedMeasurements: NormalizedPaginated<Measurement> = measurementDataFormatter(apiResponse);

      const {quantities}: MeasurementTableData = groupMeasurementsByDate(
        normalizedMeasurements,
        Medium.districtHeating,
      );

      const orderedQuantities: Quantity[] = [
        Quantity.power,
        Quantity.differenceTemperature,
      ];

      expect(quantities).toEqual(orderedQuantities);
    });

  });

  describe('fillMissingMeasurements', () => {

    const readIntervalMinutes = 60;
    const oneHourInSeconds: number = 60 * 60;

    const start = momentFrom('2018-01-01T00:00:00Z').toDate();
    const dateRange = newDateRange(Period.latest, Maybe.nothing(), start);
    const startHour: UnixTimestamp = dateRange.start.valueOf() / 1000;

    const missingReadouts24h = {
      1514678400: {id: 1514678400},
      1514682000: {id: 1514682000},
      1514685600: {id: 1514685600},
      1514689200: {id: 1514689200},
      1514692800: {id: 1514692800},
      1514696400: {id: 1514696400},
      1514700000: {id: 1514700000},
      1514703600: {id: 1514703600},
      1514707200: {id: 1514707200},
      1514710800: {id: 1514710800},
      1514714400: {id: 1514714400},
      1514718000: {id: 1514718000},
      1514721600: {id: 1514721600},
      1514725200: {id: 1514725200},
      1514728800: {id: 1514728800},
      1514732400: {id: 1514732400},
      1514736000: {id: 1514736000},
      1514739600: {id: 1514739600},
      1514743200: {id: 1514743200},
      1514746800: {id: 1514746800},
      1514750400: {id: 1514750400},
      1514754000: {id: 1514754000},
      1514757600: {id: 1514757600},
      1514761200: {id: 1514761200}
    };

    const measurement = (timestampAndValue: UnixTimestamp): MeasurementsByQuantity => ({
      [Quantity.power]: {
        id: timestampAndValue,
        created: timestampAndValue,
        value: timestampAndValue,
        quantity: Quantity.power,
        unit: 'W',
      },
    });

    it('fill empty readings to specified amount of lines', () => {
      const existingReadings: ExistingReadings = {};

      const actual: Readings = fillMissingMeasurements({
        existingReadings,
        readIntervalMinutes,
        dateRange,
      });

      const expected: Readings = {...missingReadouts24h};

      expect(actual).toEqual(expected);
    });

    it('adds missing measurements between existing for an hourly meter', () => {
      const existingReadings: ExistingReadings = {};

      existingReadings[startHour] = {id: startHour, measurements: measurement(startHour)};

      const twoHoursLater: UnixTimestamp = startHour + oneHourInSeconds;
      existingReadings[twoHoursLater] = {id: twoHoursLater, measurements: measurement(twoHoursLater)};

      const actual: Readings = fillMissingMeasurements({
        existingReadings,
        readIntervalMinutes,
        dateRange,
      });

      const expected: Readings = {...missingReadouts24h, ...existingReadings};

      expect(actual).toEqual(expected);
    });

    it('fills with missing readout timestamps when no measurements exists for that timestamp', () => {
      const existingReadings: ExistingReadings = {};
      const twoHoursLater: UnixTimestamp = 2 * oneHourInSeconds;
      existingReadings[twoHoursLater] = {id: twoHoursLater, measurements: measurement(twoHoursLater)};

      const actual: Readings = fillMissingMeasurements({
        existingReadings,
        readIntervalMinutes,
        dateRange,
      });

      const expected: Readings = {...missingReadouts24h, ...existingReadings};

      expect(actual).toEqual(expected);
    });

    it('can handle 15m read intervals', () => {
      const existingReadings: ExistingReadings = {};

      const actual: Readings = fillMissingMeasurements({
        existingReadings,
        readIntervalMinutes: 15,
        dateRange,
      });

      expect(Object.keys(actual).length).toEqual(96);
    });

    it('can handle 60m read interval', () => {
      const existingReadings: ExistingReadings = {};

      const actual: Readings = fillMissingMeasurements({
        existingReadings,
        readIntervalMinutes,
        dateRange,
      });

      expect(Object.keys(actual).length).toEqual(24);
    });

    describe('no read interval exists', () => {

      it('returns the input readings', () => {
        const existingReadings: ExistingReadings = {};

        const actual: Readings = fillMissingMeasurements({
          existingReadings,
          dateRange,
        });

        expect(actual).toBe(existingReadings);
      });

      it('keeps inserted map', () => {
        const existingReadings: ExistingReadings = {0: {id: 0, measurements: measurement(0)}};

        const emptyReadings: Readings = fillMissingMeasurements({
          existingReadings,
          dateRange,
        });

        const expected: Readings = {...existingReadings};
        expect(emptyReadings).toEqual(expected);
      });

    });

    describe('readIntervalMinutes === 0', () => {

      it('does not add to empty map', () => {
        const existingReadings: ExistingReadings = {};

        const emptyReadings: Readings = fillMissingMeasurements({
          existingReadings,
          readIntervalMinutes: 0,
          dateRange,
        });

        const expected: Readings = {};
        expect(emptyReadings).toEqual(expected);
      });

      it('keeps inserted map', () => {
        const existingReadings: ExistingReadings = {0: {id: 0, measurements: measurement(0)}};

        const emptyReadings: Readings = fillMissingMeasurements({
          existingReadings,
          readIntervalMinutes: 0,
          dateRange,
        });

        const expected: Readings = {...existingReadings};
        expect(emptyReadings).toEqual(expected);
      });

    });

  });

});
