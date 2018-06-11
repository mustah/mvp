import {Medium} from '../../../components/indicators/indicatorWidgetModels';
import {initTranslations} from '../../../i18n/__tests__/i18nMock';
import {Gateway} from '../../../state/domain-models-paginated/gateway/gatewayModels';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {
  allQuantities,
  Quantity,
} from '../../../state/ui/graph/measurement/measurementModels';
import {meterMeasurementsForTable, normalizedStatusChangelogFor, titleOf} from '../dialogHelper';
import {RenderableMeasurement} from '../MeterDetailsTabs';
import {MeterDetails} from '../../../state/domain-models/meter-details/meterDetailsModels';

describe('dialogHelper', () => {

  describe('titleOf', () => {

    beforeEach(() => {
      initTranslations({
        code: 'en',
        translation: {
          test: 'no translations will default to key',
        },
      });
    });

    it('prints out default message when no flags are available', () => {
      expect(titleOf([])).toEqual('no');
    });

    it('renders flags with one item', () => {
      expect(titleOf([{title: 'has error'}])).toEqual('has error');
    });

    it('renders all flags and joins them', () => {
      const flags = [{title: 'has error'}, {title: 'ok'}, {title: 'fixed'}];

      expect(titleOf(flags)).toEqual('has error, ok, fixed');
    });

  });

  describe('normalizedStatusChangelogFor', () => {

    const gateway: Gateway = {
      id: '12032010',
      serial: '005',
      location: {
        address: {id: 'Stockholmsv 33', name: 'Stockholmsv 33'},
        city: {id: 'Perstorp', name: 'Perstorp'},
        position: {longitude: 14.205929, latitude: 59.666749},
      },
      flags: [],
      flagged: false,
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
      expect(normalizedStatusChangelogFor(gateway)).toEqual({
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
          city: {
            id: 'Perstorp',
            name: 'Perstorp',
          },
          address: {
            id: 'Bäckavägen 2 A',
            name: 'Bäckavägen 2 A',
          },
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
            name: 'active',
            start: '2018-04-04 12:05:23',
          },
          {
            id: 2,
            name: 'warning',
            start: '2018-04-04 12:05:23',
          },
        ],
        status: {
          id: 'active',
          name: 'active',
        },
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
          city: {
            id: 'Perstorp',
            name: 'Perstorp',
          },
          address: {
            id: 'Bäckavägen 2 A',
            name: 'Bäckavägen 2 A',
          },
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
            name: 'active',
            start: '2018-04-04 12:05:23',
          },
          {
            id: 2,
            name: 'warning',
            start: '2018-04-04 12:05:23',
          },
        ],
        status: {
          id: 'active',
          name: 'active',
        },
        collectionPercentage: 313.04347826086956,
        measurements: [],
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

    it('orders measurements by quantity, in a custom order', () => {
      const meterWithMeasurementsInDifferentOrder: MeterDetails = {
        id: '2a162298-55cd-414e-8e46-156f9ad9b32f',
        facility: '426',
        location: {
          city: {
            id: 'Perstorp',
            name: 'Perstorp',
          },
          address: {
            id: 'Bäckavägen 2 A',
            name: 'Bäckavägen 2 A',
          },
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
            name: 'active',
            start: '2018-04-04 12:05:23',
          },
          {
            id: 2,
            name: 'warning',
            start: '2018-04-04 12:05:23',
          },
        ],
        status: {
          id: 'active',
          name: 'active',
        },
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

});
