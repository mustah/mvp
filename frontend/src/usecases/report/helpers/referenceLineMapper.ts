import {ReferenceLineProps} from 'recharts';
import {colors} from '../../../app/themes';
import {Maybe} from '../../../helpers/Maybe';
import {firstUpperTranslated} from '../../../services/translationService';
import {ThresholdQuery} from '../../../state/user-selection/userSelectionModels';

export const toReferenceLineProps = (threshold?: ThresholdQuery): ReferenceLineProps | undefined =>
  Maybe.maybe(threshold)
    .map(({unit, value}: ThresholdQuery): ReferenceLineProps => ({
      label: `${firstUpperTranslated('threshold')} ${value} ${unit}`,
      stroke: colors.deepPurpleA200,
      strokeWidth: 2,
      y: value,
      yAxisId: 'left',
      viewBox: {x: 0, y: 100},
    }))
    .getOrElseUndefined();
