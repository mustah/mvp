import {Gateway} from '../../../state/domain-models-paginated/gateway/gatewayModels';
import {statusChangelogDataFormatter} from '../../../state/domain-models-paginated/gateway/gatewaySchema';
import {NormalizedPaginated} from '../../../state/domain-models-paginated/paginatedDomainModels';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {MeterDetails} from '../../../state/domain-models/meter-details/meterDetailsModels';
import {
  allQuantities,
  Measurement,
  Medium,
  Quantity,
  Reading
} from '../../../state/ui/graph/measurement/measurementModels';
import {measurementDataFormatter} from '../../../state/ui/graph/measurement/measurementSchema';
import {groupMeasurementsByDate, MeasurementTableData, meterMeasurementsForTable} from '../dialogHelper';
import {RenderableMeasurement} from '../MeterDetailsTabs';

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

  describe('meterMeasurementsForTable', () => {

    it('adds missing quantities to meter\'s measurements', () => {
      const meter: MeterDetails = {
        id: '2a162298-55cd-414e-8e46-156f9ad9b32f',
        facility: '426',
        location: {
          address: 'Bäckavägen 2 A',
          city: 'Perstorp',
          country: 'sverige',
          position: {
            latitude: 56.13955,
            longitude: 13.39741,
          },
        },
        medium: 'District heating',
        manufacturer: 'ELV',
        statusChanged: '2018-04-04 12:05:23',
        statusChangelog: [
          {
            id: 1,
            name: 'ok',
            start: '2018-04-04 12:05:23',
          },
          {
            id: 2,
            name: 'warning',
            start: '2018-04-04 12:05:23',
          },
        ],
        collectionPercentage: 313.04347826086956,
        measurements: [
          {
            id: 1,
            quantity: Quantity.forwardTemperature,
            value: 309.14353148037117,
            unit: 'K',
            created: 1523016000,
          },
          {
            id: 2,
            quantity: Quantity.returnTemperature,
            value: 306.59347679223913,
            unit: 'K',
            created: 1523016000,
          },
          {
            id: 3,
            quantity: Quantity.differenceTemperature,
            value: 2.550054688132022,
            unit: 'K',
            created: 1523016000,
          },
        ],
        readIntervalMinutes: 60,
        gateway: {
          id: '29836b65-4682-4526-90b2-9d9b7a31f45c',
          productModel: 'CMi2110',
          serial: '12031925',
          status: {
            id: 'unknown',
            name: 'unknown',
          },
        },
        gatewaySerial: '29836b65-4682-4526-90b2-9d9b7a31f45c',
        organisationId: '',
      };

      const {entities, result}: DomainModel<RenderableMeasurement> = meterMeasurementsForTable(meter);

      const quantities = Object.keys(entities).sort();
      const expected = [...allQuantities[Medium.districtHeating]].sort();
      expect(quantities).toEqual(expected);

      expect(result).toEqual(allQuantities[Medium.districtHeating]);
    });

    it('handles meters without any measurements', () => {
      const meter: MeterDetails = {
        id: '2a162298-55cd-414e-8e46-156f9ad9b32f',
        facility: '426',
        location: {
          country: 'sverige',
          city: 'Perstorp',
          address: 'Bäckavägen 2 A',
          position: {
            latitude: 56.13955,
            longitude: 13.39741,
          },
        },
        medium: 'District heating',
        manufacturer: 'ELV',
        statusChanged: '2018-04-04 12:05:23',
        statusChangelog: [
          {
            id: 1,
            name: 'ok',
            start: '2018-04-04 12:05:23',
          },
          {
            id: 2,
            name: 'warning',
            start: '2018-04-04 12:05:23',
          },
        ],
        collectionPercentage: 313.04347826086956,
        measurements: [],
        readIntervalMinutes: 60,
        isReported: false,
        gateway: {
          id: '29836b65-4682-4526-90b2-9d9b7a31f45c',
          productModel: 'CMi2110',
          serial: '12031925',
          status: {
            id: 'unknown',
            name: 'unknown',
          },
        },
        gatewaySerial: '29836b65-4682-4526-90b2-9d9b7a31f45c',
        organisationId: '',
      };

      const {entities, result}: DomainModel<RenderableMeasurement> = meterMeasurementsForTable(meter);

      const quantities = Object.keys(entities).sort();
      const expected = [...allQuantities[Medium.districtHeating]].sort();
      expect(quantities).toEqual(expected);

      expect(result).toEqual(allQuantities[Medium.districtHeating]);
    });

    it('orders measurements by quantity, in a custom order', () => {
      const meterWithMeasurementsInDifferentOrder: MeterDetails = {
        id: '2a162298-55cd-414e-8e46-156f9ad9b32f',
        facility: '426',
        location: {
          city: 'Perstorp',
          address: 'Bäckavägen 2 A',
          country: 'sverige',
          position: {
            latitude: 56.13955,
            longitude: 13.39741,
          },
        },
        medium: 'District heating',
        manufacturer: 'ELV',
        statusChanged: '2018-04-04 12:05:23',
        statusChangelog: [
          {
            id: 1,
            name: 'ok',
            start: '2018-04-04 12:05:23',
          },
          {
            id: 2,
            name: 'warning',
            start: '2018-04-04 12:05:23',
          },
        ],
        collectionPercentage: 313.04347826086956,
        measurements: [
          {
            id: 1,
            quantity: Quantity.returnTemperature,
            value: 309.14353148037117,
            unit: 'K',
            created: 1523016000,
          },
          {
            id: 2,
            quantity: Quantity.differenceTemperature,
            value: 306.59347679223913,
            unit: 'K',
            created: 1523016000,
          },
          {
            id: 3,
            quantity: Quantity.forwardTemperature,
            value: 2.550054688132022,
            unit: 'K',
            created: 1523016000,
          },
          {
            id: 4,
            quantity: Quantity.volume,
            value: 2.550054688132022,
            unit: 'K',
            created: 1523016000,
          },
          {
            id: 5,
            quantity: Quantity.flow,
            value: 2.550054688132022,
            unit: 'K',
            created: 1523016000,
          },
          {
            id: 6,
            quantity: Quantity.power,
            value: 2.550054688132022,
            unit: 'K',
            created: 1523016000,
          },
          {
            id: 7,
            quantity: Quantity.energy,
            value: 2.550054688132022,
            unit: 'K',
            created: 1523016000,
          },
        ],
        readIntervalMinutes: 60,
        gateway: {
          id: '29836b65-4682-4526-90b2-9d9b7a31f45c',
          productModel: 'CMi2110',
          serial: '12031925',
          status: {
            id: 'unknown',
            name: 'unknown',
          },
        },
        gatewaySerial: '29836b65-4682-4526-90b2-9d9b7a31f45c',
        organisationId: '',
      };

      const {result} = meterMeasurementsForTable(meterWithMeasurementsInDifferentOrder);

      expect(result).toEqual([
        Quantity.energy,
        Quantity.volume,
        Quantity.power,
        Quantity.flow,
        Quantity.forwardTemperature,
        Quantity.returnTemperature,
        Quantity.differenceTemperature,
      ]);
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
