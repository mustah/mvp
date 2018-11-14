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
      statusChangelog: [{
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
      }],
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

    const ONE_HOUR_IN_SECONDS: number = 60 * 60;

    const now: Date = new Date('01 Jan 2010 00:00:00 UTC');

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
      const receivedData: ExistingReadings = {};

      const emptyReadings: Readings = fillMissingMeasurements({
        numberOfRows: 100,
        receivedData,
        lastDate: now,
        readIntervalMinutes: 60,
      });

      const expected: Readings = {};
      for (let i = 0; i < 100; i++) {
        const timestamp: UnixTimestamp = (now.valueOf() / 1000) - i * ONE_HOUR_IN_SECONDS;
        expected[timestamp] = {id: timestamp};
      }

      expect(emptyReadings).toEqual(expected);
    });

    it('adds missing measurements between existing for an hourly meter', () => {
      const receivedData: ExistingReadings = {};
      receivedData[0] = {
        id: 0,
        measurements: measurement(0),
      };
      const twoHoursLater: number = 2 * ONE_HOUR_IN_SECONDS;
      receivedData[twoHoursLater] = {
        id: twoHoursLater,
        measurements: measurement(twoHoursLater),
      };

      const emptyReadings: Readings = fillMissingMeasurements({
        numberOfRows: 3,
        lastDate: new Date(2 * ONE_HOUR_IN_SECONDS * 1000),
        readIntervalMinutes: 60,
        receivedData,
      });

      const expected: Readings = {...receivedData};
      const oneHourLater: number = ONE_HOUR_IN_SECONDS;
      expected[oneHourLater] = {
        id: oneHourLater,
      };

      expect(emptyReadings).toEqual(expected);
    });

    it('does not add trailing missing measurements, because that data may just not have been asked for', () => {
      const receivedData: ExistingReadings = {};
      const twoHoursLater: number = 2 * ONE_HOUR_IN_SECONDS;
      receivedData[twoHoursLater] = {
        id: twoHoursLater,
        measurements: measurement(twoHoursLater),
      };

      const somethingHigherThanOne = 3;

      const emptyReadings: Readings = fillMissingMeasurements({
        numberOfRows: somethingHigherThanOne,
        lastDate: new Date(2 * ONE_HOUR_IN_SECONDS * 1000),
        readIntervalMinutes: 60,
        receivedData,
      });

      const expected: Readings = {...receivedData};

      expect(emptyReadings).toEqual(expected);
    });

    it('rounds the date to exact intervals', () => {
      const receivedData: ExistingReadings = {};
      receivedData[0] = {
        id: 0,
        measurements: measurement(0),
      };

      const oddSeconds = 34;
      const emptyReadings: Readings = fillMissingMeasurements({
        numberOfRows: 2,
        lastDate: new Date((oddSeconds + ONE_HOUR_IN_SECONDS) * 1000),
        readIntervalMinutes: 60,
        receivedData,
      });

      const expected: Readings = {...receivedData};
      const oneHourLater: number = ONE_HOUR_IN_SECONDS;
      expected[oneHourLater] = {
        id: oneHourLater,
      };

      expect(emptyReadings).toEqual(expected);
    });

    describe('readIntervalMinutes === undefined', () => {

      it('does not add to empty map', () => {
        const receivedData: ExistingReadings = {};

        const emptyReadings: Readings = fillMissingMeasurements({
          numberOfRows: 100,
          receivedData,
          lastDate: now,
        });

        const expected: Readings = {};
        expect(emptyReadings).toEqual(expected);
      });

      it('keeps inserted map', () => {
        const receivedData: ExistingReadings = {
          0: {
            id: 0,
            measurements: measurement(0),
          }
        };

        const emptyReadings: Readings = fillMissingMeasurements({
          numberOfRows: 100,
          receivedData,
          lastDate: now,
        });

        const expected: Readings = {...receivedData};
        expect(emptyReadings).toEqual(expected);
      });

    });

    describe('readIntervalMinutes === 0', () => {

      it('does not add to empty map', () => {
        const receivedData: ExistingReadings = {};

        const emptyReadings: Readings = fillMissingMeasurements({
          numberOfRows: 100,
          receivedData,
          lastDate: now,
          readIntervalMinutes: 0
        });

        const expected: Readings = {};
        expect(emptyReadings).toEqual(expected);
      });

      it('keeps inserted map', () => {
        const receivedData: ExistingReadings = {
          0: {
            id: 0,
            measurements: measurement(0),
          }
        };

        const emptyReadings: Readings = fillMissingMeasurements({
          numberOfRows: 100,
          receivedData,
          lastDate: now,
          readIntervalMinutes: 0
        });

        const expected: Readings = {...receivedData};
        expect(emptyReadings).toEqual(expected);
      });

    });

  });

});
