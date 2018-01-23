import {labelOfMeasurement} from '../measurementHelpers';
import {Measurement} from '../measurementModels';

describe('measurementHelpers', () => {
  describe('labelOfMeasurement', () => {
    it('can get the label from an entity', () => {
      const entity: Measurement = {
        id: 7,
        value: 3,
        unit: 'mW',
        created: 1516371989132,
        quantity: 'Power',
        physicalMeter: {
          rel: 'self',
          href: 'http://localhost:8080/v1/api/physical-meters/10',
        },
      };
      expect(labelOfMeasurement(entity)).toEqual('10');
    });
  });
});
