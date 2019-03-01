import {Medium} from '../../../state/ui/graph/measurement/measurementModels';
import {isAggregate, isMedium, LegendType} from '../reportModels';

describe('reportModels', () => {

  it('is of type Medium', () => {
    const type: LegendType = Medium.gas;

    expect(isMedium(type)).toBe(true);
  });

  it('is not of type Medium', () => {
    const type: LegendType = 'aggregate';

    expect(isMedium(type)).toBe(false);
  });

  it('is of type aggregate', () => {
    const type: LegendType = 'aggregate';

    expect(isAggregate(type)).toBe(true);
  });
});
