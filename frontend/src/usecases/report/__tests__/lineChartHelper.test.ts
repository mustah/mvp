import {makeThreshold} from '../../../__tests__/testDataFactory';
import {colors} from '../../../app/colors';
import {initTranslations} from '../../../i18n/__tests__/i18nMock';
import {toReferenceLineProps} from '../helpers/lineChartHelper';

describe('lineChartHelper', () => {

  initTranslations({
    code: 'en',
    translation: {
      test: 'no translations will default to key',
    },
  });

  describe('toReferenceLineProps', () => {

    it('has no props when there is no threshold', () => {
      expect(toReferenceLineProps({}, undefined)).toBeUndefined();
    });

    it('has left axis props', () => {
      expect(toReferenceLineProps({left: 'abc'}, makeThreshold())).toEqual({
        label: 'Threshold 3 kW',
        stroke: colors.blueGrey300,
        strokeWidth: 2,
        y: '3',
        yAxisId: 'left'
      });
    });

    it('has right axis props', () => {
      expect(toReferenceLineProps({right: '123'}, makeThreshold())).toEqual({
        label: 'Threshold 3 kW',
        stroke: colors.blueGrey300,
        strokeWidth: 2,
        y: '3',
        yAxisId: 'right'
      });
    });

  });
});
