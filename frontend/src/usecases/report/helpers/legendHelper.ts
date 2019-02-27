import {head} from 'lodash';
import {Maybe} from '../../../helpers/Maybe';
import {Meter} from '../../../state/domain-models-paginated/meter/meterModels';
import {allQuantities, getMediumType, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {LegendItem} from '../reportModels';

const getFirstQuantityFor = (medium: string): Quantity[] =>
  Maybe.maybe<Quantity>(head(allQuantities[medium]))
    .map(quantity => [quantity])
    .orElse([]);

export const toLegendItem = ({id, facility, medium}: Meter): LegendItem =>
  ({
    id,
    label: facility as string,
    medium: getMediumType(medium),
    isHidden: false,
    quantities: getFirstQuantityFor(medium)
  });
