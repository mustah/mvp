import {NormalizedPaginated} from '../../../../domain-models-paginated/paginatedDomainModels';
import {ExistingReadings, Measurement, Medium, Quantity} from '../measurementModels';
import {measurementDataFormatter} from '../measurementSchema';
import {groupMeasurementsByDate, MeasurementTableData} from '../measurementSelectors';

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
