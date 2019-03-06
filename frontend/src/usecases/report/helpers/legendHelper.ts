import {flatMap} from 'lodash';
import {unique} from '../../../helpers/collections';
import {Meter} from '../../../state/domain-models-paginated/meter/meterModels';
import {
  allQuantitiesMap,
  getMediumType,
  Medium,
  quantitiesToExclude,
  Quantity
} from '../../../state/ui/graph/measurement/measurementModels';
import {IdNamed} from '../../../types/Types';
import {LegendItem, LegendItemSettings, SavedReportsState} from '../reportModels';
import {getLegendItems} from '../reportSelectors';

export const legendViewSettings: LegendItemSettings = {
  isHidden: false,
  isRowExpanded: true,
};

export const toLegendItem = ({id, facility, medium}: Meter): LegendItem => ({
  id,
  label: facility as string,
  type: getMediumType(medium),
  quantities: [],
  ...legendViewSettings
});

export const toAggregateLegendItem = ({id, name}: IdNamed): LegendItem => ({
  id,
  label: name,
  type: 'aggregate',
  quantities: [],
  ...legendViewSettings
});

export const makeColumnQuantities = (state: SavedReportsState) => {
  const quantities: Quantity[][] = flatMap(getLegendItems(state), it => it.type)
    .map((medium: Medium) => allQuantitiesMap[medium]);
  return unique(flatMap(quantities).filter(it => quantitiesToExclude.indexOf(it) === -1));
};
