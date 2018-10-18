import {Gateway} from '../../../state/domain-models-paginated/gateway/gatewayModels';
import {statusChangelogDataFormatter} from '../../../state/domain-models-paginated/gateway/gatewaySchema';
import {NormalizedPaginated} from '../../../state/domain-models-paginated/paginatedDomainModels';
import {Measurement, Medium, Quantity, Reading} from '../../../state/ui/graph/measurement/measurementModels';
import {measurementDataFormatter} from '../../../state/ui/graph/measurement/measurementSchema';
import {groupMeasurementsByDate, MeasurementTableData} from '../dialogHelper';

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
        readings: new Map<number, Reading>(),
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
      const readings = new Map<number, Reading>();
      readings.set(1538114400, {
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
      });
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

});
