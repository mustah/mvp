import {Period} from '../../../components/dates/dateModels';
import {momentAtUtcPlusOneFrom, newDateRange} from '../../../helpers/dateHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {EventLogType} from '../../../state/domain-models-paginated/meter/meterModels';
import {eventsDataFormatter} from '../../../state/domain-models-paginated/meter/meterSchema';
import {
  ExistingReadings,
  MeasurementsByQuantity,
  Quantity,
  Readings
} from '../../../state/ui/graph/measurement/measurementModels';
import {UnixTimestamp} from '../../../types/Types';
import {fillMissingMeasurements} from '../dialogHelper';

describe('dialogHelper', () => {

  describe('eventsDataFormatter', () => {

    const meterResponse = {
      facility: '1234',
      medium: '',
      manufacturer: '',
      gatewaySerial: '',
      id: '12032010',
      location: {
        address: 'Stockholmsv 33',
        city: 'Perstorp',
        country: 'sverige',
        position: {longitude: 14.205929, latitude: 59.666749},
      },
      eventLog: [
        {
          start: '2017-11-22 12:34',
          name: '234523',
          type: EventLogType.newMeter,
        },
        {
          start: '2017-11-22 11:34',
          name: 'Fel',
          type: EventLogType.statusChange,
        },
        {
          start: '2017-11-22 10:34',
          name: 'OK',
          type: EventLogType.statusChange,
        },
        {
          start: '2017-11-22 09:34',
          name: 'OK',
          type: EventLogType.statusChange,
        },
      ],
      statusChanged: '2017-11-05 23:00',
      gateway: {
        serial: '',
        productModel: '',
        id: '',
        status: {id: 'OK', name: 'OK'},
      },
      organisationId: '',
    };

    it('normalizes and uses only statusChangelog property', () => {
      expect(eventsDataFormatter(meterResponse)).toEqual({
        entities: {
          '2017-11-22 12:34_newMeter': {
            name: '234523',
            start: '2017-11-22 12:34',
            type: EventLogType.newMeter,
          },
          '2017-11-22 10:34_statusChange': {
            name: 'OK',
            start: '2017-11-22 10:34',
            type: EventLogType.statusChange,
          },
          '2017-11-22 09:34_statusChange': {
            name: 'OK',
            start: '2017-11-22 09:34',
            type: EventLogType.statusChange,
          },
          '2017-11-22 11:34_statusChange': {
            name: 'Fel',
            start: '2017-11-22 11:34',
            type: EventLogType.statusChange,
          }
        },
        result: [
          '2017-11-22 12:34_newMeter',
          '2017-11-22 11:34_statusChange',
          '2017-11-22 10:34_statusChange',
          '2017-11-22 09:34_statusChange',
        ]
      });
    });
  });

  describe('fillMissingMeasurements', () => {

    const readIntervalMinutes = 60;
    const oneHourInSeconds: number = 60 * 60;

    const start = momentAtUtcPlusOneFrom('2018-01-01T00:00:00Z').toDate();
    const dateRange = newDateRange(Period.yesterday, Maybe.nothing(), start);
    const startHour: UnixTimestamp = dateRange.start.valueOf() / 1000;

    const missingReadouts24h = {
      1_514_674_800: {id: 1_514_674_800},
      1_514_678_400: {id: 1_514_678_400},
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
