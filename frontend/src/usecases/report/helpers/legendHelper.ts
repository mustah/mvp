import {flatMap, head} from 'lodash';
import {unique} from '../../../helpers/collections';
import {Maybe} from '../../../helpers/Maybe';
import {Meter} from '../../../state/domain-models-paginated/meter/meterModels';
import {
  allQuantitiesMap,
  getMediumType,
  Medium,
  quantitiesToExclude,
  Quantity
} from '../../../state/ui/graph/measurement/measurementModels';
import {IdNamed} from '../../../types/Types';
import {LegendItem, SavedReportsState} from '../reportModels';
import {getLegendItems} from '../reportSelectors';

const getFirstQuantityFor = (legendType: string): Quantity[] =>
  Maybe.maybe<Quantity>(head(allQuantitiesMap[legendType]))
    .map(quantity => [quantity])
    .orElse([]);

export const toLegendItem = ({id, facility, medium}: Meter): LegendItem =>
  ({
    id,
    label: facility as string,
    type: getMediumType(medium),
    isHidden: false,
    quantities: getFirstQuantityFor(medium)
  });

export const toAggregateLegendItem = ({id, name}: IdNamed): LegendItem =>
  ({
    id,
    label: name,
    type: 'aggregate',
    isHidden: false,
    quantities: getFirstQuantityFor('aggregate')
  });

export const makeColumnQuantities = (state: SavedReportsState) => {
  const quantities: Quantity[][] = flatMap(getLegendItems(state), it => it.type)
    .map((medium: Medium) => allQuantitiesMap[medium]);
  return unique(flatMap(quantities).filter(it => quantitiesToExclude.indexOf(it) === -1));
};
