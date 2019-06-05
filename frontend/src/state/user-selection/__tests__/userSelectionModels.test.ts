import {Period} from '../../../components/dates/dateModels';
import {Quantity} from '../../ui/graph/measurement/measurementModels';
import {isValidThreshold, RelationalOperator, ThresholdQuery} from '../userSelectionModels';

describe('userSelectionModels', () => {
  describe('isValidThreshold', () => {
    it('accepts valid threshold query', () => {
      const validThresholdQuery: ThresholdQuery = {
        value: '0',
        dateRange: {period: Period.yesterday},
        quantity: Quantity.differenceTemperature,
        relationalOperator: RelationalOperator.gt,
        unit: '°C'
      };

      expect(isValidThreshold(validThresholdQuery)).toBe(true);
    });

    it('accepts valid threshold query with duration', () => {
      const validThresholdQuery: ThresholdQuery = {
        value: '0',
        dateRange: {period: Period.yesterday},
        quantity: Quantity.differenceTemperature,
        relationalOperator: RelationalOperator.gt,
        duration: '4',
        unit: '°C'
      };

      expect(isValidThreshold(validThresholdQuery)).toBe(true);
    });

    it('rejects undefined threshold query', () => {
      expect(isValidThreshold(undefined)).toBe(false);
    });
  });
});
